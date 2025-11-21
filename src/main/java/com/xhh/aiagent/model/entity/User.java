package com.xhh.aiagent.model.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 用户
 *
 * @TableName user
 */
@Data
public class User implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户角色: user/admin
     */
    private String userRole;

    /**
     * 用户简介
     */
    private String userProfile;

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