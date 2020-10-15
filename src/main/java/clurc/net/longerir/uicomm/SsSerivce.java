package clurc.net.longerir.uicomm;

import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import clurc.net.longerir.data.CfgData;

public class SsSerivce {
    public static final String TAG_SS = "songshuang";
    public static final String brod_SvrName = "cn.clurc.wifidevss.wifidevice.service";
    public static final String brod_ui = "cn.clurc.wifidevss.wifidevice.ui";
    public static final int BROD_CMDUI_HIDRECV = 12;
    public static final int BROD_CMDUI_HIDSTATE = 13;
    public static final int BROD_CMDUI_PRCPERCENT = 14;
    public static final int BROD_CMDUI_RMTINFO = 15;
    public static final int BROD_CMDUI_LEARNDATA = 16;
    public static final int BROD_CMDUI_BLUEMODEL = 17;

    public static final int BROD_CMDUI_SYNTHREADACTIVE = 18;

    public final static String ACTION ="android.hardware.usb.action.USB_STATE";

    private Context ctx;
    private HidThread threadhid;
    private BleThread threadble;

    private static SsSerivce instance=null;
    public static SsSerivce getInstance(){
        if(instance==null) {
            instance = new SsSerivce();
        }
        return instance;
    }

    public void Start(Context context){
        this.ctx = context;
        threadhid = new HidThread();
        threadhid.init(ctx);
        threadhid.setOnMyBlueEvents(new HidThread.OnHidStaEvents() {
            @Override
            public void OnStateChanged(boolean state) {
                sendUSBActive(state);
            }
        });
        threadble = new BleThread();
        threadble.init(context);
        threadble.setMevents(new BleThread.OnTBledStaEvents() {
            @Override
            public void OnStateChanged(boolean state) {
                sendUSBActive(state);
            }
        });

        Log.w(TAG_SS,"服务开始");
        //-------------blue blv---------------------------------------------------
        threadhid.start();
        threadble.start();
    }

    private static volatile boolean currusb = false;
    private static volatile boolean currblv = false;
    private void sendUSBActive(boolean must){
        boolean usbstate = false;
        if(threadhid.getHid()!=null)
            usbstate = threadhid.getHid().getOpened();
        boolean blvsta = false;
        if(threadble.getMyblv()!=null)
            if(threadble.getMyblv().getintConnectionState() == BluetoothProfile.STATE_CONNECTED)
                blvsta = true;
        if(!must)
            if(currusb==usbstate && currblv==blvsta)return;
        Intent a = new Intent(brod_ui);
        a.putExtra("cmd", BROD_CMDUI_HIDSTATE);
        a.putExtra("hid", usbstate);
        a.putExtra("blv", blvsta);
        ctx.sendBroadcast(a);
        currusb = usbstate;
        currblv = blvsta;
    }

    public void stopped(){
        Log.i(TAG_SS,"服务销毁");
        threadhid.uninit();
        threadble.uninit();
        threadble.interrupt();
        threadhid.interrupt();
        try {
            threadble.join();
            threadhid.join();
        } catch (Exception e) {
        }
    }

    public void setStateBroadcast(){
        sendUSBActive(true);
    }
    public boolean getHidState(){
        return currusb;
    }

    public boolean getBlvState() {
        return currblv;
    }

    public void setHIDLearn(boolean b){
        threadhid.setLearning(b);
    }

    public void setBlvLearn(boolean b){
        threadble.setLearning(b);
    }

    public void setRRCStart(byte[] prcsenddata){
        if(CfgData.selectIr==0) {
            if (threadhid.getHid() != null && threadhid.getPrcsenddata() == null) {
                threadhid.setPrcsenddata(prcsenddata);
                threadhid.setIrcomCancel(false);
            }
        }else{
            if(threadble.getMyblv()==null)return;
            if(threadble.getPrcblvsenddata() == null) {
                threadble.setPrcblvsenddata(prcsenddata);
                threadble.setBlvcomCancel(false);
            }
        }
    }

    public void setCodeStart(byte[] data){
        if(CfgData.selectIr==0) {
            if (threadhid.getHid() != null && threadhid.getCodesdata() == null) {
                threadhid.setCodesData(data);
                threadhid.setIrcomCancel(false);
            }
        }else{
            if(threadble.getMyblv()==null)return;
            if(threadble.getPrcblvsenddata() == null) {
                threadble.setCodesData(data);
                threadble.setBlvcomCancel(false);
            }
        }
    }

    public void irStop(){
        threadhid.setIrcomCancel(true);
        threadble.setBlvcomCancel(true);
    }

    public void irReadInfo(){
        if(CfgData.selectIr==0)
            threadhid.setReadinfo(true);
        else{
            if(threadble.getMyblv()!=null){
                threadble.getMyblv().readModelStr();
            }
        }
    }

    public void setRushing(int freq,int[] rushwave){
        if(CfgData.selectIr==0) {
            if (currusb) {
                if (threadhid.getHid() != null && threadhid.getRushwave() == null) {
                    threadhid.setRushwave(freq,rushwave);
                }
            }
        }else {
            if (currblv) {
                if (threadble.getMyblv() != null && threadble.getRushwave() == null) {
                    threadble.setRushwave(freq,rushwave);
                }
            }
        }
    }

    public void setCanDecoder(boolean candecode){
        if (threadhid.getHid() != null)
            threadhid.setCandecode(candecode);
        if (threadble.getMyblv() != null)
            threadble.setCandecode(candecode);
    }
}
