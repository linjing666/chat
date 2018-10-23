package com.wechat_android.wechat_android;
import struct.JavaStruct;
import struct.StructClass;
import struct.StructException;
import struct.StructField;

@StructClass
public class HttpStruct {
@StructField(order = 0)
    byte[] header;
    @StructField(order = 1)
    byte[] head=new byte[4];
@StructField(order = 2)
    byte[] len1=new byte[4];   //accout
@StructField(order = 3)
    byte[] len2=new byte[4];    //device
@StructField(order = 4)
    byte[] len3;       //device压缩后的长度
@StructField(order = 5)
    byte[]  ReqAccout=new byte[2048];
@StructField(order = 6)
    byte ReqDevice;


    public byte[] getHeader() {
        return header;
    }

    public void setHeader(byte[] header) {
        this.header = header;
    }

    public byte[] getHead() {
        return head;
    }

    public void setHead(byte[] head) {
        this.head = head;
    }

    public byte[] getLen1() {
        return len1;
    }

    public void setLen1(byte[] len1) {
        this.len1 = len1;
    }

    public byte[] getLen2() {
        return len2;
    }

    public void setLen2(byte[] len2) {
        this.len2 = len2;
    }

    public byte[] getLen3() {
        return len3;
    }

    public void setLen3(byte[] len3) {
        this.len3 = len3;
    }

    public byte[] getReqAccout() {
        return ReqAccout;
    }

    public void setReqAccout(byte[] reqAccout) {
        ReqAccout = reqAccout;
    }

    public byte getReqDevice() {
        return ReqDevice;
    }

    public void setReqDevice(byte reqDevice) {
        ReqDevice = reqDevice;
    }

    public String bao(){

        return "NETPRO_INFO[head="+head+"header="+header+"len1="+len1+",len2="+len2+",len3="+len3+",accout="+ReqAccout+",device="+ReqDevice+"]";
    }


    public   byte[] depack() throws StructException {
        HttpStruct httpStruct = new HttpStruct();
          byte [] pack = JavaStruct.pack(httpStruct);
        return pack;
    }
}

