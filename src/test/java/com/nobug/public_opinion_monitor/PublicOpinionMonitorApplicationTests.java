package com.nobug.public_opinion_monitor;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.nobug.public_opinion_monitor.dto.DataDTO;
import com.nobug.public_opinion_monitor.entity.ESMblog;
import com.nobug.public_opinion_monitor.entity.StatDay;
import com.nobug.public_opinion_monitor.service.ESMblogService;
import com.nobug.public_opinion_monitor.service.StatDayService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.AnalyzeRequest;
import org.elasticsearch.client.indices.AnalyzeResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class PublicOpinionMonitorApplicationTests {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private StatDayService statDayService;

    @Autowired
    private ESMblogService esMblogService;

    @Test
    public void getProvinceGroupData() {
        Date date = new Date();
        Long now = date.getTime();
        Long lastday = DateUtil.offsetDay(date,-7).getTime();
        QueryBuilder queryBuilder = QueryBuilders.rangeQuery("created_time").from(lastday).to(now);
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withCollapseField("mid")
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
        log.info(res.toString());
    }

    @Test
    public void test(){
        String today = DateUtil.today();
        DateTime time = DateUtil.parse(today);
        DateTime day = DateUtil.offsetDay(time, -6);
        System.out.println(day);
    }

    @Test
    public void test1() throws IOException {
        RestHighLevelClient restHighLevelClient = elasticsearchRestTemplate.execute(client -> client);
        AnalyzeRequest analyzeRequest = AnalyzeRequest.buildCustomAnalyzer("ik_max_word").build("广州|深圳|杭州");
        AnalyzeResponse resp = restHighLevelClient.indices().analyze(analyzeRequest, RequestOptions.DEFAULT);
        List<String> list = resp.getTokens().stream().map(analyzeToken -> analyzeToken.getTerm()).collect(Collectors.toList());
        log.info(list.toString());
    }

    @Test
    public void test2(){
//        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
//        String[] locations = {"广州","深圳"};
//        List<String> wordlist = new ArrayList<>();
//        Collections.addAll(wordlist, locations);
//        boolQueryBuilder.filter(QueryBuilders.termQuery("location", wordlist));
//        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
//                .withQuery(boolQueryBuilder)
//                .withCollapseField("mid")
//                .build();
//        SearchHits<ESMblog> search = elasticsearchRestTemplate.search(nativeSearchQuery, ESMblog.class);
        String s = "医疗|能源|经济|政治";
        if(s.contains("|")){
            log.info("|||||");
        }else if(s.contains("&&&")){
            log.info("&&&&&");
        }else{
            log.info("未知");
        }
    }

    @Test
    public void testGetLately14(){
        List<StatDay> lately14 = statDayService.getLately13();
        log.info(lately14.toString());
    }

    @Test
    public void testGetStatDay(){
        StatDay statDay = esMblogService.getStatDay();
        log.info(statDay.toString());
    }

    @Test
    public void getpassdata(){
        Calendar ca1 = Calendar.getInstance();
        //今日结束
        ca1.set(Calendar.HOUR_OF_DAY, 23);
        ca1.set(Calendar.MINUTE, 59);
        ca1.set(Calendar.SECOND, 59);
        long end = ca1.getTimeInMillis();
        long time = DateUtil.offsetDay(new Date(end), -14).getTime();
        NativeSearchQuery queryBuilder = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.rangeQuery("created_time").lte(time))
                .build();
        SearchHits<ESMblog> search = elasticsearchRestTemplate.search(queryBuilder, ESMblog.class);
        log.info(search.toString());
    }


}
