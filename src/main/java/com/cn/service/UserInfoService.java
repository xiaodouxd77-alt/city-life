package com.cn.service;

import com.cn.entity.UserInfo;
import com.baomidou.mybatisplus.spring.service.IService;

public interface UserInfoService extends IService<UserInfo> {

    /**
     * 保存或更新用户详情
     */
    boolean saveOrUpdateInfo(UserInfo info);

}
