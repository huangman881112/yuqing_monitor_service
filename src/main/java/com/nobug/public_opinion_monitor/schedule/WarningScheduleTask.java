package com.nobug.public_opinion_monitor.schedule;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nobug.public_opinion_monitor.dto.SearchAnalysisDTO;
import com.nobug.public_opinion_monitor.entity.WarningTask;
import com.nobug.public_opinion_monitor.service.ESMblogService;
import com.nobug.public_opinion_monitor.service.WarningTaskService;
import com.nobug.public_opinion_monitor.service.impl.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;

/**
 * 预警定时任务
 *
 * @date：2023/2/24
 * @author：nobug
 */
@Slf4j
@Component("warningScheduleTask")
public class WarningScheduleTask {

    @Autowired
    private ESMblogService esMblogService;
    
    @Autowired
    private WarningTaskService warningTaskService;

    @Autowired
    private MailService mailService;

    public void warningRun(String param){
        Long taskId = Long.valueOf(param);
        WarningTask warningTask = warningTaskService.getOne(new LambdaQueryWrapper<WarningTask>().eq(WarningTask::getTaskId, taskId));
        //1、根据warningTask构建SearchAnalysisDTO
        SearchAnalysisDTO searchAnalysisDTO = new SearchAnalysisDTO();
        searchAnalysisDTO.setHotWord(warningTask.getKeyword());
        searchAnalysisDTO.setIgnoreWord(warningTask.getIgnoreword());
        searchAnalysisDTO.setLocation(warningTask.getLocation());
        searchAnalysisDTO.setSentiment(warningTask.getSentiment());
        //默认统计近三天的舆论数
        Date date = new Date();
        String startDate = DateUtil.formatDateTime(date);
        String endDate = DateUtil.formatDateTime(DateUtil.offsetDay(date, -3));
        searchAnalysisDTO.setStartDate(startDate);
        searchAnalysisDTO.setEndDate(endDate);
        //2、调用服务，根据SearchAnalysisDTO构建查询，取得舆情数量
        Long taskMonitorNum = esMblogService.getTaskMonitorNum(searchAnalysisDTO);
        //4、判断是否大于等于预警阈值，是则发送邮件
        Integer hotIndex = warningTask.getHotIndex();
        if(taskMonitorNum>=hotIndex){
            String emails = warningTask.getEmails();
            String[] emailArr = emails.split(",");
            log.info("预警数量：{}，向{}发送邮件了",taskMonitorNum, Arrays.toString(emailArr));
            for(String email: emailArr){
                String taskName = warningTask.getTaskName();
                Integer sentiment = warningTask.getSentiment();
                String sentimentStr = "";
                if(sentiment==1)
                    sentimentStr = "正面";
                else if(sentiment==0)
                    sentimentStr = "中性";
                else
                    sentimentStr = "负面";
                String subject = "预警任务："+taskName;
                String text = "尊敬的用户，温馨提示：\n您的预警任务："+taskName+
                        "，所监测"+sentimentStr+"舆情已达到："+taskMonitorNum+"，请登录系统查看！";
                mailService.sendTextMailMessage(email, subject, text);
            }
//            int i = 1/0;
        }else{
            log.info("未达到预警值");
        }
    }
}
