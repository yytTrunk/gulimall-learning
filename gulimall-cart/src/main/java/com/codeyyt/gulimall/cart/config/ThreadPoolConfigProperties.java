package com.codeyyt.gulimall.cart.config;

import lombok.Data;


//@Component
//@ConfigurationProperties(prefix = "gulimall.thread")
@Data
public class ThreadPoolConfigProperties {
    private Integer coreSize = 30;
    private Integer maxSize = 100000;
    private Integer keepAliveTime = 30;
}
