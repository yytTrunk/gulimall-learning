package com.codeyyt.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.codeyyt.gulimall.common.exception.BizCodeEnum;
import com.codeyyt.gulimall.member.exception.PhoneExistException;
import com.codeyyt.gulimall.member.exception.UsernameExistException;
import com.codeyyt.gulimall.member.vo.MemberLoginVo;
import com.codeyyt.gulimall.member.vo.MemberRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.codeyyt.gulimall.member.entity.MemberEntity;
import com.codeyyt.gulimall.member.service.MemberService;
import com.codeyyt.gulimall.common.utils.PageUtils;
import com.codeyyt.gulimall.common.utils.R;

/**
 * 会员
 *
 * @author codeyyt
 * @email XXXXXX@gmail.com
 * @date 2020-08-05 00:01:16
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 注册
     */
    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo memberRegistVo){
        try{
            memberService.regist(memberRegistVo);
        }catch (PhoneExistException e){
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnum.PHONE_EXIST_EXCEPTION.getMsg());
        }catch (UsernameExistException e){
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(),BizCodeEnum.USER_EXIST_EXCEPTION.getMsg());
        }


        return R.ok();
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo){
        MemberEntity entity = memberService.login(vo);
        if(entity!= null){
            return R.ok().put("data",entity);

        }else {
            return R.error(BizCodeEnum.LOGINACCI_PASSWORD_INVALID_EXCEPTION.getCode(),BizCodeEnum.LOGINACCI_PASSWORD_INVALID_EXCEPTION.getMsg());
        }
    }

}
