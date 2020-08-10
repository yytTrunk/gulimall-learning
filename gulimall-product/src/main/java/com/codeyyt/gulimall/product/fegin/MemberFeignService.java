package com.codeyyt.gulimall.product.fegin;

import com.codeyyt.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-member")
public interface MemberFeignService {

    @RequestMapping("/member/testRPC/list")
    public R list(@RequestParam String params);

}
