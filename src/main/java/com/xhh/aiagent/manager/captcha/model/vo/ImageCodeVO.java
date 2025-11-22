package com.xhh.aiagent.manager.captcha.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
public class ImageCodeVO implements Serializable {

    /**
     * key
     */
    private String key;

    /**
     * 验证码图片
     */
    private String img;

    private static final long serialVersionUID = 1L;
}
