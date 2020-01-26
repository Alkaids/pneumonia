package com.gravel.pneumonia.entity;

import lombok.Data;

/**
 * @ClassName latestMessage
 * @Description: TODO
 * @Author gravel
 * @Date 2020/1/26
 * @Version V1.0
 **/
@Data
public class LatestMessage {

    /**
     * 是否有新的消息
     */
    private boolean hadNewPost;

    /**
     * 邮件主题
     */
    private String topic;

    /**
     * 邮件正文，通过 thymeleaf 封装成HTML文本
     */
    private String html;

    /**
     * 清空对象
     */
    public void clear(){
        this.hadNewPost = false;
        this.topic = null;
        this.html = null;
    }
}
