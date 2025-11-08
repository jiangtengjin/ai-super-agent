# 数据库初始化
# @author <a href="https://github.com/jiangtengjin">xhh</a>

-- 创建库
create
    database if not exists ai_agent;

-- 切换库
use
ai_agent;

-- 对话历史表
create table chat_history
(
    id              bigint auto_increment comment 'id' primary key,
    message         text                               not null comment '消息',
    conversationId  varchar(64)                        NOT NULL COMMENT '会话ID',
    messageType     varchar(32)                        not null comment 'user/ai',
    createTime      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint  default 0                 not null comment '是否删除',
    INDEX idx_conversationId (conversationId),         -- 提升基于会话ID的查询性能
    INDEX idx_createTime (createTime),                  -- 提升基于时间的查询性能
    INDEX idx_appId_createTime (conversationId, createTime) -- 游标查询核心索引
) comment '对话历史' collate = utf8mb4_unicode_ci;

