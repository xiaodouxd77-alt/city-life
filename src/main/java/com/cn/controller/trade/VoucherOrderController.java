package com.cn.controller.trade;


import com.cn.dto.Result;
import com.cn.service.VoucherOrderService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {

    @Resource
    private VoucherOrderService voucherOrderService;

    /**
     * 模拟支付（将订单标记为已支付）
     */
    @PostMapping("/{id}/mock-pay")
    public Result mockPay(@PathVariable("id") Long orderId) {
        return voucherOrderService.payOrder(orderId, 2);
    }

    /**
     * 秒杀优惠券
     */
    @PostMapping("seckill/{id}")
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
        return voucherOrderService.seckillVoucher(voucherId);
    }

    /**
     * 普通券直接购买
     */
    @PostMapping("buy/{id}")
    public Result buyVoucher(@PathVariable("id") Long voucherId) {
        return voucherOrderService.buyVoucher(voucherId);
    }

    /**
     * 支付订单
     */
    @PostMapping("/{id}/pay")
    public Result payOrder(@PathVariable("id") Long orderId, @RequestBody Map<String, Integer> body) {
        Integer payType = body != null ? body.get("payType") : 1;
        return voucherOrderService.payOrder(orderId, payType);
    }

    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    public Result cancelOrder(@PathVariable("id") Long orderId) {
        return voucherOrderService.cancelOrder(orderId);
    }

    /**
     * 核销订单
     */
    @PostMapping("/{id}/use")
    public Result useOrder(@PathVariable("id") Long orderId, @RequestBody Map<String, Object> body) {
        String verifyCode = body != null && body.get("verifyCode") != null
                ? String.valueOf(body.get("verifyCode")) : "";
        Object shopIdValue = body != null ? body.get("shopId") : null;
        Long shopId = shopIdValue == null ? null : Long.valueOf(String.valueOf(shopIdValue));
        return voucherOrderService.useOrder(orderId, verifyCode, shopId);
    }

    /**
     * 退款
     */
    @PostMapping("/{id}/refund")
    public Result refundOrder(@PathVariable("id") Long orderId) {
        return voucherOrderService.refundOrder(orderId);
    }

    /**
     * 我的订单列表
     */
    @GetMapping("/my")
    public Result myOrders(@RequestParam(required = false) Integer status) {
        return voucherOrderService.myOrders(status);
    }

    /**
     * 订单详情
     */
    @GetMapping("/{id}")
    public Result orderDetail(@PathVariable("id") Long orderId) {
        return voucherOrderService.orderDetail(orderId);
    }
}
