package com.lenyan.lenaiagent.tools;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.core.io.FileSystemResource;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.util.Properties;

/**
 * 邮件发送工具演示类
 * 不依赖Spring，可直接运行
 */
public class EmailSendingToolDemo {

    public static void main(String[] args) {
        System.out.println("======== 邮件发送测试 ========");
        
        // 设置发件人和收件人邮箱
        String fromEmail = "xxx@qq.com";
        String toEmail = "xxx@qq.com";

        String password = "xxx";
        
        try {
            // 创建邮件发送器
            JavaMailSenderImpl mailSender = createMailSender(fromEmail, password);
            
            // 测试1：发送简单文本邮件
            System.out.println("\n1. 发送简单文本邮件...");
            String subject1 = "测试邮件 - 文本格式";
            String content1 = "你好，这是一封测试邮件！\n\n来自邮件发送工具的测试。";
            boolean success1 = sendSimpleTextEmail(mailSender, fromEmail, toEmail, subject1, content1);
            System.out.println(success1 ? "✅ 文本邮件发送成功" : "❌ 文本邮件发送失败");
            
            // 测试2：发送HTML邮件
            System.out.println("\n2. 发送HTML格式邮件...");
            String subject2 = "测试邮件 - HTML格式";
            String content2 = "<h1 style='color:blue;'>HTML邮件测试</h1>"
                    + "<p>这是一封<b>HTML格式</b>的测试邮件！</p>"
                    + "<p>来自<i>邮件发送工具</i>的测试。</p>"
                    + "<hr/>"
                    + "<p>发送时间：" + new java.util.Date() + "</p>";
            boolean success2 = sendHtmlEmail(mailSender, fromEmail, toEmail, subject2, content2);
            System.out.println(success2 ? "✅ HTML邮件发送成功" : "❌ HTML邮件发送失败");
            
            // 测试3：发送带附件的邮件
            System.out.println("\n3. 发送带附件的邮件...");
            String subject3 = "测试邮件 - 带附件";
            String content3 = "你好，这是一封带附件的测试邮件！\n\n请查看附件。";
            // 注意修改为您系统中实际存在的文件路径
            String attachmentPath = "D:/temp/test.txt";
            // 如果文件不存在，创建一个测试文件
            createTestFileIfNotExists(attachmentPath);
            boolean success3 = sendEmailWithAttachment(mailSender, fromEmail, toEmail, subject3, content3, attachmentPath);
            System.out.println(success3 ? "✅ 带附件邮件发送成功" : "❌ 带附件邮件发送失败");
            
            System.out.println("\n======== 测试完成 ========");
            
        } catch (Exception e) {
            System.err.println("❌ 邮件发送测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 创建邮件发送器
     */
    private static JavaMailSenderImpl createMailSender(String username, String password) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("smtp.qq.com");
        sender.setPort(465);
        sender.setUsername(username);
        sender.setPassword(password);
        
        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.debug", "false");  // 设置为false不输出调试信息
        
        return sender;
    }
    
    /**
     * 发送简单文本邮件
     */
    private static boolean sendSimpleTextEmail(JavaMailSenderImpl mailSender, 
                                            String fromEmail, 
                                            String toEmail, 
                                            String subject, 
                                            String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            System.err.println("发送文本邮件失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 发送HTML邮件
     */
    private static boolean sendHtmlEmail(JavaMailSenderImpl mailSender,
                                      String fromEmail,
                                      String toEmail,
                                      String subject,
                                      String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // 第二个参数true表示这是HTML
            
            mailSender.send(message);
            return true;
        } catch (MessagingException e) {
            System.err.println("发送HTML邮件失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 发送带附件的邮件
     */
    private static boolean sendEmailWithAttachment(JavaMailSenderImpl mailSender,
                                               String fromEmail,
                                               String toEmail,
                                               String subject,
                                               String content,
                                               String attachmentPath) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content);
            
            FileSystemResource file = new FileSystemResource(new File(attachmentPath));
            helper.addAttachment(file.getFilename(), file);
            
            mailSender.send(message);
            return true;
        } catch (MessagingException e) {
            System.err.println("发送带附件邮件失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 如果测试文件不存在，创建一个
     */
    private static void createTestFileIfNotExists(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                // 确保目录存在
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                
                // 创建文件并写入测试内容
                java.io.FileWriter writer = new java.io.FileWriter(file);
                writer.write("这是一个测试附件文件。\n创建时间: " + new java.util.Date());
                writer.close();
                System.out.println("已创建测试附件文件: " + filePath);
            }
        } catch (Exception e) {
            System.err.println("创建测试文件失败: " + e.getMessage());
        }
    }
} 