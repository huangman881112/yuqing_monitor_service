package com.nobug.public_opinion_monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nobug.public_opinion_monitor.dao.ScheduleSettingDao;
import com.nobug.public_opinion_monitor.entity.ScheduleSetting;
import com.nobug.public_opinion_monitor.schedule.CronTaskRegistrar;
import com.nobug.public_opinion_monitor.schedule.SchedulingRunnable;
import com.nobug.public_opinion_monitor.service.ScheduleSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @date：2023/2/24
 * @author：nobug
 */
@Service
public class ScheduleSettingServiceImpl extends ServiceImpl<ScheduleSettingDao, ScheduleSetting>
        implements ScheduleSettingService {

    @Autowired
    private CronTaskRegistrar cronTaskRegistrar;
    @Autowired
    private ScheduleSettingDao scheduleSettingDao;

    @Override
    public boolean add(ScheduleSetting setting) {
        //1、保存定时任务配置
        boolean save = save(setting);
        if(!save){
            //保存失败
            return false;
        }
        //保存成功
        if(setting.getStatus()==1){
            //2、状态为1，直接放入任务器
            SchedulingRunnable task = new SchedulingRunnable(setting.getBeanName(),
                    setting.getMethodName(), setting.getMethodParams());
            cronTaskRegistrar.addCronTask(task, setting.getCron());
        }
        return save;
    }

    @Override
    public boolean update(ScheduleSetting setting) {
        LambdaUpdateWrapper<ScheduleSetting> luw = new LambdaUpdateWrapper();
        luw.eq(ScheduleSetting::getTaskId, setting.getTaskId());
        //1、查询修改前的任务
        ScheduleSetting preTask = getOne(luw);
        //2、根据taskId更新
        boolean update = update(setting, luw);
        if (!update) {
            return false;
        } else {
            //3、修改成功,则先删除任务器中的任务,并重新添加
            SchedulingRunnable task1 = new SchedulingRunnable(preTask.getBeanName(), preTask.getMethodName(), preTask.getMethodParams());
            cronTaskRegistrar.removeCronTask(task1);
            if (setting.getStatus().equals(1)) {// 如果修改后的任务状态是1就加入任务器
                SchedulingRunnable task = new SchedulingRunnable(setting.getBeanName(), setting.getMethodName(), setting.getMethodParams());
                cronTaskRegistrar.addCronTask(task, setting.getCron());
            }
        }
        return update;
    }

    @Override
    public boolean deleteByTaskIds(List<Long> taskIds) {
        //1、查询删除前的任务
        LambdaQueryWrapper<ScheduleSetting> lqw = new LambdaQueryWrapper<>();
        lqw.in(ScheduleSetting::getTaskId, taskIds);
        List<ScheduleSetting> settings = scheduleSettingDao.selectList(lqw);
        //2、删除
        boolean remove = remove(lqw);
        if(!remove){
            return false;
        }else{
            // 删除成功时要清除定时任务器中的对应任务
            for(ScheduleSetting preTask: settings){
                SchedulingRunnable task = new SchedulingRunnable(preTask.getBeanName(), preTask.getMethodName(), preTask.getMethodParams());
                cronTaskRegistrar.removeCronTask(task);
            }
        }
        return remove;
    }

    @Override
    public boolean changeStatus(Long taskId) {
        //1、查询修改前的状态
        LambdaQueryWrapper<ScheduleSetting> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ScheduleSetting::getTaskId, taskId);
        ScheduleSetting preTask = getOne(lqw);
        int currStatus = 1-preTask.getStatus();
        //2、修改状态
        LambdaUpdateWrapper<ScheduleSetting> luw = new LambdaUpdateWrapper();
        luw.eq(ScheduleSetting::getTaskId, taskId)
            .set(ScheduleSetting::getStatus, currStatus);
        boolean update = update(null, luw);
        if(!update){
            return false;
        }else{
            SchedulingRunnable task = new SchedulingRunnable(preTask.getBeanName(), preTask.getMethodName(), preTask.getMethodParams());
            if(currStatus==1){
                //添加任务
                cronTaskRegistrar.addCronTask(task, preTask.getCron());
            }else{
                //清除任务
                cronTaskRegistrar.removeCronTask(task);
            }
        }
        return update;
    }
}
