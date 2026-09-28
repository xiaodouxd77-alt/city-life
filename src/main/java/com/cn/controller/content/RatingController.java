package com.cn.controller.content;

import com.cn.dto.Result;
import com.cn.entity.Rating;
import com.cn.service.RatingService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/rating")
public class RatingController {

    @Resource
    private RatingService ratingService;

    /**
     * 创建/更新评分
     */
    @PostMapping
    public Result saveRating(@RequestBody Rating rating) {
        return ratingService.saveRating(rating);
    }

    /**
     * 查询店铺评分统计
     */
    @GetMapping("/shop/{shopId}")
    public Result getShopRating(@PathVariable("shopId") Long shopId) {
        return ratingService.getShopRating(shopId);
    }

    /**
     * 查询当前用户对某个笔记的评分
     */
    @GetMapping("/blog/{blogId}")
    public Result getBlogRating(@PathVariable("blogId") Long blogId) {
        return ratingService.getBlogRating(blogId);
    }
}
