package com.nobug.public_opinion_monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nobug.public_opinion_monitor.dao.StatDayDao;
import com.nobug.public_opinion_monitor.entity.StatDay;
import com.nobug.public_opinion_monitor.service.StatDayService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * StatDayService 实现类
 *
 * @date：2023/3/11
 * @author：nobug
 */
@Service
public class StatDayServiceImpl extends ServiceImpl<StatDayDao, StatDay> implements StatDayService {
    @Override
    public List<StatDay> getLately13() {
        LambdaQueryWrapper<StatDay> lqw = new LambdaQueryWrapper<>();
        lqw.orderByDesc(StatDay::getCreateTime);
        Page<StatDay> page = new Page<>(0, 13);
        Page<StatDay> statDayPage = page(page, lqw);
        return statDayPage.getRecords();
    }
}
