package com.codeyyt.gulimall.ware.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.codeyyt.gulimall.common.utils.PageUtils;
import com.codeyyt.gulimall.common.utils.Query;

import com.codeyyt.gulimall.ware.dao.WareInfoDao;
import com.codeyyt.gulimall.ware.entity.WareInfoEntity;
import com.codeyyt.gulimall.ware.service.WareInfoService;
import org.springframework.util.StringUtils;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<WareInfoEntity>();

        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            key = (String) params.get("key");
            wrapper.eq("id", key).or()
                    .like("name",key)
                    .or().like("address",key)
                    .or().like("areacode",key);
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

}