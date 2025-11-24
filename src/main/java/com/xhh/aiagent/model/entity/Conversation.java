package com.xhh.aiagent.model.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 对话
 *
 * @TableName conversation
 */
@Data
public class Conversation implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 对话名称
     */
    private String name;

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}