CREATE DATABASE pomonitor;
USE pomonitor;


SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
-- ----------------------------
--  Table structure for `user`
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `telephone` varchar(255) DEFAULT NULL COMMENT '手机号',
  `password` varchar(255) DEFAULT NULL COMMENT '密码',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
  `end_login_time` datetime DEFAULT NULL COMMENT '最后登陆时间',
  `status` int(1) DEFAULT '1' COMMENT '状态（1代表正常 2代表注销）',
  `username` varchar(255) DEFAULT NULL COMMENT '用户姓名',
  `wechat_number` varchar(255) DEFAULT NULL COMMENT '微信号',
  `openid` varchar(255) DEFAULT NULL COMMENT 'openid',
  `login_count` int(11) DEFAULT '0' COMMENT '登录次数',
  `identity` int(1) DEFAULT NULL COMMENT '身份标识',
  `organization_id` bigint(20) DEFAULT NULL COMMENT '所属机构id',
  `user_type` int(1) DEFAULT NULL COMMENT '用户类型(0普通用户,1渠道商,2渠道专员,3管理员)',
  `user_level` int(1) DEFAULT NULL COMMENT '用户等级',
  `wechatflag` int(1) DEFAULT NULL COMMENT '微信绑定状态（1代表绑定 0代表捆绑）',
  `is_online` int(1) DEFAULT NULL COMMENT '是否在线',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `delete_flag` int(1) DEFAULT '0' COMMENT '是否删除(1是，0否)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniqu_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
--  Records of `user`
-- ----------------------------
BEGIN;
INSERT INTO `user` VALUES
(null, '13900000000', '13900000000', 'e10adc3949ba59abbe56e057f20f883e', '13900000000@qq.com', '2022-02-14 17:30:25', '1', null, null, null, '2', null, null, '3', null, null, '1', '2022-02-14 17:22:13', '2022-02-14 17:22:13',0),
(null, '13900000001', '13900000001', 'e10adc3949ba59abbe56e057f20f883e', '13900000001@qq.com', '2022-02-14 17:30:25', '1', null, null, null, '2', null, null, '3', null, null, '1', '2022-02-14 17:22:13', '2022-02-14 17:22:13',0);
COMMIT;


