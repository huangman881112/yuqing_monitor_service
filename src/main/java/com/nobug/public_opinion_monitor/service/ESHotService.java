package com.nobug.public_opinion_monitor.service;

import com.nobug.public_opinion_monitor.dto.DataDTO;
import com.nobug.public_opinion_monitor.dto.TopicDTO;
import com.nobug.public_opinion_monitor.dto.SearchDTO;
import com.nobug.public_opinion_monitor.entity.ESHot;
import com.nobug.public_opinion_monitor.utils.SnowFlake;

import java.util.List;

/**
 * ESHotService servcie层
 *
 * @date：2023/2/16
 * @author：nobug
 */
public interface ESHotService {

    /**
     * 获取近day日累计话题数
     * @param day
     * @return
     */
    Long getTopicNearly(int day);

    /**
     * 获取词云图数据,最近wordNum条数据,eg:
     * [
     * {name:"热搜话题", value:"热度"},
     * ...
     * ]
     * @param wordNum
     * @return
     */
    List<DataDTO> getWordCloudData(int wordNum);

    /**
     * 根据查询条件分页查询话题列表
     * @param searchDTO 查询条件
     * @return
     */
    TopicDTO pageSearchTopicList(SearchDTO searchDTO);

    /**
     * 返回最近一天热度最高的话题
     * @return
     */
    ESHot getMaxHot();

    /**
     * 根据id返回话题信息
     * @param id
     * @return
     */
    ESHot getById(String id);

    /**
     * 返回最近的话题
     * @return
     */
    ESHot getRecently();
}
