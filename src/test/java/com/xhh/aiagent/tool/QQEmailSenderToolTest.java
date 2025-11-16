package com.xhh.aiagent.tool;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class QQEmailSenderToolTest {

    @Value("${spring.mail.username}")
    private String from;

    @Resource
    private JavaMailSender javaMailSender;

    @Test
    void sendSimpleMessage() {
        new QQEmailSenderTool(from, javaMailSender).sendSimpleMessage(
                "jiangtengjin@jgsu.edu.cn",
                "test",
                "test");
    }

    @Test
    void sendHtmlMessage() {
        new QQEmailSenderTool(from, javaMailSender).sendHtmlMessage(
                "jiangtengjin@jgsu.edu.cn",
                "test",
                "<h1>test</h1>");
    }
}