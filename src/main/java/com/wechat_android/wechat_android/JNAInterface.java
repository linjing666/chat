package com.wechat_android.wechat_android;


import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;

import java.io.IOException;

public class JNAInterface {
    public interface CLibrary extends Library {
        CLibrary INSTANCE = (CLibrary) Native.loadLibrary("ecdh_x64", CLibrary.class);

        boolean GenEcdh(int nid, byte[] szPriKey, IntByReference pLenPri, byte[] szPubKey, IntByReference pLenPub);
    }

    byte[] pri = new byte[2048];
    byte[] put = new byte[2048];
    byte[] Put;
    IntByReference pril = new IntByReference();
    IntByReference putl = new IntByReference();

    JNAInterface() throws UnknownError, IOException, InterruptedException {

        boolean l = CLibrary.INSTANCE.GenEcdh(713, put, pril, put, putl);
        System.out.println("bool应答：" + l);
        System.out.println(pril);
        int Pril,Putl;


       if(l)
        {
             Pril=pril.getValue();
             Putl=putl.getValue();
            Put=new byte[Putl];

            System.out.println(Pril+" "+Putl);


            for (int i=0;i<Putl;i++){
               Put[i]=put[i];
            }
        }

    }


}
