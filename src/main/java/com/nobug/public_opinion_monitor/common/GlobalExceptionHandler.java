package com.nobug.public_opinion_monitor.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器
 *
 * @date：2023/2/17
 * @author：nobug
 */
@RestControllerAdvice
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> doSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException ex){
        //log.info(ex.getMessage());
        if(ex.getMessage().contains("Duplicate entry")){
            String[] split = ex.getMessage().split(" ");
            String msg = split[2]+"已存在";
            return R.failed(msg);
        }
        return R.failed("未知错误");
    }

    @ExceptionHandler(GlobalException.class)
    public R<String> doGlobalException(GlobalException ex){
        return R.failed(ex.getMessage());
    }
}
