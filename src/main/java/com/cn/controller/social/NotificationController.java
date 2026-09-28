package com.cn.controller.social;

import com.cn.dto.Result;
import com.cn.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    /**
     * 获取通知列表（含已读和未读）
     */
    @GetMapping("/unread")
    public Result getUnread() {
        return notificationService.getUnread();
    }

    /**
     * 获取未读通知数量。
     */
    @GetMapping("/unread-count")
    public Result getUnreadCount() {
        return notificationService.getUnreadCount();
    }

    /**
     * 标记单条已读
     */
    @PutMapping("/{id}/read")
    public Result markRead(@PathVariable("id") Long id) {
        return notificationService.markRead(id);
    }

    /**
     * 全部已读
     */
    @PutMapping("/read-all")
    public Result markAllRead() {
        return notificationService.markAllRead();
    }
}
