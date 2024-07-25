package com.nobug.public_opinion_monitor.interceptor;

import com.alibaba.fastjson.JSON;
import com.nobug.public_opinion_monitor.common.GlobalException;
import com.nobug.public_opinion_monitor.common.R;
import com.nobug.public_opinion_monitor.utils.JwtUtil;
import com.nobug.public_opinion_monitor.utils.constant.CommonConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录拦截器
 *
 * @date：2023/2/26
 * @author：nobug
 */
@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        Claims claims = null;

        try {
            claims = JwtUtil.parseFromRequest(request);
        }catch (ExpiredJwtException e){
            response.getWriter().write(JSON.toJSONString(R.res(CommonConstants.EXPIRED_JWT,null, "登录已过期，请重新登录")));
            return false;
        }

        if(claims!=null){
            //请求中携带authorization，且jwt解析正常
            return true;
        }else{
            response.getWriter().write(JSON.toJSONString(R.failed("NOTLOGIN")));
            return false;
        }
    }
}
