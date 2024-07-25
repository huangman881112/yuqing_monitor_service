package com.nobug.public_opinion_monitor.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 用户实体
 *
 * @date：2023/2/8
 * @author：nobug
 */
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;
    //解决将Long转String传给前端，解决Long型精度丢失的问题
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long userId;
    private String telephone;
    private String password;
    private String email;
    private String endLoginTime;
    private Integer status;
    private String username;
    private String wechatNumber;
    private String openid;
    private Integer loginCount;
    private Integer identity;
    private String organizationId;
    private Integer isOnline;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
