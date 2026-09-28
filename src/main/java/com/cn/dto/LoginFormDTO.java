package com.cn.dto;

import lombok.Data;

/**
 * 登录表单
 */
@Data
public class LoginFormDTO {
    private String phone;
    private String code;
    private String password;
    private String nickName;
    private String icon;
}
