package com.nobug.public_opinion_monitor.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.nobug.public_opinion_monitor.dto.*;
import com.nobug.public_opinion_monitor.entity.ESHot;
import com.nobug.public_opinion_monitor.service.ESHotService;


import com.nobug.public_opinion_monitor.service.ESMblogService;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.Cardinality;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ESHotService实现类
 *
 * @date：2023/2/16
 * @author：nobug
 */
@Service
public class ESHotServiceImpl implements ESHotService {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    private ESMblogService esMblogService;

    @Override
    public ESHot getMaxHot() {
        Date date = new Date();
        Long now = date.getTime();
        long lastday = DateUtil.offsetDay(date, -1).getTime();
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withSort(SortBuilders.fieldSort("hot").order(SortOrder.DESC))
                .build();
        SearchHit<ESHot> searchHit = elasticsearchRestTemplate.searchOne(nativeSearchQuery, ESHot.class);
        if(searchHit!=null)
            return searchHit.getContent();
        return null;
    }

    @Override
    public ESHot getById(String id) {
        IdsQueryBuilder idsQueryBuilder = new IdsQueryBuilder();
        idsQueryBuilder.addIds(id);
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(idsQueryBuilder).build();
        ESHot esHot = elasticsearchRestTemplate.searchOne(nativeSearchQuery, ESHot.class).getContent();
        return esHot;
    }

    @Override
    public ESHot getRecently() {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withSort(SortBuilders.fieldSort("created_time").order(SortOrder.DESC))
                .build();
        ESHot esHot = elasticsearchRestTemplate.searchOne(nativeSearchQuery, ESHot.class).getContent();
        return esHot;
    }

    @Override
    public TopicDTO pageSearchTopicList(SearchDTO searchDTO) {
        //1、获取参数
        String hotWord = searchDTO.getHotWord();
        String startDate = searchDTO.getStartDate();
        String endDate = searchDTO.getEndDate();
        Integer pageNo = searchDTO.getPageNo();
        Integer pageSize = searchDTO.getPageSize();
        //2、构造查询
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        if(!StringUtils.isEmpty(hotWord)){
            queryBuilder.must(QueryBuilders.queryStringQuery(hotWord).field("title"));
        }
        Long start = null, end = null;
        if(!StringUtils.isEmpty(startDate) && !StringUtils.isEmpty(endDate)){
            start = DateUtil.parse(startDate).getTime();
            end = DateUtil.parse(endDate).getTime();
            queryBuilder.filter(QueryBuilders.rangeQuery("created_time").from(start).to(end));
        }
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withSort(SortBuilders.fieldSort("hot").order(SortOrder.DESC))      //热度降序
                .withPageable(PageRequest.of(pageNo-1, pageSize))       //分页
                .build();
        SearchHits<ESHot> search = elasticsearchRestTemplate.search(nativeSearchQuery, ESHot.class);
        //3、获取话题列表
        List<String> topics = search.stream().map(esHotSearchHit ->
                esHotSearchHit.getContent().getTitle()
        ).collect(Collectors.toList());
        //校验topics，size为0直接返回
        if(topics.size()==0){
            return new TopicDTO(new ArrayList<>(), 0 , pageSize, pageNo);
        }
        //4、获取各个话题的统计信息
        Map<String, SentimentDTO> map = esMblogService.statTopicListInfo(topics, start, end);
        //5、统计去重数量
        NativeSearchQuery nativeSearchQuery1 = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .build();
        long totalTopic = elasticsearchRestTemplate.count(nativeSearchQuery1, ESHot.class);
        //6、构造返回值
        List<HotDTO> topicList = search.stream().map(esHotSearchHit -> {
            HotDTO hotDTO = new HotDTO();
            hotDTO.setId(esHotSearchHit.getId());
            hotDTO.setHot(esHotSearchHit.getContent().getHot());
            hotDTO.setCreated_time(esHotSearchHit.getContent().getCreated_time());
            String title = esHotSearchHit.getContent().getTitle();
            hotDTO.setTitle(title);
            SentimentDTO sentimentDTO = map.get(title);
            if(sentimentDTO == null)
                sentimentDTO = new SentimentDTO();
            hotDTO.setPositive(sentimentDTO.getPositiveCount());
            hotDTO.setNeutral(sentimentDTO.getNeuterCount());
            hotDTO.setNegative(sentimentDTO.getNegativeCount());
            hotDTO.setTotal(sentimentDTO.getPositiveCount()+sentimentDTO.getNeuterCount()+sentimentDTO.getNegativeCount());
            return hotDTO;
        }).collect(Collectors.toList());
        TopicDTO res = new TopicDTO();
        res.setPageNo(pageNo);
        res.setPageSize(pageSize);
        res.setTotal((int) totalTopic);
        res.setTopicList(topicList);
        return res;
    }

    @Override
    public Long getTopicNearly(int day) {
        Date date = new Date();
        Long now = date.getTime();
        DateTime today = DateUtil.parse(DateUtil.formatDate(date));
        long lastday = DateUtil.offsetDay(today, -day + 1).getTime();
        QueryBuilder queryBuilder = QueryBuilders.rangeQuery("created_time").from(lastday).to(now);
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .build();
        long count = elasticsearchRestTemplate.count(nativeSearchQuery, ESHot.class);
        return count;
    }

    @Override
    public List<DataDTO> getWordCloudData(int wordNum) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withSort(SortBuilders.fieldSort("created_time").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, wordNum))
                .build();
        SearchHits<ESHot> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery, ESHot.class);
        List<DataDTO> hots = searchHits.stream().map(searchHit -> {
            DataDTO wordCloudDTO = new DataDTO();
            wordCloudDTO.setName(searchHit.getContent().getTitle());
            wordCloudDTO.setValue(searchHit.getContent().getHot());
            return wordCloudDTO;
        }).collect(Collectors.toList());
        return hots;
    }

}
