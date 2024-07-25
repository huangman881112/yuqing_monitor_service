package com.nobug.public_opinion_monitor.config;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.nobug.public_opinion_monitor.interceptor.LoginInterceptor;
import com.nobug.public_opinion_monitor.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * Web配置类
 *
 * @date：2023/2/9
 * @author：nobug
 */
@Configuration
public class WebConfiger  implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    //配置fastjson
    @Bean
    public HttpMessageConverters fastJsonHttpMessageConverters() {
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        List<MediaType> mediaTypes = new ArrayList<>(16);
        mediaTypes.add(MediaType.APPLICATION_ATOM_XML);
        mediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
        mediaTypes.add(MediaType.APPLICATION_JSON);
        mediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
        fastConverter.setSupportedMediaTypes(mediaTypes);
        HttpMessageConverter converter = fastConverter;
        return new HttpMessageConverters(converter);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //定义白名单
        String[] urls = new String[]{
                "/istelephoneexists",
                "/isaccountexists",
                "/login",
                "/register",
                "/getcaptcha",
                "/verifycaptcha",
                "/updatepwd",
        };
        registry.addInterceptor(loginInterceptor).addPathPatterns("/**").excludePathPatterns(urls);
    }
}
