package com.nobug.public_opinion_monitor.common;

/**
 * 全局异常类
 *
 * @date：2023/2/10
 * @author：nobug
 */
public class GlobalException extends RuntimeException{
    public GlobalException(String msg){
        super(msg);
    }
}
