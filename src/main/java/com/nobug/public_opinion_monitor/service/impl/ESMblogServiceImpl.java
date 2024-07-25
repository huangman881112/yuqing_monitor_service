package com.nobug.public_opinion_monitor.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.nobug.public_opinion_monitor.common.GlobalException;
import com.nobug.public_opinion_monitor.dto.*;
import com.nobug.public_opinion_monitor.entity.ESHot;
import com.nobug.public_opinion_monitor.entity.ESMblog;
import com.nobug.public_opinion_monitor.entity.StatDay;
import com.nobug.public_opinion_monitor.service.ESHotService;
import com.nobug.public_opinion_monitor.service.ESMblogService;
import com.nobug.public_opinion_monitor.service.StatDayService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.AnalyzeRequest;
import org.elasticsearch.client.indices.AnalyzeResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.index.query.*;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchModule;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.aggregations.metrics.SumAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilterBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * ESMblogService实现类
 *
 * @date：2023/2/16
 * @author：nobug
 */
@Service
@Slf4j
public class ESMblogServiceImpl implements ESMblogService {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private ESHotService esHotService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StatDayService statDayService;

    public String getKey() {
        return key;
    }


    /**
     * topics中每个话题的情感分类博文数
     * @param topics 话题列表
     * @param start 起始时间
     * @param end 终止时间
     * @return
     */
    public Map<String, SentimentDTO> statTopicListInfo(List<String> topics, Long start, Long end){
        Map<String, SentimentDTO> map = new HashMap<>();
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        if(start!=null && end!=null){
            queryBuilder.must(QueryBuilders.rangeQuery("created_time").from(start).to(end));
        }
        queryBuilder.filter(QueryBuilders.termsQuery("topic", topics));
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .addAggregation(AggregationBuilders.terms("topic_sentiment_group")
                        .script(new Script("doc['topic'].value+'@@'+doc['sentiment'].value")).size(topics.size()*3))
                .build();
        SearchHits<ESMblog> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery, ESMblog.class);
        Terms terms = searchHits.getAggregations().get("topic_sentiment_group");
        terms.getBuckets().stream().forEach(bucket -> {
            String[] str = bucket.getKeyAsString().split("@@");
            long docCount = bucket.getDocCount();
            String key = str[0];
            int sentiment = Integer.valueOf(str[1]);
            SentimentDTO sentimentDTO = map.get(key);
            if(sentimentDTO == null)
                sentimentDTO = new SentimentDTO();
            if(sentiment==1){
                sentimentDTO.setPositiveCount(docCount);
            }else if(sentiment==0){
                sentimentDTO.setNeuterCount(docCount);
            }else{
                sentimentDTO.setNegativeCount(docCount);
            }
            map.put(key, sentimentDTO);
        });
//        log.info(map.toString());
        return map;
    }

    @Override
    public TopicAnalysisDTO getTop6ArticleByTopicId(String topicId) {
        //1、创建返回对象
        TopicAnalysisDTO res = new TopicAnalysisDTO();
        //2、根据话题id查询话题信息
        ESHot esHot = esHotService.getById(topicId);
        String topic = esHot.getTitle();
        //3、根据话题title查询top6博文
        QueryBuilder queryBuilder = QueryBuilders.termQuery("topic", topic);
        NativeSearchQuery nativeSearchQuery1 = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withSort(SortBuilders.fieldSort("comments_count").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("reposts_count").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("attitudes_count").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 6))
                .build();
        SearchHits<ESMblog> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery1, ESMblog.class);
        List<ESMblog> esMblogs = searchHits.stream().map(searchHit -> searchHit.getContent()).collect(Collectors.toList());
        //4、构造返回对象
        res.setArticles(esMblogs);
        res.setTitle(topic);
        res.setCreated_time(esHot.getCreated_time());
        res.setHot(esHot.getHot());
        res.setId(esHot.getId());
        return res;
    }

    @Override
    public List<DataDTO> getSentimentPieData(int day, String topicId) {
        //1、创建返回对象
        List<DataDTO> res = new ArrayList<>();
        res.add(new DataDTO("负面", 0l));
        res.add(new DataDTO("中性", 0l));
        res.add(new DataDTO("正面", 0l));
        Date date = new Date();
        Long now = date.getTime();
        DateTime today = DateUtil.parse(DateUtil.formatDate(date));
        long lastday = DateUtil.offsetDay(today, -day + 1).getTime();
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.filter(QueryBuilders.rangeQuery("created_time").from(lastday).to(now));
        //如果topicId不空，统计给定话题
        if(!StringUtils.isEmpty(topicId)){
            ESHot esHot = esHotService.getById(topicId);
            String topic = esHot.getTitle();
            queryBuilder.must(QueryBuilders.termQuery("topic", topic));
        }
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .addAggregation(AggregationBuilders.terms("sentiment_group").field("sentiment").size(3))
                .build();
        SearchHits<ESMblog> search = elasticsearchRestTemplate.search(nativeSearchQuery, ESMblog.class);
        Terms terms = search.getAggregations().get("sentiment_group");
        terms.getBuckets().forEach(bucket -> {
            Integer key = Integer.valueOf(bucket.getKeyAsString());
            long docCount = bucket.getDocCount();
            DataDTO pieDataDTO = res.get(key + 1);
            pieDataDTO.setValue(docCount);
        });
        return res;
    }

    @Override
    public SentimentDTO getSearchStats(SearchAnalysisDTO searchAnalysisDTO) {
        //1、根据SearchAnalysisDTO获取QueryBuilder
        QueryBuilder boolQueryBuilder = getSearchAnalysisQueryBuilder(searchAnalysisDTO);
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .addAggregation(AggregationBuilders.terms("sentiment_group").field("sentiment").size(3))
                .build();
        //2、查询并解析
        SentimentDTO res = new SentimentDTO();
        SearchHits<ESMblog> search = elasticsearchRestTemplate.search(nativeSearchQuery, ESMblog.class);
        Terms terms = search.getAggregations().get("sentiment_group");
        terms.getBuckets().forEach(bucket -> {
            Integer key = Integer.valueOf(bucket.getKeyAsString());
            long docCount = bucket.getDocCount();
            if(key==1){
                res.setPositiveCount(docCount);
            }else if(key==0){
                res.setNeuterCount(docCount);
            }else{
                res.setNegativeCount(docCount);
            }
            res.setTotal(res.getTotal()+docCount);
        });
        return res;

    }

    @Override
    public List<DataDTO> getSearchProvinceGroupData(SearchAnalysisDTO searchAnalysisDTO) {
        //1、根据SearchAnalysisDTO获取QueryBuilder
        QueryBuilder queryBuilder = getSearchAnalysisQueryBuilder(searchAnalysisDTO);
        //2、根据queryBuilder构建请求、查询并返回结果
        List<DataDTO> res = getProvinceGroupDataByQuery(queryBuilder);
        return res;
    }

    @Override
    public List<DataDTO> getSearchKeyWordHit(SearchAnalysisDTO searchAnalysisDTO) throws IOException {
        //1 获取参数
        String searchWord = searchAnalysisDTO.getSearchWord();
        //2 根据SearchAnalysisDTO获取QueryBuilder
        BoolQueryBuilder boolQueryBuilder = getSearchAnalysisQueryBuilder(searchAnalysisDTO);
        List<String> wordList = null;
        List<DataDTO> res = new ArrayList<>();
        if(StringUtils.isEmpty(searchWord)){
            //3.1 任务分析
            String hotWord = searchAnalysisDTO.getHotWord();
            //3.1.1 构建关键词列表
            String[] split;
            if(hotWord.contains("|") && hotWord.contains("&")){
                throw new GlobalException("暂不支持同时使用’|‘、’&‘");
            }else if(hotWord.contains("|")){
                split = hotWord.split("\\|");
            }else{
                split = hotWord.split("&");
            }
            wordList = new ArrayList<>();
            Collections.addAll(wordList, split);
            //3.1.2 统计数量
            for(String word: wordList){
                BoolQueryBuilder temp = (BoolQueryBuilder) copyQueryBuilder(boolQueryBuilder);
                temp.filter(QueryBuilders.matchPhraseQuery("text", word));
                NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                        .withQuery(temp)
                        .build();
                long hitCount = elasticsearchRestTemplate.count(nativeSearchQuery, ESMblog.class);
                res.add(new DataDTO(word, hitCount));
            }
        }else{
            //3.2 搜索分析
            //3.2.1 获取分词列表
            wordList = getBreakUpText("ik_max_word", searchWord);
            //3.2.2 统计各个分词的数量
            for(String word: wordList){
                BoolQueryBuilder temp = new BoolQueryBuilder();
                BeanUtils.copyProperties(boolQueryBuilder, temp);
                temp.should(QueryBuilders.matchPhraseQuery("location", word))
                        .should(QueryBuilders.matchPhraseQuery("text", word));
                NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                        .withQuery(temp)
                        .build();
                long hitCount = elasticsearchRestTemplate.count(nativeSearchQuery, ESMblog.class);
                res.add(new DataDTO(word, hitCount));
            }
        }
        //4 返回数量最大的五个
        res.sort((o1, o2) -> (int) (o1.getValue()-o2.getValue()));
        if(res.size()>5){
            res = res.subList(res.size()-5,res.size());
        }
        return res;
    }

    @Override
    public TrendDTO getSearchPublicOpinionTrend(SearchAnalysisDTO searchAnalysisDTO) {
        //1、根据SearchAnalysisDTO获取QueryBuilder
        QueryBuilder queryBuilder = getSearchAnalysisQueryBuilder(searchAnalysisDTO);
        int day = searchAnalysisDTO.getDay();
        Date date = new Date();
        //2、根据queryBuilder获取舆情走势数据
        return getPublicOpinionTrendByQuery(queryBuilder, date, day);
    }

    @Override
    public List<ESMblog> getTop10Articles(SearchAnalysisDTO searchAnalysisDTO) {
        //1、根据SearchAnalysisDTO获取QueryBuilder
        QueryBuilder queryBuilder = getSearchAnalysisQueryBuilder(searchAnalysisDTO);
        //2、设置高亮
        HighlightBuilder.Field textField = new HighlightBuilder.Field("text")
                .preTags("<span class=\"highlight\">")
                .postTags("</span>");
        HighlightBuilder.Field locationField = new HighlightBuilder.Field("location")
                .preTags("<span class=\"highlight\">")
                .postTags("</span>");
        //3、根据queryBuilder构造查询，解析并返回
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
//                .withSort(SortBuilders.fieldSort("comments_count").order(SortOrder.DESC))
//                .withSort(SortBuilders.fieldSort("reposts_count").order(SortOrder.DESC))
//                .withSort(SortBuilders.fieldSort("attitudes_count").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(textField, locationField)
                .build();
        SearchHits<ESMblog> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery, ESMblog.class);
        List<ESMblog> esMblogs = searchHits.stream().map(searchHit ->{
            ESMblog content = searchHit.getContent();
            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
            highlightFields.entrySet().forEach(highlightFieldsEntry->{
                String key = highlightFieldsEntry.getKey();
                if("text".equals(key)){
                    StringBuilder sb = new StringBuilder();
                    highlightFieldsEntry.getValue().forEach(fragment ->{
                        sb.append(fragment);
                    });
                    content.setText(sb.toString());
                }
                if("location".equals(key)){
                    StringBuilder sb = new StringBuilder();
                    highlightFieldsEntry.getValue().forEach(fragment ->{
                        sb.append(fragment);
                    });
                    content.setLocation(sb.toString());
                }
            });
            return content;
        }).collect(Collectors.toList());
        return esMblogs;
    }

    @Override
    public List<String> getBreakUpText(String tokenizer, String text) throws IOException {
        RestHighLevelClient restHighLevelClient = elasticsearchRestTemplate.execute(client -> client);
        AnalyzeRequest analyzeRequest = AnalyzeRequest.buildCustomAnalyzer(tokenizer).build(text);
        AnalyzeResponse response = restHighLevelClient.indices().analyze(analyzeRequest, RequestOptions.DEFAULT);
        List<String> res = response.getTokens().stream().map(analyzeToken -> analyzeToken.getTerm()).collect(Collectors.toList());
        return res;
    }

    @Override
    public Long getTaskMonitorNum(SearchAnalysisDTO searchAnalysisDTO) {
        //1、根据SearchAnalysisDTO获取QueryBuilder
        QueryBuilder queryBuilder = getSearchAnalysisQueryBuilder(searchAnalysisDTO);
        //2、去重统计数量
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .build();

        return elasticsearchRestTemplate.count(nativeSearchQuery, ESMblog.class);
    }

    @Override
    public StatDay getStatDay() {
        //1、构造查询
        //今日零点
        Date date = new Date();
        DateTime begin = DateUtil.beginOfDay(date);
        long start = begin.getTime();
        //今日结束
        DateTime end1 = DateUtil.endOfDay(date);
        long end = end1.getTime();
        QueryBuilder queryBuilder = QueryBuilders.rangeQuery("created_time").from(start).to(end);
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .addAggregation(AggregationBuilders.terms("sentiment_group").field("sentiment").size(3))
                .build();
        //2、查询并解析情感倾向性
        StatDay res = new StatDay();
        SearchHits<ESMblog> search = elasticsearchRestTemplate.search(nativeSearchQuery, ESMblog.class);
        Terms terms = search.getAggregations().get("sentiment_group");
        terms.getBuckets().forEach(bucket -> {
            Integer key = Integer.valueOf(bucket.getKeyAsString());
            long docCount = bucket.getDocCount();
            if(key==1){
                res.setPositiveCount(docCount);
            }else if(key==0){
                res.setNeuterCount(docCount);
            }else{
                res.setNegativeCount(docCount);
            }
            long total = res.getTotal()==null? 0 : res.getTotal();
            res.setTotal(total+docCount);
        });
        //2、查询并解析总评论数
        SumAggregationBuilder sumAggregationBuilder = AggregationBuilders.sum("sum_comments").field("comments_count");
        NativeSearchQuery nativeSearchQuery1 = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .addAggregation(sumAggregationBuilder)
                //不需要获取source结果集
                .withSourceFilter(new FetchSourceFilterBuilder().build())
                .build();
        SearchHits<ESMblog> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery1, ESMblog.class);
        Sum sum_comments = searchHits.getAggregations().get("sum_comments");
        res.setCommentCount(Math.round(sum_comments.getValue()));
        res.setCreateTime(new Date(end));
        return res;
    }

    @Override
    public ArticleDTO pageSearchArticleList(SearchDTO searchDTO) {
        //1、获取参数
        String hotWord = searchDTO.getHotWord();
        String location = searchDTO.getLocation();
        String startDate = searchDTO.getStartDate();
        String endDate = searchDTO.getEndDate();
        Integer pageNo = searchDTO.getPageNo();
        Integer pageSize = searchDTO.getPageSize();
        Integer sentiment = searchDTO.getSentiment();
        //2、构造查询
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        if(!StringUtils.isEmpty(hotWord)){
            queryBuilder.must(QueryBuilders.queryStringQuery(hotWord).field("text"));
        }
        if(!StringUtils.isEmpty(location)){
            queryBuilder.must(QueryBuilders.queryStringQuery(location).field("location"));
        }
        Long start = null, end = null;
        if(!StringUtils.isEmpty(startDate) && !StringUtils.isEmpty(endDate)){
            start = DateUtil.parse(startDate).getTime();
            end = DateUtil.parse(endDate).getTime();
            queryBuilder.filter(QueryBuilders.rangeQuery("created_time").from(start).to(end));
        }
        if(sentiment!=null){
            queryBuilder.filter(QueryBuilders.termQuery("sentiment", sentiment));
        }
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withSort(SortBuilders.fieldSort("comments_count").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("reposts_count").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("attitudes_count").order(SortOrder.DESC))
                .withPageable(PageRequest.of(pageNo-1, pageSize))       //分页
                .build();
        //3、查询并解析
        SearchHits<ESMblog> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery, ESMblog.class);
        List<ESMblog> mblogList = searchHits.stream().map(searchHit -> {
            ESMblog esMblog = searchHit.getContent();
            if(StringUtils.isEmpty(esMblog.getTopic())){
                esMblog.setTopic("暂无话题");
            }
            if(StringUtils.isEmpty(esMblog.getSource())){
                esMblog.setSource("未知来源");
            }
            return esMblog;
        }).collect(Collectors.toList());
        //4、统计去重数量
        NativeSearchQuery nativeSearchQuery1 = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .build();
        long totalArticle = elasticsearchRestTemplate.count(nativeSearchQuery1, ESMblog.class);
        //5、构造返回
        ArticleDTO res = new ArticleDTO();
        res.setArticleList(mblogList);
        res.setPageNo(pageNo);
        res.setPageSize(pageSize);
        res.setTotal((int) totalArticle);
        return res;
    }

    @Override
    public List<DataDTO> getProvinceGroupData(int day, String topicId) {
        Date date = new Date();
        Long now = date.getTime();
        DateTime today = DateUtil.parse(DateUtil.formatDate(date));
        long lastday = DateUtil.offsetDay(today, -day + 1).getTime();
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.filter(QueryBuilders.rangeQuery("created_time").from(lastday).to(now));
        //如果topicId不空，统计给定话题
        if(!StringUtils.isEmpty(topicId)){
            ESHot esHot = esHotService.getById(topicId);
            String topic = esHot.getTitle();
            queryBuilder.must(QueryBuilders.termQuery("topic", topic));
        }
        return getProvinceGroupDataByQuery(queryBuilder);
    }

    @Override
    public Long getNegativeNearly(int day) {
        //1、设置redis
        List<StatDay> statDayList = setRedisStatDay();
        List<Long> negative = statDayList.stream().limit(day - 1).map(statDay -> statDay.getNegativeCount()).collect(Collectors.toList());
        Long lastCount = CollectionUtil.isEmpty(negative)? 0l : negative.stream().reduce(Long::sum).get();
        Date date = new Date();
        Long now = date.getTime();
        //今日零点
        DateTime begin = DateUtil.beginOfDay(date);
        long today = begin.getTime();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder
                .filter(QueryBuilders.rangeQuery("created_time").from(today).to(now))
                .must(QueryBuilders.termQuery("sentiment",-1));
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .build();

        long todayCount = elasticsearchRestTemplate.count(nativeSearchQuery, ESMblog.class);
        return lastCount+todayCount;
    }

    @Override
    public Long getArticleNearly(int day) {
        //1、设置redis
        List<StatDay> statDayList = setRedisStatDay();
        List<Long> article = statDayList.stream().limit(day - 1).map(statDay -> statDay.getTotal()).collect(Collectors.toList());
        Long lastCount = CollectionUtil.isEmpty(article)? 0l : article.stream().reduce(Long::sum).get();
        Date date = new Date();
        Long now = date.getTime();
        //今日零点
        DateTime begin = DateUtil.beginOfDay(date);
        long today = begin.getTime();
        QueryBuilder queryBuilder = QueryBuilders.rangeQuery("created_time").from(today).to(now);
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .build();

        long todayCount = elasticsearchRestTemplate.count(nativeSearchQuery, ESMblog.class);
        return lastCount+todayCount;
    }

    @Override
    public Long getCommentNearly(int day) {
        //1、设置redis
        List<StatDay> statDayList = setRedisStatDay();
        List<Long> comment = statDayList.stream().limit(day - 1).map(statDay -> statDay.getCommentCount()).collect(Collectors.toList());
        Long lastCount = CollectionUtil.isEmpty(comment)? 0l : comment.stream().reduce(Long::sum).get();
        Date date = new Date();
        Long now = date.getTime();
        //今日零点
        DateTime begin = DateUtil.beginOfDay(date);
        long today = begin.getTime();
        QueryBuilder queryBuilder = QueryBuilders.rangeQuery("created_time").from(today).to(now);
        SumAggregationBuilder sumAggregationBuilder = AggregationBuilders.sum("sum_comments").field("comments_count");
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .addAggregation(sumAggregationBuilder)
                //不需要获取source结果集
                .withSourceFilter(new FetchSourceFilterBuilder().build())
                .build();
        SearchHits<ESMblog> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery, ESMblog.class);
        Sum sum_comments = searchHits.getAggregations().get("sum_comments");
        return Math.round(sum_comments.getValue())+lastCount;
    }

    /**
     * 以下为抽出来的公共方法
     */

    /**
     * 根据QueryBuilder构建查询，返回舆情地图数据
     * @param queryBuilder
     * @return
     */
    @NotNull
    private List<DataDTO> getProvinceGroupDataByQuery(QueryBuilder queryBuilder) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .addAggregation(AggregationBuilders.terms("province_group").field("province").size(99))
                .build();
        SearchHits<ESMblog> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery, ESMblog.class);
        Terms terms = searchHits.getAggregations().get("province_group");
        List<DataDTO> res = terms.getBuckets().stream().map(bucket -> {
            DataDTO provinceGroupDTO = new DataDTO();
            provinceGroupDTO.setName((String) bucket.getKey());
            provinceGroupDTO.setValue(bucket.getDocCount());
            return provinceGroupDTO;
        }).collect(Collectors.toList());
