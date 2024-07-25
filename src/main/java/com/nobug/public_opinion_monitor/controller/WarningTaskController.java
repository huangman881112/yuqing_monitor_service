package com.nobug.public_opinion_monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nobug.public_opinion_monitor.common.GlobalException;
import com.nobug.public_opinion_monitor.common.R;
import com.nobug.public_opinion_monitor.dto.SearchDTO;
import com.nobug.public_opinion_monitor.entity.WarningTask;
import com.nobug.public_opinion_monitor.service.WarningTaskService;
import com.nobug.public_opinion_monitor.utils.JwtUtil;
import com.nobug.public_opinion_monitor.utils.constant.CommonConstants;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 预警任务Controller
 *
 * @date：2023/2/20
 * @author：nobug
 */
@RestController
@RequestMapping(value = "/warningtask")
@Slf4j
public class WarningTaskController {

    @Autowired
    private WarningTaskService warningTaskService;

    /**
     * 当warningTask中taskId字段为null时为新增，否则为修改
     * @param warningTask
     * @return
     */
    @PostMapping(value = "/saveorupdatewarningtask")
    public R saveOrUpdateWarningTask(@RequestBody WarningTask warningTask){
        try{
            //调用服务
            warningTaskService.saveOrUpdateTask(warningTask);
        }catch (Exception e) {
            throw new GlobalException("发生错误：" + e.getMessage());
        }
        return R.ok(null, "操作成功");
    }

    /**
     * 根据searchDTO分页查询任务列表
     * @param searchDTO
     * @return
     */
    @PostMapping(value = "/pagesearchwarningtask")
    public R<Page> pageSearchWarningTask(@RequestBody SearchDTO searchDTO){
        Page<WarningTask> res = null;
        try{
            res = warningTaskService.pageSearchTask(searchDTO);
        }catch (Exception e){
            throw new GlobalException("分页请求任务列表失败："+e.getMessage());
        }
        return R.ok(res);
    }

    /**
     * 根据taskIds字符串分割出id列表。批量删除任务(逻辑删除)
     * @param taskIds
     * @return
     */
    @DeleteMapping(value = "/deletetask")
    public R deleteTask(@RequestBody List<Long> taskIds){
        try{
            //1、校验解析参数
//            assert !StringUtils.isEmpty(taskIds);
//            log.info(taskIds);
//            List<Long> idList = Arrays.stream(taskIds.split(",")).map(id -> Long.valueOf(id)).collect(Collectors.toList());
            assert taskIds.size()>0;
            //2、调用服务批量删除
            warningTaskService.deleteBatchByTaskId(taskIds);
        }catch (Exception e){
            throw new GlobalException("删除任务失败："+e.getMessage());
        }
        return R.ok(null, "删除成功");
    }

    @GetMapping(value = "/changestatus")
    public R changeStatus(Long taskId, Integer status){
        try{
            //1、校验参数
            assert taskId!=null : "参数：taskId不能为空";
            assert status!=null : "参数：status不能为空";
            //2、调用服务
            warningTaskService.changeStatus(taskId, status);
        }catch (Exception e){
            throw new GlobalException("修改状态失败："+e.getMessage());
        }
        return R.ok(null, "操作成功");
    }


}
