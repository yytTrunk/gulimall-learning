package com.codeyyt.gulimall.common.to.mq;

import lombok.Data;

@Data
public class StockLockedTo {
    private Long id;
    private StockDetailTo detail;
}
