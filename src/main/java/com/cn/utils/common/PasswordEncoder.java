package com.cn.utils.common;

import cn.hutool.core.util.RandomUtil;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * 密码加密工具类
 * 采用"盐值+MD5"的方式对密码进行加密存储。
 * 每次加密都会生成随机的盐值，即使用户使用相同的密码，加密后的结果也不同，
 */
public class PasswordEncoder {

    /**
     * 加密密码（自动生成随机盐值）
     * 对外提供的加密入口，会自动生成20位随机字符串作为盐值。
     */
    public static String encode(String password) {
        // 1. 生成20位的随机字符串作为盐值
        String salt = RandomUtil.randomString(20);

        // 2. 调用私有方法进行加密
        return encode(password, salt);
    }

    /**
     * 加密密码（使用指定盐值）
     * 内部使用的加密方法，将密码和盐值拼接后进行MD5摘要。
     */
    private static String encode(String password, String salt) {
        // 1. 将密码和盐值拼接：password + salt
        // 2. 将拼接后的字符串转为字节数组
        // 3. 计算MD5摘要，得到32位的十六进制字符串
        String md5Hex = DigestUtils.md5DigestAsHex((password + salt).getBytes(StandardCharsets.UTF_8));

        // 4. 按照"盐值@MD5摘要"的格式返回
        return salt + "@" + md5Hex;
    }

    /**
     * 验证密码是否正确
     * 从加密密码中提取出盐值，然后用相同的算法重新计算MD5，
     * 最后比较计算结果是否与存储的加密密码一致。
     */
    public static Boolean matches(String encodedPassword, String rawPassword) {
        // 1. 参数校验：任一参数为null则返回false
        if (encodedPassword == null || rawPassword == null) {
            return false;
        }

        // 2. 校验加密密码格式：必须包含@分隔符
        if (!encodedPassword.contains("@")) {
            throw new RuntimeException("密码格式不正确！");
        }

        // 3. 按@分割，取出盐值部分
        // arr[0] = 盐值，arr[1] = MD5摘要
        String[] arr = encodedPassword.split("@");
        String salt = arr[0];  // 获取盐值

        // 4. 使用相同的盐值和算法重新计算密码的MD5
        // 然后比较计算结果是否与存储的一致
        return encodedPassword.equals(encode(rawPassword, salt));
    }
}