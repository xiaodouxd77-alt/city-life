package com.cn.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.cn.dto.Result;
import com.cn.entity.Admin;
import com.cn.mapper.AdminMapper;
import com.cn.service.AdminService;
import com.cn.utils.auth.JwtUtil;
import com.cn.utils.common.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Resource
    private JwtUtil jwtUtil;

    /**
     * 管理员登录，校验账号密码并生成管理员 JWT。
     */
    @Override
    public Result login(String username, String password) {
        Admin admin = query().eq("username", username).one();
        if (admin == null) {
            return Result.fail("管理员不存在");
        }
        if (!PasswordEncoder.matches(admin.getPassword(), password)) {
            return Result.fail("密码错误");
        }
        String token = jwtUtil.generateToken(admin.getId(), admin.getUsername(), "", admin.getRole());
        return Result.ok(token);
    }
}
