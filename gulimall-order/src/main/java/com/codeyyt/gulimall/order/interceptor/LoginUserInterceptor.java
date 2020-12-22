package com.codeyyt.gulimall.order.interceptor;

import com.codeyyt.gulimall.common.constant.AuthServerConstant;
import com.codeyyt.gulimall.common.constant.CartConstant;
import com.codeyyt.gulimall.common.vo.MemberVo;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/order/order/status/**", requestURI);
        if(match) {
            return true;
        }

        MemberVo attribute = (MemberVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute == null){
            request.getSession().setAttribute("msg","请先登录");
            response.sendRedirect("http://localhost:88/api/auth/login.html");
            return false;
        }else{
            loginUser.set(attribute);
            return true;
        }
    }
}
