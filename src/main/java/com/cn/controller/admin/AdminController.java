package com.cn.controller.admin;

import com.cn.dto.Result;
import com.cn.service.AdminService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private AdminService adminService;
    /**
     * 登录
     *
     * @param body 登录信息
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        return adminService.login(username, password);
    }
}
