package com.nobug.public_opinion_monitor.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 情感倾向饼图DTO
 *
 * @date：2023/2/19
 * @author：nobug
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataDTO {

    private String name;
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long value;

}
