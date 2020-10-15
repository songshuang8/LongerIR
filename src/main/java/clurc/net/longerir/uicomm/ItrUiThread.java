package clurc.net.longerir.uicomm;

import android.content.Context;
import android.content.Intent;

import static clurc.net.longerir.uicomm.SsSerivce.*;
import static clurc.net.longerir.data.CfgData.ByteArrToString;

public class ItrUiThread {
    public static void toBroadcastHidRec(Context ctx,byte[] data) {
        Intent intent = new Intent(brod_ui);
        intent.putExtra("cmd", BROD_CMDUI_HIDRECV);
        intent.putExtra("obj", ByteArrToString(data));
        ctx.sendBroadcast(intent);
    }

    public static void toBroadcastLearInfo(Context ctx,String wavestr,String decode,int freq) {
        Intent intent = new Intent(brod_ui);
        intent.putExtra("cmd", BROD_CMDUI_LEARNDATA);
        intent.putExtra("wavestr", wavestr);
        intent.putExtra("freq", freq);
        intent.putExtra("decode", decode);
        ctx.sendBroadcast(intent);
    }

    public static void sendPercent(Context ctx,int bfb){
        Intent intent = new Intent(brod_ui);
        intent.putExtra("cmd", BROD_CMDUI_PRCPERCENT);
        intent.putExtra("obj", bfb);
        ctx.sendBroadcast(intent);
    }

    public static byte[] getExitCmdData(){ //0xbe, 0xff, 0xff, 0xaa, 0xaa, 0xaa, 0xaa, 0x64
        byte[] buf = new byte[8];
        buf[0] = (byte) 0xbe;
        buf[1] = (byte)0xff;
        buf[2] = (byte)0xff;
        for (int i = 3; i < 7; i++)
            buf[i] = (byte)0xaa;
        buf[7] = (byte)0x64;
        return buf;
    }

    public static byte[] getLearCmdData(boolean b){
        byte[] buf = new byte[8];
        buf[0] = (byte) 0xf1;
        buf[1] = (byte)0xf2;
        buf[2] = 4;
        buf[3] = 0;
        buf[4] = (byte)0xDC;
        if(b)
            buf[5] = 1;
        else
            buf[5] = 0;
        buf[7] = (byte)0xf1;
        for (int i = 0; i < 7; i++) {
            buf[7] += buf[i]&0xff;
        }
        return buf;
    }

    public static byte[] getBleLearCmdData(boolean b){
        byte[] buf = new byte[8];
        buf[0] = (byte) 0xf1;
        buf[1] = (byte)0xf2;
        buf[2] = (byte)0xf2;
        buf[3] = (byte)0xf2;
        buf[4] = (byte)0xf2;
        buf[5] = (byte)0xf2;
        if(b)
            buf[6] = 1;
        else
            buf[6] = 0;
        buf[7] = 0;
        for (int i = 0; i < 7; i++) {
            buf[7] += buf[i]&0xff;
        }
        return buf;
    }
}
