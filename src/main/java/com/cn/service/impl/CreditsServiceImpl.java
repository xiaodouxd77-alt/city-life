package com.cn.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.cn.dto.Result;
import com.cn.dto.UserDTO;
import com.cn.entity.*;
import com.cn.mapper.*;
import com.cn.service.*;
import com.cn.utils.common.RedisIdWorker;
import com.cn.utils.common.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CreditsServiceImpl extends ServiceImpl<CreditsLogMapper, CreditsLog> implements CreditsService {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private BadgeMapper badgeMapper;

    @Resource
    private UserBadgeMapper userBadgeMapper;

    @Resource
    private VoucherService voucherService;

    @Resource
    private CreditsExchangeMapper creditsExchangeMapper;

    @Resource
    private VoucherOrderMapper voucherOrderMapper;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String SIGN_IN_KEY = "sign:";

    // ==================== credits earning ====================

    /**
     * 每日签到，当天首次签到增加积分并更新签到天数。
     */
    @Override
    @Transactional
    public Result dailySignIn() {
        Long userId = UserHolder.getUser().getId();
        UserInfo userInfo = userInfoService.getById(userId);
        if (userInfo == null) {
            return Result.fail("user not found");
        }
        LocalDate today = LocalDate.now();
        if (today.equals(userInfo.getLastSignDate())) {
            return Result.fail("already signed in today");
        }
        int creditsToAdd = 2;
        userInfo.setCredits(userInfo.getCredits() + creditsToAdd);
        userInfo.setTotalSignDays(userInfo.getTotalSignDays() + 1);
        userInfo.setLastSignDate(today);
        userInfo.setUpdateTime(LocalDateTime.now());
        userInfoService.updateById(userInfo);
        addLog(userId, creditsToAdd, "sign", "daily sign-in +" + creditsToAdd);
        checkAndAwardBadges(userId);
        return Result.ok("sign-in success, credits +" + creditsToAdd);
    }

    /**
     * 给指定用户增加积分（通用积分增加方法）。
     */
    @Override
    @Transactional
    public void addCredits(Long userId, int amount, String type, String remark) {
        UserInfo userInfo = userInfoService.getById(userId);
        if (userInfo == null) return;
        userInfo.setCredits(userInfo.getCredits() + amount);
        userInfo.setUpdateTime(LocalDateTime.now());
        userInfoService.updateById(userInfo);
        addLog(userId, amount, type, remark);
    }

    /**
     * 用户发布笔记，增加发布笔记计数和积分奖励。
     */
    @Override
    @Transactional
    public void onBlogPublished(Long userId) {
        UserInfo userInfo = userInfoService.getById(userId);
        if (userInfo != null) {
            userInfo.setTotalBlogs(userInfo.getTotalBlogs() != null ? userInfo.getTotalBlogs() + 1 : 1);
            userInfo.setCredits(userInfo.getCredits() + 5);
            userInfo.setUpdateTime(LocalDateTime.now());
            userInfoService.updateById(userInfo);
            addLog(userId, 5, "blog", "publish blog +5");
        }
        checkAndAwardBadges(userId);
    }

    /**
     * 笔记被点赞，增加笔记作者的获赞数和积分奖励。
     */
    @Override
    @Transactional
    public void onBlogLiked(Long authorId) {
        UserInfo userInfo = userInfoService.getById(authorId);
        if (userInfo != null) {
            userInfo.setTotalLikes(userInfo.getTotalLikes() != null ? userInfo.getTotalLikes() + 1 : 1);
            userInfo.setCredits(userInfo.getCredits() + 1);
            userInfo.setUpdateTime(LocalDateTime.now());
            userInfoService.updateById(userInfo);
            addLog(authorId, 1, "like", "blog liked +1");
        }
        checkAndAwardBadges(authorId);
    }

    /**
     * 用户登录，每日首次登录奖励积分（Redis 判重）。
     */
    @Override
    @Transactional
    public void onLogin(Long userId) {
        String redisKey = SIGN_IN_KEY + userId + ":" + LocalDate.now();
        Boolean hasKey = stringRedisTemplate.hasKey(redisKey);
        if (Boolean.TRUE.equals(hasKey)) {
            return;
        }
        UserInfo userInfo = userInfoService.getById(userId);
        if (userInfo != null) {
            userInfo.setCredits(userInfo.getCredits() + 1);
            userInfo.setUpdateTime(LocalDateTime.now());
            userInfoService.updateById(userInfo);
            addLog(userId, 1, "login", "first login today +1");
            stringRedisTemplate.opsForValue().set(redisKey, "1");
        }
    }

    /**
     * 关注新用户，奖励积分。
     */
    @Override
    @Transactional
    public void onFollow(Long userId) {
        addCredits(userId, 1, "follow", "follow user +1");
    }

    // ==================== credits consumption ====================

    /**
     * 用积分兑换优惠券，校验积分余额后扣减积分并记录兑换。
     */
    @Override
    @Transactional
    public Result exchangeVoucher(Long voucherId) {
        Long userId = UserHolder.getUser().getId();
        UserInfo userInfo = userInfoService.getById(userId);
        if (userInfo == null) {
            return Result.fail("user not found");
        }
        Voucher voucher = voucherService.getById(voucherId);
        if (voucher == null) {
            return Result.fail("voucher not found");
        }
        // 积分商城只允许兑换平台通用券，不能兑换绑定店铺的普通券或秒杀券。
        if (voucher.getShopId() != null || voucher.getType() == null || voucher.getType() != 0
                || voucher.getStatus() == null || voucher.getStatus() != 1) {
            return Result.fail("该优惠券不支持积分兑换");
        }
        Long ownedCount = voucherOrderMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<VoucherOrder>()
                        .eq("user_id", userId)
                        .eq("voucher_id", voucherId)
                        .in("status", 1, 2, 3, 5)
        );
        if (ownedCount > 0) {
            return Result.fail("您已拥有该优惠券");
        }
        int cost = getVoucherCreditsCost(voucher);
        if (userInfo.getCredits() < cost) {
            return Result.fail("insufficient credits, need " + cost + ", current " + userInfo.getCredits());
        }
        long orderId = redisIdWorker.nextId("order");
        String verifyCode = RandomUtil.randomNumbers(6);
        LocalDateTime now = LocalDateTime.now();
        userInfo.setCredits(userInfo.getCredits() - cost);
        userInfo.setUpdateTime(LocalDateTime.now());
        userInfoService.updateById(userInfo);
        CreditsExchange exchange = new CreditsExchange();
        exchange.setUserId(userId);
        exchange.setVoucherId(voucherId);
        exchange.setCreditsCost(cost);
        exchange.setStatus(1);
        creditsExchangeMapper.insert(exchange);
        VoucherOrder order = new VoucherOrder();
        order.setId(orderId);
        order.setUserId(userId);
        order.setVoucherId(voucherId);
        // Points redemptions are platform-wide vouchers, independent of the source template's shop.
        order.setShopId(null);
        order.setPayType(4);
        order.setStatus(2);
        order.setCreateTime(now);
        order.setPayTime(now);
        order.setUpdateTime(now);
        order.setVerifyCode(verifyCode);
        voucherOrderMapper.insert(order);
        addLog(userId, -cost, "exchange", "exchange voucher \"" + voucher.getTitle() + "\" cost " + cost + " credits");
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", String.valueOf(orderId));
        result.put("verifyCode", verifyCode);
        result.put("creditsCost", cost);
        return Result.ok(result);
    }

    /**
     * 根据优惠券面额计算所需积分。
     */
    private int getVoucherCreditsCost(Voucher voucher) {
        Long payValue = voucher.getPayValue();
        if (payValue == null) return 100;
        if (payValue >= 10000) return 1000;
        if (payValue >= 8000) return 800;
        if (payValue >= 5000) return 500;
        if (payValue >= 3000) return 300;
        return 100;
    }

    // ==================== query ====================

    /**
     * 获取当前登录用户的积分信息。
     */
    @Override
    public Result getCreditsInfo() {
        Long userId = UserHolder.getUser().getId();
        UserInfo userInfo = userInfoService.getById(userId);
        if (userInfo == null) {
            return Result.ok(Map.of("credits", 0, "totalSignDays", 0, "totalBadges", 0));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("credits", userInfo.getCredits());
        result.put("totalSignDays", userInfo.getTotalSignDays() != null ? userInfo.getTotalSignDays() : 0);
        result.put("totalBadges", userInfo.getTotalBadges() != null ? userInfo.getTotalBadges() : 0);
        result.put("totalBlogs", userInfo.getTotalBlogs() != null ? userInfo.getTotalBlogs() : 0);
        result.put("totalLikes", userInfo.getTotalLikes() != null ? userInfo.getTotalLikes() : 0);
        result.put("lastSignDate", userInfo.getLastSignDate());
        result.put("todaySigned", LocalDate.now().equals(userInfo.getLastSignDate()));
        Long availableCoupons = voucherOrderMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<VoucherOrder>()
                        .eq("user_id", userId)
                        .eq("status", 2)
        );
        result.put("availableCoupons", availableCoupons != null ? availableCoupons : 0);
        return Result.ok(result);
    }

    /**
     * 获取当前登录用户的积分流水记录。
     */
    @Override
    public Result getCreditsLogs() {
        Long userId = UserHolder.getUser().getId();
        List<CreditsLog> logs = query()
                .eq("user_id", userId)
                .orderByDesc("create_time")
                .last("LIMIT 50")
                .list();
        return Result.ok(logs);
    }

    /**
     * 获取可积分兑换的优惠券列表。
     */
    @Override
    public Result getExchangeableVouchers() {
        // 积分商城只展示 shop_id 为 NULL 的平台通用券。
        List<Voucher> vouchers = voucherService.list(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Voucher>()
                        .isNull("shop_id")
                        .eq("type", 0)
                        .eq("status", 1)
        );
        List<Map<String, Object>> result = new ArrayList<>();
        for (Voucher v : vouchers) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", v.getId());
            item.put("title", v.getTitle());
            item.put("subTitle", v.getSubTitle());
            item.put("rules", v.getRules());
            item.put("payValue", v.getPayValue());
            item.put("actualValue", v.getActualValue());
            item.put("creditsCost", getVoucherCreditsCost(v));
            item.put("shopId", null);
            item.put("universal", true);
            item.put("useScope", "\u901a\u7528\u4f18\u60e0\u5238\uff0c\u5168\u90e8\u5546\u5bb6\u53ef\u7528");
            result.add(item);
        }
        return Result.ok(result);
    }

    /**
     * 获取当前登录用户的积分兑换历史。
     */
    @Override
    public Result getExchangeHistory() {
        Long userId = UserHolder.getUser().getId();
        List<CreditsExchange> list = creditsExchangeMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditsExchange>()
                        .eq("user_id", userId)
                        .orderByDesc("create_time")
                        .last("LIMIT 20")
        );
        List<Map<String, Object>> result = new ArrayList<>();
        for (CreditsExchange exchange : list) {
            Map<String, Object> item = BeanUtil.beanToMap(exchange);
            Voucher voucher = voucherService.getById(exchange.getVoucherId());
            item.put("voucherTitle", voucher != null ? voucher.getTitle() : "未知优惠券");
            result.add(item);
        }
        return Result.ok(result);
    }

    // ==================== badge system ====================

    /**
     * 检查用户所有勋章条件，自动颁发新满足条件的勋章。
     */
    @Override
    public void checkAndAwardBadges(Long userId) {
        UserInfo userInfo = userInfoService.getById(userId);
        if (userInfo == null) return;
        List<Badge> allBadges = badgeMapper.selectList(null);
        List<UserBadge> owned = userBadgeMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserBadge>()
                        .eq("user_id", userId)
        );
        Set<Long> ownedBadgeIds = owned.stream().map(UserBadge::getBadgeId).collect(Collectors.toSet());
        int newBadgeCount = 0;
        for (Badge badge : allBadges) {
            if (ownedBadgeIds.contains(badge.getId())) continue;
            boolean meetCondition = checkBadgeCondition(badge, userInfo);
            if (meetCondition) {
                UserBadge ub = new UserBadge();
                ub.setUserId(userId);
                ub.setBadgeId(badge.getId());
                ub.setEarnedAt(LocalDateTime.now());
                userBadgeMapper.insert(ub);
                newBadgeCount++;
                log.info("user {} earned badge: {}", userId, badge.getName());
            }
        }
        if (newBadgeCount > 0) {
            userInfo.setTotalBadges(userInfo.getTotalBadges() != null
                    ? userInfo.getTotalBadges() + newBadgeCount : newBadgeCount);
            userInfoService.updateById(userInfo);
        }
    }

    /**
     * 根据勋章条件类型判断用户是否满足该勋章条件。
     */
    private boolean checkBadgeCondition(Badge badge, UserInfo userInfo) {
        String type = badge.getConditionType();
        int val = badge.getConditionValue();
        int totalBlogs = userInfo.getTotalBlogs() != null ? userInfo.getTotalBlogs() : 0;
        int totalLikes = userInfo.getTotalLikes() != null ? userInfo.getTotalLikes() : 0;
        int fans = userInfo.getFans() != null ? userInfo.getFans() : 0;
        int signDays = userInfo.getTotalSignDays() != null ? userInfo.getTotalSignDays() : 0;
        int credits = userInfo.getCredits() != null ? userInfo.getCredits() : 0;
        switch (type) {
            case "blog_count": return totalBlogs >= val;
            case "like_count": return totalLikes >= val;
            case "fan_count":  return fans >= val;
            case "sign_days":  return signDays >= val;
            case "credits":    return credits >= val;
            default: return false;
        }
    }

    /**
     * 获取用户已获得的勋章列表。
     */
    @Override
    public Result getUserBadges(Long userId) {
        if (userId == null) {
            userId = UserHolder.getUser().getId();
        }
        List<UserBadge> userBadges = userBadgeMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserBadge>()
                        .eq("user_id", userId)
        );
        if (userBadges.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        List<Long> badgeIds = userBadges.stream().map(UserBadge::getBadgeId).collect(Collectors.toList());
        List<Badge> badges = badgeMapper.selectBatchIds(badgeIds);
        badges.sort(Comparator.comparingInt(Badge::getSortOrder));
        Map<Long, LocalDateTime> earnedMap = userBadges.stream()
                .collect(Collectors.toMap(UserBadge::getBadgeId, UserBadge::getEarnedAt));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Badge b : badges) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", b.getId());
            item.put("name", b.getName());
            item.put("description", b.getDescription());
            item.put("icon", b.getIcon());
            item.put("iconText", b.getIconText());
            item.put("earnedAt", earnedMap.get(b.getId()));
            result.add(item);
        }
        return Result.ok(result);
    }

    /**
     * 获取所有勋章定义。
     */
    @Override
    public Result getAllBadges() {
        List<Badge> badges = badgeMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Badge>()
                        .orderByAsc("sort_order")
        );
        return Result.ok(badges);
    }

    // ==================== utility methods ====================

    /**
     * 添加一条积分变动流水记录。
     */
    private void addLog(Long userId, int amount, String type, String remark) {
        CreditsLog logRecord = new CreditsLog();
        logRecord.setUserId(userId);
        logRecord.setAmount(amount);
        logRecord.setType(type);
        logRecord.setRemark(remark);
        logRecord.setCreateTime(LocalDateTime.now());
        save(logRecord);
    }
}
