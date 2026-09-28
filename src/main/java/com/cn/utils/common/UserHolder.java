package com.cn.utils.common;

import com.cn.dto.UserDTO;

/**
 * 用户信息持有者
 * 基于ThreadLocal实现在同一个请求线程内共享用户信息和Token
 */
public class UserHolder {

    /** 存储当前线程的用户信息 */
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    /** 存储当前线程的Token */
    private static final ThreadLocal<String> tokenHolder = new ThreadLocal<>();

    /** 保存用户信息到当前线程 */
    public static void saveUser(UserDTO user) {
        tl.set(user);
    }

    /** 获取当前线程的用户信息 */
    public static UserDTO getUser() {
        return tl.get();
    }

    /** 清除当前线程的所有数据（请求结束时调用，防止内存泄漏） */
    public static void removeUser() {
        tl.remove();
        tokenHolder.remove();
    }

    /** 保存Token到当前线程 */
    public static void saveToken(String token) {
        tokenHolder.set(token);
    }

    /** 获取当前线程的Token */
    public static String getToken() {
        return tokenHolder.get();
    }
}