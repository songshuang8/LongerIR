package clurc.net.longerir.uicomm;

import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.media.session.MediaSession;
import android.util.Log;
import android.widget.Toast;

import clurc.net.longerir.R;
import clurc.net.longerir.data.IrPrcDownComm;
import clurc.net.longerir.data.webHttpClientCom;
import clurc.net.longerir.ircommu.BleLearData;
import clurc.net.longerir.ircommu.LearData;
import clurc.net.longerir.ircommu.MadeBleRushData;
import clurc.net.longerir.manager.BlueDeiceCommu;

import static clurc.net.longerir.uicomm.SsSerivce.*;

public class BleThread extends Thread{
    private Context ctx;
    BleLearData ld =  new  BleLearData(1);

    private volatile byte[] prcblvsenddata=null;
    private volatile byte[] codesdata=null;
    private volatile int rushfreq=38000;
    private volatile int[] rushwave = null;

    private volatile boolean blvcomCancel = false;
    private volatile boolean learning = false;

    public void init(Context actx){
        Log.i(TAG_SS, "创建BLE对象");
        this.ctx = actx;
        BlueDeiceCommu.getInstance().setInit(ctx);
        BlueDeiceCommu.getInstance().setOnMyBlueEvents(blvevents);
    }

    private static BleThread instance=null;
    public static BleThread getInstance(){
        if(instance==null) {
            instance = new BleThread();
        }
        return instance;
    }

    public void uninit(){
        BlueDeiceCommu.getInstance().disconnect();
    }

    public void run(){
        boolean currlearstate = false;
        while(!isInterrupted()){
            try {
                BlueDeiceCommu.getInstance().connect();
                if(BlueDeiceCommu.getInstance().getintConnectionState() != BluetoothProfile.STATE_CONNECTED){
                    Thread.sleep(2000);
                    continue;
                }
                mevents.OnStateChanged(false);
                sleep(1000);
                BlueDeiceCommu.getInstance().readModelStr();
                if(currlearstate){ //当前是否为学习，重启后再次推至
                    byte[] buf = ItrUiThread.getBleLearCmdData(learning);
                    BlueDeiceCommu.getInstance().writeMyByte8(buf);
                }
                while(!isInterrupted()){
                    if(prcblvsenddata!=null){
                        byte[] copy = new byte[prcblvsenddata.length];
                        for (int i = 0; i < copy.length; i++) copy[i] = prcblvsenddata[i];
                        DoBleCommunicaton(copy);
                        prcblvsenddata = null;
                    }
                    if(codesdata!=null){
                        byte[] copy = new byte[codesdata.length];
                        for (int i = 0; i < copy.length; i++) copy[i] = codesdata[i];
                        DoBleCodes(copy);
                        codesdata = null;
                    }
                    if(rushwave!=null){
                        MadeBleRushData md = new MadeBleRushData(rushfreq,rushwave);
                        byte[] rush = md.getData();
                        if(rush!=null)
                            DoBleRushing(rush);
                        rushwave = null;
                    }
                    //-------------
                    if(currlearstate!=learning){
                        byte[] buf = ItrUiThread.getBleLearCmdData(learning);
                        BlueDeiceCommu.getInstance().writeMyByte8(buf);
                        currlearstate = learning;
                    }
                    //------------
                    if(BlueDeiceCommu.getInstance().getintConnectionState() != BluetoothProfile.STATE_CONNECTED){
                        Thread.sleep(1000);
                        break;
                    }
                    Thread.sleep(200);
                }
                Thread.sleep(1000);
            }catch(InterruptedException e){
                //e.printStackTrace();
                break;
            }
        }
    }

