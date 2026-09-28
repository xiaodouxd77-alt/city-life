package com.cn.service;

import com.cn.dto.Result;
import com.cn.entity.VoucherOrder;
import com.baomidou.mybatisplus.spring.service.IService;

public interface VoucherOrderService extends IService<VoucherOrder> {

    Result seckillVoucher(Long voucherId);

    Result buyVoucher(Long voucherId);

    Result payOrder(Long orderId, Integer payType);

    Result createAlipayOrder(Long orderId);

    boolean completeAlipayPayment(Long orderId, java.math.BigDecimal totalAmount, String tradeNo);

    Result cancelOrder(Long orderId);

    Result useOrder(Long orderId, String verifyCode, Long shopId);

    Result refundOrder(Long orderId);

    Result myOrders(Integer status);

    Result orderDetail(Long orderId);
}

