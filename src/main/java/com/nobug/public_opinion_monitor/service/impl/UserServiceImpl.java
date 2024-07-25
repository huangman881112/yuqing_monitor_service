package com.nobug.public_opinion_monitor.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nobug.public_opinion_monitor.common.GlobalException;
import com.nobug.public_opinion_monitor.dao.UserDao;
import com.nobug.public_opinion_monitor.entity.User;
import com.nobug.public_opinion_monitor.service.UserService;
import com.nobug.public_opinion_monitor.utils.AESUtil;
import com.nobug.public_opinion_monitor.utils.MD5Util;
import com.nobug.public_opinion_monitor.utils.SnowFlake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * UserService实现类
 *
 * @date：2023/2/8
 * @author：nobug
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserDao, User> implements UserService {

    @Autowired
    private UserDao userDao;

    private SnowFlake snowFlake = new SnowFlake();

    @Override
    public User selectUserByTelephoneOrEmail(String account) {
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getTelephone, account)
                .or()
                .eq(User::getEmail, account);
        return userDao.selectOne(lambdaQueryWrapper);
    }

    @Override
    public void updateUserLoginCountByPhoneOrEmail(Map<String, Object> map) {
        LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(User::getTelephone, map.get("account"))
                .or()
                .eq(User::getEmail, map.get("account"))
                .set(User::getEndLoginTime,map.get("end_login_time"))
                .set(User::getLoginCount,map.get("login_count"))
                .set(User::getIsOnline,1);
        userDao.update(null,lambdaUpdateWrapper);
    }

    @Override
    public User getUserById(Long userId) {
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getUserId, userId);
        return userDao.selectOne(lambdaQueryWrapper);
    }

    @Override
    public boolean updateUserPwdByTelephoneOrEmail(String account, String password) {
        boolean ret = true;
        try{
            LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(User::getTelephone, account)
                    .or()
                    .eq(User::getEmail, account)
                    .set(User::getPassword, password);
            userDao.update(null, lambdaUpdateWrapper);
        }catch (Exception e){
            ret = false;
            throw new GlobalException(e.getMessage());
        }
        return ret;
    }

    @Override
    public void editIs_change_pas(Long userId) {

    }

    @Override
    public Boolean saveUser(User user) {
        boolean flag = false;
        try {
            long userId = snowFlake.getId();
            user.setId(null);
            user.setUserId(userId);
            String password = AESUtil.aesDecrypt(user.getPassword());
            user.setPassword(MD5Util.getMD5(password));
            user.setStatus(1);
            user.setLoginCount(0);
            user.setIsOnline(0);
            user.setIdentity(1);
            userDao.insert(user);
            flag = true;
        } catch (Exception e) {
            throw new GlobalException("前端入参密码AES解密失败");
        } finally {
            return flag;
        }
    }

    @Override
    public Map<String, String> getqrcode(String telephone) {
        return null;
    }

    @Override
    public User selectUserByopenid(String openid) {
        return null;
    }

    @Override
    public int addapply(String openid, String name, String telephone, String industry, String company) {
        return 0;
    }

    @Override
    public Map<String, Object> selectUserApplyByopenid(String openid) {
        return null;
    }

    @Override
    public List<Map<String, Object>> getUserByorganizationid(Integer id) {
        return null;
    }

    @Override
    public Map<String, Object> getUserInfoById(Long userId) {
        return null;
    }

    @Override
    public void setAlloffline() {

    }

    @Override
    public List<Map<String, Object>> getAllcommentator(Long userId) {
        return null;
    }

    @Override
    public Map<String, Object> onlinestatistical(Long userId) {
        return userDao.onlinestatistical(userId);
    }

    @Override
    public void updateLoginFailCountAndTime(Map<String, Object> mapParam) {

    }

    @Override
    public void updateIsOnline(Long userId) {
        LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(User::getUserId,userId).set(User::getIsOnline, 0);
        userDao.update(null,lambdaUpdateWrapper);
    }
}
