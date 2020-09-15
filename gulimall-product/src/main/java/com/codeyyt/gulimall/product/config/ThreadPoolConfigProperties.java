package com.codeyyt.gulimall.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


//@Component
//@ConfigurationProperties(prefix = "gulimall.thread")
@Data
public class ThreadPoolConfigProperties {
    private Integer coreSize = 30;
    private Integer maxSize = 100000;
    private Integer keepAliveTime = 30;
}
