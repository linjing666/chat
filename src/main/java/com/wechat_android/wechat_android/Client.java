package com.wechat_android.wechat_android;

import com.wechat_android.wechat_android.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.awt.Component.log;

@Slf4j

public class Client {
//    ioloop = ioloop; 未知类型
//    recv_cb = recv_cb;未知类型
    private final int HEARTBEAT_TIMEOUT = 60;
    private static final int CMDID_NOOP_REQ = 6;
    private final int HEARTBEAT_SEQ = 0xFFFFFFFF;
    private final int CMDID_IDENTIFY_REQ = 205;
    private final int IDENTIFY_SEQ = 0xFFFFFFFE;
    private static final int CMDID_MANUALAUTH_REQ = 253;
    private static final int CMDID_PUSH_ACK = 24;  // #推送通知
    private static final int PUSH_SEQ = 0;
    public static Socket sck;
    public static int UNPACK_NEED_RESTART = -2;
    public static int UNPACK_FAIL = -1;
    private String host;
    private int port = 443;
    private String usrName;
    private String passwd;
    private long last_heartbeat_time = 0;
    private int cnt = 0;
    private int seq = 1;
    private static String login_aes_key;
    private byte recv_data;
    private byte heartbeat_callback;
    public Client(Map obj){
        this.host = obj.get("host").toString();
        this.usrName = obj.get("usrName").toString();
        this.passwd = obj.get("passwd").toString();
    }
    private static byte[] listTobyte1(List<Byte> list) {
        if (list == null || list.size() < 0)
            return null;
        byte[] bytes = new byte[list.size()];
        int i = 0;
        Iterator<Byte> iterator = list.iterator();
        while (iterator.hasNext()) {
            bytes[i] = iterator.next();
            i++;
        }
        return bytes;
    }
    public byte[] pack(int cmd_id,byte[]buf){
        byte[] header1;
        if(buf==null){
            header1 = NumberUtil.intToByte4(0+16);
        }
        else{
            header1 = NumberUtil.intToByte4((buf).length+16);
        }
        byte[] header2 = {(byte) 0x00, (byte) 0x10, (byte) 0x00, (byte) 0x01};
        byte[] header3 = NumberUtil.intToByte4(cmd_id);
        byte[] heartByte;
        if (this.CMDID_NOOP_REQ == cmd_id){  //心跳包
            heartByte = NumberUtil.intToByte4(this.HEARTBEAT_SEQ);
        }
        else if (this.CMDID_IDENTIFY_REQ == cmd_id) {
            //登录确认包(暂未实现;该请求确认成功后服务器会直接推送消息内容,否则服务器只下发推送通知,需要主动同步消息)
            heartByte = NumberUtil.intToByte4(this.IDENTIFY_SEQ);
        }
        else {
            heartByte = NumberUtil.intToByte4(this.seq);

            //封包编号自增
            this.seq += 1;
        }
        List collect = new ArrayList();
        collect.add(header1);
        collect.add(header2);
        collect.add(header3);
        collect.add(heartByte);
        if (buf!=null) {
            collect.add(buf);
        }
        byte[] sum = Util.getListString(collect);

        return sum;
    }
    public boolean send_heart_beat(Socket sck){
        Boolean tf = false;
        if (new Date().getTime() - this.last_heartbeat_time >= this.HEARTBEAT_TIMEOUT) {
            byte[] sendData = this.pack(this.CMDID_NOOP_REQ, null);
            try {
                OutputStream out = sck.getOutputStream();
                out.write(sendData);
//                sck.shutdownOutput();
                this.last_heartbeat_time = new Date().getTime();
                tf = true;
            } catch (Exception e){
            }
        } else {
            tf = false;
        }
        return tf;
    }
    public void login(Socket sck) throws IOException, InterruptedException {
        Map obj = Business.login_req2buf("ccup524", "woaini123");
        byte[] httpStruct = (byte[]) (obj.get("httpStruct"));
        login_aes_key = obj.get("login_aes_key").toString();
        byte[] send_data = this.pack(this.CMDID_MANUALAUTH_REQ, httpStruct);
        OutputStream out = sck.getOutputStream();
        out.write(send_data);
        sck.shutdownOutput();

    }
    public void start(String userName, String pwd){
        try {
            String host = this.host;
            sck=new Socket("14.215.158.119",this.port);
            sck.isConnected();
            this.send_heart_beat(sck);
            this.login(sck);
            int len = 0;
            byte[] bytes1;
            while (len< 100) {
                InputStream inputStream = sck.getInputStream();
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                len = bytes.length;
                if (len > 100) {
                    bytes1 = bytes;
                    log.info(Arrays.toString(bytes1));
                    unpack(bytes1);
                }
            }

            if (sck.isConnected()) {
                log.info("成功建立链接");
            }
            sck.close();
        } catch (Exception e) {
            log.info("e:"+e);
        }
    }
    //    #长链接解包
    public Map unpack(byte[] bytes) {
        if (bytes.length < 16) {
            return null;

        } else {
            //按照>I4xII解包
            //len_ack
            byte[] header1 = new byte[4];
            System.arraycopy(bytes, 0, header1, 0, 4);
            int len_ack = ((((header1[0] & 0xff) << 24) | ((header1[1] & 0xff) << 16) | ((header1[2] & 0xff) << 8) | ((header1[3] & 0xff) << 0)));
            //cmd_id_ack
            byte[] header2 = new byte[4];
            System.arraycopy(bytes, 8, header2, 0, 4);
            int cmd_id_ack = ((((header2[0] & 0xff) << 24) | ((header2[1] & 0xff) << 16) | ((header2[2] & 0xff) << 8) | ((header2[3] & 0xff) << 0)));
            //seq_id_ack？是否小端的方式
            byte[] header3 = new byte[4];
            System.arraycopy(bytes, 12, header3, 0, 4);
            int seq_id_ack = ((((header3[3] & 0xff) << 24) | ((header3[2] & 0xff) << 16) | ((header3[1] & 0xff) << 8) | ((header3[0] & 0xff) << 0)));
            log.info("seq_id_ack:" + seq_id_ack);


            if (CMDID_PUSH_ACK == cmd_id_ack && PUSH_SEQ == seq_id_ack) {

            } else {
                int cmd_id = cmd_id_ack - 1000000000;
                if (cmd_id == CMDID_NOOP_REQ) { //心跳响应

                } else if (CMDID_MANUALAUTH_REQ == cmd_id) { // 登录响应
                    log.info("登录响应");
                    byte[] body = new byte[len_ack - 16];
                    System.arraycopy(bytes, 16, body, 0, len_ack - 16);
                    int code = Business.login_buf2Resp(body, login_aes_key);
                    if (code == -106) {
                            // 授权后,尝试自动重新登陆
                        log.info("授权后,尝试自动重新登陆中.....");
                        try {
                            this.login(sck); // 这里打个断点再跑 因为我不知道怎么写等到验证成功再往下执行
                        }catch (Exception E){

                        }
                    } else if (code == -301) {
                            // 不知道干嘛
                        log.info("正在重新登陆........................");
                        Map rt = new HashMap();
                        rt.put("UNPACK_NEED_RESTART", UNPACK_NEED_RESTART);
                        rt.put("bytes", null);
                        return rt;
                    } else {
                        log.info("登陆失败........................");
                        // 登录失败
                        Map rt = new HashMap();
                        rt.put("UNPACK_NEED_RESTART", UNPACK_FAIL);
                        rt.put("bytes", null);
                        return rt;
                    }
                }

                return null;

            }
        }
        return null;
    }
}
