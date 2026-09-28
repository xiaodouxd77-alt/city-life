package com.cn.service;

import com.cn.dto.Result;
import com.cn.entity.CreditsLog;
import com.baomidou.mybatisplus.spring.service.IService;

public interface CreditsService extends IService<CreditsLog> {

    Result dailySignIn();

    void addCredits(Long userId, int amount, String type, String remark);

    void onBlogPublished(Long userId);

    void onBlogLiked(Long authorId);

    void onLogin(Long userId);

    void onFollow(Long userId);

    Result exchangeVoucher(Long voucherId);

    Result getCreditsInfo();

    Result getCreditsLogs();

    Result getExchangeableVouchers();

    Result getExchangeHistory();

    void checkAndAwardBadges(Long userId);

    Result getUserBadges(Long userId);

    Result getAllBadges();
}
