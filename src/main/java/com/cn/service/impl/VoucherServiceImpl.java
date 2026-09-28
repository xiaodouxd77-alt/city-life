package com.cn.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.cn.dto.Result;
import com.cn.entity.SeckillVoucher;
import com.cn.entity.Voucher;
import com.cn.mapper.VoucherMapper;
import com.cn.service.SeckillVoucherService;
import com.cn.service.VoucherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.util.List;

import static com.cn.utils.cache.RedisConstants.SECKILL_STOCK_KEY;

@Slf4j
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements VoucherService {

    @Resource
    private SeckillVoucherService seckillVoucherService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 启动时将 MySQL 中的秒杀券库存同步到 Redis。
     */
    @PostConstruct
    public void initSeckillStock() {
        List<SeckillVoucher> list = seckillVoucherService.list();
        for (SeckillVoucher sv : list) {
            String key = SECKILL_STOCK_KEY + sv.getVoucherId();
            stringRedisTemplate.opsForValue().set(key, String.valueOf(sv.getStock()));
            log.info("同步秒杀库存到 Redis: {} = {}", key, sv.getStock());
        }
    }

    /**
     * 查询指定店铺下的全部优惠券和秒杀券信息。
     */
    @Override
    public Result queryVoucherOfShop(Long shopId) {
        // 查询优惠券信息
        List<Voucher> vouchers = getBaseMapper().queryVoucherOfShop(shopId);
        // 返回结果
        return Result.ok(vouchers);
    }

    /**
     * 新增秒杀券，同时保存优惠券、秒杀信息和 Redis 库存。
     */
    @Override
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        // 保存优惠券
        save(voucher);
        // 保存秒杀信息
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);
        // 保存秒杀库存到Redis中
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY + voucher.getId(), voucher.getStock().toString());
    }

    /**
     * 删除优惠券，秒杀券会同时清理秒杀记录和 Redis 库存。
     */
    @Override
    @Transactional
    public Result deleteVoucher(Long id) {
        Voucher voucher = getById(id);
        if (voucher == null) {
            return Result.fail("优惠券不存在");
        }
        // 如果是秒杀券，清理秒杀券记录和 Redis 库存
        if (voucher.getType() != null && voucher.getType() == 1) {
            seckillVoucherService.removeById(id);
            stringRedisTemplate.delete(SECKILL_STOCK_KEY + id);
        }
        // 删除优惠券
        removeById(id);
        return Result.ok();
    }

}
