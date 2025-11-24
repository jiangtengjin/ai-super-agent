# 数据库初始化
# @author <a href="https://github.com/jiangtengjin">xhh</a>

-- 创建库
create
    database if not exists ai_agent;

-- 切换库
use
    ai_agent;

-- 以下是建表语句
-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                       not null comment '账号: 邮箱',
    userPassword varchar(512)                       not null COMMENT '密码',
    userName     varchar(256)                       null comment '用户名',
    userAvatar   varchar(1024)                      null comment '用户头像',
    userRole     varchar(256)                       null comment '用户角色: user/admin',
    userProfile  varchar(512)                       null comment '用户简介',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null comment '更新时间',
    editTime     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '编辑时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    INDEX idx_userName (userName),
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_createTime (createTime)
) comment '用户' collate = utf8mb4_unicode_ci;


-- 对话历史表
create table if not exists chat_history
(
    id             bigint auto_increment comment 'id' primary key,
    message        text                               not null comment '消息',
    conversationId varchar(64)                        not null COMMENT '会话ID',
    messageType    varchar(32)                        not null comment 'user/ai',
    createTime     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint  default 0                 not null comment '是否删除',
    INDEX idx_conversationId (conversationId), -- 提升基于会话ID的查询性能
    INDEX idx_createTime (createTime),         -- 提升基于时间的查询性能
    INDEX idx_id_createTime (id, createTime)   -- 游标查询核心索引
) comment '对话历史' collate = utf8mb4_unicode_ci;

-- 对话表
create table if not exists conversation
(
    id             bigint auto_increment comment 'id' primary key,
    name           varchar(64)                        not null comment '对话名称',
    conversationId varchar(64)                        not null COMMENT '会话ID',
    userId         bigint                             not null comment '用户ID',
    createTime     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime default CURRENT_TIMESTAMP not null comment '更新时间',
    editTime       datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '编辑时间',
    isDelete       tinyint  default 0                 not null comment '是否删除',
    INDEX idx_userId (userId),                -- 提升基于用户 ID 的查询性能
    INDEX idx_conversationId (conversationId) -- 提升基于对话 ID 的查询性能

) comment '对话历史' collate = utf8mb4_unicode_ci;

-- 添加索引，用于提升游标查询的效率
ALTER TABLE conversation
    ADD INDEX idx_id_create (id, createTime);