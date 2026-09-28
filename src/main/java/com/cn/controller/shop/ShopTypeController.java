package com.cn.controller.shop;


import com.cn.dto.Result;
import com.cn.entity.ShopType;
import com.cn.service.ShopTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {
    @Resource
    private ShopTypeService typeService;
    /**
     * 查询所有商铺类型
     *
     * @return 商铺类型列表
     */
    @GetMapping("list")
    public Result queryTypeList() {
        List<ShopType> typeList = typeService
                .query().orderByAsc("sort").list();
        return Result.ok(typeList);
    }
}
