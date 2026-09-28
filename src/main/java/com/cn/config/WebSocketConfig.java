package com.cn.config;

import com.cn.utils.auth.JwtUtil;
import com.cn.ws.JwtHandshakeInterceptor;
import com.cn.ws.NotificationWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import jakarta.annotation.Resource;

/**
 * WebSocket 配置
 * 端点: ws://localhost:8081/ws/notification?token=xxx
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new NotificationWebSocketHandler(), "/ws/notification") // WebSocket 处理器
                .addInterceptors(new JwtHandshakeInterceptor(jwtUtil, stringRedisTemplate)) // 为该端点添加握手拦截器，创建 JWT 握手拦截器实例
                .setAllowedOrigins("*"); // 允许任何域名/IP 发起 WebSocket 连接
    }
}
