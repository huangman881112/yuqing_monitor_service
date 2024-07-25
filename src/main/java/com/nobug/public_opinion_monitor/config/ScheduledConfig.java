package com.nobug.public_opinion_monitor.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 定时任务线程池配置
 *
 * @date：2023/2/24
 * @author：nobug
 */
@Configuration
public class ScheduledConfig {

    @Autowired
    private ScheduledPoolProperties scheduledPoolProperties;

    @Bean
    public TaskExecutor taskExecutor(){

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //设置核心线程数
        executor.setCorePoolSize(scheduledPoolProperties.getExecutorConfig().getCorePoolSize());
        //设置最大线程数
        executor.setMaxPoolSize(scheduledPoolProperties.getExecutorConfig().getMaxPoolSize());
        //设置阻塞队列容
        executor.setQueueCapacity(scheduledPoolProperties.getExecutorConfig().getQueueCapacity());
        //设置救急线程存活时间（秒）
        executor.setKeepAliveSeconds(scheduledPoolProperties.getExecutorConfig().getKeepAliveTime());
        //设置默认线程名称(线程前缀名称，区分不同线程池之间的线程比如：taskExecutor-query-)
        executor.setThreadNamePrefix("taskExecutor-query-");
        //设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //设置允许核心线程超时，默认是false
        executor.setAllowCoreThreadTimeOut(false);
        //用来设置线程池关闭的时候等待所有任务都完成再继续销毁其他的Bean，这样这些异步任务的销毁就会先于Redis线程池的销毁。
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;

    }

}
