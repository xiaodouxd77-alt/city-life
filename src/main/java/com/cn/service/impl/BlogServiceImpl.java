package com.cn.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.cn.dto.Result;
import com.cn.dto.ScrollResult;
import com.cn.dto.UserDTO;
import com.cn.entity.Blog;
import com.cn.entity.BlogComments;
import com.cn.entity.Follow;
import com.cn.entity.User;
import com.cn.mapper.BlogMapper;
import com.cn.service.BlogCommentsService;
import com.cn.service.BlogService;
import com.cn.service.FollowService;
import com.cn.service.NotificationService;
import com.cn.service.CreditsService;
import com.cn.service.UserService;
import com.cn.utils.common.SystemConstants;
import com.cn.utils.common.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.cn.utils.cache.RedisConstants.BLOG_LIKED_KEY;
import static com.cn.utils.cache.RedisConstants.FEED_KEY;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private FollowService followService;

    @Resource
    private NotificationService notificationService;

    @Resource
    private BlogCommentsService blogCommentsService;

    @Resource
    private com.cn.service.UserInfoService userInfoService;
    @Resource
    private CreditsService creditsService;

    /**
     * 启动时根据关注关系和正常笔记重建所有用户的关注 Feed。
     */
    @jakarta.annotation.PostConstruct
    public void rebuildFeed() {
        // 只推送正常状态的笔记
        List<Blog> allBlogs = query().eq("status", 0).list();
        if (allBlogs.isEmpty()) return;
        for (Blog blog : allBlogs) {
            List<Follow> followers = followService.query().eq("follow_user_id", blog.getUserId()).list();
            for (Follow f : followers) {
                // score 用 blog.id：自增唯一且递增，保证 feed 按发布顺序倒序且 score 不重复，
                // 避免多条笔记 score 相同时游标分页无限循环。
                stringRedisTemplate.opsForZSet().add(FEED_KEY + f.getUserId(),
                        blog.getId().toString(), blog.getId());
            }
        }
    }

    /**
     * 查询热门笔记，笔记按点赞数倒序排序，且只返回正常笔记。
     */
    @Override
    public Result queryHotBlog(Integer current) {
        // 热门笔记直接按 MySQL liked 字段倒序分页，只展示未隐藏的正常笔记。
        Page<Blog> page = query()
                .eq("status", 0)
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(blog -> {
            this.queryBlogUser(blog);
            this.isBlogLiked(blog);
        });
        batchFillCommentCounts(records);
        return Result.ok(records);
    }

    /**
     * 根据笔记 ID 查询详情，并补充作者、点赞状态和评论数量。
     */
    @Override
    public Result queryBlogById(Long id) {
        // 1.查询blog
        Blog blog = getById(id);
        if (blog == null) {
            return Result.fail("笔记不存在！");
        }
        // 隐藏的笔记仅作者和管理员可见
        if (blog.getStatus() != null && blog.getStatus() == 1) {
            UserDTO user = UserHolder.getUser();
            if (user == null || (!user.getId().equals(blog.getUserId()) && !"ADMIN".equals(user.getRole()))) {
                return Result.fail("笔记不存在！");
            }
        }
        // 2.查询blog有关的用户
        queryBlogUser(blog);
        // 3.查询blog是否被点赞
        isBlogLiked(blog);
        fillCommentCount(blog);
        return Result.ok(blog);
    }

    /**
     * 判断当前登录用户是否已点赞指定笔记，并写入笔记对象。
     */
    @Override
    public void isBlogLiked(Blog blog) {
        // 1.获取登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            // 用户未登录，无需查询是否点赞
            return;
        }
        Long userId = user.getId();
        // 点赞状态存在 Redis ZSet：member=userId，score=点赞时间。
        // score 不为空说明当前用户已经点过赞。
        String key = "blog:liked:" + blog.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        blog.setIsLike(score != null);
    }

    /**
     * 点赞或取消点赞笔记，并同步维护点赞数、Redis 点赞集合和通知。
     */
    @Override
    public Result likeBlog(Long id) {
        // 1.获取登录用户
        Long userId = UserHolder.getUser().getId();
        // 用 Redis ZSet 判断是否已点赞，避免每次都查点赞关系表。
        String key = BLOG_LIKED_KEY + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        if (score == null) {
            // 3.如果未点赞，可以点赞
            // 3.1.数据库点赞数 + 1
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
            // 3.2.保存点赞用户到 Redis ZSet。
            // 这里用 ZSet 而不是 Set，是为了后续能按点赞时间展示点赞用户。
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
                // 发送点赞通知
                Blog blog = getById(id);
                if (blog != null) {
                    UserDTO user = UserHolder.getUser();
                    notificationService.send("LIKE", blog.getUserId(), userId,
                            (user.getNickName() != null ? user.getNickName() : "用户") + " 赞了你的笔记", id);
                }
                // 给笔记作者加积分（修改点）
                if (blog != null) {
                    creditsService.onBlogLiked(blog.getUserId());
                }
            }
        } else {
            // 4.如果已点赞，取消点赞
            // 4.1.数据库点赞数 -1
            boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
            // 4.2.把用户从Redis的set集合移除
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
        }
        return Result.ok();
    }

    /**
     * 查询指定笔记最早点赞的前五名用户。
     */
    @Override
    public Result queryBlogLikes(Long id) {
        String key = BLOG_LIKED_KEY + id;
        // 查询最早点赞的 5 个用户；如果要展示最近点赞，可以改成 reverseRange。
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if (top5 == null || top5.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        // 2.解析出其中的用户id
        List<Long> ids = top5.stream().map(Long::valueOf).collect(Collectors.toList());
        String idStr = StrUtil.join(",", ids);
        // 3.根据用户id查询用户 WHERE id IN ( 5 , 1 ) ORDER BY FIELD(id, 5, 1)
        List<UserDTO> userDTOS = userService.query()
                .in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list()
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        // 4.返回
        return Result.ok(userDTOS);
    }

    /**
     * 发布探店笔记，并将新笔记推送到粉丝的关注 Feed。
     */
    @Override
    public Result saveBlog(Blog blog) {
        // 1.获取登录用户
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        // 如果未选择店铺，设置默认值（数据库 shop_id 为 NOT NULL 无默认值）
        if (blog.getShopId() == null) {
            blog.setShopId(0L);
        }
        // 2.保存探店笔记
        boolean isSuccess = save(blog);
        if(!isSuccess){
            return Result.fail("新增笔记失败!");
        }
        // 查询笔记作者的所有粉丝。
        // follow_user_id 是“被关注的人”，user_id 是“粉丝”。
        List<Follow> follows = followService.query().eq("follow_user_id", user.getId()).list();
        // 4.给发布者增加积分
        userInfoService.update()
                .setSql("credits = credits + 5").eq("user_id", user.getId()).update();
        // 推模式 feed：发布时把笔记 id 推到每个粉丝的 feed:{followerId}。
        // 查询关注流时只需要读自己的 Redis ZSet，避免每次跨多个关注作者做数据库聚合。
        for (Follow follow : follows) {
            // 5.1.获取粉丝id
            Long followerId = follow.getUserId();
            // 5.2.推送（score 用 blog.id，与 rebuildFeed 规则一致，保证唯一递增）
            String key = FEED_KEY + followerId;
            stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), blog.getId());
        }
        // 6.返回id
        // 发布笔记积分&检查勋章
        creditsService.onBlogPublished(user.getId());
        return Result.ok(blog.getId());
    }

    /**
     * 滚动分页查询当前登录用户的关注 Feed。
     */
    @Override
    public Result queryBlogOfFollow(Long max, Integer offset) {
        Long userId = UserHolder.getUser().getId();
        String key = FEED_KEY + userId;
        // 按 score 从大到小查。score 是发布时间戳，max 是上一页返回的最小时间。
        // offset 用于处理多条笔记 score 相同的情况，避免下一页重复读取。
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, SystemConstants.DEFAULT_PAGE_SIZE);
        if (typedTuples == null || typedTuples.isEmpty()) return Result.ok();

        List<Long> ids = new ArrayList<>(typedTuples.size());
        // minTime 记录本批最小时间戳（即最后一条，score 降序），os 统计与最小时间戳相同的条数，
        // 作为下一页的 offset，避免相同 score 的笔记被重复或跳过。
        long minTime = 0;
        int os = 1;
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples) {
            ids.add(Long.valueOf(tuple.getValue()));
            long time = tuple.getScore().longValue();
            if (time == minTime) { os++; } else { minTime = time; os = 1; }
        }
        String idStr = StrUtil.join(",", ids);
        // Redis 只保存 blogId，详情仍从 MySQL 查；ORDER BY FIELD 保持 Redis 返回顺序。
        List<Blog> blogs = query().in("id", ids).eq("status", 0).last("ORDER BY FIELD(id," + idStr + ")").list();
        for (Blog blog : blogs) { queryBlogUser(blog); isBlogLiked(blog); }
        batchFillCommentCounts(blogs);

        ScrollResult r = new ScrollResult();
        r.setList(blogs);
        r.setOffset(os);
        r.setMinTime(minTime);
        return Result.ok(r);
    }

    /**
     * 查询笔记作者信息，并填充到笔记展示字段中。
     */
    @Override
    public void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }

    /**
     * 批量查询笔记作者信息，减少逐条查询用户造成的数据库访问。
     */
    @Override
    public void batchQueryBlogUser(List<Blog> blogs) {
        if (blogs == null || blogs.isEmpty()) return;
        List<Long> userIds = blogs.stream().map(Blog::getUserId).distinct().collect(Collectors.toList());
        List<User> users = userService.listByIds(userIds);
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        for (Blog blog : blogs) {
            User user = userMap.get(blog.getUserId());
            if (user != null) {
                blog.setName(user.getNickName());
                blog.setIcon(user.getIcon());
            }
        }
    }

    /**
     * 批量判断当前登录用户对多篇笔记的点赞状态。
     */
    @Override
    public void batchIsBlogLiked(List<Blog> blogs) {
        UserDTO user = UserHolder.getUser();
        if (user == null || blogs.isEmpty()) return;
        Long userId = user.getId();
        for (Blog blog : blogs) {
            String key = BLOG_LIKED_KEY + blog.getId();
            Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
            blog.setIsLike(score != null);
        }
    }

    /**
     * 查询单篇笔记的正常评论数量，并填充到笔记对象中。
     */
    @Override
    public void fillCommentCount(Blog blog) {
        if (blog == null || blog.getId() == null) return;
        long count = blogCommentsService.query()
                .eq("blog_id", blog.getId())
                .eq("status", false)
                .count();
        blog.setComments((int) count);
    }

    /**
     * 批量统计多篇笔记的正常评论数量，并填充到对应笔记对象中。
     */
    @Override
    public void batchFillCommentCounts(List<Blog> blogs) {
        if (blogs == null || blogs.isEmpty()) return;
        List<Long> ids = blogs.stream()
                .map(Blog::getId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) return;

        Map<Long, Long> countMap = blogCommentsService.query()
                .in("blog_id", ids)
                .eq("status", false)
                .list()
                .stream()
                .collect(Collectors.groupingBy(BlogComments::getBlogId, Collectors.counting()));

        for (Blog blog : blogs) {
            blog.setComments(countMap.getOrDefault(blog.getId(), 0L).intValue());
        }
    }
}
