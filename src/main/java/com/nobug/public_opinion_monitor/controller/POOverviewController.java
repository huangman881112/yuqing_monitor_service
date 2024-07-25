package com.nobug.public_opinion_monitor.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.nobug.public_opinion_monitor.common.GlobalException;
import com.nobug.public_opinion_monitor.common.R;
import com.nobug.public_opinion_monitor.dto.DataDTO;
import com.nobug.public_opinion_monitor.dto.TrendDTO;
import com.nobug.public_opinion_monitor.service.ESHotService;
import com.nobug.public_opinion_monitor.service.ESMblogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 舆情概览controller
 *
 * @date：2023/2/16
 * @author：nobug
 */
@RestController
@RequestMapping(value = "/overview")
@Slf4j
public class POOverviewController {

    @Autowired
    private ESHotService esHotService;
    @Autowired
    private ESMblogService esMblogService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 去重统计近day日话题数
     * @param day
     * @return
     */
    @GetMapping(value = "/gettotaltopicnearly")
    public R<Long> getTotalTopicNearly(int day){
        Long totalTopic = 0l;
        try{
            String key = "totalTopic-" + day;
            //1、缓存中取值
            totalTopic = (Long)redisTemplate.opsForValue().get(key);
            if(totalTopic == null){
                //2、null，则es查询
                totalTopic = esHotService.getTopicNearly(day);
                //3、写入redis
                redisTemplate.opsForValue().set(key, totalTopic, getExpiredMillisecond(), TimeUnit.MILLISECONDS);
            }
        }catch (Exception e){
            throw new GlobalException("获取微博话题数失败："+e.getMessage());
        }
        return R.ok(totalTopic);
    }

    /**
     * 去重统计获取近day日博文数
     * @param day
     * @return
     */
    @GetMapping(value = "/gettotalarticlenearly")
    public R<Long> getTotalArticleNearly(int day) {
        Long totalArticle = 0l;
        try{
            String key = "totalArticle-" + day;
            //1、缓存中取值
            totalArticle = (Long)redisTemplate.opsForValue().get(key);
            if(totalArticle == null){
                //2、null，则es查询
                totalArticle = esMblogService.getArticleNearly(day);
                //3、写入redis
                redisTemplate.opsForValue().set(key, totalArticle, getExpiredMillisecond(), TimeUnit.MILLISECONDS);
            }
        }catch (Exception e){
            throw new GlobalException("获取微博博文数失败："+e.getMessage());
        }
        return R.ok(totalArticle);
    }

    /**
     * 获取近day日负面舆情数
     * @param day
     * @return
     */
    @GetMapping(value = "/gettotalnegativenearly")
    public R<Long> getTotalNegativeNearly(int day) {
        Long totalNegative = 0l;
        try{
            String key = "totalNegative-" + day;
            //1、缓存中取值
            totalNegative = (Long)redisTemplate.opsForValue().get(key);
            if(totalNegative == null){
                //2、null，则es查询
                totalNegative = esMblogService.getNegativeNearly(day);
                //3、写入redis
                redisTemplate.opsForValue().set(key, totalNegative, getExpiredMillisecond(), TimeUnit.MILLISECONDS);
            }
        }catch (Exception e){
            throw new GlobalException("获取负面舆情数失败："+e.getMessage());
        }
        return R.ok(totalNegative);
    }

    /**
     * 获取近day日用户评论数
     * @param day
     * @return
     */
    @GetMapping(value = "/gettotalcommentnearly")
    public R<Long> getTotalCommentNearly(int day) {
        //log.info(String.valueOf(day));
        Long totalCommets = 0l;
        try{
            String key = "totalCommets-" + day;
            //1、缓存中取值
            totalCommets = (Long)redisTemplate.opsForValue().get(key);
            if(totalCommets == null){
                //2、null，则es查询
                totalCommets = esMblogService.getCommentNearly(day);
                //3、写入redis
                redisTemplate.opsForValue().set(key, totalCommets, getExpiredMillisecond(), TimeUnit.MILLISECONDS);
            }
        }catch (Exception e){
            throw new GlobalException("获取用户评论数失败："+e.getMessage());
        }
        return R.ok(totalCommets);
    }

    /**
     * 获取前100条热搜词条数据
     * @return
     */
    @GetMapping(value = "/getwordclouddata")
    public R<List<DataDTO>> getWordCloudData(){
        List<DataDTO> res = null;
        try{
            String key = "wordclouddata";
            //1、缓存中取值
            res = (List<DataDTO>) redisTemplate.opsForValue().get(key);
            if(res == null){
                //2、null，则es查询
                res = esHotService.getWordCloudData(100);
                //3、写入redis
                redisTemplate.opsForValue().set(key, res, getExpiredMillisecond(), TimeUnit.MILLISECONDS);
            }
        }catch (Exception e){
            throw new GlobalException("获取词云图数据失败："+e.getMessage());
        }
        return R.ok(res);
    }

    /**
     * 获取近day日各省份舆论数
     * @param day
     * @return
     */
    @GetMapping(value = "/getprovincegroupdata")
    public R<List<DataDTO>> getProvinceGroupData(int day){
        List<DataDTO> res = null;
        try{
            String key = "provincegroupdata"+day;
            //1、缓存中取值
            res = (List<DataDTO>) redisTemplate.opsForValue().get(key);
            if(res == null){
                //2、null，则es查询，topicId为null，统计所有话题
                res = esMblogService.getProvinceGroupData(day, null);
                //3、写入redis
                redisTemplate.opsForValue().set(key, res, getExpiredMillisecond(), TimeUnit.MILLISECONDS);
            }
        }catch (Exception e){
            throw new GlobalException("获取舆情地图数据失败："+e.getMessage());
        }
        return R.ok(res);
    }

    /**
     * 获取近day日舆情走势
     * @param day
     * @return
     */
    @GetMapping(value = "/getpublicopiniontrend")
    public R<TrendDTO> getPublicOpinionTrend(int day){
        TrendDTO res = null;
        try{
            String key = "publicopiniontrend"+day;
            //1、缓存中取值
            res = (TrendDTO) redisTemplate.opsForValue().get(key);
            if(res == null){
                //2、null，则es查询，topicId为null，统计所有话题
                res = esMblogService.getPublicOpinionTrend(day, null);
                //3、写入redis
                redisTemplate.opsForValue().set(key, res, getExpiredMillisecond(), TimeUnit.MILLISECONDS);
            }
        }catch (Exception e){
            throw new GlobalException("获取趋势图数据失败："+e.getMessage());
        }
        return R.ok(res);
    }

    /**
     * 获取缓存过期毫秒数
     * @return
     */
    private long getExpiredMillisecond(){
        Date date = new Date();
        int minute = DateUtil.minute(date);
        DateTime expired;
        if(minute%2==0){
            expired = DateUtil.offsetMinute(date, 2);
        }else{
            expired = DateUtil.offsetMinute(date, 1);
        }
        Random random = new Random();
        long expiredMillisecond = expired.getTime() / (1000 * 60) * 60 * 1000 + random.nextInt(1000);
        return expiredMillisecond-date.getTime();
    }

}
