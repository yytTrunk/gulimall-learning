package com.codeyyt.gulimall.order.to;

import com.codeyyt.gulimall.order.entity.OrderEntity;
import com.codeyyt.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateTo {
    private OrderEntity order;
    private List<OrderItemEntity> orderItems;
    private BigDecimal payPrice;
    private BigDecimal fare;
}
