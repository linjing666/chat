package wxclient;

import java.io.UnsupportedEncodingException;

public interface CallBack {
    void onData(byte[] data) throws UnsupportedEncodingException;
}