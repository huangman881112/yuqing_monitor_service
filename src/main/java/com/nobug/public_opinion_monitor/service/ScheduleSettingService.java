package com.nobug.public_opinion_monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nobug.public_opinion_monitor.entity.ScheduleSetting;

import java.util.List;

/**
 * ScheduleSetting Service层
 *
 * @date：2023/2/24
 * @author：nobug
 */
public interface ScheduleSettingService extends IService<ScheduleSetting> {

    /**
     * 新增定时任务
     * @param setting
     * @return
     */
    boolean add(ScheduleSetting setting);

    /**
     * 修改定时任务
     * @param setting
     * @return
     */
    boolean update(ScheduleSetting setting);

    /**
     * 根据taskId删除定时任务
     * @param taskIds
     * @return
     */
    boolean deleteByTaskIds(List<Long> taskIds);

    /**
     * 改变定时任务状态
     * @param taskId
     * @return
     */
    boolean changeStatus(Long taskId);

}
