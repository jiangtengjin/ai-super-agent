package com.xhh.aiagent.model.entity;

import cn.hutool.core.bean.BeanUtil;
import com.xhh.aiagent.model.vo.LoginUserVO;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

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


    /**
     * 将实体类转换为VO
     *
     * @param user
     * @return
     */
    public LoginUserVO objToVo(User user) {
        return BeanUtil.copyProperties(user, LoginUserVO.class);
    }
}