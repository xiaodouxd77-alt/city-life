package com.cn.controller.trade;


import com.cn.dto.Result;
import com.cn.entity.Voucher;
import com.cn.service.VoucherService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/voucher")
public class VoucherController {

    @Resource
    private VoucherService voucherService;

    /**
     * 新增秒杀券
     * @param voucher 优惠券信息，包含秒杀信息
     * @return 优惠券id
     */
    @PostMapping("seckill")
    public Result addSeckillVoucher(@RequestBody Voucher voucher) {
        voucherService.addSeckillVoucher(voucher);
        return Result.ok(voucher.getId());
    }

    /**
     * 新增普通券
     * @param voucher 优惠券信息
     * @return 优惠券id
     */
    @PostMapping
    public Result addVoucher(@RequestBody Voucher voucher) {
        voucherService.save(voucher);
        return Result.ok(voucher.getId());
    }


    /**
     * 查询店铺的优惠券列表
     * @param shopId 店铺id
     * @return 优惠券列表
     */
    @GetMapping("/list/{shopId}")
    public Result queryVoucherOfShop(@PathVariable("shopId") Long shopId) {
       return voucherService.queryVoucherOfShop(shopId);
    }

    /**
     * 删除优惠券（同时清理秒杀券记录和 Redis 库存）
     */
    @DeleteMapping("/{id}")
    public Result deleteVoucher(@PathVariable("id") Long id) {
        return voucherService.deleteVoucher(id);
    }
}
