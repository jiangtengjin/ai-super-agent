package com.xhh.aiagent.manager.captcha;

import cn.hutool.core.codec.Base64;
import com.google.code.kaptcha.Producer;
import com.xhh.aiagent.manager.captcha.constant.CaptchaConstant;
import com.xhh.aiagent.manager.captcha.model.vo.ImageCodeVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.util.FastByteArrayOutputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Component
@Slf4j
public class CaptchaManager {

    @Resource
    private Producer captchaProducer;

    @Resource
    private RedissonClient redisson;

    /**
     * 获取图片验证码
     * 注意：这个方法会产生异常，需要由调用者处理
     *
     * @return
     */
    public ImageCodeVO getImageCode() throws IOException {
        // 保存验证码信息,生成UUID并构建验证码键名
        String uuid = UUID.randomUUID().toString();
        String verifyKey = String.format("%s:%s", CaptchaConstant.CAPTCHA_CODE_KEY_PREFIX, uuid);
        String capStr = null, code = null;
        BufferedImage image = null;
        //字符型验证码
        capStr = code = captchaProducer.createText();
        image = captchaProducer.createImage(capStr);
        log.info("图形验证码生成成功：{}", code);
        //将生成的验证码存入Redis缓存，并设置过期时间
        redisson.getBucket(verifyKey).setIfAbsent(code, Duration.ofMinutes(5));
        // 转换流信息写出
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        ImageIO.write(image, "jpg", os);
        //将验证码图片转换为Base64编码格式,将UUID和Base64编码后的图片添加到响应对象并返回
        return ImageCodeVO.builder()
                .key(uuid)
                .img(Base64.encode(os.toByteArray()))
                .build();
    }

    /**
     * 获取缓存中的验证码
     *
     * @param key
     * @return
     */
    public String getCacheValue(String key){
        String cacheKey = String.format("%s:%s", CaptchaConstant.CAPTCHA_CODE_KEY_PREFIX, key);
        return (String) redisson.getBucket(cacheKey).get();
    }

}
