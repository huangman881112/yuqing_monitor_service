package com.nobug.public_opinion_monitor.schedule;

import cn.hutool.core.date.DateUtil;
import com.nobug.public_opinion_monitor.entity.ESHot;
import com.nobug.public_opinion_monitor.entity.ESMblog;
import com.nobug.public_opinion_monitor.entity.StatDay;
import com.nobug.public_opinion_monitor.service.ESMblogService;
import com.nobug.public_opinion_monitor.service.StatDayService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 每日统计定时任务
 *
 * @date：2023/3/11
 * @author：nobug
 */
@Slf4j
@Component("dayStatScheduleTask")
public class DayStatScheduleTask {

    @Autowired
    private ESMblogService esMblogService;

    @Autowired
    private StatDayService statDayService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    public void dayStatRun(){
        log.info(DateUtil.formatDateTime(new Date(System.currentTimeMillis()))+" 每日统计任务开始执行————————");
        //1、统计数据
        StatDay statDay = esMblogService.getStatDay();
        //2、插入数据库
        statDayService.save(statDay);
        log.info("插入数据库："+statDay.toString());
        //3、删除缓存
        redisTemplate.delete(ESMblogService.key);
        //4、ES清空14天前的数据
        Date currentTime = statDay.getCreateTime();
        long time = DateUtil.offsetDay(currentTime, -14).getTime();
        NativeSearchQuery queryBuilder = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.rangeQuery("created_time").lte(time))
                .build();
        elasticsearchRestTemplate.delete(queryBuilder, ESMblog.class, elasticsearchRestTemplate.getIndexCoordinatesFor(ESMblog.class));
        elasticsearchRestTemplate.delete(queryBuilder, ESHot.class, elasticsearchRestTemplate.getIndexCoordinatesFor(ESHot.class));
        log.info(DateUtil.formatDateTime(new Date(System.currentTimeMillis()))+" 每日统计任务结束————————");
    }
}
