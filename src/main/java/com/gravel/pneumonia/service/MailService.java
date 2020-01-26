package com.gravel.pneumonia.service;

/**
 * @ClassName MailService
 * @Description: 邮件发送service
 * @Author gravel
 * @Date 2020/1/26
 * @Version V1.0
 **/

public interface MailService {

    /**
     * 邮件发送方法
     * @param from
     * @param to
     * @param topic
     * @param content
     */
    public void sendMail(String from,String[] to,String topic,String content);

    /**
     * 使用系统默认配置发送
     * @param topic
     * @param content
     */
    public void sendMail(String topic,String content);
}
