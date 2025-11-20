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
    void testChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "我喜欢一个女孩很久了，但是不知道怎么迈出这一步";
        String response = loveApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(response);
    }

    @Test
    void testChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "什么是舔狗";
        LoveApp.LoveReport report = loveApp.doChatWithStructuredOutput(message, chatId);
        Assertions.assertNotNull(report);
    }

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是蒋腾进";
        LoveApp.LoveReport report = loveApp.doChatWithStructuredOutput(message, chatId);
        Assertions.assertNotNull(report);
        // 第二轮
        message = "什么是健康的爱情关系";
        report = loveApp.doChatWithStructuredOutput(message, chatId);
        Assertions.assertNotNull(report);
        // 第三轮
        message = "我应该怎么做";
        report = loveApp.doChatWithStructuredOutput(message, chatId);
        Assertions.assertNotNull(report);
    }

    @Test
    void doChatWithTools() {
        testMessage("我想向喜欢的女生表白，帮我发送一篇表白邮件给她，邮箱地址是：jiangtengjin@jgsu.edu.cn");

//        // 测试联网搜索问题的答案
//        testMessage("周末想带女朋友去深圳约会，推荐几个适合情侣的小众打卡地？");
//
//        // 测试网页抓取：恋爱案例分析
//        testMessage("最近和对象吵架了，去百度上看看其他情侣是怎么解决矛盾的？");
//
//        // 测试资源下载：图片下载
//        testMessage("直接下载一张适合做手机壁纸的星空情侣图片为文件");
//
//        // 测试终端操作：执行代码
//        testMessage("执行 Python3 脚本来生成数据分析报告");
//
//        // 测试文件操作：保存用户档案
//        testMessage("保存我的恋爱档案为文件");
//
//        // 测试 PDF 生成
//        testMessage("生成一份‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTool(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithMcp() {
        String chatId = UUID.randomUUID().toString();
        String message = "帮我找一些可爱小狗的图片";
        String answer = loveApp.doChatWithMcp(message, chatId);
        Assertions.assertNotNull(answer);
    }
}
