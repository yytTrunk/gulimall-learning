package com.codeyyt.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.codeyyt.gulimall.common.utils.PageUtils;
import com.codeyyt.gulimall.order.entity.OrderSettingEntity;

import java.util.Map;

/**
 * 订单配置信息
 *
 * @author codeyyt
 * @email XXXXXX@gmail.com
 * @date 2020-08-04 23:55:29
 */
public interface OrderSettingService extends IService<OrderSettingEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

