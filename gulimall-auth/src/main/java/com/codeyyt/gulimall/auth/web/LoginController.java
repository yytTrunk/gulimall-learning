package com.codeyyt.gulimall.auth.web;

import com.alibaba.fastjson.TypeReference;
import com.codeyyt.gulimall.auth.fegin.MemberFeignService;
import com.codeyyt.gulimall.auth.vo.UserLoginVo;
import com.codeyyt.gulimall.auth.vo.UserRegistVo;
import com.codeyyt.gulimall.common.constant.AuthServerConstant;
import com.codeyyt.gulimall.common.exception.BizCodeEnum;
import com.codeyyt.gulimall.common.utils.R;
import com.codeyyt.gulimall.auth.fegin.ThirdPartyFeignService;
import com.codeyyt.gulimall.common.vo.MemberVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class LoginController {

    @Autowired
    ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone){

        // TODO 接口防刷
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if(!StringUtils.isEmpty(redisCode)){
            long time = Long.parseLong(redisCode.split("_")[1]);
            if(System.currentTimeMillis() - time < 60000){
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(),BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        // 2 验证码的再次校验
        // 便于测试，把这里写死
        //String code = UUID.randomUUID().toString().substring(0, 6);
        String code = "test";
        String code_time = code + "_" + System.currentTimeMillis();
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, code_time,10, TimeUnit.MINUTES);
        thirdPartyFeignService.sendCode(phone, code);

        return R.ok();
    }

    /**
     * TODO 重定向携带数据，利用session原理，分布式可能存在问题
     * @param vo
     * @param result
     * @param redirectAttributes
     * @return
     */
    @PostMapping("api/auth/regist")
    public String register(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes){
        // 校验
        if(result.hasErrors()){
            Map<String, String> collect = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, DefaultMessageSourceResolvable::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors",collect);
            return "redirect:http://localhost:88/api/auth/reg.html";
        }

        // 校验验证码
        String code = "test";//vo.getCode();
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if(!StringUtils.isEmpty(redisCode) && code.equals(redisCode.split("_")[0])){
            redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
            // TODO 调用远程服务进行注册
            R r = memberFeignService.regist(vo);
            if(r.getCode() == 0){
                return "redirect:http://localhost:88/api/auth/login.html";
            }else{
                Map<String,String> errors = new HashMap<>();
                errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
                redirectAttributes.addFlashAttribute("errors",errors);
                return "redirect:http://localhost:88/api/auth/reg.html";
            }

        }else{
            Map<String,String> errors = new HashMap<>();
            errors.put("code","验证码错误");
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://localhost:88/api/auth/reg.html";
        }
    }

    @PostMapping("api/auth/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session){

        R r = memberFeignService.login(vo);
        if(r.getCode() == 0){
            MemberVo memberVo = r.getData(new TypeReference<MemberVo>() {});
            session.setAttribute(AuthServerConstant.LOGIN_USER, memberVo);
            log.info("登录成功:{}", memberVo);
            return "redirect:http://localhost:10009";
        }else{
            Map<String,String> errors = new HashMap<>();
            errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.littlemall.com/login.html";
        }
    }
}
