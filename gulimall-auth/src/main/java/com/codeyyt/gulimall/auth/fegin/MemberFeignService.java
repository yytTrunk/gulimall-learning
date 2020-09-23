package com.codeyyt.gulimall.auth.fegin;

import com.codeyyt.gulimall.auth.vo.UserLoginVo;
import com.codeyyt.gulimall.auth.vo.UserRegistVo;
import com.codeyyt.gulimall.auth.vo.WeiboUserVo;
import com.codeyyt.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo UserRegistVo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    R oauthLogin(@RequestBody WeiboUserVo vo);
}
