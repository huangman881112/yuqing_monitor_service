package com.nobug.public_opinion_monitor.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * ES Hot实体类
 *
 * @date：2023/2/16
 * @author：nobug
 */
@Data
@Document(indexName = "hot",createIndex = false)
public class ESHot {

    @Id
    @Field(type = FieldType.Text)
    private String id;
    @Field(analyzer="ik_max_word", searchAnalyzer = "ik_max_word")
    private String title;
    @Field(type = FieldType.Integer)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long hot;
    @Field(type = FieldType.Long)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long created_time;

}
