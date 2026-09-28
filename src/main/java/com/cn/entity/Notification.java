package com.cn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息通知
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_notification")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接收通知的用户id
     */
    private Long userId;

    /**
     * 通知类型: LIKE, COMMENT, REPLY, FOLLOW, SYSTEM
     */
    private String type;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 关联的目标id
     */
    private Long targetId;

    /**
     * 触发通知的用户id
     */
    private Long fromUserId;

    /**
     * 是否已读: 0-未读, 1-已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
