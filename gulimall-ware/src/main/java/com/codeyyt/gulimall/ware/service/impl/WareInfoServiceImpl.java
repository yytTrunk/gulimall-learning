package com.codeyyt.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.codeyyt.gulimall.common.utils.R;
import com.codeyyt.gulimall.ware.feign.MemberFeignService;
import com.codeyyt.gulimall.ware.vo.FareVo;
import com.codeyyt.gulimall.ware.vo.MemberAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    @Autowired
    MemberFeignService memberFeignService;

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

    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();
        R r = memberFeignService.addrInfo(addrId);
        MemberAddressVo data = r.getData("memberReceiveAddress",new TypeReference<MemberAddressVo>() {
        });
        if(data != null){
            String phone = data.getPhone();
            String substring = phone.substring(phone.length() - 1);
            BigDecimal bigDecimal = new BigDecimal(substring);
            fareVo.setAddress(data);
            fareVo.setFare(bigDecimal);
            return fareVo;

        }

        return null;
    }

}