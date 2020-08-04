package com.codeyyt.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.codeyyt.gulimall.common.utils.PageUtils;
import com.codeyyt.gulimall.ware.entity.WareOrderTaskDetailEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author codeyyt
 * @email XXXXXX@gmail.com
 * @date 2020-08-04 23:30:29
 */
public interface WareOrderTaskDetailService extends IService<WareOrderTaskDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

