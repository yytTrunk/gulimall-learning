package com.codeyyt.gulimall.product.vo;

import com.codeyyt.gulimall.product.entity.SkuImagesEntity;
import com.codeyyt.gulimall.product.entity.SkuInfoEntity;
import com.codeyyt.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {

    SkuInfoEntity info;

    List<SkuImagesEntity> images;

    SpuInfoDescEntity desp;

    List<SkuItemSaleAttrVo> saleAttr;

    List<SpuItemAttrGroupVo> groupAttrs;

    boolean hasStock = true;


}
