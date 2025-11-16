package com.xhh.aiagent.tool;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * qq 邮箱邮件发送工具
 */
@Slf4j
public class QQEmailSenderTool {

    // 邮件发件人
    private final String from;

    // 邮件发送器
    private final JavaMailSender javaMailSender;

    public QQEmailSenderTool(String from, JavaMailSender javaMailSender) {
        this.from = from;
        this.javaMailSender = javaMailSender;
    }

    /**
     * 发送简单文本邮件
     * 直接返回，不需要将工具执行结果添加到上下文
     *
     * @param to        接收人
     * @param subject   主题
     * @param text      内容
     */
    @Tool(description = "Send a simple text email", returnDirect = true)
    public void sendSimpleMessage(
            @ToolParam(description = "The email of recipient") String to,
            @ToolParam(description = "The email of subject") String subject,
            @ToolParam(description = "The email of content") String text){
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(this.from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            javaMailSender.send(message);
            log.info("Mail sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send mail to {}", to, e);
        }
    }

    /**
     * 发送 HTML 格式邮件
     * 直接返回，不需要将工具执行结果添加到上下文
     *
     * @param to        接收人
     * @param subject   主题
     * @param htmlBody  内容
     */
    @Tool(description = "Send an HTML format email", returnDirect = true)
    public void sendHtmlMessage(
            @ToolParam(description = "The email of recipient") String to,
            @ToolParam(description = "The email of subject") String subject,
            @ToolParam(description = "The email of content") String htmlBody){
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(this.from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true表示内容是HTML

            // 添加附件示例
            // helper.addAttachment("document.pdf", new File("path/to/file.pdf"));

            javaMailSender.send(message);
            log.info("Mail sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send mail to {}", to, e);
        }
    }

}
