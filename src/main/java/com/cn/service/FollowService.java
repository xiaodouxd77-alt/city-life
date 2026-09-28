package com.cn.service;

import com.cn.dto.Result;
import com.cn.entity.Follow;
import com.baomidou.mybatisplus.spring.service.IService;

public interface FollowService extends IService<Follow> {

    Result follow(Long followUserId, Boolean isFollow);

    Result isFollow(Long followUserId);

    Result followCommons(Long id);

    /**
     * Query the current user's following list.
     */
    Result myFollows();
}
