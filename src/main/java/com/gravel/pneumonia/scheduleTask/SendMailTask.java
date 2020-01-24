package com.gravel.pneumonia.scheduleTask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName SendMailTask
 * @Description: 发送邮件
 * @Author gravel
 * @Date 2020/1/24
 * @Version V1.0
 **/
@Component
@EnableScheduling
@Slf4j
public class SendMailTask {

    @Autowired
    private JavaMailSender mailSender;


    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${send.mail.to}")
    private String[] sendMailTos;

    /**
     * 抓取页面地址
     */
    private static final String CRAW_URL = "https://3g.dxy.cn/newh5/view/pneumonia_peopleapp";
    /**
     * 最新数据时间
     */
    private Long latestTime = null;

    private AtomicInteger modCount = new AtomicInteger(0);

    /**
     * 日期格式化
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    private static final ZoneOffset ZONE_OFFSET = ZoneOffset.of("+8");

    /**
     * 定时发送 HTML 文本邮件
     *
     * @throws javax.mail.MessagingException
     */
    @Scheduled(cron = "${send.mail.cron}")
    public void sendMail() throws MessagingException, IOException {
        // 开始时间
        long startMilliSecond = LocalDateTime.now().toInstant(ZONE_OFFSET).toEpochMilli();
        JSONObject htmlData = crawAllPenumouiaMessage();
        if (!htmlData.getBoolean("hadNewPost")) {
            return;
        }
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(sendMailTos);
        helper.setSubject(htmlData.getString("topic"));
        helper.setText(htmlData.getString("html"), true);
        helper.setFrom(from);
        log.info("开始发送--->第「{}」次发送。。。", modCount.incrementAndGet());
        log.info("from: {}", from);
        log.info("to: {}", sendMailTos);
        mailSender.send(message);
        log.info("发送成功,本次耗时：{} 毫秒！",LocalDateTime.now().toInstant(ZONE_OFFSET).toEpochMilli() - startMilliSecond);
    }

    /**
     * 从丁香园抓取疫情数据
     *
     * @return
     */

    public JSONObject crawAllPenumouiaMessage() throws IOException {
        // 返回的数据
        JSONObject res = new JSONObject();
        log.info("开始抓取丁香园数据：{}", CRAW_URL);
        Document doc = Jsoup.connect(CRAW_URL).get();
        // 由于页面比较特殊，我直接解析页面JS 中的数据
        String statisticsData = doc.getElementById("getStatisticsService").html();
        String timelineData = doc.getElementById("getTimelineService").html();

        /**
         * 头部数据
         * {
         * "id":1,
         * "createTime":1579537899000,"modifyTime":1579841872000,
         * "infectSource":"野生动物，可能中华菊头蝠",
         * "passWay":"未完全掌握，存在人传人、医务人员感染、一定范围社区传播",
         * "imgUrl":"https://img1.dxycdn.com/2020/0123/733/3392575782185696736-73.jpg",
         * "dailyPic":"","summary":"","deleted":false,
         * "countRemark":"全国 确诊 881 例 疑似 1073 例 治愈 34 例 死亡 26 例",
         * "virus":"新型冠状病毒 2019-nCoV",
         * "remark1":"病毒是否变异：存在可能",
         * "remark2":"疫情是否扩散：是","remark3":"","remark4":"","remark5":""
         * }
         */
        JSONObject statisticsJSON = JSON.parseObject(subStr(statisticsData, "= {"));
        log.info("头部数据抓取成功 ：{}", statisticsJSON.toJSONString());

        /**
         * 最新数据
         * {
         * "id":178,
         * "pubDate":1579841037000,
         * "pubDateStr":"13分钟前",
         * "title":"宁夏新增确诊1例疑似1例",
         * "summary":"昨天0-24时，宁夏报告新型冠状病毒感染的肺炎新增确诊病例1例，新增疑似病例1例。截至1月23日24时，宁夏累计报告新型冠状病毒感染的肺炎确诊病例2例(重症病例1例)，其中银川市1例、中卫市1例；疑似病例1例。",
         * "infoSource":"央视新闻",
         * "sourceUrl":"http://m.weibo.cn/2656274875/4464239232497670",
         * "provinceId":"64","provinceName":"宁夏回族自治区","createTime":1579841835000,"modifyTime":1579841835000}
         */
        JSONObject timelineDataJSON = JSON.parseArray(subStr(timelineData, "= [")).getJSONObject(0);
        log.info("最新数据抓取成功 ：{}", timelineDataJSON.toJSONString());

        Long pubDate = timelineDataJSON.getLong("pubDate");
        // 判断 是否需要重新发送
        if (this.latestTime != null && this.latestTime.equals(pubDate)) {
            res.put("hadNewPost", false);
            return res;
        }
        this.latestTime = pubDate;

        // 头部数据拼装
        List<String> statisticsList = new ArrayList<>();
        statisticsList.add("传染源: " + statisticsJSON.getString("infectSource"));
        statisticsList.add("病毒: " + statisticsJSON.getString("virus"));
        statisticsList.add("传播途径: " + statisticsJSON.getString("passWay"));
        statisticsList.add(statisticsJSON.getString("remark1"));
        statisticsList.add(statisticsJSON.getString("remark2"));

        // 邮件标题
        String topic = timelineDataJSON.getString("title");
        Context context = new Context();
        context.setVariable("topic", topic);
        context.setVariable("time", formatDate(timelineDataJSON.getDate("pubDate")));
        context.setVariable("countRemark", statisticsJSON.getString("countRemark"));
        context.setVariable("statisticsList", statisticsList);

        context.setVariable("summary", timelineDataJSON.getString("summary"));
        context.setVariable("source", timelineDataJSON.getString("infoSource"));
        context.setVariable("href", timelineDataJSON.getString("sourceUrl"));

        res.put("hadNewPost", true);
        res.put("html", templateEngine.process("latestMessage", context));
        res.put("topic", topic);
        return res;
    }

    private String formatDate(Date date) {
        return "截止：" + DATE_TIME_FORMATTER.format(LocalDateTime.ofInstant(date.toInstant(), ZONE_ID));
    }


    private String subStr(String str, String startReg) {
        int start = str.indexOf(startReg) + 2;
        int end = str.indexOf("}catch(e){}");
        return str.substring(start, end);
    }
}
