package com.lenyan.lenaiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * 邮件发送测试类
 */
@SpringBootTest
public class EmailSendTest {

    @Autowired
    private JavaMailSender mailSender;

    @Test
    public void testSendEmail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("3482565966@qq.com");  // 发件人
        message.setTo("13534758041@163.com");  // 收件人
        message.setSubject("测试邮件");  // 邮件标题
        message.setText("这是一封测试邮件，用于验证Spring Boot邮件配置是否正确。");  // 邮件内容

        mailSender.send(message);
        System.out.println("邮件发送成功！");
    }
}