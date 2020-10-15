package clurc.net.longerir.uicomm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;


import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.IrPrcDownComm;
import clurc.net.longerir.data.webHttpClientCom;
import clurc.net.longerir.ircommu.LearData;
import clurc.net.longerir.ircommu.MadeRushData;
import clurc.net.longerir.manager.UsbHidDevice;

import static clurc.net.longerir.Utils.BluebleUtils.ByteArrToStr;
import static clurc.net.longerir.uicomm.SsSerivce.*;

public class HidThread extends Thread{
    private volatile UsbHidDevice hid;
    private Context ctx;

    private volatile boolean usbin = false;
    private volatile boolean usbchange = false;
    private volatile boolean readinfo = false;
    private volatile boolean learning = false;
    private volatile boolean ircomCancel = false;
    private volatile boolean candecode = true;

    private volatile byte[] prcsenddata=null;
    private volatile byte[] codedata=null;
    private volatile int rushfreq;
    private volatile int[] rushwave = null;

    public void init(Context actx){
        this.ctx = actx;
        hid = new UsbHidDevice(actx, 0x2019, 0x0329);
        registerUsbReceiver();
    }

    public void uninit(){
        ctx.unregisterReceiver(mUsbReceiver);
    }

    public void run(){
        boolean currlearstate = false;
        LearData ld =  new  LearData(0.5f);
        hid.Init();
        if(hid.getActive()){
            hid.tryOpenDevice();
        }
        while(!isInterrupted()){
            try{
                if(usbchange){
                    if(usbin) {
                        currlearstate = false;
                        hid.Init();
                        if (hid.getActive()) {
                            hid.tryOpenDevice();
                        }
                    }else{
                        hid.setNull();
                    }
                    usbchange = false;
                }
                mevents.OnStateChanged(false);
                //------------------------------
                if(hid.getOpened()){
                    //-----------
                    if(readinfo){
                        readinfo = false;
                        DoIrReadInfo();
                    }
                    //-------------
                    if(prcsenddata!=null){
                        byte[] copy = new byte[prcsenddata.length];
                        for (int i = 0; i < copy.length; i++) copy[i] = prcsenddata[i];
                        DoIrCommunicaton(copy);
                        prcsenddata = null;
                    }
                    //-------------
                    if(codedata!=null){
                        byte[] copy = new byte[codedata.length];
                        for (int i = 0; i < copy.length; i++) copy[i] = codedata[i];
                        DoIrCodes(copy);
                        codedata = null;
                    }
                    if(rushwave!=null){
                        MadeRushData md = new MadeRushData(rushfreq,rushwave);
                        byte[] rush = md.getData();
                        if(rush!=null) {
                            int frameslen = rush.length / 8;
                            byte[] abf = new byte[8];
                            for (int i = 0; i < frameslen; i++) {
                                System.arraycopy(rush, 8 * i, abf, 0, 8);
                                hid.write(abf);
                                Thread.sleep(2);
                            }
                            rushwave = null;
                        }
                    }
                    //
                    if(currlearstate!=learning){
                        byte[] buf = ItrUiThread.getLearCmdData(learning);
                        hid.write(buf);
                        currlearstate = learning;
                    }
                    //-----------
                    while(!isInterrupted()) {
                        byte[] buf = hid.read(30);
                        if (buf == null)
                            break;
                        if (currlearstate) { //解码
                            if (ld.AppendData(buf)) {
                                final String wavestr = ld.getWaveArr();
                                if (wavestr.length() > 3) {
                                    final int freq = ld.getFreq();
                                    Log.w(TAG_SS,"===>lear:"+freq+","+wavestr);
                                    if(candecode) {
                                        webHttpClientCom.getInstance(null).ThreadHttpCall("DoLearPro2?freq=" + freq, wavestr, "POST", new webHttpClientCom.RestOnWebPutEvent() {
                                                    @Override
                                                    public void onSuc(byte[] out) {
                                                        ItrUiThread.toBroadcastLearInfo(ctx, wavestr,new String(out), freq);
                                                    }

                                                    @Override
                                                    public void onFail(boolean netfaulre, String res) {

                                                    }
                                                });
                                    }else{
                                        ItrUiThread.toBroadcastLearInfo(ctx, wavestr, null, freq);
                                    }
                                }
                                ld.clear();
                            }
                        } else
                            ItrUiThread.toBroadcastHidRec(ctx,buf);
                    }
                    Thread.sleep(20);
                }else {
                    Thread.sleep(100);
                }
            }catch (InterruptedException e){
                //e.printStackTrace();
                break;
            }
        }
    }

