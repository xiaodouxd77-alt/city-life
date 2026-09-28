package com.cn.utils.cache;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.cn.utils.cache.RedisConstants.CACHE_NULL_TTL;
import static com.cn.utils.cache.RedisConstants.LOCK_SHOP_KEY;

/**
 * Redis 缓存工具类
 *
 * 封装了三种常用的缓存模式，解决高并发场景下的缓存问题：
 * 1. 缓存穿透：查询不存在的数据导致请求直达数据库
 * 2. 缓存击穿：热点key失效导致高并发请求直达数据库
 * 3. 缓存雪崩：大量key同时失效导致数据库压力暴增
 *
 * @date 2024
 */
@Slf4j
@Component
public class CacheClient {

    private final StringRedisTemplate stringRedisTemplate;

    //缓存重建线程池，用于异步执行缓存重建任务，避免阻塞主线程，固定10个线程，可根据实际需求调整
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 设置缓存（带TTL过期时间）
     *
     * @param key   缓存的键
     * @param value 缓存的值（会被转为JSON字符串存储）
     * @param time  过期时间数值
     * @param unit  过期时间单位
     */
    public void set(String key, Object value, Long time, TimeUnit unit) {
        // 将对象转为JSON字符串存入Redis，并设置过期时间
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    /**
     * 设置逻辑过期缓存
     *
     * 不同于Redis原生的TTL过期，这里把过期时间作为业务字段存储在JSON中。
     * 优点：热点key不会因为TTL到期而突然消失，可以先返回旧数据再异步更新。
     * 缺点：会一直占用内存空间，需要额外的清理机制。
     *
     * @param key   缓存的键
     * @param value 缓存的值
     * @param time  逻辑过期时间数值
     * @param unit  逻辑过期时间单位
     */
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        // 创建RedisData对象，封装数据和过期时间
        RedisData redisData = new RedisData();
        redisData.setData(value);
        // 计算过期时间点 = 当前时间 + 指定的时长
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        // 写入Redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 缓存穿透解决方案
     *
     * 原理：当查询的数据在数据库中不存在时，将空值（""）也缓存起来，
     *      下次同样的请求就不会穿透到数据库了。
     * 适用场景：访问量不大，或者对数据一致性要求较高的场景。
     *
     * @param keyPrefix  key的前缀，如"cache:shop:"
     * @param id         数据的唯一标识
     * @param type       返回的数据类型Class对象
     * @param dbFallback 数据库查询回调函数，由调用方提供具体实现
     * @param time       缓存过期时间数值
     * @param unit       缓存过期时间单位
     * @return 查询到的数据，如果数据库也不存在则返回null
     * @param <R> 返回的数据类型
     * @param <ID> ID的类型
     */
    public <R,ID> R queryWithPassThrough(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit){
        String key = keyPrefix + id;
        // 缓存穿透方案：Redis 没有 -> 查数据库；数据库也没有 -> 缓存空字符串。
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否存在
        if (StrUtil.isNotBlank(json)) {
            // 3.存在，直接返回
            return parseCachedJson(json, type);
        }
        // json != null 但内容为空字符串，说明之前查过数据库且确认不存在。
        if (json != null) {
            return null;
        }

        // 4.不存在，根据id查询数据库
        R r = dbFallback.apply(id);
        // 5.不存在，返回错误
        if (r == null) {
            // 空值 TTL 要短，避免之后真实创建了数据却长期读不到。
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        // 6.存在，写入redis
        this.set(key, r, time, unit);
        return r;
    }

    /**
     * 解析缓存内容，兼容历史逻辑过期缓存中的 data 包装结构。
     */
    private <R> R parseCachedJson(String json, Class<R> type) {
        JSONObject jsonObject = JSONUtil.parseObj(json);
        if (jsonObject.containsKey("expireTime") && jsonObject.containsKey("data")) {
            Object data = jsonObject.get("data");
            if (data == null) {
                return null;
            }
            return JSONUtil.toBean(JSONUtil.toJsonStr(data), type);
        }
        return JSONUtil.toBean(json, type);
    }

    /**
     * 逻辑过期解决方案（解决缓存击穿）
     *
     * 原理：不给Redis设置TTL，而是在数据中维护一个逻辑过期时间。
     *      查询时判断是否过期，过期则尝试获取分布式锁，
     *      获取成功的线程去异步重建缓存，其他线程继续使用旧数据。
     * 优点：高并发下性能好，不会出现所有请求都阻塞等待的情况。
     * 缺点：数据有一段时间的不一致（最终一致性）。
     *
     * @param keyPrefix  key的前缀
     * @param id         数据的唯一标识
     * @param type       返回的数据类型Class对象
     * @param dbFallback 数据库查询回调函数
     * @param time       逻辑过期时间数值
     * @param unit       逻辑过期时间单位
     * @return 查询到的数据（可能是旧数据）
     * @param <R> 返回的数据类型
     * @param <ID> ID的类型
     */
    public <R, ID> R queryWithLogicalExpire(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 1.从redis查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否存在
        if (StrUtil.isBlank(json)) {
            // 3.存在，直接返回
            return null;
        }
        // 4.命中，需要先把json反序列化为对象
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();
        // 5.判断是否过期
        if(expireTime.isAfter(LocalDateTime.now())) {
            // 5.1.未过期，直接返回店铺信息
            return r;
        }
        // 已过期也先返回旧数据，只让拿到锁的线程去后台重建缓存。
        String lockKey = LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);
        // 6.2.判断是否获取锁成功
        if (isLock){
            // 6.3.成功，开启独立线程，实现缓存重建
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // 查询数据库
                    R newR = dbFallback.apply(id);
                    // 重建缓存
                    this.setWithLogicalExpire(key, newR, time, unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }finally {
                    // 释放锁
                    unlock(lockKey);
                }
            });
        }
        // 没拿到锁的请求直接返回旧值，避免大量请求阻塞在数据库上。
        return r;
    }


    /**
     * 互斥锁解决方案（解决缓存击穿）
     *
     * 原理：当缓存失效时，只有一个线程能获取到锁去查询数据库，
     *      其他线程休眠等待后重试。
     * 优点：数据一致性高，不会出现旧数据。
     * 缺点：高并发下会有大量线程阻塞等待，性能较差。
     *
     * @param keyPrefix  key的前缀
     * @param id         数据的唯一标识
     * @param type       返回的数据类型Class对象
     * @param dbFallback 数据库查询回调函数
     * @param time       缓存过期时间数值
     * @param unit       缓存过期时间单位
     * @return 查询到的数据
     * @param <R> 返回的数据类型
     * @param <ID> ID的类型
     */
    public <R, ID> R queryWithMutex(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 1.从redis查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            // 3.存在，直接返回
            return JSONUtil.toBean(shopJson, type);
        }
        // 判断命中的是否是空值
        if (shopJson != null) {
            // 返回一个错误信息
            return null;
        }

        // 互斥锁方案：缓存失效时只有一个线程能查数据库并写回缓存。
        String lockKey = LOCK_SHOP_KEY + id;
        R r = null;
        try {
            boolean isLock = tryLock(lockKey);
            // 4.2.判断是否获取成功
            if (!isLock) {
                // 4.3.获取锁失败，休眠并重试
                Thread.sleep(50);
                return queryWithMutex(keyPrefix, id, type, dbFallback, time, unit);
            }
            // 4.4.获取锁成功，根据id查询数据库
            r = dbFallback.apply(id);
            // 5.不存在，返回错误
            if (r == null) {
                // 将空值写入redis
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                // 返回错误信息
                return null;
            }
            // 6.存在，写入redis
            this.set(key, r, time, unit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            // 7.释放锁
            unlock(lockKey);
        }
        // 8.返回
        return r;
    }

    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}
