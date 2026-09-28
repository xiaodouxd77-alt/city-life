package com.cn.service.impl;

import com.cn.entity.BlogComments;
import com.cn.mapper.BlogCommentsMapper;
import com.cn.service.BlogCommentsService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements BlogCommentsService {

}
