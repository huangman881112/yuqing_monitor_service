package com.nobug.public_opinion_monitor.service;

import com.nobug.public_opinion_monitor.dto.*;
import com.nobug.public_opinion_monitor.entity.ESMblog;
import com.nobug.public_opinion_monitor.entity.StatDay;
import org.elasticsearch.index.query.BoolQueryBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * ESMblogService  servcie层
 *
 * @date：2023/2/16
 * @author：nobug
 */
public interface ESMblogService {

    String key = "statday";

    /**
     * 获取近day日累计评论数
     * @param day
     * @return
     */
    Long getCommentNearly(int day);

    /**
     * 获取近day日累计负面舆情数
     * @param day
     * @return
     */
    Long getNegativeNearly(int day);

    /**
     * 获取近day日累计博文数
     * @param day
     * @return
     */
    Long getArticleNearly(int day);

    /**
     * 获取近day日各省份舆论数
     * @param day
     * @param topicId topicId为null时，统计所有话题
     * @return
     */
    List<DataDTO> getProvinceGroupData(int day, String topicId);

    /**
     * 获取近day日舆情趋势数据,
     * @param day
     * @param topicId topicId为null时，统计所有话题
     * @return
     */
    TrendDTO getPublicOpinionTrend(int day, String topicId);

    /**
     * 统计话题列表中每个话题的正面博文数、中性博文数、负面博文数
     * @param topics 话题列表
     * @return Map,key:String 话题 value:SentimentDTO 统计信息
     */
    Map<String, SentimentDTO> statTopicListInfo(List<String> topics, Long start, Long end);

    /**
     * 根据查询条件分页查询博文列表
     * @param searchDTO 查询条件
     * @return
     */
    ArticleDTO pageSearchArticleList(SearchDTO searchDTO);

    /**
     * 根据话题id查询话题信息及该话题下前6篇文章，按评论数、转发数、点赞数降序
     * @param topicId 话题id
     * @return
     */
    TopicAnalysisDTO getTop6ArticleByTopicId(String topicId);

    /**
     * 根据话题id统计近day日情感倾向性数据
     * @param day
     * @param topicId topicId为null时，统计所有话题
     * @return
     */
    List<DataDTO> getSentimentPieData(int day, String topicId);

    /**
     * 获取 total, positiveTotal, neuterCount, negativeCount
     * @param searchAnalysisDTO
     * @return
     */
    SentimentDTO getSearchStats(SearchAnalysisDTO searchAnalysisDTO);

    /**
     * 根据查询条件获取舆情地图数据
     * @param searchAnalysisDTO
     * @return
     */
    List<DataDTO> getSearchProvinceGroupData(SearchAnalysisDTO searchAnalysisDTO);

    /**
     * 根据查询条件获取关键词命中数据
     * @param searchAnalysisDTO
     * @return
     */
    List<DataDTO> getSearchKeyWordHit(SearchAnalysisDTO searchAnalysisDTO) throws IOException;

    /**
     * 根据查询条件获取舆情走势数据
     * @param searchAnalysisDTO
     * @return
     */
    TrendDTO getSearchPublicOpinionTrend(SearchAnalysisDTO searchAnalysisDTO);

    /**
     * 根据查询条件获取前十篇文章，按评论数、转发数、点赞数降序
     * @param searchAnalysisDTO
     * @return
     */
    List<ESMblog> getTop10Articles(SearchAnalysisDTO searchAnalysisDTO);

    /**
     * 根据指定分词器对text分词，返回分词列表
     * @param tokenizer 指定分词器
     * @param text
     * @return
     * @throws IOException
     */
    List<String> getBreakUpText(String tokenizer, String text) throws IOException;

    /**
     * 根据查询条件，获取任务监测舆情数量
     * @param searchAnalysisDTO
     * @return
     */
    Long getTaskMonitorNum(SearchAnalysisDTO searchAnalysisDTO);

    /**
     * 统计每日的总舆情、正面舆情、中性舆情、负面舆情、总评论数
     * @return
     */
    StatDay getStatDay();

}
