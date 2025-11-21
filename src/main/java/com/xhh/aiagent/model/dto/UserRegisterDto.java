package com.xhh.aiagent.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用于接收用户注册请求参数
 */
@Data
@NoArgsConstructor
public class UserRegisterDto implements Serializable {

    // 账号
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String rePassword;

    /**
     * 邮箱验证码
     */
    private String code;

    /**
     * 邮箱验证码 key
     */
    private String key;

    private static final long serialVersionUID = 1L;

}
