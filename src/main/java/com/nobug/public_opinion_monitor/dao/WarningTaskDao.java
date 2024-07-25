package com.nobug.public_opinion_monitor.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nobug.public_opinion_monitor.entity.WarningTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预警任务Dao层
 *
 * @date：2023/2/20
 * @author：nobug
 */
@Mapper
public interface WarningTaskDao extends BaseMapper<WarningTask> {
}
