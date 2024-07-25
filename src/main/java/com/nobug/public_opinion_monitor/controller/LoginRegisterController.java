package com.nobug.public_opinion_monitor.controller;

import cn.hutool.core.date.DateUtil;
import com.nobug.public_opinion_monitor.common.GlobalException;
import com.nobug.public_opinion_monitor.common.R;
import com.nobug.public_opinion_monitor.dao.UserDao;
import com.nobug.public_opinion_monitor.dto.UserDTO;
import com.nobug.public_opinion_monitor.entity.User;
import com.nobug.public_opinion_monitor.service.UserService;
import com.nobug.public_opinion_monitor.service.impl.MailService;
import com.nobug.public_opinion_monitor.utils.*;
import com.nobug.public_opinion_monitor.utils.constant.CommonConstants;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 登录注册controller
 *
 * @date：2023/2/8
 * @author：nobug
 */
@RestController
@RequestMapping(value = "/")
@Slf4j
public class LoginRegisterController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MailService mailService;

    /**
     * 登出
     * @param request
     */
    @GetMapping(value = "/logout")
    public R logout(HttpServletRequest request){
        try {
            //1、从请求头中获取Claims
            Claims claims = JwtUtil.parseFromRequest(request);
            //log.info(String.valueOf(claims));
            //2、更新在线状态
            userService.updateIsOnline(Long.valueOf(claims.getId()));

            return R.ok(null, "用户登出成功！");
        }catch (Exception e){
            throw new GlobalException("发生错误:" + e.getMessage());
        }
    }

    /**
     * 校验账户是否存在
     * @param account
     * @return
     */
    @GetMapping(value = "/isaccountexists")
    public R isTelephoneExists(String account){
        User user = userService.selectUserByTelephoneOrEmail(account);
        Boolean res = null!=user ? true: false;
        return R.ok(res);
    }
    /**
     * 登录
     * @param loginMap
     * @return
     */
    @PostMapping(value = "/login")
    public R<String> login(@RequestBody Map<String, String> loginMap){
        int code = -1;
        String msg = "";
        String token = "";
        try{
            //1、获取请求参数，其中password进行AES对称解密
            String account = loginMap.get("account");
            String password = AESUtil.aesDecrypt(loginMap.get("password"));
            //log.info("解密密码：{}",password);
            //2、根据手机号或邮箱获取用户信息
            User user = userService.selectUserByTelephoneOrEmail(account);
            //log.info(user.toString());

            if(null != user){
                if(user.getStatus() == 0){
                    code = CommonConstants.USER_FORBID;
                    msg = "用户禁止登录";
                }else{
                    if(MD5Util.getMD5(password).equals(user.getPassword())){
                        if(user.getStatus() == 2){
                            code = CommonConstants.USER_REVOKED;
                            msg = "账户已被注销";
                        }else{
                            code = CommonConstants.LOGIN_SUCCESS;
                            msg = "用户登录成功";
                            //log.info("~~~~~~~~~~~{}",user.getLoginCount());
                            //2、更新用户最后登录时间、登录次数和在线状态
                            Integer login_count = user.getLoginCount()!=null? user.getLoginCount()+ 1:1;
                            String end_login_time = DateUtil.formatDateTime(new Date());
                            Map<String, Object> usermap = new HashMap<>();
                            usermap.put("account", account);
                            usermap.put("end_login_time", end_login_time);
                            usermap.put("login_count", login_count);
                            userService.updateUserLoginCountByPhoneOrEmail(usermap);
                            //3、生成token
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("telephone", user.getTelephone());
                            dataMap.put("email", user.getEmail());
                            dataMap.put("organizationId", user.getOrganizationId());
                            token = JwtUtil.createJwt(Long.toString(user.getUserId()), user.getUsername(), dataMap);
                        }
                    }else{
                        code = CommonConstants.ERR_PWD;
                        msg = "登录密码错误";
                    }
                }
            }else{
                code = CommonConstants.USER_NOT_FOUND;
                msg = "用户不存在";
            }
            return R.res(code, token, msg);
        }catch (Exception e){
            throw new GlobalException("发生错误:" + e.getMessage());
        }
    }

    /**
     * 注册
     * @param userDTO
     * @return
     */
    @PostMapping(value = "/register")
    public R register(@RequestBody UserDTO userDTO){
        //log.info("{}",userDTO);
        try {
            String account = userDTO.getTelephone()==null?userDTO.getEmail():userDTO.getTelephone();
            String captcha = userDTO.getCaptcha();
            //1、校验验证码
            Object redisCode = redisTemplate.opsForValue().get(account);
            if(null==redisCode || !redisCode.equals(captcha)){
                return R.failed("验证码校验失败");
            }
            //2、校验通过，删除key
            Set keys = redisTemplate.keys(account);
            redisTemplate.delete(keys);
            //3、注册
            User user = new User();
            user.setTelephone(userDTO.getTelephone());
            user.setEmail(userDTO.getEmail());
            user.setUsername(userDTO.getUsername());
            user.setPassword(userDTO.getPassword());
            Boolean flag = userService.saveUser(user);
            if (flag) {
                return R.ok(null, "注册成功，请登录");
            } else {
                return R.failed("注册失败，请联系管理员");
            }
        }catch (Exception e){
            throw new GlobalException("发生错误:" + e.getMessage());
        }
    }

    /**
     * 发送验证码
     * @param type
     * @param account
     * @return
     */
    @GetMapping(value = "/getcaptcha")
    public R getCaptcha(String type, String account, String operate){
        try{
            String code = CaptchaCodeUtil.getCode();
            StringBuilder text = new StringBuilder("【舆情监测系统】您正在使用邮件");
            log.info(code);
            if(CommonConstants.REGISTER.equals(operate)){
                text.append("注册账号");
            }else if(CommonConstants.UPDATEPWD.equals(operate)) {
                text.append("更改密码");
            }else{
                return R.failed("参数operate错误，没有"+operate+" 操作");
            }
            text.append("，验证码："+code+"，请在5分钟内按页面提示提交验证码，切勿将验证码泄露于他人！");
            String subject = "舆情监测系统验证码";
            if(CommonConstants.PHONEWAY.equals(type)){
                // 调用短信服务API
            }else if (CommonConstants.EMAILWAY.equals(type)){
                // 向邮箱发送验证码

                mailService.sendTextMailMessage(account, subject, text.toString());
            }else{
                return R.failed("参数type错误，没有"+type+" 注册方式");
            }
            redisTemplate.opsForValue().set(account, code, 5 , TimeUnit.MINUTES);
            return R.ok(null,"验证码已发送");
        }catch (Exception e){
            return R.failed("验证码发送失败，请稍后再试");
        }
    }

    /**
     * 根据手机号或邮箱更新密码
     * @param userDTO
     * @return
     */
    @PutMapping(value = "/updatepwd")
    public R updatePwd(@RequestBody UserDTO userDTO){
        try {
            String account = userDTO.getAccount();
            String captcha = userDTO.getCaptcha();
            //1、校验验证码
            Object redisCode = redisTemplate.opsForValue().get(account);
            if(null==redisCode || !redisCode.equals(captcha)){
                return R.failed("验证码校验失败");
            }
            //2、校验通过，删除key
            Set keys = redisTemplate.keys(account);
            redisTemplate.delete(keys);
            //3、修改密码
            String password = AESUtil.aesDecrypt(userDTO.getPassword());
            //log.info("解密密码：{}",password);
            boolean flag = userService.updateUserPwdByTelephoneOrEmail(account, MD5Util.getMD5(password));
            if (flag) {
                return R.ok(null, "修改密码成功");
            } else {
                return R.failed("修改密码失败，请联系管理员");
            }
        }catch (Exception e){
            throw new GlobalException("发生错误:" + e.getMessage());
        }
    }

}
