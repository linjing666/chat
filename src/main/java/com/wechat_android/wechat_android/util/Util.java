package com.wechat_android.wechat_android.util;

import com.google.protobuf.ByteString;
import com.wechat_android.wechat_android.AesCBC;
import com.wechat_android.wechat_android.Define;
import com.wechat_android.wechat_android.ZLibUtils;
import sun.misc.BASE64Encoder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Util {
    public static String __LOGIN_RSA_VER158_KEY_N__ = "E161DA03D0B6AAD21F9A4FB27C32A3208AF25A707BB0E8ECE79506FBBAF97519D9794B7E1B44D2C6F2588495C4E040303B4C915F172DD558A49552762CB28AB309C08152A8C55A4DFC6EA80D1F4D860190A8EE251DF8DECB9B083674D56CD956FF652C3C724B9F02BE5C7CBC63FC0124AA260D889A73E91292B6A02121D25AAA7C1A87752575C181FFB25A6282725B0C38A2AD57676E0884FE20CF56256E14529BC7E82CD1F4A1155984512BD273D68F769AF46E1B0E3053816D39EB1F0588384F2F4B286E5CFAFB4D0435BDF7D3AA8D3E0C45716EAD190FDC66884B275BA08D8ED94B1F84E7729C25BD014E7FA3A23123E10D3A93B4154452DDB9EE5F8DAB67";
    public static String __LOGIN_RSA_VER158_KEY_E__ = "65537";
    /**
     * qingqioutou
     *
     */
    public static Map headers (){
        Map header = new HashMap();
        Map headers = new HashMap();
        headers.put("Accept", "*/*");
        headers.put("Cache-Control", "no-cache");
        headers.put("Connection", "close");
        headers.put("Content-type", "application/octet-stream");
        headers.put("User-Agent", "MicroMessenger Client");
        return header;
    }
    /**
     * 好友类型
     */
    public static final int CONTACT_TYPE_ALL = 0xFFFF;  // # 所有好友
    public static final int CONTACT_TYPE_FRIEND = 1; // # 朋友
    public static final int CONTACT_TYPE_CHATROOM = 2; // # 群聊
    public static final int CONTACT_TYPE_OFFICAL = 4; // # 公众号
    public static final int CONTACT_TYPE_BLACKLIST = 8; // # 黑名单中的好友
    public static final int CONTACT_TYPE_DELETED = 16; // # 已删除的好友

    /**
     * ECDH key
     */
    public static ByteString EcdhPriKey;
    public static ByteString EcdhPubKey;
    /**
     * # session key(封包解密时的aes key/iv)
     */
    public static String sessionKey;
    /**
     * # cookie(登陆成功后返回,通常长15字节)
     */
    public static byte[] cookie;

    /**
     * # uin
     */
    public static int uin = 0;

    /**
     * # wxid
     */
    public static String wxid = "";

    /**
     * # sqlite3数据库
     */
//    public static int conn = Integer.parseInt(null);
    public static void util(){

    }
    /**利用MD5进行加密
     * @param str  待加密的字符串
     * @return  加密后的字符串
     * @throws NoSuchAlgorithmException  没有这种产生消息摘要的算法
     * @throws UnsupportedEncodingException
     */
    public static String EncoderByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定计算方法
        MessageDigest md5=MessageDigest.getInstance("MD5");
        BASE64Encoder base64en = new BASE64Encoder();
        //加密后的字符串
        String newstr=base64en.encode(md5.digest(str.getBytes("utf-8")));
        return newstr;
    }

    /**
     * 加密
     *
     * @param publicKey
     * @param srcBytes
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static byte[] encrypt(PublicKey publicKey, byte[] srcBytes) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (publicKey != null) {
            // Cipher负责完成加密或解密工作，基于RSA
            Cipher cipher = Cipher.getInstance("RSA");
            // 根据公钥，对Cipher对象进行初始化
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] resultBytes = cipher.doFinal(srcBytes);
            return resultBytes;
        }
        return null;
    }
    // 根据n、e值还原公钥
    public static PublicKey getPublicKey(String modulus, String publicExponent)

            throws NoSuchAlgorithmException, InvalidKeySpecException {

        BigInteger bigIntModulus = new BigInteger(new BigInteger(modulus,16).toString(10), 10);

        BigInteger bigIntPrivateExponent = new BigInteger(publicExponent,10);

        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(bigIntModulus, bigIntPrivateExponent);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        return publicKey;

    }
    /**
     * 先压缩后RSA加密
     * @param src
     * @return
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */
    public static byte[] compress_and_rsa (byte[] src) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {

        // 得到公钥

        byte[] compressData = ZLibUtils.compress(src);
        try {
            return encrypt(getPublicKey(__LOGIN_RSA_VER158_KEY_N__, __LOGIN_RSA_VER158_KEY_E__), compressData);
        } catch (Exception e) {
            byte[] a = {};
            return a;

        }
    }
    // ase加密压缩
    public static byte[] compress_and_aes (byte[] src, String key){
        byte[] compressData = ZLibUtils.compress(src);
        byte[] aes;
        int padding = 16 - (compressData.length % 16);
        byte[] bytePadding = new byte[padding];
        for (int i = 0; i < padding; i++) {
            bytePadding[i] = (byte) padding;
        }
        byte[] senddata = new byte[compressData.length + bytePadding.length];
        System.arraycopy(compressData, 0, senddata, 0, compressData.length);
        System.arraycopy(bytePadding, 0, senddata, compressData.length, bytePadding.length);
        try {
            aes = AesCBC.getInstance().encrypt(senddata, "utf-8", key, key);

        }catch (Exception e) {
            aes = new byte[0];
        }
        return aes;
    }
    // AES-128-CBC解密解压
    public static byte[] decompress_and_aesDecrypt(byte[] senddata, String key) {
        byte[] aes;
        try {
            aes = AesCBC.getInstance().decrypt(senddata, "utf-8", key, key);
            int len = aes.length - (aes[aes.length-1] & 0xFF);
            byte[] decryptaes = new byte[len];
            System.arraycopy(aes, 0, decryptaes, 0, len);
            byte[] decompressData = ZLibUtils.decompress(decryptaes);
            return decompressData;
        }catch (Exception e) {
            return null;
        }
    }
    // # AES-128-CBC解密
    public static byte[] aesDecrypt(byte[] senddata, String key){
        byte[] aes;
        try {
            aes = AesCBC.getInstance().decrypt(senddata, "utf-8", key, key);
            int len = aes.length - (aes[aes.length - 1] & 0xFF);
            byte[] decryptaes = new byte[len];
            System.arraycopy(aes, 0, decryptaes, 0, len);
            return decryptaes;
        }catch (Exception e){
            return null;
        }
    }
    public static byte[] encodeVarint(int value) {
        String result = Integer.toBinaryString(value);
        result = new StringBuffer(result).reverse().toString();
        String data = insertSplit(result,7,",");
        String[] arr = data.split(",");
        byte[] bytes = new byte[arr.length];
        for (int i = 0; i<arr.length; i++) {
            if (i < arr.length - 1) {
                arr[i] = "1" + new StringBuffer(arr[i]).reverse().toString();
            } else {
                arr[i] = new StringBuffer(arr[i]).reverse().toString();
            }
            System.out.println(arr[i]);
            System.out.println(Integer.parseInt(arr[i],2));
            bytes[i] = (byte) Integer.parseInt(arr[i],2);
        }
        return bytes;
    }
    public static Map decoderVarint(byte[] bytes, int index){
        Map req2buf = new HashMap();
        String str = conver2HexStr(bytes);
        String[] arr = str.split(",");
        boolean isStope = true;
        int sum = index;
        for (int i = index; isStope; i++){
            sum += 1;
            if (arr[i].split("").length<8) {
                isStope = false;
            }
        }
        byte[] varintBytes = new byte[sum - index];
        System.arraycopy(bytes, index, varintBytes, 0, sum - index);
        uncodeVarint(varintBytes);
        req2buf.put("decoder", uncodeVarint(varintBytes));
        req2buf.put("nCur", sum);
        return req2buf;
    }
    public static int uncodeVarint(byte[] bytes) {
        String str = conver2HexStr(bytes);
        String[] arr = str.split(",");

        for (int i = 0; i<arr.length; i++) {
            if (i < arr.length - 1) {
                arr[i] = arr[i].substring(1);
            }
        }
        for (int start = 0, end = arr.length - 1; start < end; start++, end--) {
            String temp = arr[end];
            arr[end] = arr[start];
            arr[start] = temp;
        }
        StringBuffer sb = new StringBuffer();
        for(int i = 0;i<arr.length;i++){
            sb.append(arr[i]);
        }
        String sb1 = sb.toString();
        return Integer.parseInt(sb1,2);
    }
    /**
     * byte数组转换为二进制字符串,每个字节以","隔开
     * **/
    public static String conver2HexStr(byte [] b)
    {
        StringBuffer result = new StringBuffer();
        for(int i = 0;i<b.length;i++)
        {
            result.append(Long.toString(b[i] & 0xff, 2)+",");
        }
        return result.toString().substring(0, result.length()-1);
    }

    /**
     *
     * @param result 分割的数据
     * @param num   分割的数量
     * @param separator 分割符
     * @return
     */
    public static String insertSplit(String result,Integer num, String separator){
        StringBuffer buffer = new StringBuffer(result);
        for(int i = num; i < buffer.length(); i += (num+1)){
            buffer.insert(num,separator);
        }
        return buffer.toString();
    }

    // tyy 多个数组合并
    public static byte[] getListString(List collect) {
        byte[] aa0 = null;
        // tyy 每次都是两个数组合并 所以合并的次数为 collect.size() ，第一个是虚拟的数组
        for (int i = 0; i < collect.size(); i++) {
            byte[] aa1 = (byte[]) collect.get(i);
            byte[] newInt = onArrayTogater(aa0, aa1);
            aa0 = newInt;
        }
        return aa0;
    }

    private static byte[] onArrayTogater(byte[] aa, byte[] bb) {
        // TODO Auto-generated method stub
        if (aa == null) {
            return bb;
        }
        byte[] collectionInt = new byte[aa.length + bb.length];
        System.arraycopy(aa, 0, collectionInt, 0, aa.length);
        System.arraycopy(bb, 0, collectionInt, aa.length, bb.length);
        return collectionInt;

    }
    public static void openIE (String url) {
        try{
            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
        }catch (Exception e){

        }
    }
//    public static void main(String[] args) {
//        byte[] test = encodeVarint(701);
//        System.out.println("11111112---" + uncodeVarint(test));
//    }
}
