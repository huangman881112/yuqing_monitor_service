package com.nobug.public_opinion_monitor.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * stat_day实体类
 *
 * @date：2023/3/11
 * @author：nobug
 */
@Data
@TableName("stat_day")
public class StatDay {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    //解决将Long转String传给前端，解决Long型精度丢失的问题
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long total;
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long positiveCount;
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long neuterCount;
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long negativeCount;
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long commentCount;
    private Date createTime;

}
