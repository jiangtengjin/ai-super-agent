package com.xhh.aiagent.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ConversationVO implements Serializable {

    /**
     * 对话名称
     */
    private String name;

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 创建时间
     */
    private Date createTime;


    private static final long serialVersionUID = 1L;
}
