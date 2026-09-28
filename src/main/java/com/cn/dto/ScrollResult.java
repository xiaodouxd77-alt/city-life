package com.cn.dto;

import lombok.Data;

import java.util.List;

/**
 * 滚动分页数据封装类
 */
@Data
public class ScrollResult {
    private List<?> list; // 数据列表
    private Long minTime; // 时间戳
    private Integer offset; // 偏移量
}
