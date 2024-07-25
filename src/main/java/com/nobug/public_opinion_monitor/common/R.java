package com.nobug.public_opinion_monitor.common;

import com.nobug.public_opinion_monitor.utils.constant.CommonConstants;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 响应信息体
 *
 * @date：2023/2/8
 * @author：nobug
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private int code;

    @Getter
    @Setter
    private String msg;

    @Getter
    @Setter
    private T data;

    public static <T> R<T> retResult(T data, int code, String msg) {
        R<T> apiResult = new R<T>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMsg(msg);
        return apiResult;
    }

    public static <T> R<T> ok() {
        return retResult(null, CommonConstants.SUCCESS, null);
    }

    public static <T> R<T> ok(T data) {
        return retResult(data, CommonConstants.SUCCESS, null);
    }

    public static <T> R<T> ok(T data, String msg) {
        return retResult(data, CommonConstants.SUCCESS, msg);
    }


    public static <T> R<T> failed() {
        return retResult(null, CommonConstants.FAIL, null);
    }

    public static <T> R<T> failed(String msg) {
        return retResult(null, CommonConstants.FAIL, msg);
    }

    public static <T> R<T> failed(T data) {
        return retResult(data, CommonConstants.FAIL, null);
    }

    public static <T> R<T> failed(T data, String msg) {
        return retResult(data, CommonConstants.FAIL, msg);
    }

    public static <T> R<T> res(Integer code, T data, String msg) {
        return retResult(data, code, msg);
    }
}

