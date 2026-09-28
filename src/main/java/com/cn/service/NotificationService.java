package com.cn.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.cn.dto.Result;
import com.cn.entity.Notification;

public interface NotificationService extends IService<Notification> {

    /**
     * 发送通知
     */
    void send(String type, Long userId, Long fromUserId, String content, Long targetId);

    /**
     * 获取未读通知列表
     */
    Result getUnread();

    /**
     * 获取当前用户的未读通知数量。
     */
    Result getUnreadCount();

    /**
     * 标记单条已读
     */
    Result markRead(Long id);

    /**
     * 全部已读
     */
    Result markAllRead();
}
