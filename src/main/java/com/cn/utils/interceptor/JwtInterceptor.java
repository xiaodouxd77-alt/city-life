package com.cn.utils.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.cn.dto.UserDTO;
import com.cn.utils.common.UserHolder;
import com.cn.utils.auth.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.cn.utils.cache.RedisConstants.TOKEN_BLACKLIST_KEY;

/**
 * JWT 认证拦截器。
 *
 * <p>职责只做“识别登录用户”：如果请求头里带了合法 JWT，就解析出用户信息并放入
 * {@link UserHolder}。真正决定接口是否必须登录的是后面的 {@link LoginInterceptor}。</p>
 */
public class JwtInterceptor implements HandlerInterceptor {

    private JwtUtil jwtUtil;
    private StringRedisTemplate stringRedisTemplate;

    public JwtInterceptor(JwtUtil jwtUtil, StringRedisTemplate stringRedisTemplate) {
        this.jwtUtil = jwtUtil;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // OPTIONS 预检放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 前端统一使用 authorization: Bearer token。
        String header = request.getHeader("authorization");
        if (StrUtil.isBlank(header) || !header.startsWith("Bearer ")) {
            return true;
        }
        //去掉 "Bearer " 前缀
        String token = header.substring(7);
        try {
            // JWT 本身无状态，退出登录时不能直接删除 token。
            // 项目把退出过的 token 摘要写入 Redis 黑名单，这里先查黑名单。
            // 原始 JWT 可能很长（几百字节），直接用做 Redis Key 浪费内存且性能差。用 MD5 摘要作为 Key 既节省空间又保证唯一性
            String tokenHash = DigestUtil.md5Hex(token);
            String blacklistKey = TOKEN_BLACKLIST_KEY + tokenHash;
            Boolean isBlacklisted = stringRedisTemplate.hasKey(blacklistKey);
            if (Boolean.TRUE.equals(isBlacklisted)) {
                // token 已登出，不设置用户，让 LoginInterceptor 拦截
                return true;
            }

            if (jwtUtil.validateToken(token)) {
                Claims claims = jwtUtil.parseToken(token);
                UserDTO userDTO = new UserDTO();
                userDTO.setId(claims.get("userId", Long.class));
                userDTO.setNickName(claims.get("nickName", String.class));
                userDTO.setIcon(claims.get("icon", String.class));
                userDTO.setRole(claims.get("role", String.class));
                // UserHolder 底层是 ThreadLocal，同一次请求里的 Service 可以直接获取当前用户。
                UserHolder.saveUser(userDTO);
                UserHolder.saveToken(token);
            }
        } catch (Exception e) {
            // token 无效，不设置用户
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束后清理 ThreadLocal，防止 Tomcat 线程复用导致下个请求读到残留数据。
        // removeUser() 会同时清理 user 和 token 两个 ThreadLocal。
        UserHolder.removeUser();
    }
}
