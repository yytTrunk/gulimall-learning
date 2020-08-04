package com.codeyyt.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.codeyyt.gulimall.common.utils.PageUtils;
import com.codeyyt.gulimall.order.entity.OrderReturnApplyEntity;

import java.util.Map;

/**
 * 订单退货申请
 *
 * @author codeyyt
 * @email XXXXXX@gmail.com
 * @date 2020-08-04 23:55:29
 */
public interface OrderReturnApplyService extends IService<OrderReturnApplyEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

