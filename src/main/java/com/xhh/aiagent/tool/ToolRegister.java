package com.xhh.aiagent.tool;

import jakarta.annotation.Resource;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * 工具注册器
 */
@Configuration
public class ToolRegister {

    @Value("${search.api_key}")
    private String apiKey;

    @Value("${spring.mail.username}")
    private String from;

    @Resource
    private JavaMailSender javaMailSender;

    @Bean
    public ToolCallback[] allTools(){
        // QQ 邮件发送工具
        QQEmailSenderTool qqEmailSenderTool = new QQEmailSenderTool(from, javaMailSender);
        // 文件操作工具
        FileOperationTool fileOperationTool = new FileOperationTool();
        // PDF 生成工具
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        // 资源下载工具
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        // 终端操作工具
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        // 网页爬取工具
        WebScrapTool webScrapTool = new WebScrapTool();
        // 网页搜索工具
        WebSearchTool webSearchTool = new WebSearchTool(apiKey);
        // 任务终止工具
        TerminateTool terminateTool = new TerminateTool();
        // 向人类寻求帮助
        AskHumanTool askHumanTool = new AskHumanTool();
        return ToolCallbacks.from(
                qqEmailSenderTool,
                fileOperationTool,
                pdfGenerationTool,
                resourceDownloadTool,
                terminalOperationTool,
                webScrapTool,
                webSearchTool,
                terminateTool,
                askHumanTool
        );
    }

}
