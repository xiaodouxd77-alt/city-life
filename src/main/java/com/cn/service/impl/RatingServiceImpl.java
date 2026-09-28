package com.cn.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.cn.dto.Result;
import com.cn.entity.Rating;
import com.cn.entity.Shop;
import com.cn.mapper.RatingMapper;
import com.cn.service.RatingService;
import com.cn.service.ShopService;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.cn.utils.common.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cn.utils.cache.RedisConstants.CACHE_SHOP_KEY;

@Slf4j
@Service
public class RatingServiceImpl extends ServiceImpl<RatingMapper, Rating> implements RatingService {

    @Resource
    private ShopService shopService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 保存或更新用户对笔记的评分，并同步刷新店铺评分。
     */
    @Override
    public Result saveRating(Rating rating) {
        Long userId = UserHolder.getUser().getId();
        rating.setUserId(userId);
        rating.setCreateTime(LocalDateTime.now());
        rating.setUpdateTime(LocalDateTime.now());

        // 检查是否已评分该笔记
        Rating exist = null;
        if (rating.getBlogId() != null) {
            exist = query().eq("user_id", userId).eq("blog_id", rating.getBlogId()).one();
            if (exist != null) {
                // 更新已有评分
                exist.setScore(rating.getScore());
                exist.setUpdateTime(LocalDateTime.now());
                updateById(exist);
                updateShopScore(rating.getShopId());
                return Result.ok();
            }
        }

        save(rating);
        // 更新店铺均分
        if (rating.getShopId() != null) {
            updateShopScore(rating.getShopId());
        }

        return Result.ok(rating.getId());
    }

    /**
     * 查询店铺评分统计，包括平均分、评分数和各星级数量。
     */
    @Override
    public Result getShopRating(Long shopId) {
        // 计算均分
        List<Rating> ratings = query().eq("shop_id", shopId).list();
        if (ratings == null || ratings.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("avgScore", 0.0);
            result.put("count", 0);
            return Result.ok(result);
        }
        double avgScore = ratings.stream().mapToInt(Rating::getScore).average().orElse(0.0);
        // 各星级数量
        Map<Integer, Long> scoreCount = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int s = i;
            scoreCount.put(i, ratings.stream().filter(r -> r.getScore() == s).count());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("avgScore", Math.round(avgScore * 10.0) / 10.0);
        result.put("count", ratings.size());
        result.put("scoreCount", scoreCount);
        return Result.ok(result);
    }

    /**
     * 查询当前登录用户对指定笔记的评分记录。
     */
    @Override
    public Result getBlogRating(Long blogId) {
        Rating rating = query().eq("blog_id", blogId).eq("user_id", UserHolder.getUser().getId()).one();
        return Result.ok(rating);
    }

    /**
     * 根据评分记录重新计算并更新店铺平均分，同时清除店铺详情缓存。
     */
    private void updateShopScore(Long shopId) {
        List<Rating> ratings = query().eq("shop_id", shopId).list();
        if (ratings != null && !ratings.isEmpty()) {
            double avgScore = ratings.stream().mapToInt(Rating::getScore).average().orElse(0.0);
            int score = (int) Math.round(avgScore * 10); // 存储为整数（原分*10）
            Shop shop = new Shop();
            shop.setId(shopId);
            shop.setScore(score);
            shopService.updateById(shop);
            stringRedisTemplate.delete(CACHE_SHOP_KEY + shopId);
        }
    }
}
