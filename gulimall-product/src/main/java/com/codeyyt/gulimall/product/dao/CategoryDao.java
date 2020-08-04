package com.codeyyt.gulimall.product.dao;

import com.codeyyt.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author codeyyt
 * @email XXXXXX@gmail.com
 * @date 2020-08-04 00:16:20
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
