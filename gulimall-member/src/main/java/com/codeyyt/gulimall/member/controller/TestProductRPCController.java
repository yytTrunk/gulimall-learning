package com.codeyyt.gulimall.member.controller;

import com.codeyyt.gulimall.common.utils.PageUtils;
import com.codeyyt.gulimall.common.utils.R;
import com.codeyyt.gulimall.member.entity.GrowthChangeHistoryEntity;
import com.codeyyt.gulimall.member.service.GrowthChangeHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 * 成长值变化历史记录
 *
 * @author codeyyt
 * @email XXXXXX@gmail.com
 * @date 2020-08-05 00:01:16
 */
@RestController
@RequestMapping("member/testRPC")
public class TestProductRPCController {
    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam String params){
        return R.ok().put("data", "hello, I am gulimall-member. revcive value = " + params);
    }
}


