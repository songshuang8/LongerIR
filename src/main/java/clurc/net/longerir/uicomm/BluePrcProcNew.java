package clurc.net.longerir.uicomm;

import android.os.SystemClock;

import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.IrPrcDownComm;
import clurc.net.longerir.manager.BlueDeiceCommu;

import static java.lang.Math.abs;

public class BluePrcProcNew {
    private IrPrcDownComm prccommdata;
    private BlueDeiceCommu myblv;
    private long txtime;
    private boolean bcancel,brecved;
    private byte[] currsend;
    private byte[] currsend_turn;

    public BluePrcProcNew(IrPrcDownComm prccomm,BlueDeiceCommu amyblv){
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
            }

            @Override
            public void OnLeared(byte[] buf) {
                if(bcancel || buf==null)return;
                if(CfgData.ComPareMydata(buf,currsend_turn)) {
                    brecved = true;
                }
            }
            @Override
            public void OnReadBack(byte[] buf) {
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
        brecved = false;
        myblv.writeMyByte8(currsend);
        txtime = SystemClock.uptimeMillis();
    }

    public void CheckTimeOut(){
        if(brecved) {
            prccommdata.DoFrameInc();
            TxData();
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
