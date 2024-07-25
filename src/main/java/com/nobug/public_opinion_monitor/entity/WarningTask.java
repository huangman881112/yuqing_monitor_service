package com.nobug.public_opinion_monitor.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 预警任务实体
 *
 * @date：2023/2/20
 * @author：nobug
 */
@Data
@TableName("warning_task")
public class WarningTask {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long userId;
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long taskId;
    private String taskName;
    private String description;
    private String keyword;
    private String ignoreword;
    private String location;
    private Integer sentiment;
    private Integer frequency;
    private Integer hotIndex;
    private Integer autoRun;
    private String emails;
    private int status;
    @TableField(fill = FieldFill.INSERT)
    private Integer deleteFlag;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
