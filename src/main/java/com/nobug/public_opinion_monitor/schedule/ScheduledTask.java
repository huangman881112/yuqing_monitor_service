package com.nobug.public_opinion_monitor.schedule;

import java.util.concurrent.ScheduledFuture;

/**
 * ScheduledFuture包装类
 *
 * @date：2023/2/24
 * @author：nobug
 */
public final class ScheduledTask {

    volatile ScheduledFuture<?> future;
    /**
     * 取消定时任务
     */
    public void cancel() {
        ScheduledFuture<?> future = this.future;
        if (future != null) {
            future.cancel(true);
        }
    }

}
