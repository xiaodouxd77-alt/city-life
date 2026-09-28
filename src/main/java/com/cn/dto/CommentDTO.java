package com.cn.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评论数据传输对象
 */
@Data
public class CommentDTO {
    private Long id;
    private Long userId;
    private Long blogId;
    private String blogTitle;
    private String userName;
    private String userIcon;
    private String content;
    private Integer liked;
    private LocalDateTime createTime;
}
