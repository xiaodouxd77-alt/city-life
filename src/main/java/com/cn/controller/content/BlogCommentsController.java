package com.cn.controller.content;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cn.dto.CommentDTO;
import com.cn.dto.Result;
import com.cn.dto.UserDTO;
import com.cn.entity.Blog;
import com.cn.entity.BlogComments;
import com.cn.entity.User;
import com.cn.service.BlogCommentsService;
import com.cn.service.BlogService;
import com.cn.service.NotificationService;
import com.cn.service.UserService;
import com.cn.utils.common.SystemConstants;
import com.cn.utils.common.UserHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/blog-comments")
public class BlogCommentsController {

    @Resource
    private BlogCommentsService blogCommentsService;

    @Resource
    private UserService userService;

    @Resource
    private BlogService blogService;

    @Resource
    private NotificationService notificationService;

    /**
     * 发表评论
     */
    @PostMapping
    public Result addComment(@RequestBody BlogComments comment) {
        UserDTO user = UserHolder.getUser();
        comment.setUserId(user.getId());
        comment.setStatus(false);
        blogCommentsService.save(comment);
        // 更新笔记的评论数
        syncBlogCommentCount(comment.getBlogId());
        // 发送评论通知给笔记作者
        Blog blog = blogService.getById(comment.getBlogId());
        if (blog != null && !blog.getUserId().equals(user.getId())) {
            notificationService.send("COMMENT", blog.getUserId(), user.getId(),
                    (user.getNickName() != null ? user.getNickName() : "用户") + " 评论了你的笔记", comment.getBlogId());
        }
        return Result.ok(comment.getId());
    }

    /**
     * 查询笔记的评论列表
     */
    @GetMapping("/of/{blogId}")
    public Result queryComments(@PathVariable("blogId") Long blogId,
                                @RequestParam(value = "current", defaultValue = "1") Integer current) {
        Page<BlogComments> page = blogCommentsService.query()
                .eq("blog_id", blogId)
                .eq("status", false)
                .orderByAsc("create_time")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));

        List<CommentDTO> dtos = page.getRecords().stream().map(comment -> {
            CommentDTO dto = BeanUtil.copyProperties(comment, CommentDTO.class);
            User u = userService.getById(comment.getUserId());
            if (u != null) {
                dto.setUserName(u.getNickName());
                dto.setUserIcon(u.getIcon());
            }
            return dto;
        }).collect(Collectors.toList());

        return Result.ok(dtos);
    }

    /**
     * 删除自己的评论
     */
    @DeleteMapping("/{id}")
    public Result deleteComment(@PathVariable("id") Long id) {
        UserDTO user = UserHolder.getUser();
        BlogComments comment = blogCommentsService.getById(id);
        if (comment == null) {
            return Result.fail("评论不存在");
        }
        if (!comment.getUserId().equals(user.getId())) {
            return Result.fail("只能删除自己的评论");
        }
        blogCommentsService.removeById(id);
        syncBlogCommentCount(comment.getBlogId());
        return Result.ok();
    }

    /**
     * 举报评论
     */
    @PostMapping("/{id}/report")
    public Result reportComment(@PathVariable("id") Long id) {
        BlogComments comment = blogCommentsService.getById(id);
        if (comment == null) {
            return Result.fail("评论不存在");
        }
        comment.setStatus(true); // 标记为被举报
        blogCommentsService.updateById(comment);
        syncBlogCommentCount(comment.getBlogId());
        return Result.ok();
    }

    /**
     * 管理员切换评论隐藏/显示状态
     */
    @PutMapping("/{id}/toggle-status")
    public Result toggleCommentStatus(@PathVariable("id") Long id) {
        BlogComments comment = blogCommentsService.getById(id);
        if (comment == null) {
            return Result.fail("评论不存在");
        }
        comment.setStatus(comment.getStatus() == null || !comment.getStatus());
        blogCommentsService.updateById(comment);
        syncBlogCommentCount(comment.getBlogId());
        return Result.ok();
    }

    /**
     * 查询我的笔记收到的评论（用于消息通知）
     */
    @GetMapping("/my-blog")
    public Result queryCommentsOnMyBlogs(
            @RequestParam(value = "current", defaultValue = "1") Integer current) {
        Long userId = UserHolder.getUser().getId();

        // 查我的所有笔记 ID
        List<Long> blogIds = blogService.query()
                .eq("user_id", userId)
                .list()
                .stream()
                .map(Blog::getId)
                .collect(Collectors.toList());

        if (blogIds.isEmpty()) {
            return Result.ok(new ArrayList<>());
        }

        // 查这些笔记下的评论（排除我自己的评论）
        Page<BlogComments> page = blogCommentsService.query()
                .in("blog_id", blogIds)
                .ne("user_id", userId)
                .eq("status", false)
                .orderByDesc("create_time")
                .page(new Page<>(current, 20));

        List<CommentDTO> dtos = page.getRecords().stream().map(comment -> {
            CommentDTO dto = BeanUtil.copyProperties(comment, CommentDTO.class);
            // 评论者信息
            User u = userService.getById(comment.getUserId());
            if (u != null) {
                dto.setUserName(u.getNickName());
                dto.setUserIcon(u.getIcon());
            }
            // 博客标题
            Blog blog = blogService.getById(comment.getBlogId());
            if (blog != null) {
                dto.setBlogTitle(blog.getTitle());
            }
            return dto;
        }).collect(Collectors.toList());

        return Result.ok(dtos);
    }
    /**
     * 同步笔记的评论数
     */
    private void syncBlogCommentCount(Long blogId) {
        if (blogId == null) return;
        long count = blogCommentsService.query()
                .eq("blog_id", blogId)
                .eq("status", false)
                .count();
        blogService.update()
                .set("comments", (int) count)
                .eq("id", blogId)
                .update();
    }
}
