package com.codeyyt.gulimall.search.service;

import com.codeyyt.gulimall.search.vo.SearchParamVo;
import com.codeyyt.gulimall.search.vo.SearchResponseVo;

public interface MallSearchService {
    SearchResponseVo search(SearchParamVo searchParamVo);
}