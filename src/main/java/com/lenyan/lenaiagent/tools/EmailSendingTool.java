package com.lenyan.lenaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.util.Properties;

/**
 * 邮件发送工具类
 */
@Component
public class EmailSendingTool {

    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:3482565966@qq.com}")
    private String fromEmail;
    
    @Value("${spring.mail.password:}")
    private String password;
    
    @Value("${spring.mail.host:smtp.qq.com}")
    private String host;
    
    @Value("${spring.mail.port:465}")
    private int port;
    
    /**
     * 初始化JavaMailSender
     */
    private void initMailSender() {
        if (mailSender == null) {
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            sender.setHost(host);
            sender.setPort(port);
            sender.setUsername(fromEmail);
            sender.setPassword(password);
            
            Properties props = sender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.debug", "false");
            
            mailSender = sender;
        }
    }

    /**
     * 发送简单文本邮件
     */
    @Tool(description = "Send a simple text email")
    public String sendTextEmail(
            @ToolParam(description = "Recipient email address") String toEmail, 
            @ToolParam(description = "Email subject") String subject, 
            @ToolParam(description = "Email content") String content) {
        
        try {
            initMailSender();
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            return "Text email sent successfully to " + toEmail;
        } catch (Exception e) {
            return "Failed to send email: " + e.getMessage();
        }
    }
    
    /**
     * 发送HTML邮件
     */
    @Tool(description = "Send an HTML email")
    public String sendHtmlEmail(
            @ToolParam(description = "Recipient email address") String toEmail, 
            @ToolParam(description = "Email subject") String subject, 
            @ToolParam(description = "HTML content") String htmlContent) {
        
        try {
            initMailSender();
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            return "HTML email sent successfully to " + toEmail;
        } catch (MessagingException e) {
            return "Failed to send HTML email: " + e.getMessage();
        }
    }
    
    /**
     * 发送带附件的邮件
     */
    @Tool(description = "Send an email with attachment")
    public String sendEmailWithAttachment(
            @ToolParam(description = "Recipient email address") String toEmail, 
            @ToolParam(description = "Email subject") String subject, 
            @ToolParam(description = "Email content") String content,
            @ToolParam(description = "File path of attachment") String attachmentPath) {
        
        try {
            initMailSender();
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content);
            
            FileSystemResource file = new FileSystemResource(new File(attachmentPath));
            helper.addAttachment(file.getFilename(), file);
            
            mailSender.send(message);
            return "Email with attachment sent successfully to " + toEmail;
        } catch (MessagingException e) {
            return "Failed to send email with attachment: " + e.getMessage();
        }
    }
} 