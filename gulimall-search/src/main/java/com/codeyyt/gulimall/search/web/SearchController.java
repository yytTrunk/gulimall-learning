package com.codeyyt.gulimall.search.web;

import com.codeyyt.gulimall.search.service.MallSearchService;
import com.codeyyt.gulimall.search.vo.SearchParamVo;
import com.codeyyt.gulimall.search.vo.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("search/list.html")
    public String listPage(SearchParamVo searchParamVo, Model model, HttpServletRequest request){
        searchParamVo.set_queryString(request.getQueryString());
        SearchResponseVo result = mallSearchService.search(searchParamVo);
        model.addAttribute("result",result);

        System.out.println(result);

        return "list";
    }
}
