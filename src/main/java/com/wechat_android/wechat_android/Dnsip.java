package com.wechat_android.wechat_android;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;



public class Dnsip {
    // 登录返回-301自动切换DNS
    final Integer dns_retry_times = 3;

    // 短链接ip池
    List<String> short_ip = new ArrayList<String>();
    // 长链接ip池
    List<String> long_ip = new ArrayList<String>();

    public void get_ips() {
    /*访问'http://dns.weixin.qq.com/cgi-bin/micromsg-bin/newgetdns',
    返回短链接ip列表short_ip，长链接ip列表long_ip.
    */
        try {
            Document doc = Jsoup.connect("http://dns.weixin.qq.com/cgi-bin/micromsg-bin/newgetdns").get();
            Elements short_weixin = doc.select("domain[name=short.weixin.qq.com]").select("ip");
            for (Element ip : short_weixin) {
                short_ip.add(ip.text());
            }
            Elements long_weixin = doc.select("domain[name=long.weixin.qq.com]").select("ip");
            for (Element ip : long_weixin) {
                long_ip.add(ip.text());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String fetch_longlink_ip(){
        if(long_ip.isEmpty())
            get_ips();
        Random r = new Random(123);
        r.nextInt(long_ip.size());

        return long_ip.get(r.nextInt(long_ip.size()));
    }

    public String fetch_shortlink_ip(){
        if(short_ip.isEmpty())
            get_ips();
        Random r = new Random(123);
        r.nextInt(short_ip.size());

        return short_ip.get(r.nextInt(short_ip.size()));
    }






}
