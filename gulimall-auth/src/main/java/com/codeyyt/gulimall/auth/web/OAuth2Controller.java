package com.codeyyt.gulimall.auth.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 处理社交登录
 */
@Controller
@RequestMapping("/oauth2")
@Slf4j
public class OAuth2Controller {

//    @Autowired
//    MemberFeignService memberFeignService;
//
//    @GetMapping("/weibo/success")
//    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {
//
//        Map<String,String> map = new HashMap<>();
//        map.put("client_id","3418344333");
//        map.put("client_secret","249ee96d94525e3572d65ebfbf78e928");
//        map.put("grant_type","authorization_code");
//        map.put("redirect_uri","http://auth.littlemall.com/oauth2/weibo/success");
//        map.put("code", code);
//        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<String,String>(), new HashMap<String,String>(), map);
//        if (response.getStatusLine().getStatusCode() == 200) {
//            String json = EntityUtils.toString(response.getEntity());
//            WeiboUserVo weiboUserVo = JSON.parseObject(json, WeiboUserVo.class);
//            //System.out.println(weiboUserVo);
//
//
//            // TODO 远程调用  第一次登录，为当前用户注册一个账号
//            R r = memberFeignService.oauthLogin(weiboUserVo);
//            if (r.getCode() == 0) {
//                MemberVo data = r.getData(new TypeReference<MemberVo>() {});
//                log.info("登录成功:{}",data );
//                // TODO session作用域
//                session.setAttribute("loginUser",data);
//                return "redirect:http://littlemall.com";
//            }else{
//
//                return "redirect:http://auth.littlemall.com/login.html";
//            }
//
//
//        }else{
//            return "redirect:http://auth.littlemall.com/login.html";
//        }
//
//
//    }
}
