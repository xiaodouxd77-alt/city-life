package com.cn.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.cn.dto.Result;
import com.cn.entity.SeckillVoucher;
import com.cn.entity.Shop;
import com.cn.entity.Voucher;
import com.cn.entity.VoucherOrder;
import com.cn.mapper.VoucherOrderMapper;
import com.cn.service.SeckillVoucherService;
import com.cn.service.ShopService;
import com.cn.service.VoucherOrderService;
import com.cn.service.VoucherService;
import com.cn.utils.common.RedisIdWorker;
import com.cn.utils.common.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.cn.utils.cache.RedisConstants.SECKILL_STOCK_KEY;

/**
 * 浼樻儬鍒歌鍗曚笟鍔″疄鐜般€?
 *
 * 绉掓潃閾捐矾鐨勫叧閿偣锛?
 * 1. Redis Lua 鍏堝師瀛愬垽鏂簱瀛樺拰涓€浜轰竴鍗曪紱
 * 2. RedisIdWorker 鐢熸垚鍏ㄥ眬鍞竴璁㈠崟鍙凤紱
 * 3. Redisson 閿佸拰鏁版嵁搴撴鏌ュ厹搴曢槻閲嶅璁㈠崟锛?
 * 4. 璁㈠崟鐘舵€佹満璐熻矗鏀粯銆佸彇娑堛€佹牳閿€銆侀€€娆俱€?
 */
