package com.codeyyt.gulimall.member.dao;

import com.codeyyt.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author codeyyt
 * @email XXXXXX@gmail.com
 * @date 2020-08-05 00:01:16
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
