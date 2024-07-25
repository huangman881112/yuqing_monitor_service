package com.nobug.public_opinion_monitor.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.nobug.public_opinion_monitor.entity.ESHot;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Hot索引扩展
 *
 * @date：2023/2/18
 * @author：nobug
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotDTO{

    private String id;
    private String title;
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long hot;
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long positive;
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long neutral;
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long negative;
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long total;
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long created_time;


}
