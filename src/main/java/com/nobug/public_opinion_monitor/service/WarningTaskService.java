package com.nobug.public_opinion_monitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nobug.public_opinion_monitor.dto.SearchDTO;
import com.nobug.public_opinion_monitor.entity.WarningTask;

import java.util.List;

/**
 * 预警任务Service层
 *
 * @date：2023/2/20
 * @author：nobug
 */
public interface WarningTaskService extends IService<WarningTask> {

    /**
     * 新增或修改预警任务
     * 当warningTask的task_id字段为null，为新增；否则为修改
     * @param warningTask
     */
    void saveOrUpdateTask(WarningTask warningTask);

    /**
     * 根据请求参数分页查询任务列表
     * @param searchDTO 请求参数DTO
     * @return
     */
    Page<WarningTask> pageSearchTask(SearchDTO searchDTO);

    /**
     * 根据taskIds列表批量删除(逻辑删除)
     * @param taskIds
     */
    void deleteBatchByTaskId(List<Long> taskIds);

    /**
     * 根据taskId修改任务状态
     * @param taskId
     * @param status
     */
    void changeStatus(Long taskId, int status);
}
