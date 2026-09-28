package com.cn.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知 WebSocket 处理器
 * 维护 userId -> WebSocketSession 集合的映射，支持向同一用户的多个在线端推送消息
 */
@Slf4j
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    /**
     * 在线用户会话映射表：userId -> WebSocketSession 集合
     * 使用 ConcurrentHashMap 保证线程安全
     */
    private static final Map<Long, Set<WebSocketSession>> sessionMap = new ConcurrentHashMap<>();

    /**
     * 连接建立成功后：从 attributes 中获取 userId，将会话加入映射表
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessionMap.computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet()).add(session);
            log.debug("WebSocket 连接建立: userId={}", userId);
        }
    }

    /**
     * 连接关闭后：从映射表中移除当前会话，若该用户无其他在线会话则删除键
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessionMap.computeIfPresent(userId, (ignored, sessions) -> {
                sessions.remove(session);
                return sessions.isEmpty() ? null : sessions;
            });
            log.debug("WebSocket 连接断开: userId={}", userId);
        }
    }

    /**
     * 处理客户端发来的文本消息（如心跳包），当前暂不处理
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 客户端发来的消息（心跳等），暂不处理
    }

    /**
     * 向指定用户的所有在线会话推送消息
     * 会自动清理已关闭的无效会话
     */
    public static void pushToUser(Long userId, String message) {
        Set<WebSocketSession> sessions = sessionMap.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                sessions.remove(session);
                continue;
            }
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                log.error("WebSocket 推送失败: userId={}", userId, e);
                sessions.remove(session);
            }
        }
        if (sessions.isEmpty()) {
            sessionMap.remove(userId, sessions);
        }
    }

    /**
     * 获取当前在线用户数（即 sessionMap 的 key 数量）
     */
    public static int getOnlineCount() {
        return sessionMap.size();
    }
}