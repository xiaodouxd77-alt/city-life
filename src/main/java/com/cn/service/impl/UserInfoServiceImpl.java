package com.cn.service.impl;

import com.cn.entity.UserInfo;
import com.cn.mapper.UserInfoMapper;
import com.cn.service.UserInfoService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    /**
     * 保存或更新用户扩展资料，新建时补齐默认粉丝、关注、积分和等级信息。
     */
    @Override
    public boolean saveOrUpdateInfo(UserInfo info) {
        UserInfo exist = getById(info.getUserId());
        if (exist == null) {
            info.setFans(0);
            info.setFollowee(0);
            info.setCredits(0);
            info.setLevel(false);
            info.setCreateTime(LocalDateTime.now());
            info.setUpdateTime(LocalDateTime.now());
            return save(info);
        } else {
            info.setUpdateTime(LocalDateTime.now());
            return updateById(info);
        }
    }

}
