package com.nobug.public_opinion_monitor.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nobug.public_opinion_monitor.dao.WarningTaskDao;
import com.nobug.public_opinion_monitor.dto.SearchDTO;
import com.nobug.public_opinion_monitor.entity.ScheduleSetting;
import com.nobug.public_opinion_monitor.entity.WarningTask;
import com.nobug.public_opinion_monitor.schedule.CronTaskRegistrar;
import com.nobug.public_opinion_monitor.schedule.SchedulingRunnable;
import com.nobug.public_opinion_monitor.service.ScheduleSettingService;
import com.nobug.public_opinion_monitor.service.WarningTaskService;
import com.nobug.public_opinion_monitor.utils.JwtUtil;
import com.nobug.public_opinion_monitor.utils.SnowFlake;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 预警任务Service实现类
 *
 * @date：2023/2/20
 * @author：nobug
 */
@Service
@Slf4j
public class WarningTaskServiceImpl extends ServiceImpl<WarningTaskDao, WarningTask> implements WarningTaskService {

    @Autowired
    private WarningTaskDao warningTaskDao;

    @Autowired
    private WarningTaskService warningTaskService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private ScheduleSettingService scheduleSettingService;

    @Autowired
    private CronTaskRegistrar cronTaskRegistrar;

    private SnowFlake snowFlake = new SnowFlake();

    @Override
    public Page<WarningTask> pageSearchTask(SearchDTO searchDTO) {
        //1、解析请求参数
        Integer pageNo = searchDTO.getPageNo();
        Integer pageSize = searchDTO.getPageSize();
        assert pageNo!=null;
        assert pageSize!=null;
        String startDate = searchDTO.getStartDate();
        String endDate = searchDTO.getEndDate();
        String hotWord = searchDTO.getHotWord();
        Integer status = searchDTO.getStatus();
        Integer sentiment = searchDTO.getSentiment();
        String location = searchDTO.getLocation();
        //2、获取userId
        Claims claims = JwtUtil.parseFromRequest(request);
        Long userId = Long.valueOf(claims.getId());
        Page<WarningTask> warningTaskPage = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<WarningTask> lqw = new LambdaQueryWrapper<>();
        lqw.eq(WarningTask::getUserId, userId)
                .eq(status!=null, WarningTask::getStatus, status)
                .eq(sentiment!=null, WarningTask::getSentiment, sentiment)
                .between(!StringUtils.isEmpty(startDate)&&!StringUtils.isEmpty(endDate),
                        WarningTask::getCreateTime, startDate, endDate)
                .like(!StringUtils.isEmpty(location),WarningTask::getLocation, location)
                .like(!StringUtils.isEmpty(hotWord),WarningTask::getTaskName, hotWord);

        warningTaskService.page(warningTaskPage, lqw);
        return warningTaskPage;
    }

    @Override
    @Transactional(isolation = Isolation.DEFAULT)
    public void deleteBatchByTaskId(List<Long> taskIds) {
        LambdaQueryWrapper<WarningTask> lqw = new LambdaQueryWrapper<>();
        lqw.in(WarningTask::getTaskId,taskIds);
        warningTaskDao.delete(lqw);
        scheduleSettingService.deleteByTaskIds(taskIds);
    }

    @Override
    public void changeStatus(Long taskId, int status) {
        //1 查询定时配置
        ScheduleSetting setting = scheduleSettingService.getOne(new LambdaQueryWrapper<ScheduleSetting>()
                .eq(ScheduleSetting::getTaskId, taskId));
        if(status==1){
            //启动任务
            //2.1 注册任务
            SchedulingRunnable task = new SchedulingRunnable(setting.getBeanName(), setting.getMethodName(), setting.getMethodParams());
            cronTaskRegistrar.addCronTask(task, setting.getCron());
            //2.2 修改状态为启动中
        }else{
            //停止任务
            //3.1 移除任务
            SchedulingRunnable task = new SchedulingRunnable(setting.getBeanName(), setting.getMethodName(), setting.getMethodParams());
            cronTaskRegistrar.removeCronTask(task);
        }
        //4 更新warning_task表状态
        LambdaUpdateWrapper<WarningTask> luw = new LambdaUpdateWrapper();
        luw.eq(WarningTask::getTaskId, taskId)
                .set(WarningTask::getStatus, status);
        warningTaskService.update(null, luw);
    }

    @Override
    @Transactional(isolation = Isolation.DEFAULT)
    public void saveOrUpdateTask(WarningTask warningTask) {
        //1 校验参数
        Long task_id = warningTask.getTaskId();
        if(task_id==null){
            //2 新增逻辑
            //2.1 获取Claims
            Claims claims = JwtUtil.parseFromRequest(request);
            //2.2 设置userId、taskId
            warningTask.setUserId(Long.valueOf(claims.getId()));
            long taskId = snowFlake.getId();
            warningTask.setTaskId(taskId);
            warningTask.setStatus(warningTask.getAutoRun());
            //2.3 创建定时任务配置
            ScheduleSetting setting = new ScheduleSetting();
            setting.setTaskId(taskId);
            setting.setBeanName("warningScheduleTask");
            setting.setMethodName("warningRun");
            setting.setMethodParams(String.valueOf(taskId));
            setting.setName(warningTask.getTaskName());
            setting.setStatus(warningTask.getAutoRun());
            setting.setCron(parseMin2Cron(warningTask.getFrequency()));
            //2.4 插入warning_task表
            warningTaskService.save(warningTask);
            //2.5 插入schedule_setting
            scheduleSettingService.add(setting);
        }else{
            //3 修改逻辑
            //3.1 创建定时任务配置
            ScheduleSetting setting = new ScheduleSetting();
            setting.setTaskId(task_id);
            setting.setBeanName("warningScheduleTask");
            setting.setMethodName("warningRun");
            setting.setMethodParams(String.valueOf(task_id));
            setting.setName(warningTask.getTaskName());
            setting.setStatus(warningTask.getAutoRun());
            setting.setCron(parseMin2Cron(warningTask.getFrequency()));
            warningTask.setStatus(warningTask.getAutoRun());
            //3.2 更新warning_task表
            LambdaUpdateWrapper<WarningTask> lambdaUpdateWrapper = new LambdaUpdateWrapper();
            lambdaUpdateWrapper.eq(WarningTask::getTaskId, warningTask.getTaskId());
            warningTaskService.update(warningTask,lambdaUpdateWrapper);
            //3.3 更新schedule_setting
            scheduleSettingService.update(setting);
        }
    }

    /**
     * 转换分钟数为Cron表达式
     * @param minute
     * @return
     */
    private String parseMin2Cron(int minute){
        String cron = "";
        switch(minute){
            case 1:
                cron = "0 */1 * * * ?";
                break;
            case 5:
                cron = "0 */5 * * * ?";
                break;
            case 30:
                cron = "0 */30 * * * ?";
                break;
            case 60:
                cron = "0 0 */1 * * ?";
                break;
            case 720:
                cron = "0 0 */12 *  * ?";
                break;
            case 1440:
                cron = "0 0 0 */1 * ?";
                break;
        }
        return cron;
    }

}

