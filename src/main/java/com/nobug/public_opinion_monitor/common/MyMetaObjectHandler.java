package com.nobug.public_opinion_monitor.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.nobug.public_opinion_monitor.utils.JwtUtil;
import com.nobug.public_opinion_monitor.utils.constant.CommonConstants;
import io.jsonwebtoken.Claims;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 自定义mybatis-plus自动填充处理器
 *
 * @date：2023/2/20
 * @author：nobug
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", Date.class, new Date()); // 起始版本 3.3.0(推荐使用)
        this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date()); // 起始版本 3.3.0(推荐)
        this.strictInsertFill(metaObject, "deleteFlag", Integer.class, 0);

    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date()); // 起始版本 3.3.0(推荐)
    }
}
