package com.codeyyt.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.codeyyt.gulimall.common.utils.PageUtils;
import com.codeyyt.gulimall.member.entity.MemberEntity;
import com.codeyyt.gulimall.member.exception.PhoneExistException;
import com.codeyyt.gulimall.member.exception.UsernameExistException;
import com.codeyyt.gulimall.member.vo.MemberLoginVo;
import com.codeyyt.gulimall.member.vo.MemberRegistVo;
import com.codeyyt.gulimall.member.vo.WeiboMemberVo;

import java.util.Map;

/**
 * 会员
 *
 * @author codeyyt
 * @email XXXXXX@gmail.com
 * @date 2020-08-05 00:01:16
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo memberRegistVo);

    void checkUsernameUnique(String name) throws UsernameExistException;

    void checkMobileUnique(String mobile) throws PhoneExistException;

    MemberEntity login(MemberLoginVo vo);

    MemberEntity oauthLogin(WeiboMemberVo vo);
}

