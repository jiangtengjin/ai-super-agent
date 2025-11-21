package com.xhh.aiagent.manager;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 发送邮箱验证码
 */
@Component
@Slf4j
public class CodeMailManager {

    // 发件人
    @Value("${spring.mail.username}")
    private String from;

    // 缓存 key 前缀
    private final String CACHE_KEY_PREFIX = "code:mail:";

    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private RedissonClient redisson;

    /**
     * 发送验证码到指定邮箱
     *
     * @param to    邮箱地址
     * @return      缓存 key
     */
    public String sendMail(String to) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(this.from);
            message.setTo(to);
            // 邮件主题
            String subject = "AI 超级智能体";
            message.setSubject(subject);
            // 邮件内容
            String code = generateCode(); // 验证码
            String text = String.format("您好，请以此验证码注册账号：%s，请勿将验证码告知他人。请在页面输入完成验证，5分钟之内有效，如非本人操作请忽略。", code);
            message.setText(text);
            javaMailSender.send(message);
            // 缓存到 Redis 中并设置超时时间为 5 分钟
            String key = UUID.randomUUID().toString();
            String cacheKey = CACHE_KEY_PREFIX + key;
            cacheCode(cacheKey, code);
            log.info("邮件成功发送，验证码：{}", code);
            return key;
        } catch (Exception e) {
            log.error("发送邮件失败", e);
            return null;
        }
    }

    /**
     * 将验证码缓存到 redis 中，并设置超时时间为 5 分钟
     *
     * @param cacheKey      缓存 key
     * @param cacheValue    缓存 value
     */
    private void cacheCode(String cacheKey, String cacheValue){
        redisson.getBucket(cacheKey).setIfAbsent(cacheValue, Duration.ofMinutes(5));
    }

    /**
     * 获取缓存中的验证码
     *
     * @param key        缓存 key
     * @return           验证码
     */
    public String getCacheValue (String key) {
        String cacheKey = CACHE_KEY_PREFIX + key;
        return (String) redisson.getBucket(cacheKey).get();
    }

    /**
     * 生成 6 位随机字符串
     *
     * @return  验证码
     */
    private String generateCode() {
        return RandomUtil.randomNumbers(6);
    }

}
