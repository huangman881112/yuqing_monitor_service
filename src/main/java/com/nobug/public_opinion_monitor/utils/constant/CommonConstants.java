package com.nobug.public_opinion_monitor.utils.constant;

/**
 * 公共常量
 *
 * @date：2023/2/8
 * @author：nobug
 */
public interface CommonConstants {
    /**
     * 成功响应
     */
    Integer SUCCESS = 1;
    /**
     * 失败响应
     */
    Integer FAIL = 0;
    /**
     * 用户不存在
     */
    Integer USER_NOT_FOUND = 100;

    /**
     * 登录成功编码
     */
    Integer LOGIN_SUCCESS = 101;

    /**
     * 登录密码错误
     */
    Integer ERR_PWD = 102;
    /**
     * 用户禁止登录
     */
    Integer USER_FORBID= 103;
    /**
     * 账户已注销
     */
    Integer USER_REVOKED = 104;

    /**
     * 登录过期
     */
    Integer EXPIRED_JWT = -2;

    String UNAUTHENTICATED = "缺少token认证信息";

    String EMAILWAY = "email-way";

    String PHONEWAY = "phone-way";

    String UPDATEPWD = "updatepwd";

    String REGISTER = "register";
}
