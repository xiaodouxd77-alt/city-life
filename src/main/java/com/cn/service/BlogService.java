package com.cn.service;

import com.cn.dto.Result;
import com.cn.entity.Blog;
import com.baomidou.mybatisplus.spring.service.IService;

import java.util.List;

public interface BlogService extends IService<Blog> {

    Result queryHotBlog(Integer current);

    Result queryBlogById(Long id);

    Result likeBlog(Long id);

    Result queryBlogLikes(Long id);

    Result saveBlog(Blog blog);

    Result queryBlogOfFollow(Long max, Integer offset);

    void queryBlogUser(Blog blog);

    void isBlogLiked(Blog blog);

    void batchQueryBlogUser(List<Blog> blogs);

    void batchIsBlogLiked(List<Blog> blogs);

    void fillCommentCount(Blog blog);

    void batchFillCommentCounts(List<Blog> blogs);

    void rebuildFeed();

}
