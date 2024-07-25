package com.nobug.public_opinion_monitor.controller;

import com.nobug.public_opinion_monitor.common.GlobalException;
import com.nobug.public_opinion_monitor.common.R;
import com.nobug.public_opinion_monitor.dto.*;
import com.nobug.public_opinion_monitor.entity.ESHot;
import com.nobug.public_opinion_monitor.entity.ESMblog;
import com.nobug.public_opinion_monitor.service.ESHotService;
import com.nobug.public_opinion_monitor.service.ESMblogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 舆情分析controller
 *
 * @date：2023/2/19
 * @author：nobug
 */
@RestController
@RequestMapping("/analysis")
public class POAnalysisController {

    @Autowired
    private ESMblogService esMblogService;
    @Autowired
    private ESHotService esHotService;

    /**
     * 根据话题id查询话题信息及该话题下前6篇文章，按评论数、转发数、点赞数降序
     * @param topicId
     * @return
     */
    @GetMapping(value = "/articledetail/gettop6articlebytopicid")
    public R<TopicAnalysisDTO> getTop6ArticleByTopicId(String topicId){
        TopicAnalysisDTO res = null;
        try{
            //1、校验参数
            if(StringUtils.isEmpty(topicId)){
                //id为空  默认查找热度最高的话题
                ESHot esHot = esHotService.getMaxHot();
                if(esHot==null){
                    esHot = esHotService.getRecently();
                }
                topicId = esHot.getId();
            }
            //2、调用服务
            res = esMblogService.getTop6ArticleByTopicId(topicId);
        }catch (Exception e){
            throw new GlobalException("获取top6文章失败："+e.getMessage());
        }
        return R.ok(res);
    }

    /**
     * 根据话题id获取近day日舆情走势
     * @param day
     * @param topicId
     * @return
     */
    @GetMapping(value = "/articledetail/getpublicopiniontrendbytopicid")
    public R<TrendDTO> getPublicOpinionTrendByTopicId(int day, String topicId){
        TrendDTO res = null;
        try{
            //1、校验参数
            if(StringUtils.isEmpty(topicId)){
                //id为空  默认查找热度最高的话题
                ESHot esHot = esHotService.getMaxHot();
                if(esHot==null){
                    esHot = esHotService.getRecently();
                }
                topicId = esHot.getId();
            }
            //调用服务
            res = esMblogService.getPublicOpinionTrend(day, topicId);
        }catch (Exception e){
            throw new GlobalException("获取趋势图数据失败："+e.getMessage());
        }
        return R.ok(res);
    }

    /**
     * 根据话题id获取近day日舆情地图数据
     * @param day
     * @param topicId
     * @return
     */
    @GetMapping(value = "/articledetail/getprovincegroupdatabytopicid")
    public R<List<DataDTO>> getProvinceGroupDataByTopicId(int day, String topicId){
        List<DataDTO> res = null;
        try{
            //1、校验参数
            if(StringUtils.isEmpty(topicId)){
                //id为空  默认查找热度最高的话题
                ESHot esHot = esHotService.getMaxHot();
                if(esHot==null){
                    esHot = esHotService.getRecently();
                }
                topicId = esHot.getId();
            }
            //调用服务
            res = esMblogService.getProvinceGroupData(day, topicId);
        }catch (Exception e){
            throw new GlobalException("获取舆情地图数据失败："+e.getMessage());
        }
        return R.ok(res);
    }

    /**
     * 根据话题id统计近day日情感倾向性数据
     * @param day
     * @param topicId topicId为null时，统计所有话题
     * @return
     */
    @GetMapping(value = "/articledetail/getsentimentpiedatabytopicid")
    public R<List<DataDTO>> getSentimentPieDataByTopicId(int day, String topicId){
        List<DataDTO> res = null;
        try{
            //1、校验参数
            if(StringUtils.isEmpty(topicId)){
                //id为空  默认查找热度最高的话题
                ESHot esHot = esHotService.getMaxHot();
                if(esHot==null){
                    esHot = esHotService.getRecently();
                }
                topicId = esHot.getId();
            }
            //调用服务
            res = esMblogService.getSentimentPieData(day, topicId);
        }catch (Exception e){
            throw new GlobalException("获取情感倾向饼图数据失败："+e.getMessage());
        }
        return R.ok(res);
    }

    /**
     * 获取 total, positiveTotal, neuterCount, negativeCount
     * 如果SearchAnalysisDTO中searchWord为null，则为任务分析，否则即搜索分析
     * @param searchAnalysisDTO
     * @return
     */
    @PostMapping(value = "/search/getsearchstats")
    public R<SentimentDTO> getSearchStats(@RequestBody SearchAnalysisDTO searchAnalysisDTO){
        //1、校验请求参数
        Integer day = searchAnalysisDTO.getDay();
        assert day!=null : "参数：day 不能为空";
        String searchWord = searchAnalysisDTO.getSearchWord();
        String hotWord = searchAnalysisDTO.getHotWord();
        assert !StringUtils.isEmpty(hotWord)||!StringUtils.isEmpty(searchWord) : "参数：searchWord、keyWord 不能同时为空";
        //2、调用servcie层服务
        SentimentDTO res = null;
        try{
            res = esMblogService.getSearchStats(searchAnalysisDTO);
        }catch (Exception e){
            throw new GlobalException("获取统计信息失败："+e.getMessage());
        }
        return R.ok(res);
    }

