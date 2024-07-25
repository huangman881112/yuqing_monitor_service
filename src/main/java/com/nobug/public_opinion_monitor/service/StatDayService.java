package com.nobug.public_opinion_monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nobug.public_opinion_monitor.entity.StatDay;

import java.util.List;

/**
 * StatDay Service层
 *
 * @date：2023/3/11
 * @author：nobug
 */
public interface StatDayService extends IService<StatDay> {
    List<StatDay> getLately13();
}