-- ----------------------------
--  Table structure for `user_apply`
-- ----------------------------
DROP TABLE IF EXISTS `user_apply`;
CREATE TABLE `user_apply` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `openid` varchar(50) DEFAULT NULL COMMENT 'openid',
  `name` varchar(255) DEFAULT NULL COMMENT '姓名',
  `telephone` varchar(255) DEFAULT NULL COMMENT '手机号码',
  `industry` varchar(255) DEFAULT NULL COMMENT '行业',
  `company` varchar(255) DEFAULT NULL COMMENT '公司',
  `applytime` datetime DEFAULT NULL,
  `dealstatus` int(1) DEFAULT '0' COMMENT '处理状态',
  PRIMARY KEY (`id`),
  UNIQUE KEY `openid` (`openid`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
--  Table structure for `user_log`
-- ----------------------------
DROP TABLE IF EXISTS `user_log`;
CREATE TABLE `user_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `article_public_id` varchar(50) DEFAULT NULL COMMENT '日志id',
  `method_name` varchar(50) DEFAULT NULL COMMENT '方法名称',
  `module_name` varchar(50) DEFAULT NULL COMMENT '模块名称',
  `submodule_name` varchar(255) DEFAULT NULL COMMENT '子模块名称',
  `times` datetime DEFAULT NULL COMMENT '开始时间',
  `timee` datetime DEFAULT NULL COMMENT '结束时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `username` varchar(255) DEFAULT NULL COMMENT '用户名称',
  `organization_id` bigint(20) DEFAULT NULL COMMENT '组织id',
  `organization_name` varchar(255) DEFAULT NULL COMMENT '组织名称',
  `status` int(1) DEFAULT NULL COMMENT '用户状态',
  `parameters` text COMMENT '请求参数',
  `class_name` varchar(255) DEFAULT NULL COMMENT '类名',
  `module_id` int(11) DEFAULT NULL COMMENT '模块id',
  `submodule_id` int(11) DEFAULT NULL COMMENT '子模块id',
  `operation` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniqu_user_id` (`user_id`) USING BTREE,
  UNIQUE KEY `uniqu_article_public_id` (`article_public_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
--  Table structure for `warning_task`
-- ----------------------------
DROP TABLE IF EXISTS `warning_task`;
CREATE TABLE `warning_task` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `task_id` bigint(20) DEFAULT NULL COMMENT '任务id',
  `task_name` varchar(255) DEFAULT NULL COMMENT '任务名称',
  `description` varchar(255) DEFAULT NULL COMMENT '任务描述',
  `keyword` varchar(255) DEFAULT NULL COMMENT '关键词',
  `ignoreword` varchar(255) DEFAULT NULL COMMENT '屏蔽词',
  `location` varchar(255) DEFAULT NULL COMMENT '位置',
  `sentiment` int(1) DEFAULT NULL COMMENT '情感(-1负面，0中性，1正面)',
  `frequency` int(4) DEFAULT NULL COMMENT '预警频次',
  `hot_index` int(11) DEFAULT NULL COMMENT '预警阈值',
  `auto_run` int(1) DEFAULT NULL COMMENT '自动运行(1是，0否)',
  `emails` varchar(255) DEFAULT NULL COMMENT '邮箱',
  `status` int(1) DEFAULT NULL COMMENT '任务状态(1正在运行，0未启动，-1异常)',
  `delete_flag` int(1) DEFAULT 0 COMMENT '是否删除(1是，0否)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniqu_task_id` (`task_id`) USING BTREE,
  KEY `key_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
--  Table structure for `scheduled`
-- ----------------------------
DROP TABLE IF EXISTS `schedule_setting`;
CREATE TABLE `schedule_setting` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `task_id` bigint(20) DEFAULT NULL COMMENT '任务id',
  `bean_name` varchar(127) NOT NULL COMMENT 'bean名称',
  `method_name` varchar(127) NOT NULL COMMENT '方法名称',
  `method_params` varchar(127) DEFAULT NULL COMMENT '方法参数',
  `name` varchar(127) DEFAULT NULL COMMENT '任务名称',
  `cron` varchar(63) NOT NULL COMMENT '任务表达式',
  `status` int(2) DEFAULT '0' COMMENT '状态(0.禁用; 1.启用)',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `key_bean_name` (`bean_name`) USING BTREE,
  UNIQUE KEY `uniqu_task_id` (`task_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='定时任务配置表';

INSERT INTO `schedule_setting`
VALUES
(1, null, 'dayStatScheduleTask', 'dayStatRun', null, '定时更新每日数据', '59 59 23 * * ?', 1, '2023-03-11 20:30:00', '2023-03-11 20:30:00');

-- ----------------------------
--  Table structure for `hot`
-- ----------------------------
DROP TABLE IF EXISTS `hot`;
CREATE TABLE `hot` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL COMMENT '热搜标题',
  `hot` bigint(20) DEFAULT NULL COMMENT '热度',
  `created_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniqu_title` (`title`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='热搜表';

-- ----------------------------
--  Table structure for `mblog`
-- ----------------------------
DROP TABLE IF EXISTS `mblog`;
CREATE TABLE `mblog` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
	`mid` bigint(20) NOT NULL COMMENT '微博id',
  `topic` varchar(255) DEFAULT NULL COMMENT '话题',
	`text` tinytext  DEFAULT NULL COMMENT '博文',
	`created_time` bigint(20) NULL DEFAULT NULL COMMENT '创建时间',
	`created_time_text` varchar(255) DEFAULT NULL COMMENT '创建时间文本',
	`author` varchar(127) DEFAULT NULL COMMENT '作者',
	`comments_count` int(11) NOT NULL COMMENT '评论数',
  `reposts_count` int(11) NOT NULL COMMENT '转发数',
	`attitudes_count` int(11) NOT NULL COMMENT '点赞数',
	`source` varchar(127) DEFAULT NULL COMMENT '发布源',
	`location` varchar(127) DEFAULT NULL COMMENT '发布位置',
	`province` varchar(20) DEFAULT NULL COMMENT '省份',
  `link` varchar(255) DEFAULT NULL COMMENT '链接',
	`sentiment` int(1) NOT NULL COMMENT '情感倾向',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniqu_mid` (`mid`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='微博博文表';


-- ----------------------------
--  Table structure for `user_favorites`
-- ----------------------------
DROP TABLE IF EXISTS `user_favorites`;
CREATE TABLE `user_favorites` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `mid` bigint(20) NOT NULL COMMENT '微博id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `delete_flag` int(1) DEFAULT 0 COMMENT '是否删除(1是，0否)',
  PRIMARY KEY (`id`),
  KEY `key_user_mid` (`user_id`,`mid`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
--  Table structure for `stat_day`
-- ----------------------------
DROP TABLE IF EXISTS `stat_day`;
CREATE TABLE `stat_day` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `total` bigint(20) DEFAULT NULL COMMENT '日舆情数',
  `positive_count` bigint(20) NOT NULL COMMENT '日正面舆情数',
  `neuter_count` bigint(20) NOT NULL COMMENT '日中性舆情数',
  `negative_count` bigint(20) NOT NULL COMMENT '日负面舆情数',
  `comment_count` bigint(20) NOT NULL COMMENT '日评论数',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4;