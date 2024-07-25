package com.nobug.public_opinion_monitor.config;

import org.springframework.context.annotation.Configuration;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Cors跨域解决
 *
 * @date：2023/2/10
 * @author：nobug
 */
@WebFilter(filterName = "CorsFilter")
@Configuration
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        String origin = ((HttpServletRequest) req).getHeader("Origin");

        response.setHeader("Access-Control-Allow-Origin",origin);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PATCH, DELETE, PUT");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "token, Origin, X-Requested-With, Content-Type, Accept, Authorization ");
        chain.doFilter(req, res);
    }
}
