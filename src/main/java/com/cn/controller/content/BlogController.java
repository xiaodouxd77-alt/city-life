package com.cn.controller.content;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cn.dto.Result;
import com.cn.dto.UserDTO;
import com.cn.entity.Blog;
import com.cn.entity.Rating;
import com.cn.service.BlogService;
import com.cn.service.RatingService;
import com.cn.utils.common.SystemConstants;
import com.cn.utils.common.UserHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private BlogService blogService;

    @Resource
    private RatingService ratingService;

    /**
     * 发布笔记（可选带评分）
     */
    @PostMapping
    public Result saveBlog(@RequestBody Blog blog) {
        Integer score = blog.getScore();
        Result result = blogService.saveBlog(blog);
        // 如果带评分且有关联店铺，保存评分
        if (score != null && score >= 1 && score <= 5 && blog.getShopId() != null) {
            Rating rating = new Rating();
            rating.setShopId(blog.getShopId());
            rating.setScore(score);
            // 通过 result 获取 blogId
            if (result.getData() != null) {
                rating.setBlogId(Long.valueOf(result.getData().toString()));
            }
            ratingService.saveRating(rating);
        }
        return result;
    }
    /**
     * 点赞笔记
     */
    @PutMapping("/like/{id}")
    public Result likeBlog(@PathVariable("id") Long id) {
        return blogService.likeBlog(id);
    }
    /**
     * 查询当前用户发布的笔记
     */
    @GetMapping("/of/me")
    public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        UserDTO user = UserHolder.getUser();
        Page<Blog> page = blogService.query()
                .eq("user_id", user.getId()).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        List<Blog> records = page.getRecords();
        blogService.batchQueryBlogUser(records);
        blogService.batchIsBlogLiked(records);
        blogService.batchFillCommentCounts(records);
        return Result.ok(records);
    }
    /**
     * 查询热门笔记
     */
    @GetMapping("/hot")
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogService.queryHotBlog(current);
    }
    /**
     * 查询笔记详情
     */
    @GetMapping("/{id}")
    public Result queryBlogById(@PathVariable("id") Long id) {
        return blogService.queryBlogById(id);
    }
    /**
     * 查询笔记点赞数量
     */
    @GetMapping("/likes/{id}")
    public Result queryBlogLikes(@PathVariable("id") Long id) {
        return blogService.queryBlogLikes(id);
    }
    /**
     * 查询用户笔记
     */
    @GetMapping("/of/user")
    public Result queryBlogByUserId(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam("id") Long id) {
        // 公开查询：只展示正常状态的笔记
        Page<Blog> page = blogService.query()
                .eq("user_id", id).eq("status", 0)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        List<Blog> records = page.getRecords();
        blogService.batchQueryBlogUser(records);
        blogService.batchIsBlogLiked(records);
        blogService.batchFillCommentCounts(records);
        return Result.ok(records);
    }
    /**
     * 查询笔记的关注用户笔记
     */
    @GetMapping("/of/follow")
    public Result queryBlogOfFollow(
            @RequestParam("lastId") Long max, @RequestParam(value = "offset", defaultValue = "0") Integer offset){
        return blogService.queryBlogOfFollow(max, offset);
    }

    /**
     * 删除笔记
     */
    @DeleteMapping("/{id}")
    public Result deleteBlog(@PathVariable("id") Long id) {
        blogService.removeById(id);
        return Result.ok();
    }

    /**
     * 管理员切换笔记隐藏/显示状态
     */
    @PutMapping("/{id}/toggle-status")
    public Result toggleBlogStatus(@PathVariable("id") Long id) {
        Blog blog = blogService.getById(id);
        if (blog == null) {
            return Result.fail("笔记不存在");
        }
        blog.setStatus(blog.getStatus() != null && blog.getStatus() == 1 ? 0 : 1);
        blogService.updateById(blog);
        return Result.ok();
    }

    /**
     * 手动重建所有用户的关注 feed（Redis）
     */
    @PostMapping("/feed/rebuild")
    public Result rebuildFeed() {
        blogService.rebuildFeed();
        return Result.ok();
    }
    /**
     * 查询店铺笔记
     */
    @GetMapping("/of/shop")
    public Result queryBlogByShopId(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam("shopId") Long shopId) {
        // 公开查询：只展示正常状态的笔记
        Page<Blog> page = blogService.query()
                .eq("shop_id", shopId).eq("status", 0)
                .orderByDesc("create_time")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        List<Blog> records = page.getRecords();
        blogService.batchQueryBlogUser(records);
        blogService.batchIsBlogLiked(records);
        blogService.batchFillCommentCounts(records);
        return Result.ok(records);
    }
}
