package com.gravel.pneumonia.task;

import com.gravel.pneumonia.entity.LatestMessage;
import com.gravel.pneumonia.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName RssTask
 * @Description: 拉取tg频道 https://t.me/s/nCoV2019 的数据, 需要科学上网
 * @Author gravel
 * @Date 2020/1/26
 * @Version V1.0
 **/
@Slf4j
@EnableScheduling
@Component
public class TgMessageTask {

    @Autowired
    private MailService mailService;

    private String latestTopic = null;

    /**
     * 定义HTML标签的正则表达式
     */
    private static final String REG_EX_HTML = "<[^>]+>";

    private static final String CRAW_URL = "https://t.me/s/nCoV2019";

    /**
     * @throws IOException
     */
    @Scheduled(cron = "${send.mail.cron}")
    public void scheduledTgTask() throws IOException {
        LatestMessage latestMessage = this.getLatestMessage();
        // 如果没有新的数据
        if (!latestMessage.isHadNewPost()) {
            return;
        }
        mailService.sendMail(latestMessage.getTopic(), latestMessage.getHtml());
    }


    /**
     * 获取RSS 中最新的一条数据
     *
     * @return
     * @throws IOException
     */
    private LatestMessage getLatestMessage() throws IOException {

        Document doc = Jsoup.connect(CRAW_URL).get();
        log.info("开始抓取 tg 的数据--》{}",CRAW_URL);
        Element latestItem = doc.select("div.tgme_widget_message_wrap").last();
        LatestMessage latestMessage = new LatestMessage();
        String content = latestItem.select("div.tgme_widget_message_text").html();
        String topic = getRegTopic(content);
        log.info(topic);
        if (topic.equals(this.latestTopic)) {
            log.info("tg 频道没有新的数据！");
            latestMessage.setHadNewPost(false);
            return latestMessage;
        }
        this.latestTopic = topic;

        latestMessage.setHtml(content);
        latestMessage.setTopic(topic);
        latestMessage.setHadNewPost(true);

        return latestMessage;
    }


    private String getRegTopic(String str) {

        int start = str.indexOf("【");
        int end = str.indexOf("】") + 1;
        str = str.substring(start, end);
        Pattern pattern = Pattern.compile(REG_EX_HTML, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        // 过滤html标签
        return matcher.replaceAll("");
    }
}
