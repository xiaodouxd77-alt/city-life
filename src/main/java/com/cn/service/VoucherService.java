package com.cn.service;

import com.cn.dto.Result;
import com.cn.entity.Voucher;
import com.baomidou.mybatisplus.spring.service.IService;

public interface VoucherService extends IService<Voucher> {

    Result queryVoucherOfShop(Long shopId);

    void addSeckillVoucher(Voucher voucher);

    Result deleteVoucher(Long id);
}
