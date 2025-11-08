package com.xhh.aiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Test
    void testChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "破解";
        LoveApp.LoveReport report = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(report);
    }

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是蒋腾进";
        LoveApp.LoveReport report = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(report);
        // 第二轮
        message = "什么是健康的爱情关系";
        report = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(report);
        // 第三轮
        message = "我应该怎么做";
        report = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(report);
    }
}
