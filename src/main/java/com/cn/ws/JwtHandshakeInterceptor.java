package com.cn.ws;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.cn.utils.auth.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

import static com.cn.utils.cache.RedisConstants.TOKEN_BLACKLIST_KEY;

/**
 * WebSocket 握手拦截器：从 URL 参数中提取 JWT token 并校验
 */
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;

    public JwtHandshakeInterceptor(JwtUtil jwtUtil, StringRedisTemplate stringRedisTemplate) {
        this.jwtUtil = jwtUtil;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 握手前拦截：校验 token 有效性并将 userId 存入 attributes
     *
     * @param request  请求对象
     * @param response 响应对象
     * @param wsHandler WebSocket 处理器
     * @param attributes 握手属性，可传递给 WebSocket 会话
     * @return true 表示放行，false 表示拒绝连接
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            // 从 URL 参数中获取 token
            String token = servletRequest.getServletRequest().getParameter("token");
            // token 为空则拒绝连接
            if (StrUtil.isBlank(token)) {
                return false;
            }
            try {
                // 检查 token 是否在黑名单中（已退出登录）
                if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(TOKEN_BLACKLIST_KEY + DigestUtil.md5Hex(token)))) {
                    return false;
                }
                // 解析 JWT token，获取 claims
                Claims claims = jwtUtil.parseToken(token);
                // 从 claims 中提取 userId
                Long userId = claims.get("userId", Long.class);
                if (userId == null) {
                    return false;
                }
                // 将 userId 存入 attributes，后续可在 WebSocket 会话中获取
                attributes.put("userId", userId);
                return true;
            } catch (Exception e) {
                // 解析异常直接拒绝连接
                return false;
            }
        }
        return false;
    }

    /**
     * 握手完成后回调：当前暂无特殊处理
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}