//        log.info(res.toString());
        return res;
    }

    /**
     * 根据QueryBuilder构建查询，返回前day日舆情走势数据
     * @param queryBuilder
     * @param day
     * @return
     */
    private TrendDTO getPublicOpinionTrendByQuery(QueryBuilder queryBuilder, Date currDate, int day){
        //1、设置map日期序列(LinkedHashMap保证有序)
        Map<String, SentimentDTO> map = new LinkedHashMap<>();
        SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd"); //设置格式
        Long timestamp = DateUtil.offsetDay(currDate,-day).getTime();
        for(int i=day;i>0;i--){
            timestamp += 24*60*60*1000;
            map.put(format.format(timestamp),new SentimentDTO());
        }
        //2、根据queryBuilder构造查询
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .addAggregation(AggregationBuilders.terms("date_sentiment_group")
                        .script(new Script("doc['created_time_text'].value+'@@'+doc['sentiment'].value"))
                        .size(day*3))
                .build();
        //3、解析响应
        SearchHits<ESMblog> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery, ESMblog.class);
        Terms terms = searchHits.getAggregations().get("date_sentiment_group");
        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        terms.getBuckets().stream().forEach(bucket -> {
            String[] str = bucket.getKeyAsString().split("@@");
            long docCount = bucket.getDocCount();
            String key = str[0];
            int sentiment = Integer.valueOf(str[1]);
            SentimentDTO sentimentDTO = map.get(key);
            if(sentiment==1){
                sentimentDTO.setPositiveCount(docCount);
            }else if(sentiment==0){
                sentimentDTO.setNeuterCount(docCount);
            }else{
                sentimentDTO.setNegativeCount(docCount);
            }
            map.put(key, sentimentDTO);
        });
        //4、构造返回
        TrendDTO trendDTO = new TrendDTO();
        List<String> timeSeries = map.keySet().stream().collect(Collectors.toList());
        List<Long> positiveSeries = new ArrayList<>();
        List<Long> neuterSeries = new ArrayList<>();
        List<Long> negativeSeries = new ArrayList<>();
        map.values().stream().forEach(sentimentDTO -> {
            positiveSeries.add(sentimentDTO.getPositiveCount());
            neuterSeries.add(sentimentDTO.getNeuterCount());
            negativeSeries.add(sentimentDTO.getNegativeCount());
        });
        trendDTO.setTimeSeries(timeSeries);
        trendDTO.setPositiveSeries(positiveSeries);
        trendDTO.setNeuterSeries(neuterSeries);
        trendDTO.setNegativeSeries(negativeSeries);
        return trendDTO;
    }

    /**
     * 根据SearchAnalysisDTO构建QueryBuilder
     * @param searchAnalysisDTO
     * @return
     */
    private BoolQueryBuilder getSearchAnalysisQueryBuilder(SearchAnalysisDTO searchAnalysisDTO) {
        //1 获取参数
        Integer day = searchAnalysisDTO.getDay();
        String searchWord = searchAnalysisDTO.getSearchWord();
        String hotWord = searchAnalysisDTO.getHotWord();
        String ignoreWord = searchAnalysisDTO.getIgnoreWord();
        String location = searchAnalysisDTO.getLocation();
        Integer sentiment = searchAnalysisDTO.getSentiment();
        //2 构造查询条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if(day!=null){
            Date date = new Date();
            Long now = date.getTime();
            DateTime today = DateUtil.parse(DateUtil.formatDate(date));
            long lastday = DateUtil.offsetDay(today, -day + 1).getTime();
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("created_time").from(lastday).to(now));
        }
        if(sentiment!=null){
            boolQueryBuilder.must(QueryBuilders.termQuery("sentiment", sentiment));
        }
        if(StringUtils.isEmpty(searchWord)){
            //3.1 任务分析
            //3.1.1 构造位置查询条件
            if(!StringUtils.isEmpty(location)){
                boolQueryBuilder.must(QueryBuilders.matchQuery("location", location).operator(Operator.OR));
            }
            //3.1.2 构造屏蔽词查询条件
            if(!StringUtils.isEmpty(ignoreWord)){
                Operator operator = getOperatorType(ignoreWord);
                boolQueryBuilder.mustNot(QueryBuilders.matchQuery("text", ignoreWord).operator(operator));
            }
            //3.1.3 构造关键词查询条件
            Operator operator = getOperatorType(hotWord);
            boolQueryBuilder.must(QueryBuilders.matchQuery("text", hotWord).operator(operator));
        }else{
            //3.2 搜索分析,分词匹配text、location
            boolQueryBuilder
                    .filter(QueryBuilders.multiMatchQuery(searchWord,"text","location"));
        }
        return boolQueryBuilder;
    }

    /**
     * 根据关键词/屏蔽词表达式，解析操作类型
     * @param text
     * @return
     */
    private Operator getOperatorType(String text){
        Operator res = Operator.OR;
        if(text.contains("|") && text.contains("&")){
            throw new GlobalException("暂不支持同时使用’|‘、’&‘");
        }
        if(text.contains("&")){
            res = Operator.AND;
        }
        return res;
    }

    /**
     * 深拷贝QueryBuilder
     * @param queryBuilder 源QueryBuilder
     * @return
     * @throws IOException
     */
    private QueryBuilder copyQueryBuilder(QueryBuilder queryBuilder) throws IOException {
        SearchModule searchModule = new SearchModule(Settings.EMPTY, false, Collections.emptyList());
        XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(
                new NamedXContentRegistry(searchModule.getNamedXContents()), LoggingDeprecationHandler.INSTANCE, queryBuilder.toString());

        return AbstractQueryBuilder.parseInnerQueryBuilder(parser);
    }

    /**
     * 设置redis，并返回最近13天的统计数据
     * @return
     */
    private List<StatDay> setRedisStatDay(){
        List<StatDay> statDayList = (List<StatDay>) redisTemplate.opsForValue().get(key);
        if(CollectionUtil.isEmpty(statDayList)){
            statDayList = statDayService.getLately13();
            //失效的时间
            DateTime end = DateUtil.endOfDay(new Date());
            long expired = end.getTime();
            redisTemplate.opsForValue().set(key, statDayList, (expired-System.currentTimeMillis())/1000, TimeUnit.SECONDS);
        }
        return statDayList;
    }

    /**
     * 获取近day日 topicId话题下的舆情走向数据
     * @param day
     * @param topicId topicId为null时，统计所有话题
     * @return
     */
    @Override
    public TrendDTO getPublicOpinionTrend(int day, String topicId) {
        //如果topicId不空，统计给定话题
        if(!StringUtils.isEmpty(topicId)) {
            //1、构造queryBuilder
            Date date = new Date();
            Long now = date.getTime();
            DateTime today = DateUtil.parse(DateUtil.formatDate(date));
            long lastday = DateUtil.offsetDay(today, -day + 1).getTime();
            BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
            queryBuilder.filter(QueryBuilders.rangeQuery("created_time").from(lastday).to(now));


            ESHot esHot = esHotService.getById(topicId);
            String topic = esHot.getTitle();
            queryBuilder.must(QueryBuilders.termQuery("topic", topic));
            //2、根据queryBuilder返回舆情走势数据
            return getPublicOpinionTrendByQuery(queryBuilder, date, day);
        }else{
            //1、设置redis
            List<StatDay> statDayList = setRedisStatDay();
            List<Long> positive = statDayList.stream().limit(day - 1).map(statDay -> statDay.getPositiveCount()).collect(Collectors.toList());
            List<Long> neuter = statDayList.stream().limit(day - 1).map(statDay -> statDay.getNeuterCount()).collect(Collectors.toList());
            List<Long> negative = statDayList.stream().limit(day - 1).map(statDay -> statDay.getNegativeCount()).collect(Collectors.toList());
            List<String> timeSeries = statDayList.stream().limit(day - 1).map(statDay -> DateUtil.formatDate(statDay.getCreateTime())).collect(Collectors.toList());
            Date date = new Date();
            int diff = day-1 - statDayList.size();
            for(int i=1;i<=diff;i++){
                //结尾补diff天
                positive.add(0l);
                neuter.add(0l);
                negative.add(0l);
                timeSeries.add(DateUtil.formatDate(DateUtil.offsetDay(date,-i)));
            }
            Collections.reverse(positive);
            Collections.reverse(neuter);
            Collections.reverse(negative);
            Collections.reverse(timeSeries);

            Long now = date.getTime();
            //今日零点
            DateTime begin = DateUtil.beginOfDay(date);
            long today = begin.getTime();
            QueryBuilder queryBuilder = QueryBuilders.rangeQuery("created_time").from(today).to(now);
            NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                    .withQuery(queryBuilder)
                    .addAggregation(AggregationBuilders.terms("sentiment_group").field("sentiment").size(3))
                    .build();
            //2、查询并解析
            SearchHits<ESMblog> search = elasticsearchRestTemplate.search(nativeSearchQuery, ESMblog.class);
            Terms terms = search.getAggregations().get("sentiment_group");
            terms.getBuckets().forEach(bucket -> {
                Integer key = Integer.valueOf(bucket.getKeyAsString());
                long docCount = bucket.getDocCount();
                if(key==1){
                    positive.add(docCount);
                }else if(key==0){
                    neuter.add(docCount);
                }else{
                    negative.add(docCount);
                }
            });
            timeSeries.add(DateUtil.formatDate(date));
            return new TrendDTO(timeSeries, positive, neuter, negative);
        }
    }


}
