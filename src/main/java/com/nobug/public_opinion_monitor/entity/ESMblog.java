package com.nobug.public_opinion_monitor.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * ES Mblog实体类
 *
 * @date：2023/2/16
 * @author：nobug
 */
@Data
@Document(indexName = "mblog",createIndex = false)
public class ESMblog {

    @Id
    @Field(type = FieldType.Text)
    private String id;
    @Field(type = FieldType.Keyword)
    private String topic;
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_max_word")
    private String text;
    @Field(type = FieldType.Long)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long created_time;
    @Field(type = FieldType.Keyword)
    private String created_time_text;
    @Field(type = FieldType.Keyword)
    private String author;
    @Field(type = FieldType.Integer)
    private Integer comments_count;
    @Field(type = FieldType.Integer)
    private Integer reposts_count;
    @Field(type = FieldType.Integer)
    private Integer attitudes_count;
    @Field(type = FieldType.Keyword)
    private String source;
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_max_word")
    private String location;
    @Field(type = FieldType.Keyword)
    private String province;
    @Field(type = FieldType.Keyword)
    private String link;
    @Field(type = FieldType.Integer)
    private String sentiment;

}
