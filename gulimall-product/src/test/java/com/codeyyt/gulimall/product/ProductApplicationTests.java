package com.codeyyt.gulimall.product;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductApplicationTests {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void testRedis() {
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();

        //valueOperations.set("test", "123123");

        System.out.println(valueOperations.get("test"));

    }
}
