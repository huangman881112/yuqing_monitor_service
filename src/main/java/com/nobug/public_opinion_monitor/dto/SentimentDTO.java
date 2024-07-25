package com.nobug.public_opinion_monitor.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 情感统计DTO
 *
 * @date：2023/2/18
 * @author：nobug
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SentimentDTO {

    @JSONField(serializeUsing = ToStringSerializer.class)
    private long total;
    @JSONField(serializeUsing = ToStringSerializer.class)
    private long positiveCount;
    @JSONField(serializeUsing = ToStringSerializer.class)
    private long neuterCount;
    @JSONField(serializeUsing = ToStringSerializer.class)
    private long negativeCount;

}
