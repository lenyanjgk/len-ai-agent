package com.lenyan.lenaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * 邮件发送工具类
 */
@Component
public class EmailSendingTool {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 发送简单文本邮件
     */
    @Tool(description = "Send a simple text email to recipient")
    public String sendSimpleEmail(
            @ToolParam(description = "Email address of the recipient") String to,
            @ToolParam(description = "Subject of the email") String subject,
            @ToolParam(description = "Text content of the email") String text
    ) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            return "Email sent successfully to: " + to;
        } catch (Exception e) {
            return "Error sending email: " + e.getMessage();
        }
    }
} 