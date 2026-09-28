package com.cn.utils.interceptor;

import cn.hutool.core.util.StrUtil;
import com.cn.utils.auth.JwtUtil;
import com.cn.dto.UserDTO;
import com.cn.utils.common.UserHolder;
import io.jsonwebtoken.Claims;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 管理员权限拦截器：校验 JWT role 是否为 ADMIN
 */
public class AdminInterceptor implements HandlerInterceptor {

    private JwtUtil jwtUtil;

    public AdminInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String header = request.getHeader("authorization");
        if (StrUtil.isBlank(header) || !header.startsWith("Bearer ")) {
            response.setStatus(401);
            return false;
        }
        String token = header.substring(7);
        try {
            Claims claims = jwtUtil.parseToken(token);
            String role = claims.get("role", String.class);
            if (!"ADMIN".equals(role)) {
                response.setStatus(403);
                return false;
            }
            // 将管理员信息存入 ThreadLocal
            UserDTO userDTO = new UserDTO();
            userDTO.setId(claims.get("userId", Long.class));
            userDTO.setNickName(claims.get("nickName", String.class));
            UserHolder.saveUser(userDTO);
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserHolder.removeUser();
    }
}
