package clurc.net.longerir.uicomm;

import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.os.Handler;
import android.os.Message;

import clurc.net.longerir.Utils.SysFun;
import clurc.net.longerir.base.MobileEncode;
import clurc.net.longerir.data.IrPrcDownComm;

import static android.content.Context.CONSUMER_IR_SERVICE;

public class MobileIrPort {
    private Context context;
    private byte[] data;
    private Handler mhandle;
    private boolean active = false;
    private boolean cancel = false;

    public MobileIrPort(Context context,byte[] data,final Handler mHandler){
        this.context = context;
        this.data = data;
        this.mhandle = mHandler;
    }

    public void DoStart(){
        if(!SysFun.IfHasIrDaPort(context))return;
        active = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                ConsumerIrManager ir=(ConsumerIrManager)context.getSystemService(CONSUMER_IR_SERVICE);
                int freq = 0;
                IrPrcDownComm prccommdata = new IrPrcDownComm(data);
                int repeat = 0;

                while (true) {
                    if(cancel)return;
                    byte[] sb = prccommdata.getCurrData();
                    int[] irwaves = MobileEncode.longer_ir_encoder(sb);
                    ir.transmit(freq, irwaves);

                    try {
                        Thread.sleep(100);
                    }catch (InterruptedException e){
                        break;
                    }
                    repeat++;
                    if(repeat>1) {
                        prccommdata.DoFrameInc();
                        sendHandlePercentMsg(prccommdata.getPercent());
                        if(prccommdata.getHaveOk())
                            break;
                        repeat = 0;
                    }
                }
                //  send exit;
                byte[] sb =ItrUiThread.getExitCmdData();
                int[] irwaves = MobileEncode.longer_ir_encoder(sb);
                ir.transmit(freq, irwaves);
                try {
                    Thread.sleep(300);
                }catch (InterruptedException e){
                }
                ir.transmit(freq, irwaves);
                active = false;
            }
        }).start();
    }

    private void sendHandlePercentMsg(int per){
        Message msg = new Message();
        msg.arg1 = per;
        mhandle.sendMessage(msg);
    }

    public boolean getActived(){
        return active;
    }

    public void setCancel(){
        cancel = true;
    }
}
