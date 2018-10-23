package com.wechat_android.wechat_android;

import com.google.protobuf.ByteString;
import com.google.protobuf.TextFormat;
import com.wechat_android.wechat_android.util.Util;
import lombok.extern.slf4j.Slf4j;


import javax.validation.constraints.Null;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

import static com.wechat_android.wechat_android.util.Util.conver2HexStr;

@Slf4j
public class Business {
    public static Map login_req2buf(String userName, String pwd) throws IOException, InterruptedException {
        Map req2buf = new HashMap();
        JNAInterface jna = new JNAInterface();
        int Putl = jna.putl.getValue();
        try {
            // protobuf组包1
//            String login_aes_key = RandomStringUtils.randomAlphanumeric(16);
            String login_aes_key = "DamsiARfS7hzdUC0";

            ByteString BSlogin_aes_key = ByteString.copyFrom(login_aes_key.getBytes("utf8"));
            Login_zubao_pdy.ManualAuthAccountRequest.Builder accountRequest = Login_zubao_pdy.ManualAuthAccountRequest.newBuilder();
            accountRequest.setAes(Login_zubao_pdy.ManualAuthAccountRequest.AesKey.newBuilder()
                    .setLen(16)
                    .setKey(ByteString.copyFromUtf8(login_aes_key)));
            accountRequest.setEcdh(Login_zubao_pdy.ManualAuthAccountRequest.Ecdh.newBuilder()
                    .setNid(713)
                    .setEcdhKey(Login_zubao_pdy.ManualAuthAccountRequest.Ecdh.EcdhKey.newBuilder()
                            .setLen(Putl)
                            .setKey(ByteString.copyFrom(jna.Put))
                    )
            );
            accountRequest.setUserName(userName);
            accountRequest.setPassword1(CreateMD5.getMd5(pwd));
            accountRequest.setPassword2(CreateMD5.getMd5(pwd));
            Login_zubao_pdy.ManualAuthAccountRequest AccoutRequest = accountRequest.build();

            // protobuf组包2
            Login_zubao_pdy.ManualAuthDeviceRequest.Builder deviceRequest = Login_zubao_pdy.ManualAuthDeviceRequest.newBuilder();
            Login_zubao_pdy.LoginInfo.Builder login = Login_zubao_pdy.LoginInfo.newBuilder();
            Login_zubao_pdy.ManualAuthDeviceRequest._Tag2.Builder tag2 = Login_zubao_pdy.ManualAuthDeviceRequest._Tag2.newBuilder();
            login.setAesKey(BSlogin_aes_key);
            login.setUin(0);
            login.setGuid(Define.__GUID__);
            login.setClientVer(Define.__CLIENT_VERSION__);
            login.setAndroidVer(Define.__ANDROID_VER__);
            login.setUnknown(1);
            String imei = Define.__IMEI__;
            String softInfoXml = MessageFormat.format(Define.__SOFTINFO__,Define.__IMEI__, Define.__ANDROID_ID__, Define.__MANUFACTURER__+" "+Define.__MODELNAME__, Define.__MOBILE_WIFI_MAC_ADDRESS__,
                    Define.__CLIENT_SEQID_SIGN__, Define.__AP_BSSID__, Define.__MANUFACTURER__, "taurus", Define.__MODELNAME__, Define.__IMEI__);
            int unknown5 = 0;
            String clientSeqID = Define.__CLIENT_SEQID__;
            String clientSeqID_sign = Define.__CLIENT_SEQID_SIGN__;
            String loginDeviceName = Define.__MANUFACTURER__+" "+Define.__MODELNAME__;
            String deviceInfoXml = MessageFormat.format(Define.__DEVICEINFO__, Define.__MANUFACTURER__, Define.__MODELNAME__);
            String language = Define.__LANGUAGE__;
            String timeZone = "8.00";
            int unknown13 = 0;
            int unknown14 = 0;
            String deviceBrand = Define.__MANUFACTURER__;
            String deviceModel = Define.__MODELNAME__+"armeabi-v7a";
            String osType = Define.__ANDROID_VER__;
            String realCountry = "cn";
            int unknown22 = 2;  // # Unknown
            deviceRequest.setLogin(login);
            deviceRequest.setTag2(tag2);
            deviceRequest.setImei(imei);
            deviceRequest.setSoftInfoXml(softInfoXml);
            deviceRequest.setUnknown5(unknown5);
            deviceRequest.setClientSeqID(clientSeqID);
            deviceRequest.setClientSeqIDSign(clientSeqID_sign);
            deviceRequest.setLoginDeviceName(loginDeviceName);
            deviceRequest.setDeviceInfoXml(deviceInfoXml);
            deviceRequest.setLanguage(language);
            deviceRequest.setTimeZone(timeZone);
            deviceRequest.setUnknown13(unknown13);
            deviceRequest.setUnknown14(unknown14);
            deviceRequest.setDeviceBrand(deviceBrand);
            deviceRequest.setDeviceModel(deviceModel);
            deviceRequest.setOsType(osType);
            deviceRequest.setRealCountry(realCountry);
            deviceRequest.setUnknown22(unknown22);
            // 加密
//            Login_zubao_pdy.ManualAuthAccountRequest AccoutRequest = maar.build();
            byte[] reqAccount = Util.compress_and_rsa(AccoutRequest.toByteArray());
            Login_zubao_pdy.ManualAuthDeviceRequest Device = deviceRequest.build();
            byte[] reqdevice = Util.compress_and_aes(Device.toByteArray(), login_aes_key);
            // 封包包体

            byte[] newByte1 = NumberUtil.intToByte4(AccoutRequest.toByteArray().length);
            byte[] newByte2 = NumberUtil.intToByte4(Device.toByteArray().length);
            byte[] newByte3 = NumberUtil.intToByte4(reqAccount.length);
            List collect = new ArrayList();
            collect.add(newByte1);
            collect.add(newByte2);
            collect.add(newByte3);
            byte[] newByte = Util.getListString(collect);

            byte[] body = new byte[reqAccount.length + newByte.length];
            System.arraycopy(newByte, 0, body, 0, newByte.length);
            System.arraycopy(reqAccount, 0, body, newByte.length, reqAccount.length);
            byte[] body1 = new byte[body.length + reqdevice.length]; // 此处body1才是正确的
            System.arraycopy(body, 0, body1, 0, body.length);
            System.arraycopy(reqdevice, 0, body1, body.length, reqdevice.length);
            // 封包包头
            byte[] header = SocketHeader(body1);
            byte[] senddata = new byte[body1.length + header.length];
            System.arraycopy(header, 0, senddata, 0, header.length);
            System.arraycopy(body1, 0, senddata, header.length, body1.length);

            req2buf.put("httpStruct", senddata);

            req2buf.put("login_aes_key", login_aes_key);
        }catch (Exception error) {
            log.info(error.getMessage());
        }
        return req2buf;
    }
    public   static  byte[] SocketHeader(byte[] body){
        byte[] header1 = {(byte)0};
        byte[] header2 = {(byte) ((0x7 << 4) + 0xf)};
        byte[] header3 = NumberUtil.intToByte4(Define.__CLIENT_VERSION__);
        byte[] header4 = {(byte)0, (byte)0, (byte)0, (byte)0};
        byte[] header5 = {(byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0};
        byte[] header7 = Util.encodeVarint(701);
        byte[] header8 = Util.encodeVarint(body.length);
        byte[] header9 = Util.encodeVarint(body.length);
        byte[] header10 = {(byte) Define.__LOGIN_RSA_VER__};
        byte[] header11 = {(byte)1, (byte)2};
        List collect = new ArrayList();
        collect.add(header1);
        collect.add(header2);
        collect.add(header3);
        collect.add(header4);
        collect.add(header5);
        collect.add(header7);
        collect.add(header8);
        collect.add(header9);
        collect.add(header10);
        collect.add(header11);
        byte[] header = Util.getListString(collect);
        header[0] = (byte) ((header.length << 2) + 2);
        return  header;
    }

    public static int login_buf2Resp(byte[] bytes, String key) {
        try {
            Login_zubao_pdy.ManualAuthResponse loginRes = Login_zubao_pdy.ManualAuthResponse.parseFrom(UnPack(bytes, key));
            //# 更新DNS信息不会写
            // # 登录异常处理
            int code = loginRes.getResult().getCode();
            if (code == -301) {
                log.info("切换dns尝试重新登录");
            } else if (code == -106) {
                log.info("请在浏览器授权后重新登陆!");
                String url = "";
                int start = loginRes.getResult().getErrMsg().getMsg().indexOf("<Url><![CDATA[") + "<Url><![CDATA[".length();
                int end = loginRes.getResult().getErrMsg().getMsg().indexOf("]]></Url>");
                url =  loginRes.getResult().getErrMsg().getMsg().substring(start, end);
                Util.openIE(url);
            } else if(code == 0){
                //这里应该是登录成功了 其余还没写
                // # 密钥协商
//                Util.sessionKey = Util.aesDecrypt(loginRes.getAuthParam().getSession().getKey(), Util.DoEcdh(loginRes.getAuthParam().getEcdh().getEcdhKey().getKey()));
                //         # 保存uin/wxid
                //         # 初始化db

                log.info("登录成功");
            }else {
                String err;
                int start = loginRes.getResult().getErrMsg().getMsg().indexOf("<Content><![CDATA[") + "<Content><![CDATA[".length();
                int end = loginRes.getResult().getErrMsg().getMsg().indexOf("<Content><![CDATA[");
                err =  "code:" + code + "登陆结果:" + loginRes.getResult().getErrMsg().getMsg().substring(start, end);
                log.info(err);
            }
            return code;
        } catch (Exception e) {
            log.info(e.getMessage());
            return -1;
        }
    }
    public  static byte[] UnPack(byte[] bytes, String key) {
        if (bytes.length < 0x20) {
            return null; // 协议需要更新
        }
        if (key == null) {
            key = Util.sessionKey;
        }
        int nCur = 0;
        byte[] header1 = {(byte) 0x00,(byte) 0x00, (byte) 0x00, (byte) 0xbf };
        int len_ack = ((((header1[0] & 0xff) << 24) | ((header1[1] & 0xff) << 16) | ((header1[2] & 0xff) << 8) | ((header1[3] & 0xff) << 0)));
        if (bytes[nCur] == len_ack) {
            nCur = nCur + 1;
        }
        int nLenHeader = (bytes[nCur] & 0xff) >> 2; // java 只能表示127的数
        boolean bUseCompressed = (bytes[nCur] & 0x3) == 1; //# 包体是否使用压缩算法:01使用,02不使用
        nCur += 1;
        int nDecryptType = bytes[nCur] >> 4; // 解密算法(固定为AES解密): 05 aes解密 / 07 rsa解密
        int nLenCookie = bytes[nCur] & 0xf;                                                              //# cookie长度
        nCur += 1;
        nCur += 4;                                                                                       //# 服务器版本(当前固定返回4字节0)
        byte[] uinbts = new byte[4];
        System.arraycopy(bytes, nCur, uinbts, 0, 4);
        int uin = ((((uinbts[0] & 0xff) << 24) | ((uinbts[1] & 0xff) << 16) | ((uinbts[2] & 0xff) << 8) | ((uinbts[3] & 0xff) << 0)));
        nCur += 4;
        byte[] cookie_temp = new byte[nLenCookie];
        System.arraycopy(bytes, nCur, cookie_temp, 0, nLenCookie);
        if(cookie_temp != null) {
            Util.cookie = cookie_temp;
        }
        nCur += nLenCookie;
        Map nCgiNCur = Util.decoderVarint(bytes, nCur);
        int nCgi = Integer.parseInt(nCgiNCur.get("decoder").toString(), 10);
        nCur = Integer.parseInt(nCgiNCur.get("nCur").toString(), 10);
        Map nLenProtobufNCur = Util.decoderVarint(bytes, nCur);
        int nLenProtobuf = Integer.parseInt(nLenProtobufNCur.get("decoder").toString(), 10);
        nCur = Integer.parseInt(nLenProtobufNCur.get("nCur").toString(), 10);
        Map nLenCompressedNCur = Util.decoderVarint(bytes, nCur);
        int nLenCompressed = Integer.parseInt(nLenCompressedNCur.get("decoder").toString(), 10);
        nCur = Integer.parseInt(nLenCompressedNCur.get("nCur").toString(), 10);
        //     # 对包体aes解密解压缩
        byte[] body = new byte[bytes.length - nLenHeader];
        System.arraycopy(bytes, nLenHeader, body, 0, bytes.length - nLenHeader);
        byte[] protobufData;
        if (bUseCompressed) {
            protobufData = Util.decompress_and_aesDecrypt(body, key);
        } else {
            protobufData = Util.aesDecrypt(body, key);
        }
        return protobufData;
    }
}
