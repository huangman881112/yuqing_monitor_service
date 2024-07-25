package com.nobug.public_opinion_monitor.utils;

import com.nobug.public_opinion_monitor.common.GlobalException;
import com.nobug.public_opinion_monitor.config.SystemPropertiesConfig;
import com.nobug.public_opinion_monitor.service.UserService;
import com.nobug.public_opinion_monitor.utils.constant.CommonConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

/**
 * JWT工具类
 *
 * @date：2023/2/10
 * @author：nobug
 */
@Component
public class JwtUtil {

    //UserService
    private UserService userService;

    //配置文件类
    private static SystemPropertiesConfig systemPropertiesConfig;

    @Autowired
    public void init(SystemPropertiesConfig systemPropertiesConfig) {
        JwtUtil.systemPropertiesConfig = systemPropertiesConfig;
    }

    /**
     * 生成认证token
     * @param id
     * @param subject
     * @param map
     * @return
     */
    public static String createJwt(String id, String subject, Map<String, Object> map){
        //1、设置失效时间
        long now = System.currentTimeMillis();
        long exp = now + systemPropertiesConfig.getJwtFailureTime();

        //2、创建JwtBuilder
        JwtBuilder jwtBuilder = Jwts.builder().setId(id).setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(exp))
                .signWith(SignatureAlgorithm.HS256, systemPropertiesConfig.getJwtKey());

        //3、根据map设置claims
        for(Map.Entry<String, Object> entry: map.entrySet()){
            jwtBuilder.claim(entry.getKey(), entry.getValue());
        }

        //4、创建token
        return jwtBuilder.compact();
    }

    /**
     * 解析token
     * @param token
     * @return
     */
    public static Claims parseJwt(String token){
        Claims claims = Jwts.parser().setSigningKey(systemPropertiesConfig.getJwtKey()).parseClaimsJws(token).getBody();
        return claims;
    }

    /**
     * 从request中获取Claims
     * @param request
     * @return
     */
    public static Claims parseFromRequest(HttpServletRequest request){
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isEmpty(authorization)) {
            throw new GlobalException(CommonConstants.UNAUTHENTICATED);
        }
        String token = authorization.replace("Bearer ", "");
        return parseJwt(token);
    }


}
