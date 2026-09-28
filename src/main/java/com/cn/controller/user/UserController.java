package com.cn.controller.user;


import cn.hutool.core.bean.BeanUtil;
import com.cn.dto.LoginFormDTO;
import com.cn.dto.Result;
import com.cn.dto.UserDTO;
import com.cn.entity.User;
import com.cn.entity.UserInfo;
import com.cn.entity.Follow;
import com.cn.mapper.FollowMapper;
import com.cn.service.UserInfoService;
import com.cn.service.UserService;
import com.cn.service.OssService;
import com.cn.utils.common.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private FollowMapper followMapper;

    @Resource
    private OssService ossService;

    /**
     * 发送手机验证码
     */
    @PostMapping("code")
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
        // 发送短信验证码并保存验证码
        return userService.sendCode(phone, session);
    }

    /**
     * 登录功能
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm, HttpSession session){
        // 实现登录功能
        return userService.login(loginForm, session);
    }

    /**
     * 注册功能
     */
    @PostMapping("/register")
    public Result register(@RequestBody LoginFormDTO loginForm) {
        return userService.register(loginForm);
    }

    /**
     * 登出功能
     * @return 无
     */
    @PostMapping("/logout")
    public Result logout(){
        return userService.logout();
    }

    /**
     * 获取当前登录用户的信息
     * @return 当前登录用户的信息
     */
    @GetMapping("/me")
    public Result me(){
        // 获取当前登录的用户并返回
        UserDTO user = UserHolder.getUser();
        return Result.ok(user);
    }

    /**
     * 查询用户详情
     * @param userId 用户id
     * @return 用户详情
     */
    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId){
        // 查询详情
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，检查是否是当前用户查看自己的资料
            UserDTO currentUser = UserHolder.getUser();
            if (currentUser != null && currentUser.getId().equals(userId)) {
                // 自动创建 UserInfo
                info = new UserInfo();
                info.setUserId(userId);
                info.setFans(0);
                info.setFollowee(0);
                info.setCredits(0);
                info.setLevel(false);
                userInfoService.save(info);
                applyFollowStats(info, userId);
                info.setCreateTime(null);
                info.setUpdateTime(null);
                return Result.ok(info);
            }
            return Result.ok();
        }
        applyFollowStats(info, userId);
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return Result.ok(info);
    }

    /**
     * Follow counters are derived from the relation table so stale profile data cannot be displayed.
     */
    private void applyFollowStats(UserInfo info, Long userId) {
        Long following = followMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Follow>()
                .eq("user_id", userId)
                .ne("follow_user_id", userId));
        Long fans = followMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Follow>()
                .eq("follow_user_id", userId)
                .ne("user_id", userId));
        info.setFollowee(following == null ? 0 : following.intValue());
        info.setFans(fans == null ? 0 : fans.intValue());
    }

    /**
     * 编辑个人资料（昵称、头像）
     */
    @PutMapping("/profile")
    public Result updateProfile(@RequestBody UserDTO userDTO){
        return userService.updateProfile(userDTO);
    }

    /**
     * 编辑个人信息（城市、简介、性别、生日）
     */
    @PutMapping("/info")
    public Result updateInfo(@RequestBody UserInfo info){
        Long userId = UserHolder.getUser().getId();
        info.setUserId(userId);
        userInfoService.saveOrUpdateInfo(info);
        return Result.ok();
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/avatar")
    public Result uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            String url = ossService.upload(file);
            // 自动更新用户头像
            Long userId = UserHolder.getUser().getId();
            User user = new User();
            user.setId(userId);
            user.setIcon(url);
            userService.updateById(user);
            // 更新 ThreadLocal
            UserDTO currentUser = UserHolder.getUser();
            currentUser.setIcon(url);
            return Result.ok(url);
        } catch (Exception e) {
            log.error("头像上传失败", e);
            return Result.fail("头像上传失败");
        }
    }

    /**
     * 根据 id 查询用户
     */
    @GetMapping("/{id}")
    public Result queryUserById(@PathVariable("id") Long userId){
        // 查询详情
        User user = userService.getById(userId);
        if (user == null) {
            return Result.ok();
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 返回
        return Result.ok(userDTO);
    }

    /**
     * 删除用户（管理员操作，同时删除关联的用户信息）
     */
    @DeleteMapping("/{id}")
    public Result deleteUser(@PathVariable("id") Long userId) {
        // 删除用户信息
        userInfoService.removeById(userId);
        // 删除用户
        boolean removed = userService.removeById(userId);
        if (!removed) {
            return Result.fail("用户不存在");
        }
        return Result.ok();
    }
}
