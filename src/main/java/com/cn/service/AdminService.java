package com.cn.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.cn.dto.Result;
import com.cn.entity.Admin;

public interface AdminService extends IService<Admin> {
    Result login(String username, String password);
}
