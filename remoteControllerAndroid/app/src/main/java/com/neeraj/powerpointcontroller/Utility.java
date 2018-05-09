package com.neeraj.powerpointcontroller;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Created by neeraj on 21-09-2016.
 */
public class Utility {
    public static void getShort(byte[] bytes,int i,int v){
        bytes[i] = (byte) (v & 0x000000FF);
        bytes[i+1] = (byte) ((v & 0x0000FF00)>>8);
    }
    public static int readInt(InputStream inp,int numBytes){
        try {
            int num = 0;
            for(int i=0;i<numBytes;i++){
                int bt = inp.read();
                num = num + (bt<<(8*i));
            }
            return num;
        }catch (Exception e){
            return -1;
        }

    }
    public static byte[] readData(InputStream inp){
        try {
            int len = readInt(inp,4);
            Log.d("getMsg","len:"+len);
            byte[] msg = new byte[len];
            int off = 0;
            while(off<len){
                off += inp.read(msg,off,len-off);
            }
            return msg;
        }
        catch (IOException e){
            return null;
        }
    }
}