    /**
     * 获取舆情地图数据
     * 如果SearchAnalysisDTO中searchWord为null，则为任务分析，否则即搜索分析
     * @param searchAnalysisDTO
     * @return
     */
    @PostMapping(value = "/search/getsearchprovincegroupdata")
    public R<List<DataDTO>> getSearchProvinceGroupData(@RequestBody SearchAnalysisDTO searchAnalysisDTO){
        //1、校验请求参数
        Integer day = searchAnalysisDTO.getDay();
        assert day!=null : "参数：day 不能为空";
        String searchWord = searchAnalysisDTO.getSearchWord();
        String hotWord = searchAnalysisDTO.getHotWord();
        assert !StringUtils.isEmpty(hotWord)||!StringUtils.isEmpty(searchWord) : "参数：searchWord、keyWord 不能同时为空";
        //2、调用servcie层服务
        List<DataDTO> res = null;
        try{
            res = esMblogService.getSearchProvinceGroupData(searchAnalysisDTO);
        }catch (Exception e){
            throw new GlobalException("获取舆情地图数据失败："+e.getMessage());
        }
        return R.ok(res);
    }

    /**
     * 获取关键词命中数据
     * 如果SearchAnalysisDTO中searchWord为null，则为任务分析，否则即搜索分析
     * @param searchAnalysisDTO
     * @return
     */
    @PostMapping(value = "/search/getsearchkeywordhit")
    public R<List<DataDTO>> getSearchKeyWordHit(@RequestBody SearchAnalysisDTO searchAnalysisDTO){
        //1、校验请求参数
        Integer day = searchAnalysisDTO.getDay();
        assert day!=null : "参数：day 不能为空";
        String searchWord = searchAnalysisDTO.getSearchWord();
        String hotWord = searchAnalysisDTO.getHotWord();
        assert !StringUtils.isEmpty(hotWord)||!StringUtils.isEmpty(searchWord) : "参数：searchWord、keyWord 不能同时为空";
        //2、调用servcie层服务
        List<DataDTO> res = null;
        try{
            res = esMblogService.getSearchKeyWordHit(searchAnalysisDTO);
        }catch (Exception e){
            throw new GlobalException("获取关键词命中数据失败："+e.getMessage());
        }
        return R.ok(res);
    }

    /**
     * 获取舆情走势数据
     * 如果SearchAnalysisDTO中searchWord为null，则为任务分析，否则即搜索分析
     * @param searchAnalysisDTO
     * @return
     */
    @PostMapping(value = "/search/getsearchpublicopiniontrend")
    public R<TrendDTO> getSearchPublicOpinionTrend(@RequestBody SearchAnalysisDTO searchAnalysisDTO){
        //1、校验请求参数
        Integer day = searchAnalysisDTO.getDay();
        assert day!=null : "参数：day 不能为空";
        String searchWord = searchAnalysisDTO.getSearchWord();
        String hotWord = searchAnalysisDTO.getHotWord();
        assert !StringUtils.isEmpty(hotWord)||!StringUtils.isEmpty(searchWord) : "参数：searchWord、keyWord 不能同时为空";
        //2、调用servcie层服务
        TrendDTO res = null;
        try{
            res = esMblogService.getSearchPublicOpinionTrend(searchAnalysisDTO);
        }catch (Exception e){
            throw new GlobalException("获取舆情地图数据失败："+e.getMessage());
        }
        return R.ok(res);
    }

    /**
     * 获取top10文章列表
     * 如果SearchAnalysisDTO中searchWord为null，则为任务分析，否则即搜索分析
     * @param searchAnalysisDTO
     * @return
     */
    @PostMapping(value = "/search/gettop10articles")
    public R<List<ESMblog>> getTop10Articles(@RequestBody SearchAnalysisDTO searchAnalysisDTO){
        //1、校验请求参数
        String searchWord = searchAnalysisDTO.getSearchWord();
        String hotWord = searchAnalysisDTO.getHotWord();
        assert !StringUtils.isEmpty(hotWord)||!StringUtils.isEmpty(searchWord) : "参数：searchWord、keyWord 不能同时为空";
        List<ESMblog> res = null;
        try{
            res = esMblogService.getTop10Articles(searchAnalysisDTO);
        }catch (Exception e){
            throw new GlobalException("获取舆情地图数据失败："+e.getMessage());
        }
        return R.ok(res);
    }


}