@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements VoucherOrderService {

    @Resource
    private SeckillVoucherService seckillVoucherService;

    @Resource
    private VoucherService voucherService;

    @Resource
    private ShopService shopService;

    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        // seckill.lua 鏀惧湪 resources 涓嬶紝鑴氭湰鍦?Redis 涓師瀛愭墽琛岋紝閬垮厤骞跺彂瓒呭崠銆?
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    // Redis 涓嶅彲鐢ㄦ椂 read 浼氱珛鍗虫姏寮傚父锛岄€€閬夸竴涓嬮伩鍏嶅繖绛夊埛鏃ュ織銆?
    private static final long ERROR_BACKOFF_MILLIS = 1000L;

    // 瀹瑰櫒鍏抽棴鏃剁疆涓?false锛屾秷璐圭嚎绋嬫嵁姝ら€€鍑猴紝涓嶅啀璁块棶宸查攢姣佺殑杩炴帴宸ュ巶銆?
    private volatile boolean running = true;

    /**
     * 鍚姩绉掓潃璁㈠崟鍚庡彴娑堣垂绾跨▼锛屽鐞?Redis Stream 涓殑璁㈠崟娑堟伅銆?
     */
    @PostConstruct
    private void init() {
        // 鍚姩鍚庡彴绾跨▼娑堣垂 Redis Stream 涓殑绉掓潃璁㈠崟娑堟伅銆?
        // 褰撳墠 seckillVoucher 鏂规硶涔熶細鍚屾淇濆瓨璁㈠崟锛岃繖涓嚎绋嬩繚鐣欎负寮傛璁㈠崟澶勭悊鑳藉姏銆?
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }

    /**
     * 鍋滄娑堣垂绾跨▼銆傚厛缃爣蹇楀啀鎵撴柇锛屼繚璇佺嚎绋嬩笉浼氬湪杩炴帴宸ュ巶閿€姣佸悗缁х画閲嶈瘯銆?
     */
    @PreDestroy
    private void shutdown() {
        running = false;
        SECKILL_ORDER_EXECUTOR.shutdownNow();
    }

    // ==================== Stream 娑堟伅澶勭悊 ====================

    private class VoucherOrderHandler implements Runnable {

        /**
         * 寰幆璇诲彇 Redis Stream 绉掓潃璁㈠崟娑堟伅骞跺垱寤鸿鍗曘€?
         */
        @Override
        public void run() {
            while (running) {
                try {
                    // 浠ユ秷璐硅€呯粍鏂瑰紡璇诲彇 stream.orders锛宐lock 鏈€澶氱瓑寰?2 绉掋€?
                    // lastConsumed 琛ㄧず璇诲彇褰撳墠娑堣垂鑰呰繕娌″鐞嗙殑鏂版秷鎭€?
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create("stream.orders", ReadOffset.lastConsumed())
                    );
                    if (list == null || list.isEmpty()) {
                        continue;
                    }
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> value = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(value, new VoucherOrder(), true);
                    createVoucherOrder(voucherOrder);
                    // 涓氬姟澶勭悊鎴愬姛鍚?ACK锛屽惁鍒欐秷鎭細鐣欏湪 pending-list锛屽悗闈㈠彲浠ヨˉ鍋垮鐞嗐€?
                    stringRedisTemplate.opsForStream().acknowledge("stream.orders", "g1", record.getId());
                } catch (Exception e) {
                    // 瀹瑰櫒鍏抽棴鏃惰繛鎺ュ伐鍘傛鍦ㄩ攢姣侊紝姝ゆ椂鐩存帴閫€鍑猴紝涓嶈閲嶈瘯涔熶笉瑕佹墦鏃ュ織銆?
                    if (!running) {
                        break;
                    }
                    log.error("处理订单异常", e);
                    handlePendingList();
                    // Redis 涓嶅彲鐢ㄦ椂 read 浼氱珛鍒绘姏寮傚父锛岄€€閬夸竴涓嬪啀杩涘叆涓嬩竴杞€?
                    backoff();
                }
            }
            log.info("秒杀订单消费线程已退出");
        }

        /**
         * 琛ュ伩澶勭悊 Redis Stream 涓凡鎶曢€掍絾鏈?ACK 鐨勮鍗曟秷鎭€?
         *
         * 璇诲彇澶辫触鏃剁珛鍗宠繑鍥烇紝鎶婇噸璇曢€€閬夸氦缁欎富寰幆锛岄伩鍏嶅湪杩欓噷鏃犱紤姝㈤噸璇曞埛鏃ュ織銆?
         */
        private void handlePendingList() {
            while (running) {
                try {
                    // pending-list 淇濆瓨鈥滃凡缁忔姇閫掔粰娑堣垂鑰咃紝浣嗘秷璐硅€呰繕娌?ACK鈥濈殑娑堟伅銆?
                    // 杩欓噷浠?0 寮€濮嬭鍙栵紝灏介噺鎶婂紓甯镐腑鏂殑璁㈠崟琛ュ鐞嗗畬銆?
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1),
                            StreamOffset.create("stream.orders", ReadOffset.from("0"))
                    );
                    if (list == null || list.isEmpty()) {
                        return;
                    }
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> value = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(value, new VoucherOrder(), true);
                    createVoucherOrder(voucherOrder);
                    stringRedisTemplate.opsForStream().acknowledge("stream.orders", "g1", record.getId());
                } catch (Exception e) {
                    log.warn("处理pending订单异常，本轮补偿结束", e);
                    return;
                }
            }
        }
    }

    /**
     * 寮傚父鍚庣殑鐭殏閫€閬匡紝琚?shutdownNow 鎵撴柇鏃剁洿鎺ユ爣璁伴€€鍑恒€?
     */
    private void backoff() {
        try {
            Thread.sleep(ERROR_BACKOFF_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            running = false;
        }
    }

    /**
     * 鍒涘缓绉掓潃璁㈠崟锛屼娇鐢ㄧ敤鎴风淮搴﹀垎甯冨紡閿佸拰鏁版嵁搴撴牎楠岄槻姝㈤噸澶嶄笅鍗曘€?
     */
    private void createVoucherOrder(VoucherOrder voucherOrder) {
        Long userId = voucherOrder.getUserId();
        Long voucherId = voucherOrder.getVoucherId();
        // 閿佺矑搴︽寜鐢ㄦ埛鍒掑垎锛氬悓涓€涓敤鎴蜂笉鑳藉苟鍙戝垱寤哄寮犲悓涓€浼樻儬鍒歌鍗曘€?
        RLock redisLock = redissonClient.getLock("lock:order:" + userId);
        boolean isLock = redisLock.tryLock();
        if (!isLock) {
            log.error("不允许重复下单！");
            return;
        }
        try {
            // 鏁版嵁搴撳眰鍐嶆煡涓€娆★紝涓€浜轰竴鍗曟渶缁堜互 MySQL 涓槸鍚﹀瓨鍦ㄨ鍗曚负鍑嗐€?
            long count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
            if (count > 0) {
                log.error("不允许重复下单！");
                return;
            }
            // 鏁版嵁搴撴墸搴撳瓨浣跨敤 stock > 0 鏉′欢锛岄槻姝㈡暟鎹簱灞傚嚭鐜拌礋搴撳瓨銆?
            boolean success = seckillVoucherService.update()
                    .setSql("stock = stock - 1")
                    .eq("voucher_id", voucherId).gt("stock", 0)
                    .update();
            if (!success) {
                log.error("库存不足！");
                return;
            }
            // 榛樿鐘舵€佷负寰呮敮浠?
            voucherOrder.setStatus(1);
            voucherOrder.setShopId(resolveVoucherShopId(voucherId));
            save(voucherOrder);
        } finally {
            redisLock.unlock();
        }
    }

    // ==================== 绉掓潃涓嬪崟 ====================

    /**
     * 绉掓潃涓嬪崟锛屽厛閫氳繃 Lua 鍘熷瓙鏍￠獙搴撳瓨鍜屼竴浜轰竴鍗曪紝鍐嶅垱寤鸿鍗曘€?
     */
    @Override
    public Result seckillVoucher(Long voucherId) {
        Long userId = UserHolder.getUser().getId();
        // 浣跨敤 Redis 鑷鐢熸垚鍏ㄥ眬 ID锛岄伩鍏嶇鏉€楂樺苟鍙戦兘浜夋姠鏁版嵁搴撹嚜澧炰富閿€?
        long orderId = redisIdWorker.nextId("order");
        // Lua 杩斿洖鍊硷細0 鎴愬姛锛? 搴撳瓨涓嶈冻锛? 閲嶅涓嬪崟銆?
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString(), String.valueOf(orderId)
        );
        int r = result.intValue();
        if (r != 0) {
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }
        VoucherOrder order = new VoucherOrder();
        order.setId(orderId);
        order.setUserId(userId);
        order.setVoucherId(voucherId);
        createVoucherOrder(order);
        if (getById(orderId) == null) {
            return Result.fail("订单创建失败，请稍后重试");
        }
        // 璁㈠崟鐢?Stream 娑堣垂鑰呭紓姝ヨ惤搴擄紝閬垮厤楂樺苟鍙戣姹傜洿鎺ョ珵浜?MySQL銆?
        // 鐢?String 杩斿洖閬垮厤 JS 闀挎暣鍨嬬簿搴︿涪澶便€?
        return Result.ok(String.valueOf(orderId));
    }

    // ==================== 鏅€氬埜鐩存帴璐拱 ====================

    /**
     * 鏅€氫紭鎯犲埜璐拱锛屾牎楠屼紭鎯犲埜瀛樺湪鍜屼竴浜轰竴鍒稿悗鍒涘缓寰呮敮浠樿鍗曘€?
     */
    @Override
    public Result buyVoucher(Long voucherId) {
        Long userId = UserHolder.getUser().getId();
        // 妫€鏌ヤ紭鎯犲埜鏄惁瀛樺湪
        Voucher voucher = voucherService.getById(voucherId);
        if (voucher == null) {
            return Result.fail("优惠券不存在");
        }
        // 涓€浜轰竴鍒革紝妫€鏌ユ槸鍚﹀凡璐拱
        long count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        if (count > 0) {
            return Result.fail("您已领取过该优惠券");
        }
        // 鐩存帴鍒涘缓璁㈠崟
        VoucherOrder order = new VoucherOrder();
        long orderId = redisIdWorker.nextId("order");
        order.setId(orderId);
        order.setUserId(userId);
        order.setVoucherId(voucherId);
        order.setShopId(voucher.getShopId());
        order.setStatus(1); // 寰呮敮浠?
        save(order);
        // 鐢?String 杩斿洖閬垮厤 JS 闀挎暣鍨嬬簿搴︿涪澶?
        return Result.ok(String.valueOf(orderId));
    }

    // ==================== 璁㈠崟鐢熷懡鍛ㄦ湡 ====================

    // 鐘舵€佸父閲?
    private static final int STATUS_UNPAID = 1;
    private static final int STATUS_PAID = 2;
    private static final int STATUS_VERIFIED = 3;
    private static final int STATUS_CANCELED = 4;
    private static final int STATUS_REFUNDING = 5;
    private static final int STATUS_REFUNDED = 6;

    /**
     * 鏀粯璁㈠崟锛屾牎楠岃鍗曞綊灞炲拰鐘舵€佸悗鐢熸垚鏍搁攢鐮佸苟鏍囪涓哄凡鏀粯銆?
     */
    @Override
    public Result createAlipayOrder(Long orderId) {
        Long userId = UserHolder.getUser().getId();
        VoucherOrder order = getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.fail("无权操作此订单");
        }
        if (order.getStatus() != STATUS_UNPAID) {
            return Result.fail("订单状态不正确，无法支付");
        }
        Voucher voucher = voucherService.getById(order.getVoucherId());
        if (voucher == null || voucher.getPayValue() == null || voucher.getPayValue() <= 0) {
            return Result.fail("订单金额无效");
        }
        Map<String, Object> payment = new java.util.HashMap<>();
        payment.put("outTradeNo", String.valueOf(order.getId()));
        payment.put("subject", voucher.getTitle());
        payment.put("totalAmount", java.math.BigDecimal.valueOf(voucher.getPayValue(), 2).toPlainString());
        return Result.ok(payment);
    }

    @Override
    public boolean completeAlipayPayment(Long orderId, java.math.BigDecimal totalAmount, String tradeNo) {
        VoucherOrder order = getById(orderId);
        if (order == null || order.getStatus() == STATUS_CANCELED || order.getStatus() == STATUS_REFUNDED) {
            return false;
        }
        if (order.getStatus() == STATUS_PAID || order.getStatus() == STATUS_VERIFIED) {
            return true;
        }
        Voucher voucher = voucherService.getById(order.getVoucherId());
        if (voucher == null || voucher.getPayValue() == null) {
            return false;
        }
        java.math.BigDecimal expectedAmount = java.math.BigDecimal.valueOf(voucher.getPayValue(), 2);
        if (totalAmount == null || expectedAmount.compareTo(totalAmount) != 0) {
            log.warn("支付宝回调金额不匹配，orderId={}, expected={}, actual={}", orderId, expectedAmount, totalAmount);
            return false;
        }
        String verifyCode = RandomUtil.randomNumbers(6);
        boolean updated = update()
                .eq("id", orderId)
                .eq("status", STATUS_UNPAID)
                .set("pay_type", 2)
                .set("status", STATUS_PAID)
                .set("pay_time", LocalDateTime.now())
                .set("verify_code", verifyCode)
                .set("update_time", LocalDateTime.now())
                .update();
        return updated || getById(orderId).getStatus() == STATUS_PAID;
    }
    @Override
    public Result payOrder(Long orderId, Integer payType) {
        Long userId = UserHolder.getUser().getId();
        VoucherOrder order = getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.fail("无权操作此订单");
        }
        if (order.getStatus() != STATUS_UNPAID) {
            return Result.fail("订单状态不正确，无法支付");
        }
        // 妯℃嫙鏀粯
        try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        String verifyCode = RandomUtil.randomNumbers(6);
        order.setPayType(payType != null ? payType : 0);
        order.setStatus(STATUS_PAID);
        order.setPayTime(LocalDateTime.now());
        order.setVerifyCode(verifyCode);
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        return Result.ok("支付成功，核销码: " + verifyCode);
    }

    /**
     * 鍙栨秷寰呮敮浠樿鍗曪紝绉掓潃鍒歌鍗曚細鍚屾鎭㈠搴撳瓨銆?
     */
    @Override
    public Result cancelOrder(Long orderId) {
        Long userId = UserHolder.getUser().getId();
        VoucherOrder order = getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.fail("无权操作此订单");
        }
        if (order.getStatus() != STATUS_UNPAID) {
            return Result.fail("只有待支付订单可以取消");
        }
        order.setStatus(STATUS_CANCELED);
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        // 绉掓潃璁㈠崟鍙栨秷鍚庢仮澶嶅簱瀛?
        restoreStockIfSeckill(order.getVoucherId());
        return Result.ok();
    }

    /**
     * 鏍搁攢宸叉敮浠樿鍗曪紝鏍￠獙鏍搁攢鐮佸悗鏍囪璁㈠崟涓哄凡浣跨敤銆?
     */
    @Override
    public Result useOrder(Long orderId, String verifyCode, Long shopId) {
        VoucherOrder order = getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (order.getStatus() != STATUS_PAID) {
            return Result.fail("订单状态不正确，无法核销");
        }
        if (order.getVerifyCode() == null || !order.getVerifyCode().equals(verifyCode)) {
            return Result.fail("核销码错误");
        }
        Long applicableShopId = resolveApplicableShopId(order);
        if (applicableShopId != null && !applicableShopId.equals(shopId)) {
            return Result.fail("\u8be5\u4f18\u60e0\u5238\u4ec5\u9650\u6307\u5b9a\u5546\u5bb6\u4f7f\u7528");
        }
        order.setStatus(STATUS_VERIFIED);
        order.setUseTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        return Result.ok();
    }

    /**
     * 閫€娆惧凡鏀粯鏈牳閿€璁㈠崟锛屽苟鍦ㄥ畬鎴愬悗鎭㈠绉掓潃鍒稿簱瀛樸€?
     */
    @Override
    public Result refundOrder(Long orderId) {
        Long userId = UserHolder.getUser().getId();
        VoucherOrder order = getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.fail("无权操作此订单");
        }
        if (order.getStatus() != STATUS_PAID) {
            return Result.fail("只有已支付未核销的订单可以退款");
        }
        if (order.getPayType() != null && order.getPayType() == 4) {
            return Result.fail("积分兑换券暂不支持退款");
        }
        // 鏍囪閫€娆句腑
        order.setStatus(STATUS_REFUNDING);
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        // 妯℃嫙閫€娆惧鐞嗭紙瀹為檯涓氬姟搴旀帴鍏ユ敮浠樻笭閬撻€€娆炬帴鍙ｏ級
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // 绉掓潃璁㈠崟鎭㈠搴撳瓨
        restoreStockIfSeckill(order.getVoucherId());
        // 閫€娆惧畬鎴?
        order.setStatus(STATUS_REFUNDED);
        order.setRefundTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        return Result.ok();
    }

    /**
     * 濡傛灉璁㈠崟瀵瑰簲绉掓潃鍒革紝鍒欏悓鏃舵仮澶?Redis 鍜?MySQL 搴撳瓨銆?
     */
    private void restoreStockIfSeckill(Long voucherId) {
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
        if (seckillVoucher != null) {
            // 鎭㈠ Redis 搴撳瓨
            String stockKey = SECKILL_STOCK_KEY + voucherId;
            stringRedisTemplate.opsForValue().increment(stockKey);
            // 鎭㈠鏁版嵁搴撳簱瀛?
            seckillVoucherService.update()
                    .setSql("stock = stock + 1")
                    .eq("voucher_id", voucherId)
                    .update();
        }
    }

    // ==================== 璁㈠崟鏌ヨ ====================

    /**
     * 鏌ヨ褰撳墠鐧诲綍鐢ㄦ埛鐨勮鍗曞垪琛紝鍙寜璁㈠崟鐘舵€佽繃婊ゃ€?
     */
    @Override
    public Result myOrders(Integer status) {
        Long userId = UserHolder.getUser().getId();
        List<VoucherOrder> orders;
        if (status != null && status > 0) {
            orders = query().eq("user_id", userId).eq("status", status)
                    .orderByDesc("create_time").list();
        } else {
            orders = query().eq("user_id", userId)
                    .orderByDesc("create_time").list();
        }
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (VoucherOrder order : orders) {
            result.add(buildOrderResult(order));
        }
        return Result.ok(result);
    }

    /**
     * 鏌ヨ褰撳墠鐧诲綍鐢ㄦ埛鐨勮鍗曡鎯咃紝骞惰ˉ鍏呭叧鑱斾紭鎯犲埜淇℃伅銆?
     */
    @Override
    public Result orderDetail(Long orderId) {
        Long userId = UserHolder.getUser().getId();
        VoucherOrder order = getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.fail("无权查看此订单");
        }
        // 鏌ヨ鍏宠仈鐨勪紭鎯犲埜淇℃伅
        return Result.ok(buildOrderResult(order));
    }

    /**
     * 缁勮璁㈠崟鍜屼紭鎯犲埜灞曠ず淇℃伅锛屼緵鍒楄〃椤靛拰璇︽儏椤靛鐢ㄣ€?
     */
    private Map<String, Object> buildOrderResult(VoucherOrder order) {
        Voucher voucher = voucherService.getById(order.getVoucherId());
        Map<String, Object> result = BeanUtil.beanToMap(order);
        result.put("voucherTitle", voucher != null ? voucher.getTitle() : "未知优惠券");
        result.put("voucherValue", voucher != null ? voucher.getActualValue() : 0);
        result.put("voucherPayValue", voucher != null ? voucher.getPayValue() : 0);
        result.put("voucherSubTitle", voucher != null ? voucher.getSubTitle() : "");
        result.put("voucherRules", voucher != null ? voucher.getRules() : "");
        boolean fromCredits = order.getPayType() != null && order.getPayType() == 4;
        Long applicableShopId = resolveApplicableShopId(order);
        result.put("shopId", applicableShopId);
        result.put("fromCredits", fromCredits);
        result.put("universal", applicableShopId == null);
        if (applicableShopId == null) {
            result.put("useScope", "\u901a\u7528\u4f18\u60e0\u5238\uff0c\u5168\u90e8\u5546\u5bb6\u53ef\u7528");
        } else {
            Shop shop = shopService.getById(applicableShopId);
            String shopName = shop != null ? shop.getName() : "\u6307\u5b9a\u5546\u5bb6";
            result.put("shopName", shopName);
            result.put("useScope", "\u4ec5\u9650" + shopName + "\u4f7f\u7528");
        }
        return result;
    }

    /**
     * Resolve the shop bound to a voucher template.
     */
    private Long resolveVoucherShopId(Long voucherId) {
        Voucher voucher = voucherService.getById(voucherId);
        return voucher != null ? voucher.getShopId() : null;
    }

    /**
     * Preserve compatibility for records created before the scope column existed.
     */
    private Long resolveApplicableShopId(VoucherOrder order) {
        if (order.getShopId() != null) {
            return order.getShopId();
        }
        return order.getPayType() != null && order.getPayType() == 4
                ? null : resolveVoucherShopId(order.getVoucherId());
    }
}

