package com.cn.utils.common;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 全局唯一ID生成器（基于Redis）
 * 生成64位long整数，由时间戳（31位）+ 序列号（32位）拼接而成
 */
@Component
public class RedisIdWorker {

    /** 基准时间：2022-01-01 00:00:00 UTC */
    private static final long BEGIN_TIMESTAMP = 1640995200L;

    /** 序列号占32位，支持每天最多生成2^32≈42亿个ID */
    private static final int COUNT_BITS = 32;

    private StringRedisTemplate stringRedisTemplate;

    public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 生成全局唯一ID
     * @param keyPrefix 业务前缀，如"order"、"user"
     * @return 64位唯一ID
     */
    public long nextId(String keyPrefix) {
        // 1. 生成时间戳（当前秒数 - 基准秒数）
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;

        // 2. 生成序列号（Redis自增，每天重置）
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);

        // 3. 拼接：时间戳左移32位 + 序列号
        return timestamp << COUNT_BITS | count;
    }
}