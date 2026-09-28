package com.cn.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.cn.dto.Result;
import com.cn.entity.Blog;
import com.cn.entity.Shop;
import com.cn.mapper.ShopMapper;
import com.cn.service.BlogService;
import com.cn.service.ShopService;
import com.cn.utils.cache.CacheClient;
import com.cn.utils.common.SystemConstants;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.cn.utils.cache.RedisConstants.*;

/**
 * 商铺业务实现。
 *
 * 主要体现两个 Redis 使用场景：
 * 1. 商铺详情缓存，减轻数据库查询压力；
 * 2. Redis GEO 附近商铺查询，按距离返回店铺。
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;

    @Resource
    private BlogService blogService;

    /**
     * 查询店铺详情，优先读取 Redis 缓存，未命中时回源 MySQL。
     */
    @Override
    public Result queryById(Long id) {
        // 查询店铺详情走 CacheClient：
        // 先查 Redis，未命中再查 MySQL；如果 MySQL 也没有，会缓存空字符串防止缓存穿透。
        Shop shop = cacheClient
                .queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 下面两种方案没有启用，但保留在代码里便于学习和切换：
        // 互斥锁方案：热点 key 失效时，只允许一个线程重建缓存。
        // Shop shop = cacheClient
        //         .queryWithMutex(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 逻辑过期方案：缓存过期后先返回旧值，后台异步重建，适合热点数据。
        // Shop shop = cacheClient
        //         .queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, this::getById, 20L, TimeUnit.SECONDS);

        if (shop == null) {
            return Result.fail("店铺不存在！");
        }
        // 详情页显示的是该店铺下方的探店笔记数量，只统计正常状态的关联笔记。
        int blogCount = Math.toIntExact(blogService.count(Wrappers.lambdaQuery(Blog.class)
                .eq(Blog::getShopId, id)
                .eq(Blog::getStatus, 0)));
        shop.setComments(blogCount);
        return Result.ok(shop);
    }

    /**
     * 更新店铺信息，并删除对应缓存以保证下次查询加载最新数据。
     */
    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id不能为空");
        }
        // 更新时采用“先更新数据库，再删除缓存”的策略。
        // 下次读取会重新加载最新数据，比直接改缓存更不容易漏字段。
        updateById(shop);
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);
        return Result.ok();
    }

    /**
     * 按类型分页查询店铺，有经纬度时按 Redis GEO 距离排序返回附近店铺。
     */
    @Override
    public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
        // 没有经纬度时退化为普通分页；有经纬度时走 Redis GEO 附近查询。
        if (x == null || y == null) {
            Page<Shop> page = query()
                    .eq("type_id", typeId)
                    .page(new Page<>(current, SystemConstants.SHOP_PAGE_SIZE));
            return Result.ok(page.getRecords());
        }

        // Redis GEO 查询本身不支持 offset，只能 limit 到 end 后再在内存中 skip from。
        int from = (current - 1) * SystemConstants.SHOP_PAGE_SIZE;
        int end = current * SystemConstants.SHOP_PAGE_SIZE;

        // Redis 里按类型保存店铺坐标：shop:geo:{typeId} -> shopId + 经纬度。
        // 查询 5km 范围内的店铺，并让 Redis 返回距离。
        String key = SHOP_GEO_KEY + typeId;
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo() // GEOSEARCH key BYLONLAT x y BYRADIUS 10 WITHDISTANCE
                .search(
                        key,
                        GeoReference.fromCoordinate(x, y),
                        new Distance(5000),
                        RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(end)
                );
        if (results == null) {
            return Result.ok(Collections.emptyList());
        }
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
        if (list.size() <= from) {
            // 没有下一页了，结束
            return Result.ok(Collections.emptyList());
        }
        // Redis 返回的是店铺 id 和距离，真正的店铺详情仍然从 MySQL 查。
        List<Long> ids = new ArrayList<>(list.size());
        Map<String, Distance> distanceMap = new HashMap<>(list.size());
        list.stream().skip(from).forEach(result -> {
            // 4.2.获取店铺id
            String shopIdStr = result.getContent().getName();
            ids.add(Long.valueOf(shopIdStr));
            // 4.3.获取距离
            Distance distance = result.getDistance();
            distanceMap.put(shopIdStr, distance);
        });
        // ORDER BY FIELD 用来保持 Redis GEO 按距离返回的顺序。
        String idStr = StrUtil.join(",", ids);
        List<Shop> shops = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
        for (Shop shop : shops) {
            shop.setDistance(distanceMap.get(shop.getId().toString()).getValue());
        }
        // 6.返回
        return Result.ok(shops);
    }
}
