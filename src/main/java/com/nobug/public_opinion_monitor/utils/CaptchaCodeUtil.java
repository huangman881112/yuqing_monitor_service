package com.nobug.public_opinion_monitor.utils;

/**
 * 验证码工具类
 *
 * @date：2023/2/11
 * @author：nobug
 */
public class CaptchaCodeUtil {
    public static String getCode(){
        return String.valueOf((int)((Math.random()*9+1)*100000));
    }
}