    private BlueDeiceCommu.OnMyBlueEvents blvevents = new BlueDeiceCommu.OnMyBlueEvents() {
        @Override
        public void OnStateChanged(boolean state) {
            mevents.OnStateChanged(state);
        }

        @Override
        public void OnModeRecved(String modelstr){
            Intent intent = new Intent(brod_ui);
            intent.putExtra("cmd", BROD_CMDUI_BLUEMODEL);
            intent.putExtra("model", modelstr);
            ctx.sendBroadcast(intent);
        }

        @Override
        public void OnWriteed(boolean bsuc,byte[] buf) {}
        @Override
        public void OnLeared(byte[] buf) {
            if(buf.length!=8)return;
            byte[] temp = new byte[buf.length];
            for (int i = 0; i < 8; i++) {
                temp[i] = buf[7-i];
            }
            if (ld.AppendData(temp)) {
                final String wavestr = ld.getWaveArr();
                if (wavestr.length() > 3) {
                    final int freq = ld.getFreq();
                    Log.w(TAG_SS,"===>lear:"+freq+","+wavestr);
                    ItrUiThread.toBroadcastLearInfo(ctx, wavestr,freq);
                }
                ld.clear();
            }
        }
        @Override
        public void OnReadBack(byte[] buf) {}
    };

    private void DoBleCommunicaton(final byte[] data) throws InterruptedException{
        IrPrcDownComm prccommdata = new IrPrcDownComm(data);
        BluePrcProcNew proc = new BluePrcProcNew(prccommdata,BlueDeiceCommu.getInstance());
        while (!isInterrupted()) {
            if(prccommdata.getHaveOk())
                break;
            if(blvcomCancel){
                proc.seCancel();
                blvcomCancel = false;
                break;
            }
            if(BlueDeiceCommu.getInstance().getintConnectionState() != BluetoothProfile.STATE_CONNECTED)
                break;
            proc.CheckTimeOut();
            Thread.sleep(10);
            ItrUiThread.sendPercent(ctx,prccommdata.getPercent());
        }
        //exit
        Thread.sleep(500);
        BlueDeiceCommu.getInstance().writeMyByte8(ItrUiThread.getExitCmdData());
        Thread.sleep(2000);
        BlueDeiceCommu.getInstance().writeMyByte8(ItrUiThread.getExitCmdData());

        mevents.OnStateChanged(true);
        BlueDeiceCommu.getInstance().setOnMyBlueEvents(blvevents);
    }

    private void DoBleCodes(final byte[] data) throws InterruptedException{

    }

    private void DoBleRushing(final byte[] data) throws InterruptedException{
        BlueRushingProc proc = new BlueRushingProc(data,BlueDeiceCommu.getInstance());
        while (!isInterrupted()) {
            if(proc.getOk())
                break;
            if(blvcomCancel){
                proc.seCancel();
                blvcomCancel = false;
                break;
            }
            if(BlueDeiceCommu.getInstance().getintConnectionState() != BluetoothProfile.STATE_CONNECTED)
                break;
            proc.CheckTimeOut();
            Thread.sleep(10);
            ItrUiThread.sendPercent(ctx,proc.getPercent());
        }
        BlueDeiceCommu.getInstance().setOnMyBlueEvents(blvevents);
    }

    private OnTBledStaEvents mevents;
    public interface OnTBledStaEvents{
        public abstract void OnStateChanged(boolean state);
    }

    public void setMevents(OnTBledStaEvents mevents) {
        this.mevents = mevents;
    }

    public BlueDeiceCommu getMyblv() {
        return BlueDeiceCommu.getInstance();
    }

    public void setBlvcomCancel(boolean blvcomCancel) {
        this.blvcomCancel = blvcomCancel;
    }

    public byte[] getPrcblvsenddata() {
        return prcblvsenddata;
    }

    public byte[] getCodesData() {
        return codesdata;
    }

    public void setCodesData(byte[] data) {
        codesdata = data;
    }

    public void setPrcblvsenddata(byte[] data) {
        prcblvsenddata = data;
    }

    public void setLearning(boolean learning) {
        this.learning = learning;
    }

    public void setRushwave(int freq,int[] rushwave) {
        this.rushfreq = freq;
        this.rushwave = rushwave;
    }

    public int[] getRushwave() {
        return rushwave;
    }
}
