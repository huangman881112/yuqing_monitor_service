package com.nobug.public_opinion_monitor.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.nobug.public_opinion_monitor.entity.ESMblog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 话题分析DTO
 *
 * @date：2023/2/19
 * @author：nobug
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicAnalysisDTO {

    private String id;
    private String title;
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long hot;
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long created_time;
    private List<ESMblog> articles;

}
