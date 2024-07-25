package com.nobug.public_opinion_monitor.controller;

import com.nobug.public_opinion_monitor.common.GlobalException;
import com.nobug.public_opinion_monitor.common.R;
import com.nobug.public_opinion_monitor.dto.ArticleDTO;
import com.nobug.public_opinion_monitor.dto.TopicDTO;
import com.nobug.public_opinion_monitor.dto.SearchDTO;
import com.nobug.public_opinion_monitor.service.ESHotService;
import com.nobug.public_opinion_monitor.service.ESMblogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 舆情监测controller
 *
 * @date：2023/2/18
 * @author：nobug
 */
@RestController
@RequestMapping(value = "/monitor")
@Slf4j
public class POMonitorController {

    @Autowired
    private ESHotService esHotService;
    @Autowired
    private ESMblogService esMblogService;

    /**
     * 根据查询条件分页查询话题列表
     * @param searchDTO
     * @return
     */
    @PostMapping("/pagesearchtopiclist")
    public R<TopicDTO> pageSearchTopicList(@RequestBody SearchDTO searchDTO){
        TopicDTO res = null;
        try{
            //log.info(searchDTO.toString());
            //1、处理请求参数
            assert searchDTO.getPageNo() != null;
            assert searchDTO.getPageNo() != null;
            //2、调用service
            res = esHotService.pageSearchTopicList(searchDTO);
        }catch (Exception e){
            throw new GlobalException("分页请求话题列表失败：" + e.getMessage());
        }
        //3、响应
        return R.ok(res);
    }

    /**
     * 根据查询条件分页查询博文列表
     * @param searchDTO
     * @return
     */
    @PostMapping("/pagesearcharticlelist")
    public R<ArticleDTO> pageSearchArticleList(@RequestBody SearchDTO searchDTO){
        ArticleDTO res = null;
        try{
            //log.info(searchDTO.toString());
            //1、处理请求参数
            assert searchDTO.getPageNo() != null;
            assert searchDTO.getPageNo() != null;
            //2、调用service
            res = esMblogService.pageSearchArticleList(searchDTO);
        }catch (Exception e){
            throw new GlobalException("分页请求博文列表失败：" + e.getMessage());
        }
        //3、响应
        return R.ok(res);
    }

}
