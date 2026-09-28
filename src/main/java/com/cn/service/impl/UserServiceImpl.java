package com.cn.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.cn.dto.LoginFormDTO;
import com.cn.dto.Result;
import com.cn.dto.UserDTO;
import com.cn.entity.User;
import com.cn.mapper.UserMapper;
import com.cn.service.UserService;
import com.cn.service.CreditsService;
import com.cn.utils.auth.JwtUtil;
import com.cn.utils.common.PasswordEncoder;
import com.cn.utils.common.RegexUtils;
import com.cn.utils.common.UserHolder;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.cn.utils.cache.RedisConstants.*;
import static com.cn.utils.common.SystemConstants.USER_NICK_NAME_PREFIX;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private CreditsService creditsService;

    /**
     * 发送登录验证码，校验手机号后将验证码写入 Redis。
     */
    @Override
    public Result sendCode(String phone, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
        String code = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        log.debug("发送短信验证码成功，验证码：{}", code);
        return Result.ok();
    }

    /**
     * 用户登录，支持验证码或密码校验，成功后返回 JWT。
     */
    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }

        User user = query().eq("phone", phone).one();
        if (user == null) {
            return Result.fail("用户不存在，请先注册");
        }

        if (loginForm.getCode() != null && !loginForm.getCode().isEmpty()) {
            String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
            if (cacheCode == null || !cacheCode.equals(loginForm.getCode())) {
                return Result.fail("验证码错误");
            }
        } else if (loginForm.getPassword() != null && !loginForm.getPassword().isEmpty()) {
            if (!PasswordEncoder.matches(user.getPassword(), loginForm.getPassword())) {
                return Result.fail("密码错误");
            }
        } else {
            return Result.fail("请输入验证码或密码");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getNickName(), user.getIcon());
        // 修改点：登录积分
        creditsService.onLogin(user.getId());
        return Result.ok(token);
    }

    /**
     * 用户注册，校验手机号和密码后创建账号并返回 JWT。
     */
    @Override
    public Result register(LoginFormDTO loginForm) {
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
        if (loginForm.getPassword() == null || loginForm.getPassword().length() < 6) {
            return Result.fail("密码至少6位");
        }

        User existUser = query().eq("phone", phone).one();
        if (existUser != null) {
            return Result.fail("该手机号已注册");
        }

        User user = new User();
        user.setPhone(phone);
        user.setPassword(PasswordEncoder.encode(loginForm.getPassword()));
        user.setNickName(loginForm.getNickName() != null ? loginForm.getNickName()
                : USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        user.setIcon(loginForm.getIcon() != null ? loginForm.getIcon() : "");
        save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getNickName(), user.getIcon());
        return Result.ok(token);
    }

    /**
     * 用户退出登录，将当前 JWT 写入 Redis 黑名单直到过期。
     */
    @Override
    public Result logout() {
        String token = UserHolder.getToken();
        if (token == null) {
            return Result.fail("未登录");
        }
        try {
            Claims claims = jwtUtil.parseToken(token);
            Date expiration = claims.getExpiration();
            long remainingTime = expiration.getTime() - System.currentTimeMillis();
            if (remainingTime > 0) {
                String tokenHash = DigestUtil.md5Hex(token);
                String key = TOKEN_BLACKLIST_KEY + tokenHash;
                stringRedisTemplate.opsForValue().set(key, "1", remainingTime, TimeUnit.MILLISECONDS);
            }
            return Result.ok();
        } catch (Exception e) {
            log.error("退出登录失败", e);
            return Result.fail("退出登录失败");
        }
    }

    /**
     * 更新当前登录用户的昵称和头像资料。
     */
    @Override
    public Result updateProfile(UserDTO userDTO) {
        Long userId = UserHolder.getUser().getId();
        User user = new User();
        user.setId(userId);
        user.setNickName(userDTO.getNickName());
        user.setIcon(userDTO.getIcon());
        user.setUpdateTime(LocalDateTime.now());
        boolean success = updateById(user);
        if (!success) {
            return Result.fail("更新失败");
        }

        UserDTO currentUser = UserHolder.getUser();
        if (userDTO.getNickName() != null) {
            currentUser.setNickName(userDTO.getNickName());
        }
        if (userDTO.getIcon() != null) {
            currentUser.setIcon(userDTO.getIcon());
        }
        return Result.ok();
    }
}
