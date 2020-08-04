package com.codeyyt.gulimall.order.dao;

import com.codeyyt.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author codeyyt
 * @email XXXXXX@gmail.com
 * @date 2020-08-04 23:55:29
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
