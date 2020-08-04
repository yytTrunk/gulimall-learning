package com.codeyyt.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.codeyyt.gulimall.common.utils.PageUtils;
import com.codeyyt.gulimall.order.entity.RefundInfoEntity;

import java.util.Map;

/**
 * 退款信息
 *
 * @author codeyyt
 * @email XXXXXX@gmail.com
 * @date 2020-08-04 23:55:29
 */
public interface RefundInfoService extends IService<RefundInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

