package com.cn.controller.credits;

import com.cn.dto.Result;
import com.cn.service.CreditsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/credits")
public class CreditsController {

    @Resource
    private CreditsService creditsService;

    /**
     * 每日签到
     */
    @PostMapping("/sign-in")
    public Result dailySignIn() {
        return creditsService.dailySignIn();
    }

    /**
     * 查询我的积分信息
     */
    @GetMapping("/info")
    public Result getCreditsInfo() {
        return creditsService.getCreditsInfo();
    }

    /**
     * 查询积分流水
     */
    @GetMapping("/logs")
    public Result getCreditsLogs() {
        return creditsService.getCreditsLogs();
    }

    /**
     * 可兑换的优惠券列表
     */
    @GetMapping("/vouchers")
    public Result getExchangeableVouchers() {
        return creditsService.getExchangeableVouchers();
    }

    /**
     * 用积分兑换优惠券
     */
    @PostMapping("/exchange/{voucherId}")
    public Result exchangeVoucher(@PathVariable Long voucherId) {
        return creditsService.exchangeVoucher(voucherId);
    }

    /**
     * 兑换历史
     */
    @GetMapping("/exchanges")
    public Result getExchangeHistory() {
        return creditsService.getExchangeHistory();
    }

    /**
     * 我的所有勋章
     */
    @GetMapping("/badges")
    public Result getMyBadges() {
        return creditsService.getUserBadges(null);
    }

    /**
     * 指定用户的勋章
     */
    @GetMapping("/badges/{userId}")
    public Result getUserBadges(@PathVariable Long userId) {
        return creditsService.getUserBadges(userId);
    }

    /**
     * 所有勋章定义
     */
    @GetMapping("/badges/all")
    public Result getAllBadges() {
        return creditsService.getAllBadges();
    }
}
