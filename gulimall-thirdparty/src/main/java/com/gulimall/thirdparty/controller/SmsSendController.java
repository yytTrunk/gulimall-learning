package com.gulimall.thirdparty.controller;

import com.codeyyt.gulimall.common.utils.R;
import com.gulimall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
public class SmsSendController {

    @Autowired
    SmsComponent smsComponent;

    @GetMapping("/sendcode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code){

        // todo  实现接口防刷，可以将手机号存入redis中，加上过期时间60s, 这样可以避免通过刷新页面或者不断请求，导致不断请求
        //smsComponent.sendSmsCode(phone, code);

        return R.ok();
    }
}
