package com.codeyyt.gulimall.order;

import com.alibaba.cloud.seata.GlobalTransactionAutoConfiguration;
import io.seata.spring.annotation.GlobalTransactional;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRabbit
@EnableFeignClients
@EnableRedisHttpSession
@EnableDiscoveryClient
@MapperScan("com.codeyyt.gulimall.order.dao")
@SpringBootApplication(exclude = GlobalTransactionAutoConfiguration.class)
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
