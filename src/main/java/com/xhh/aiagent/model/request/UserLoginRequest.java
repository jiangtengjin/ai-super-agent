package com.xhh.aiagent.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用于接收用户登录请求参数
 */
@Data
@NoArgsConstructor
public class UserLoginRequest implements Serializable {

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 图片验证码
     */
    private String imageCode;

    private String key;

    private static final long serialVersionUID = 1L;

}
