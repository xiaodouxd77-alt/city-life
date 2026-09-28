package com.cn.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.cn.dto.Result;
import com.cn.dto.UserDTO;
import com.cn.entity.Blog;
import com.cn.entity.Follow;
import com.cn.mapper.BlogMapper;
import com.cn.mapper.FollowMapper;
import com.cn.service.FollowService;
import com.cn.service.NotificationService;
import com.cn.service.CreditsService;
import com.cn.service.UserService;
import com.cn.utils.common.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.cn.utils.cache.RedisConstants.FEED_KEY;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserService userService;
    @Resource
    private NotificationService notificationService;
    @Resource
    private com.cn.service.UserInfoService userInfoService;
    @Resource
    private BlogMapper blogMapper;
    @Resource
    private CreditsService creditsService;

    /**
     * 关注或取消关注用户，并同步关注关系、关注流、粉丝数和通知。
     */
    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
        // 当前登录用户是“发起关注的人”，followUserId 是“被关注的人”。
        Long userId = UserHolder.getUser().getId();
        if (userId.equals(followUserId)) {
            return Result.fail("\u4e0d\u80fd\u5173\u6ce8\u81ea\u5df1");
        }
        if (userService.getById(followUserId) == null) {
            return Result.fail("\u7528\u6237\u4e0d\u5b58\u5728");
        }
        String key = "follows:" + userId;
        if (isFollow) {
            if (query().eq("user_id", userId).eq("follow_user_id", followUserId).count() > 0) {
                return Result.ok();
            }
            // MySQL 保存关注关系，保证数据持久化。
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            boolean isSuccess = save(follow);
            if (isSuccess) {
                // Redis Set 保存“我关注了谁”，用于快速判断关注关系和共同关注。
                stringRedisTemplate.opsForSet().add(key, followUserId.toString());
                // Redis ZSet feed 保存“我能看到哪些关注笔记”，关注后要补齐对方历史笔记。
                refreshFollowFeed(userId, followUserId, true);
                // 更新粉丝/关注数
                userInfoService.update()
                        .setSql("followee = followee + 1").eq("user_id", userId).update();
                userInfoService.update()
                        .setSql("fans = fans + 1").eq("user_id", followUserId).update();
                // 发送关注通知
                UserDTO user = UserHolder.getUser();
                notificationService.send("FOLLOW", followUserId, userId,
                        (user.getNickName() != null ? user.getNickName() : "用户") + " 关注了你", null);
                // 关注加积分（修改点）
                creditsService.onFollow(userId);
            }
        } else {
            // 取关时删除 MySQL 关注关系。
            boolean isSuccess = remove(new QueryWrapper<Follow>()
                    .eq("user_id", userId).eq("follow_user_id", followUserId));
            if (isSuccess) {
                // 同步维护 Redis Set 和 Redis feed，避免关注页继续出现已取关作者的笔记。
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
                refreshFollowFeed(userId, followUserId, false);
                // 更新粉丝/关注数（最小为0）
                userInfoService.update()
                        .setSql("followee = GREATEST(0, followee - 1)").eq("user_id", userId).update();
                userInfoService.update()
                        .setSql("fans = GREATEST(0, fans - 1)").eq("user_id", followUserId).update();
            }
        }
        return Result.ok();
    }

    /**
     * 根据关注状态补齐或移除目标用户的历史笔记 Feed。
     */
    private void refreshFollowFeed(Long userId, Long followUserId, boolean isFollow) {
        // feed 只展示正常状态笔记，隐藏笔记不推送到关注流。
        List<Blog> blogs = blogMapper.selectList(new QueryWrapper<Blog>()
                .eq("user_id", followUserId)
                .eq("status", 0));
        if (blogs == null || blogs.isEmpty()) {
            return;
        }
        String feedKey = FEED_KEY + userId;
        for (Blog blog : blogs) {
            String blogId = blog.getId().toString();
            if (isFollow) {
                // score 使用笔记原始创建时间，保证新关注后历史笔记仍按发布时间排序。
                long score = blog.getCreateTime() == null
                        ? System.currentTimeMillis()
                        : blog.getCreateTime().toEpochSecond(ZoneOffset.of("+8")) * 1000;
                stringRedisTemplate.opsForZSet().add(feedKey, blogId, score);
            } else {
                stringRedisTemplate.opsForZSet().remove(feedKey, blogId);
            }
        }
    }

    /**
     * 判断当前登录用户是否已关注指定用户。
     */
    @Override
    public Result isFollow(Long followUserId) {
        // 1.获取登录用户
        Long userId = UserHolder.getUser().getId();
        // 2.查询是否关注 select count(*) from tb_follow where user_id = ? and follow_user_id = ?
        Long count = query().eq("user_id", userId).eq("follow_user_id", followUserId).count();
        // 3.判断
        return Result.ok(count > 0);
    }

    /**
     * 查询当前登录用户和指定用户的共同关注列表。
     */
    @Override
    public Result followCommons(Long id) {
        // 1.获取当前用户
        Long userId = UserHolder.getUser().getId();
        String key = "follows:" + userId;
        // 2.求交集
        String key2 = "follows:" + id;
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key, key2);
        if (intersect == null || intersect.isEmpty()) {
            // 无交集
            return Result.ok(Collections.emptyList());
        }
        // 3.解析id集合
        List<Long> ids = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        // 4.查询用户
        List<UserDTO> users = userService.listByIds(ids)
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        return Result.ok(users);
    }

    /**
     * Return users followed by the current user, preserving the follow-time order.
     */
    @Override
    public Result myFollows() {
        Long userId = UserHolder.getUser().getId();
        List<Follow> follows = query()
                .eq("user_id", userId)
                .orderByDesc("create_time")
                .list();
        if (follows.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        List<Long> userIds = follows.stream().map(Follow::getFollowUserId).collect(Collectors.toList());
        Map<Long, UserDTO> usersById = new HashMap<>();
        userService.listByIds(userIds).forEach(user ->
                usersById.put(user.getId(), BeanUtil.copyProperties(user, UserDTO.class)));
        List<UserDTO> result = new ArrayList<>();
        for (Follow follow : follows) {
            UserDTO user = usersById.get(follow.getFollowUserId());
            if (user != null) {
                result.add(user);
            }
        }
        return Result.ok(result);
    }
}
