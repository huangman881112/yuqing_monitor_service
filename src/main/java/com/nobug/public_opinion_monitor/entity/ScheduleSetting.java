package com.nobug.public_opinion_monitor.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * ScheduleSetting实体类
 *
 * @date：2023/2/24
 * @author：nobug
 */
@Accessors(chain = true)
@Data
@TableName("schedule_setting")
public class ScheduleSetting {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long taskId;

    private String beanName;

    private String methodName;

    private String methodParams;

    private String name;

    private String cron;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
