package clurc.net.longerir.uicomm;

import android.os.SystemClock;
import android.util.Log;

import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.IrPrcDownComm;
import clurc.net.longerir.manager.BlueDeiceCommu;

import static clurc.net.longerir.uicomm.SsSerivce.TAG_SS;
import static java.lang.Math.abs;

public class BlueRushingProc {
    private BlueDeiceCommu myblv;
    private long txtime;
    private boolean bcancel;
    private byte[] currsend,currsend_turn;;
    private byte[] rushdata;
    private int framecount;
    private int pos;
    int currstep;int steptxcnt;

    public BlueRushingProc(byte[] data,BlueDeiceCommu amyblv){
        bcancel = false;
        rushdata = data;
        myblv = amyblv;
        framecount = data.length / 8;
        pos = 0;
        myblv.setOnMyBlueEvents(new BlueDeiceCommu.OnMyBlueEvents() {
            @Override
            public void OnStateChanged(boolean state) {
                bcancel = true;
            }

            @Override
            public void OnModeRecved(String modelstr) {

            }

            @Override
            public void OnWriteed(boolean bsuc,byte[] buf) {
                if(bcancel)return;
                if(buf!=null){
                    pos++;
                    txtime = SystemClock.uptimeMillis() - 1485;
                }
            }

            @Override
            public void OnLeared(byte[] buf) {}
            @Override
            public void OnReadBack(byte[] buf) {}
        });
        currstep = -1;
        steptxcnt = 0;
        TxData();
    }

    private void TxData(){
        if(pos>=framecount)return;
        currsend = new byte[8];
        for (int i = 0; i < 8; i++)
            currsend[i] = rushdata[8*pos+i];
        //
        currsend_turn = new byte[currsend.length];
        for (int i = 0; i < currsend_turn.length; i++) currsend_turn[i] = currsend[currsend_turn.length-i-1];
        //
        myblv.writeMyByte8(currsend);
        txtime = SystemClock.uptimeMillis();
    }

    public void CheckTimeOut(){
        long alen = abs(SystemClock.uptimeMillis()-txtime);
        if(alen<1500){
            return;
        }
        TxData();
        if(currstep==pos){
            steptxcnt++;
            if(steptxcnt>=3) {
                bcancel = true;
                pos = framecount;
            }
        }else{
            steptxcnt = 0;
            currstep = pos;
        }
    }

    public boolean getOk(){
        return (pos>=framecount)?true:false;
    }

    public void seCancel() {
        bcancel = true;
    }

    public int getPercent(){
        return (pos*100 / framecount);
    }
}
