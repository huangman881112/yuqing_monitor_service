package com.nobug.public_opinion_monitor.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nobug.public_opinion_monitor.entity.User;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 用户Dao层
 *
 * @date：2023/2/8
 * @author：nobug
 */
@Mapper
public interface UserDao extends BaseMapper<User> {

//    /**
//     * 根据手机号查用户是否存在
//     * @param telephone
//     * @return
//     */
//    User selectUserByTelephone(@Param("telephone") String telephone);
//
//    /**
//     * 根据手机号更新用户登录次数
//     * @param map
//     * @return
//     */
//    Integer updateUserLoginCountByPhone(@Param("map") Map<String, Object> map);
//
//    /**
//     * 查询用户信息
//     * @param user_id
//     * @return
//     */
//    Map<String, String> getUserById(@Param("user_id")Long user_id);
//
//    /**
//     * 修改密码
//     * @param user_id
//     * @param password
//     * @return
//     */
//    boolean updateUserPwdById(@Param("user_id")Long user_id,@Param("password")String password);
//
//    /**
//     * 更新用户openid
//     * @param user_id
//     * @param openid
//     * @return
//     */
//    boolean updateUseropenidById(@Param("user_id")Long user_id,@Param("openid")String openid);
//
//    /**
//     * 新增用户
//     * @param user
//     * @return
//     */
//    Boolean saveUser(User user);
//
//    /**
//     * 更新用户openid状态
//     * @param openid
//     * @return
//     */
//    boolean updateUseropenidstatusById(@Param("openid")String openid);
//
//    /**
//     * 获取所有用户
//     * @return
//     */
//    List<User> getAllUser();
//
//    /**
//     *
//     * @param telephone
//     * @param ticket
//     * @return
//     */
//    boolean addticket(@Param("telephone")String telephone, @Param("ticket")String ticket);
//
//    /**
//     *
//     * @param telephone
//     * @return
//     */
//    Map<String, String> getqrcode(@Param("telephone")String telephone);
//
//    /**
//     *
//     * @param openid
//     * @return
//     */
//    User selectUserByopenid(@Param("openid")String openid);
//
//    /**
//     * 申请试用
//     * @param openid
//     * @param name
//     * @param telephone
//     * @param industry
//     * @param company
//     * @return
//     */
//    int addapply(@Param("openid")String openid, @Param("name")String name, @Param("telephone")String telephone,
//                 @Param("industry")String industry,
//                 @Param("company")String company);
//
//    /**
//     * 判断当前用户是否已经申请过
//     * @param openid
//     * @return
//     */
//    Map<String, Object> selectUserApplyByopenid(@Param("openid")String openid);
//
//    /**
//     * 根据所属机构id查询用户
//     * @param id
//     * @return
//     */
//    List<Map<String, Object>> getUserByorganizationid(@Param("id")Integer id);
//
//    /**
//     *
//     * @param user_id
//     * @return
//     */
//    Map<String, Object> getUserInfoById(@Param("user_id")Long user_id);
//
//    /**
//     *
//     * @param user
//     */
//    void updateOnline(User user);
//
//    /**
//     *
//     */
//    void setAlloffline();
//
//    /**
//     *
//     * @param user_id
//     * @return
//     */
//    List<Map<String, Object>> getAllcommentator(@Param("user_id")Long user_id);
//
//    /**
//     *
//     * @return
//     */
//    List<User> getAllUserNotDelete();

    /**
     *
     * @param userId
     * @return
     */
    @MapKey("userId")
    Map<String, Object> onlinestatistical(@Param("userId")Long userId);

//    /**
//     *
//     * @param mapParam
//     */
//    void updateLoginFailCountAndTime(Map<String, Object> mapParam);
//
//    /**
//     *
//     * @param userId
//     */
//    void editIs_change_pas(Long userId);
//
//    /**
//     * 更新最后登录时间
//     * @param userId
//     */
//    void updateEndLoginTime(Long userId);
}
