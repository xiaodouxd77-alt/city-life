package com.cn.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.cn.dto.Result;
import com.cn.entity.Notification;
import com.cn.mapper.NotificationMapper;
import com.cn.service.NotificationService;
import com.cn.utils.common.UserHolder;
import com.cn.ws.NotificationWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 创建通知记录，并通过 WebSocket 推送给接收用户。
     */
    @Override
    public void send(String type, Long userId, Long fromUserId, String content, Long targetId) {
        // 不给自己发通知
        if (fromUserId != null && fromUserId.equals(userId)) {
            return;
        }
        Notification notification = new Notification();
        notification.setType(type);
        notification.setUserId(userId);
        notification.setFromUserId(fromUserId);
        notification.setContent(content);
        notification.setTargetId(targetId);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        save(notification);

        // WebSocket 实时推送（JSON 格式）
        try {
            Map<String, Object> wsPayload = new LinkedHashMap<>();
            wsPayload.put("id", notification.getId());
            wsPayload.put("type", notification.getType());
            wsPayload.put("content", notification.getContent());
            wsPayload.put("targetId", notification.getTargetId());
            wsPayload.put("fromUserId", notification.getFromUserId());
            wsPayload.put("isRead", notification.getIsRead());
            wsPayload.put("createTime", notification.getCreateTime());
            NotificationWebSocketHandler.pushToUser(userId, objectMapper.writeValueAsString(wsPayload));
        } catch (Exception e) {
            log.debug("WebSocket 推送失败: {}", e.getMessage());
        }
    }

    /**
     * 查询当前登录用户最近的通知列表。
     */
    @Override
    public Result getUnread() {
        Long userId = UserHolder.getUser().getId();
        List<Notification> list = query()
                .eq("user_id", userId)
                .orderByDesc("create_time")
                .last("LIMIT 50")
                .list();
        return Result.ok(list);
    }

    /**
     * 统计当前登录用户仍未阅读的通知数量。
     */
    @Override
    public Result getUnreadCount() {
        Long userId = UserHolder.getUser().getId();
        long count = query().eq("user_id", userId).eq("is_read", 0).count();
        return Result.ok(count);
    }

    /**
     * 将当前登录用户的一条通知标记为已读。
     */
    @Override
    public Result markRead(Long id) {
        Notification notification = getById(id);
        if (notification == null || !notification.getUserId().equals(UserHolder.getUser().getId())) {
            return Result.fail("无权操作");
        }
        notification.setIsRead(1);
        updateById(notification);
        return Result.ok();
    }

    /**
     * 将当前登录用户的所有未读通知标记为已读。
     */
    @Override
    public Result markAllRead() {
        Long userId = UserHolder.getUser().getId();
        update().set("is_read", 1).eq("user_id", userId).eq("is_read", 0).update();
        return Result.ok();
    }
}
