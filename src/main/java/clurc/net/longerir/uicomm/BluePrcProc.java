package clurc.net.longerir.uicomm;

import android.os.SystemClock;

import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.IrPrcDownComm;
import clurc.net.longerir.manager.BlueDeiceCommu;

import static java.lang.Math.abs;

public class BluePrcProc {
    private IrPrcDownComm prccommdata;
    private BlueDeiceCommu myblv;
    private long txtime,recvedtime;
    private boolean bcancel;
    private byte[] currsend;
    private byte[] currsend_turn;
    private int recved;  //发出后，由于有两个数据会到来， 1个是app主动请求的，1个是ble主动发出的，可能会打架 ，

    public BluePrcProc(IrPrcDownComm prccomm,BlueDeiceCommu amyblv){
        bcancel = false;
        prccommdata = prccomm;
        myblv = amyblv;
        myblv.setOnMyBlueEvents(new BlueDeiceCommu.OnMyBlueEvents() {
            @Override
            public void OnStateChanged(boolean state) {

            }

            @Override
            public void OnModeRecved(String modelstr) {

            }

            @Override
            public void OnWriteed(boolean bsuc,byte[] buf) {
                if(bcancel)return;
                if(bsuc && buf!=null){
                    myblv.readMyByte8();
                }
            }

            @Override
            public void OnLeared(byte[] buf) {
                if(bcancel || buf==null)return;
                if(CfgData.ComPareMydata(buf,currsend_turn)) {
                    recved++;
                    recvedtime = SystemClock.uptimeMillis();
                }
            }
            @Override
            public void OnReadBack(byte[] buf) {
                if(bcancel || buf==null)return;
                if(CfgData.ComPareMydata(buf,currsend)) {
                    recved++;
                    recvedtime = SystemClock.uptimeMillis();
                }
            }
        });
        TxData();
    }

    private void TxData(){
        if(prccommdata.getHaveOk())return;
        currsend = prccommdata.getCurrData();
        //
        currsend_turn = new byte[currsend.length];
        for (int i = 0; i < currsend_turn.length; i++) currsend_turn[i] = currsend[currsend_turn.length-i-1];
        //
        myblv.writeMyByte8(currsend);
        recved = 0;
        txtime = SystemClock.uptimeMillis();
    }

    public void CheckTimeOut(){
        if(recved>=2) {
            prccommdata.DoFrameInc();
            TxData();
            return;
        }else if(recved>0){
            long alen = abs(SystemClock.uptimeMillis()-recvedtime);
            if(alen>200) {
                prccommdata.DoFrameInc();
                TxData();
            }
            return;
        }
        long alen = abs(SystemClock.uptimeMillis()-txtime);
        if(alen>2000) {
            TxData();
        }
    }

    public void seCancel() {
        bcancel = true;
    }
}
