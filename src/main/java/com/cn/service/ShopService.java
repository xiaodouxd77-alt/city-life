package com.cn.service;

import com.cn.dto.Result;
import com.cn.entity.Shop;
import com.baomidou.mybatisplus.spring.service.IService;

public interface ShopService extends IService<Shop> {

    Result queryById(Long id);

    Result update(Shop shop);

    Result queryShopByType(Integer typeId, Integer current, Double x, Double y);
}
