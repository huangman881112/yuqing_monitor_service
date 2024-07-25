package com.nobug.public_opinion_monitor.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nobug.public_opinion_monitor.entity.ScheduleSetting;
import org.apache.ibatis.annotations.Mapper;

/**
 * ScheduleSetting Dao层
 *
 * @date：2023/2/24
 * @author：nobug
 */
@Mapper
public interface ScheduleSettingDao extends BaseMapper<ScheduleSetting> {
}
