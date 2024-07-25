package com.nobug.public_opinion_monitor.schedule;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.nobug.public_opinion_monitor.entity.WarningTask;
import com.nobug.public_opinion_monitor.service.WarningTaskService;
import com.nobug.public_opinion_monitor.utils.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Runnable接口实现类，用来执行指定bean里的方法
 *
 * @date：2023/2/24
 * @author：nobug
 */
@Slf4j
public class SchedulingRunnable implements Runnable{

    private String beanName;

    private String methodName;

    public SchedulingRunnable() {
    }

    public String getParams() {
        return params;
    }

    private String params;

    public SchedulingRunnable(String beanName, String methodName) {
        this(beanName, methodName, null);
    }

    public SchedulingRunnable(String beanName, String methodName, String params) {
        this.beanName = beanName;
        this.methodName = methodName;
        this.params = params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchedulingRunnable that = (SchedulingRunnable) o;
        if (params == null) {
            return beanName.equals(that.beanName) &&
                    methodName.equals(that.methodName) &&
                    that.params == null;
        }
        return Objects.equals(beanName, that.beanName) &&
                Objects.equals(methodName, that.methodName) &&
                Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        if (params == null) {
            return Objects.hash(beanName, methodName);
        }
        return Objects.hash(beanName, methodName, params);
    }

    @Override
    public void run() {
        log.info("定时任务开始执行 - bean：{}，方法：{}，参数：{}", beanName, methodName, params);
        long startTime = System.currentTimeMillis();

        try {
            Object target = SpringContextUtils.getBean(beanName);

            Method method = null;
            if (!StringUtils.isEmpty(params)) {
                method = target.getClass().getDeclaredMethod(methodName, String.class);
            } else {
                method = target.getClass().getDeclaredMethod(methodName);
            }

            ReflectionUtils.makeAccessible(method);
            if (!StringUtils.isEmpty(params)) {
                method.invoke(target, params);
            } else {
                method.invoke(target);
            }
        } catch (Exception ex) {
            WarningTaskService warningTaskService = SpringContextUtils.getBean(WarningTaskService.class);
            CronTaskRegistrar cronTaskRegistrar = SpringContextUtils.getBean(CronTaskRegistrar.class);
            //更新warning_task表状态为异常
            LambdaUpdateWrapper<WarningTask> luw = new LambdaUpdateWrapper();
            luw.eq(WarningTask::getTaskId, Long.valueOf(params))
                    .set(WarningTask::getStatus, -1);
            warningTaskService.update(null, luw);
            log.error(String.format("定时任务执行异常 - bean：%s，方法：%s，参数：%s ", beanName, methodName, params), ex);
            //移除任务
            SchedulingRunnable task = new SchedulingRunnable(beanName, methodName, params);
            cronTaskRegistrar.removeCronTask(task);
        }

        long times = System.currentTimeMillis() - startTime;
        log.info("定时任务执行结束 - bean：{}，方法：{}，参数：{}，耗时：{} 毫秒", beanName, methodName, params, times);

    }
}
