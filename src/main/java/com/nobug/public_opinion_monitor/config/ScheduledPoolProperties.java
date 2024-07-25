package com.nobug.public_opinion_monitor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 定时任务线程池属性配置
 *
 * @date：2023/2/24
 * @author：nobug
 */
@Data
@Component
@ConfigurationProperties(value = "scheduled-pool")
public class ScheduledPoolProperties {

    private ExecutorConfig executorConfig;

    @Data
    public static class ExecutorConfig{
        /**
         * 核心线程数
         */
        private int corePoolSize;
        /**
         * 最大线程数
         */
        private int maxPoolSize;
        /**
         * 任务队列容量（阻塞队列）
         */
        private int queueCapacity;
        /**
         * 线程空闲时间
         */
        private int keepAliveTime;

    }
}
