package com.cn.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.cn.dto.Result;
import com.cn.entity.Rating;

public interface RatingService extends IService<Rating> {

    /**
     * 创建或更新评分
     */
    Result saveRating(Rating rating);

    /**
     * 查询店铺评分统计（均分、各星级数量）
     */
    Result getShopRating(Long shopId);

    /**
     * 查询指定笔记的评分
     */
    Result getBlogRating(Long blogId);
}
