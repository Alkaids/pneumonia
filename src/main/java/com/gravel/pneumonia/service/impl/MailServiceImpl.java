package com.gravel.pneumonia.service.impl;

import com.gravel.pneumonia.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;

/**
 * @ClassName MailServiceImpl
 * @Description: TODO
 * @Author gravel
 * @Date 2020/1/26
 * @Version V1.0
 **/
@Slf4j
@Service
public class MailServiceImpl implements MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${send.mail.to}")
    private String[] sendMailTos;

    /**
     * 邮件发送方法
     *
     * @param from
     * @param to
     * @param topic
     * @param content
     */
    @Override
    public void sendMail(String from, String[] to, String topic, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(topic);
            helper.setText(content, true);
            helper.setFrom(from);
            log.info("from: {}", from);
            log.info("to: {}", Arrays.toString(to));
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("发送失败：----> {0}", e);
        }
    }

    /**
     * 使用系统默认配置发送
     *
     * @param topic
     * @param content
     */
    @Override
    public void sendMail(String topic, String content) {
        sendMail(this.from, this.sendMailTos, topic, content);
    }
}
