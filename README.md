## pneumonia
抓取 [丁香园](https://3g.dxy.cn/newh5/view/pneumonia_peopleapp) 以及[nCoV2019](https://t.me/s/nCoV2019) 的实时疫情数据，并发送邮件提醒。

## 思路

### 丁香园
由于丁香园的这个页面，将所有的数据通过服务端渲染，直接写在了具体的 script 标签里面，所以直接从 js 文件解析实时数据， 不需要去遍历查找每个 html 的 tag. 执行效率要一点。
### telegram nCoV2019
抓取这个频道的数据需要科学上网。

### 其他
以上两个数据源都封装成HTML文本，然后写入模板文件，转换成字符，使用 spring mail 发送邮件。

## quickstart
按规则修改配置文件
```
# 这里按照spring mail 的配置就行，注意password是你的网易邮箱授权码
spring:
  mail:
    host: smtp.163.com
    username: xx@163.com
    password: xxxx
    default-encoding: UTF-8

send:
  mail:
    cron: 0/30 * * * * ? # 抓取频率
    to: xxx@gmail.com,xxxxx@qq.com # 需要发送的邮箱，逗号隔开
```
这里的抓取频率最好不要设置太快，避免给丁香园造成过多不必要的压力。

然后按照常规的springboot项目启动即可。

## 备注
为武汉加油！
