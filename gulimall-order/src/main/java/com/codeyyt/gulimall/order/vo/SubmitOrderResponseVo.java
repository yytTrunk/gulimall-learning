package com.codeyyt.gulimall.order.vo;

import com.codeyyt.gulimall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderResponseVo {
    private OrderEntity order;
    private Integer code;

}
