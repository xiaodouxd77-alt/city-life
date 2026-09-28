package com.cn.controller.social;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.dto.Result;
import com.cn.entity.Favorite;
import com.cn.entity.Shop;
import com.cn.mapper.FavoriteMapper;
import com.cn.mapper.ShopMapper;
import com.cn.utils.common.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Resource
    private FavoriteMapper favoriteMapper;

    @Resource
    private ShopMapper shopMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String FAVORITE_SHOP_KEY = "favorite:shop:";
    private static final String FAVORITE_BLOG_KEY = "favorite:blog:";

    /**
     * 切换收藏状态
     */
    @PostMapping("/{type}/{id}")
    public Result toggleFavorite(@PathVariable("type") String type, @PathVariable("id") Long targetId) {
        Long userId = UserHolder.getUser().getId();
        String redisKey = ("SHOP".equalsIgnoreCase(type) ? FAVORITE_SHOP_KEY : FAVORITE_BLOG_KEY) + userId;

        Favorite existing = favoriteMapper.selectOne(new QueryWrapper<Favorite>()
                .eq("user_id", userId).eq("target_type", type.toUpperCase()).eq("target_id", targetId));
        boolean isMember = existing != null;
        if (Boolean.TRUE.equals(isMember)) {
            // 取消收藏
            stringRedisTemplate.opsForSet().remove(redisKey, targetId.toString());
            favoriteMapper.delete(new QueryWrapper<Favorite>()
                    .eq("user_id", userId).eq("target_type", type.toUpperCase()).eq("target_id", targetId));
            return Result.ok(false); // false = 已取消收藏
        } else {
            // 添加收藏
            stringRedisTemplate.opsForSet().add(redisKey, targetId.toString());
            Favorite fav = new Favorite();
            fav.setUserId(userId);
            fav.setTargetType(type.toUpperCase());
            fav.setTargetId(targetId);
            fav.setCreateTime(LocalDateTime.now());
            favoriteMapper.insert(fav);
            return Result.ok(true); // true = 已收藏
        }
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/{type}/{id}/is")
    public Result isFavorite(@PathVariable("type") String type, @PathVariable("id") Long targetId) {
        Long userId = UserHolder.getUser().getId();
        String redisKey = ("SHOP".equalsIgnoreCase(type) ? FAVORITE_SHOP_KEY : FAVORITE_BLOG_KEY) + userId;
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(redisKey, targetId.toString());
        if (!Boolean.TRUE.equals(isMember)) {
            Long count = favoriteMapper.selectCount(new QueryWrapper<Favorite>()
                    .eq("user_id", userId).eq("target_type", type.toUpperCase()).eq("target_id", targetId));
            isMember = count != null && count > 0;
            if (Boolean.TRUE.equals(isMember)) {
                stringRedisTemplate.opsForSet().add(redisKey, targetId.toString());
            }
        }
        return Result.ok(Boolean.TRUE.equals(isMember));
    }

    /**
     * 我的收藏列表
     */
    @GetMapping("/{type}")
    public Result myFavorites(@PathVariable("type") String type) {
        Long userId = UserHolder.getUser().getId();
        List<Favorite> list = favoriteMapper.selectList(new QueryWrapper<Favorite>()
                .eq("user_id", userId).eq("target_type", type.toUpperCase())
                .orderByDesc("create_time"));
        return Result.ok(list);
    }

    /**
     * Current user's favorite shops with their full shop data.
     */
    @GetMapping("/shops")
    public Result myFavoriteShops() {
        Long userId = UserHolder.getUser().getId();
        List<Favorite> favorites = favoriteMapper.selectList(new QueryWrapper<Favorite>()
                .eq("user_id", userId)
                .eq("target_type", "SHOP")
                .orderByDesc("create_time"));
        List<Shop> shops = new java.util.ArrayList<>();
        for (Favorite favorite : favorites) {
            Shop shop = shopMapper.selectById(favorite.getTargetId());
            if (shop != null) {
                shops.add(shop);
            }
        }
        return Result.ok(shops);
    }
}
