package com.codeyyt.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.codeyyt.gulimall.common.utils.PageUtils;
import com.codeyyt.gulimall.coupon.entity.SmsSkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author codeyyt
 * @email XXXXXX@gmail.com
 * @date 2020-08-05 00:08:16
 */
public interface SmsSkuFullReductionService extends IService<SmsSkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

