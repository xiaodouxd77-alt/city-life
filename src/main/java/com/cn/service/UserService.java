package com.cn.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.cn.dto.LoginFormDTO;
import com.cn.dto.Result;
import com.cn.dto.UserDTO;
import com.cn.entity.User;

import jakarta.servlet.http.HttpSession;

public interface UserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);

    Result register(LoginFormDTO loginForm);

    Result logout();

    Result updateProfile(UserDTO userDTO);

}
