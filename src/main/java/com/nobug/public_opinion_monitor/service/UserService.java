package com.nobug.public_opinion_monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nobug.public_opinion_monitor.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 用户servcie层
 *
 * @date：2023/2/8
 * @author：nobug
 */
public interface UserService extends IService<User> {
    /**
     * 根据手机号或邮箱查用户是否存在
     * @param account
     * @return
     */
    User selectUserByTelephoneOrEmail(String account);

    /**
     * 根据手机号或邮箱更新用户登录次数
     * @param map
     * @return
     */
    void updateUserLoginCountByPhoneOrEmail(Map<String,Object> map);

    /**
     * 查询用户信息
     * @param userId
     * @return
     */
    User getUserById(Long userId);

    /**
     * 修改密码
     * @param account
     * @param password
     * @return
     */
    boolean updateUserPwdByTelephoneOrEmail(String account, String password);

    /**
     *
     * @param userId
     */
    void editIs_change_pas(Long userId);

    /**
     * 新增用户
     * @param user
     * @return
     */
    Boolean saveUser(User user);

    /**
     *
     * @param telephone
     * @return
     */
    Map<String, String> getqrcode(String telephone);

    /**
     *
     * @param openid
     * @return
     */
    User  selectUserByopenid(String openid);

    /**
     * 申请试用
     * @param openid
     * @param name
     * @param telephone
     * @param industry
     * @param company
     * @return
     */
    int addapply(String openid, String name, String telephone, String industry, String company);

    /**
     * 判断当前用户是否已经申请过
     * @param openid
     * @return
     */
    Map<String, Object> selectUserApplyByopenid(String openid);

    /**
     * 根据所属机构id查询用户
     * @param id
     * @return
     */
    List<Map<String, Object>> getUserByorganizationid(Integer id);

    /**
     *
     * @param userId
     * @return
     */
    Map<String, Object> getUserInfoById(Long userId);

    /**
     *
     */
    void setAlloffline();

    /**
     *
     * @param userId
     * @return
     */
    List<Map<String, Object>> getAllcommentator(Long userId);

    /**
     * 统计在线信息:当前用户的最后登录时间、登录次数、当前在线用户总数
     * @param userId
     * @return
     */
    Map<String, Object> onlinestatistical(Long userId);

    /**
     *
     * @param mapParam
     */
    void updateLoginFailCountAndTime(Map<String, Object> mapParam);

    /**
     * 更新在线状态
     * @param userId
     */
    void updateIsOnline(Long userId);
}