    private USBReceiver mUsbReceiver;
    public void registerUsbReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mUsbReceiver = new USBReceiver();
        ctx.registerReceiver(mUsbReceiver, filter);
    }
    private class USBReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            switch (intent.getAction()) {
                case UsbManager.ACTION_USB_DEVICE_ATTACHED: // 插入USB设备
                    usbchange = true;
                    usbin = true;
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED: // 拔出USB设备
                    usbchange = true;
                    usbin = false;
                    break;
                default:
                    break;
            }
        }
    }

    private void DoIrReadInfo() throws InterruptedException{
        int rmttype= 0;
        int rmtid= 0;
        ircomCancel = false;
        byte[] sendbuffer = new byte[8];

        sendbuffer[0] = (byte) 0xbb;
        sendbuffer[1] = (byte) 0xff;
        sendbuffer[2] = (byte) 0xee;
        sendbuffer[3] = 46;
        sendbuffer[4] = 0;
        sendbuffer[5] = 0;
        sendbuffer[6] = 0;
        int crc = 0;
        for (int i = 0; i < 7; i++) {
            crc += (sendbuffer[i]&0xff);
        }
        sendbuffer[7] = (byte)(crc & 0xff);
        //-------------
        while(!isInterrupted()){
            if(ircomCancel || usbchange)return;
            Log.w(TAG_SS,"===>write :"+ByteArrToStr(sendbuffer));
            if(!hid.write(sendbuffer))return;

            byte[] buf= hid.read(200);

            if(buf==null){
                Log.w(TAG_SS,"===>no read");
                Thread.sleep(100);
                continue;
            }
            Log.w(TAG_SS,"===>cved:"+ByteArrToStr(buf));
            if(!CfgData.CheckSum(buf)){
                Log.w(TAG_SS,"===>chksum err");
                Thread.sleep(100);
                continue;
            }
            Log.w(TAG_SS,"===>ok"+buf[3] + ","+buf[4]);
            rmttype= 0;
            if (buf[3]==9 && buf[4]==9)
                rmttype= 3;
            else if (buf[3]==8 && buf[4]==8)
                rmttype= 1;
            else if(buf[3]==8 && buf[4]==9)
                rmttype= 1;
            else if(buf[3]==9 && buf[4]==10)
                rmttype = 2;
            else if(buf[3]==0x30 && buf[4] == 0x35)  //($1, $0, $30, $35, $37, $36
                rmttype = 11;
            else if(buf[1]==0x7 && buf[2] == 7  && buf[3] == 0x30 && buf[4] == 0x38  && buf[5] == 0x33 && buf[6] == 0x35)  //bb 07 07 30 38 33 35 99
                rmttype = 9;
            break;
        }
        if(rmttype==10){
            Intent intent = new Intent(brod_ui);
            intent.putExtra("cmd", BROD_CMDUI_RMTINFO);
            intent.putExtra("rmttype", rmttype);
            intent.putExtra("rmtid", rmtid);
            ctx.sendBroadcast(intent);
            return;
        }
        //read remote id
        byte[] eep = new byte[8];
        int framex=0;
        while(framex<2){
            if(ircomCancel || usbchange)return;

            sendbuffer[0] = (byte) 0xbb;
            if(framex==0)
                sendbuffer[1] = (byte) 0x8;
            else
                sendbuffer[1] = (byte) 0xc;
            sendbuffer[2] = 0;
            sendbuffer[3] = (byte) 0xaa;
            sendbuffer[4] = (byte) 0xaa;
            sendbuffer[5] = (byte) 0xaa;
            sendbuffer[6] = (byte) 0xaa;
            crc = 0;
            for (int i = 0; i < 7; i++) {
                crc += (sendbuffer[i]&0xff);
            }
            sendbuffer[7] = (byte)(crc & 0xff);
            if(!hid.write(sendbuffer))return;

            byte[] buf= hid.read(60);
            if(buf==null){
                Thread.sleep(20);
                continue;
            }
            if(!CfgData.CheckSum(buf)){
                Thread.sleep(20);
                continue;
            }

            if(sendbuffer[0]==buf[0] && sendbuffer[1]==buf[1] && sendbuffer[2]==buf[2]){
                System.arraycopy(buf,3,eep,framex*4,4);
                framex++;
            }
            if(isInterrupted())break;
        }
        if(framex==2) {
            if (rmttype == 1 || rmttype==2) {
                rmtid = (eep[0]&0xff);
                rmtid <<= 8;
                rmtid += (eep[1]&0xff);
            }else{
                rmtid = (eep[5]&0xff);
                rmtid <<= 8;
                rmtid += (eep[4]&0xff);
            }
            Intent intent = new Intent(brod_ui);
            intent.putExtra("cmd", BROD_CMDUI_RMTINFO);
            intent.putExtra("rmttype", rmttype);
            intent.putExtra("rmtid", rmtid);
            ctx.sendBroadcast(intent);
        }
    }

    private void DoIrCommunicaton(final byte[] data) throws InterruptedException{
        IrPrcDownComm prccommdata = new IrPrcDownComm(data);
        while (!isInterrupted()) {
            if(prccommdata.getHaveOk())
                break;
            if(ircomCancel || usbchange)break;
            byte[] sb = prccommdata.getCurrData();
            if(hid.write(sb)==false)return;
            byte[] buf= hid.read(60);
            if(buf==null){
                Thread.sleep(60);
                continue;
            }
            if(!CfgData.CheckSum(buf)){
                Thread.sleep(60);
                continue;
            }
            if(!CfgData.ComPareMydata(buf,sb)){
                Thread.sleep(60);
                continue;
            }
            Thread.sleep(40);
            prccommdata.DoFrameInc();
            ItrUiThread.sendPercent(ctx,prccommdata.getPercent());
        }
    }

    private void DoIrCodes(final byte[] data) throws InterruptedException{
        int pos = 0;
        while (!isInterrupted()) {
            if(pos>=2)
                break;
            if(ircomCancel || usbchange)break;
            //--
            byte[] sb = new byte[8];
            if(pos==0){
                for (int i = 0; i < 8; i++) {
                    sb[i] = data[i];
                }
            }else{
                for (int i = 0; i < 8; i++) {
                    sb[i] = data[8+i];
                }
            }
            //--
            if(hid.write(sb)==false)return;
            byte[] buf= hid.read(60);
            if(buf==null){
                Thread.sleep(20);
                continue;
            }
            if(!CfgData.CheckSum(buf)){
                Thread.sleep(20);
                continue;
            }
            if(!CfgData.ComPareMydata(buf,sb)){
                Thread.sleep(20);
                continue;
            }
            pos++;
            if(pos==1) {
                ItrUiThread.sendPercent(ctx, 50);
            }else{
                ItrUiThread.sendPercent(ctx, 100);
            }
        }
    }

    private OnHidStaEvents mevents;
    public interface OnHidStaEvents{
        public abstract void OnStateChanged(boolean state);
    }

    public void setOnMyBlueEvents(OnHidStaEvents aev){
        mevents = aev;
    }

    public UsbHidDevice getHid() {
        return hid;
    }

    public byte[] getPrcsenddata() {
        return prcsenddata;
    }

    public void setPrcsenddata(byte[] prcsenddata) {
        this.prcsenddata = prcsenddata;
    }

    public void setCodesData(byte[] prcsenddata) {
        this.codedata = prcsenddata;
    }

    public byte[] getCodesdata() {
        return codedata;
    }

    public void setIrcomCancel(boolean ircomCancel) {
        this.ircomCancel = ircomCancel;
    }

    public void setReadinfo(boolean readinfo) {
        this.readinfo = readinfo;
    }

    public void setLearning(boolean learning) {
        this.learning = learning;
    }

    public int[] getRushwave() {
        return rushwave;
    }

    public void setRushwave(int freq,int[] rushwave) {
        this.rushfreq = freq;
        this.rushwave = rushwave;
    }

    public void setCandecode(boolean candecode) {
        this.candecode = candecode;
    }
}
