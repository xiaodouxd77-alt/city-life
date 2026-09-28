package com.cn.config;

import com.cn.utils.interceptor.AdminInterceptor;
import com.cn.utils.interceptor.JwtInterceptor;
import com.cn.utils.auth.JwtUtil;
import com.cn.utils.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.Resource;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // JWT 璁よ瘉鎷︽埅鍣?
        registry.addInterceptor(new JwtInterceptor(jwtUtil, stringRedisTemplate)).addPathPatterns("/**").order(0);
        // 鐧诲綍鎷︽埅鍣?
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns(
                        "/shop/**", "/voucher/**", "/shop-type/**", "/upload/**",
                        "/blog/hot", "/blog/likes/**", "/blog/of/user", "/blog/of/shop",
                        "/blog/feed/rebuild",
                        "/user/code", "/user/login", "/user/register", "/user/info/*",
                        "/blog-comments/of/**", "/admin/login", "/alipay/notify", "/alipay/return"
                ).order(1);
        // 绠＄悊鍛樻潈闄愭嫤鎴櫒
        registry.addInterceptor(new AdminInterceptor(jwtUtil))
                .addPathPatterns("/admin/**", "/blog/*/toggle-status", "/blog-comments/*/toggle-status")
                .excludePathPatterns("/admin/login")
                .order(2);
    }

    /**
     * 璺ㄥ煙閰嶇疆
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}

