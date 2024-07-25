package com.nobug.public_opinion_monitor.config;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nobug.public_opinion_monitor.dao.ScheduleSettingDao;
import com.nobug.public_opinion_monitor.entity.ScheduleSetting;
import com.nobug.public_opinion_monitor.schedule.CronTaskRegistrar;
import com.nobug.public_opinion_monitor.schedule.SchedulingRunnable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;


import java.util.List;

/**
 * 定时任务自动启动
 *
 * @date：2023/2/24
 * @author：nobug
 */
@Service
@Slf4j
public class ScheduledTaskRunner implements CommandLineRunner {

    @Autowired
    private CronTaskRegistrar cronTaskRegistrar;

    @Autowired
    private ScheduleSettingDao scheduleSettingDao;

    @Override
    public void run(String... args) throws Exception {
        //1、加载数据库中启用的定时任务
        List<ScheduleSetting> settingList = scheduleSettingDao.selectList(new LambdaQueryWrapper<ScheduleSetting>()
                .eq(ScheduleSetting::getStatus, 1));
        //2、注册定时任务
        if(CollectionUtil.isNotEmpty(settingList)){
            for(ScheduleSetting setting: settingList){
                SchedulingRunnable task = new SchedulingRunnable(setting.getBeanName(), setting.getMethodName(), setting.getMethodParams());
                cronTaskRegistrar.addCronTask(task, setting.getCron());
            }
            log.info("定时任务已加载完毕...");
        }
    }
}
