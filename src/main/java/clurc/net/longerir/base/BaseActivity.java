package clurc.net.longerir.base;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.ConsumerIrManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import clurc.net.longerir.data.IrButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import clurc.net.longerir.MainActivity;
import clurc.net.longerir.Utils.SysFun;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.TxtBtnInfo;
import clurc.net.longerir.uicomm.ItrUiThread;
import clurc.net.longerir.R;
import clurc.net.longerir.data.webHttpClientCom;
import clurc.net.longerir.uicomm.SsSerivce;

import static clurc.net.longerir.uicomm.SsSerivce.TAG_SS;


public class BaseActivity  extends AppCompatActivity {
    public Activity instance;
    public QMUITopBar mTopBar;
    private MyBroadCaseReceiver4 recver;

    public int layid;
    public String title;
    public String errstr;
    public boolean showback = true;
    public boolean backresultok = false;

    public QMUITipDialog tipDialog;

    public Vibrator mVibrator;  //声明一个振动器对象

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        getViewId();
        setContentView(layid);
        QMUIStatusBarHelper.translucent(this, getResources().getColor(R.color.app_color_theme_1));
        mTopBar = (QMUITopBar) findViewById(R.id.topbar);
        mTopBar.setBackgroundColor(getResources().getColor(R.color.app_color_theme_1));
        if(title!=null)
            mTopBar.setTitle(title);
        if (showback) {
            mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (backresultok) {
                        Intent intent = new Intent();
                        setResult(Activity.RESULT_OK, intent);
                    }
                    if (DoBack()) {
                        finish();
                        overridePendingTransition(com.qmuiteam.qmui.R.anim.abc_slide_in_bottom, com.qmuiteam.qmui.R.anim.shrink_from_bottom);
                    }
                }
            });
        }
        mVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
        DoInit();
        DoInit2(savedInstanceState);
        recver = new MyBroadCaseReceiver4();
    }

    public void getViewId() {

    }

    public void DoInit() {

    }
    public void DoInit2(Bundle savedInstanceState) {

    }

    public boolean DoBack() {
        return true;
    }

    public void DoShowing() {

    }

    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SsSerivce.brod_ui);
        registerReceiver(recver, filter); // 注册Broadcast Receiver
        DoShowing();
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(recver);// 取消注册
        mVibrator.cancel();
        super.onStop();
    }

    public void showwait() {
        tipDialog = new QMUITipDialog.Builder(instance)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(getString(R.string.str_wait))
                .create();
        tipDialog.show();
    }

    ;

    public void hidedialog() {
        tipDialog.dismiss();
    }

    public void ShowDialog(String str) {
        tipDialog = new QMUITipDialog.Builder(instance)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                .setTipWord(str)
                .create();
        tipDialog.show();
        mTopBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                tipDialog.dismiss();
            }
        }, 1500);
    }

    public interface OnActivityEventer {
        void onSuc();
        boolean onDodata(String res);
    }

    public void BackgroundRest(String urlparam, String body, String method, final OnActivityEventer aev) {
        showwait();
        webHttpClientCom.getInstance(instance).RestkHttpCall(urlparam, body, method, new webHttpClientCom.RestOnWebPutEvent() {
            @Override
            public void onSuc(byte[] out) {
                final boolean b = aev.onDodata(new String(out));
                runOnUiThread(new Runnable() {
                    public void run() {
                        hidedialog();
                        if (b) {
                            aev.onSuc();
                        } else {
                            ShowDialog(errstr);
                        }
                    }
                });
            }

            @Override
            public void onFail(final boolean netfaulre, final String res) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        hidedialog();
                        if (netfaulre)
                            showMessage(getString(R.string.str_err), getString(R.string.str_err_net));
                        if (res != null && res.length() > 0)
                            showMessage(getString(R.string.str_err), res);
                    }
                });
            }
        });
    }

//    public void BackgroundRest(String urlparam, String body, String method, final OnActivityEventer aev) {
//        showwait();
//        String[] pam = new String[3];
//        pam[0]=urlparam;pam[1]=body;pam[2] = method;
//        webtask = new WebHttpRestTask();
//        webtask.setUiHanler(new Handler(){
//            @Override
//            public void handleMessage(Message msg) {
//
//            }
//        });
//
//        class MyWebHandler extends Handler {
//            @Override
//            public void handleMessage(Message msg) {
//
//            }
//        }

    public void DoServiceMesg(int cmd, Intent intent) {

    }

    class MyBroadCaseReceiver4 extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            int sctcmd = intent.getIntExtra("cmd", 0);
//            String sendstr = (String) intent.getSerializableExtra("obj");
//            byte[] data = StringsToByteArr(sendstr);
            DoServiceMesg(sctcmd, intent);
        }
    }

    public void showMessage(String title, String contxt) {
        new QMUIDialog.MessageDialogBuilder(instance)
                .setTitle(title)
                .setMessage(contxt)
                .addAction("Ok", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
    }

    public void showToast(String message) {
        Toast.makeText(instance,message,Toast.LENGTH_SHORT).show();
    }

    //判断红外口是否就绪
    public boolean CheckIrDaPort(boolean usb,boolean ble){
        if(CfgData.selectIr == 0){
            return usb;
        }else if(CfgData.selectIr==1){
            return ble;
        }else if(CfgData.selectIr==2){
            return SysFun.IfHasIrDaPort(instance);
        }else{
            return true;
        }
    }

    public void DoIrCodes(final byte[] data,final Handler mHandler){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ConsumerIrManager ir=(ConsumerIrManager)getSystemService(CONSUMER_IR_SERVICE);
                int freq = 0;
                int repeat = 0;
                int pos = 0;
                while (true) {
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
                    int[] irwaves = MobileEncode.longer_ir_encoder(sb);
                    ir.transmit(freq, irwaves);

                    try {
                        Thread.sleep(100);
                    }catch (InterruptedException e){
                        break;
                    }
                    repeat++;
                    if(repeat>1) {
                        pos++;
                        if(pos==0) {
                            sendHandlePercentMsg(mHandler, 50);
                        }else{
                            sendHandlePercentMsg(mHandler, 100);
                        }
                        if(pos>=2)
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
            }
        }).start();
    }

    private void sendHandlePercentMsg(Handler mhandler, int per){
        Message msg = new Message();
        msg.arg1 = per;
        mhandler.sendMessage(msg);
    }

    public void playMp3(byte[] mp3SoundByteArray,final Handler mHandler) {
        try {
            // create temp file that will hold byte array
            File tempMp3 = File.createTempFile("kurchina", "wav", getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();

            final MediaPlayer mediaPlayer = new MediaPlayer();

            FileInputStream fis = new FileInputStream(tempMp3);
            mediaPlayer.setDataSource(fis.getFD());
            mediaPlayer.prepare();
            final int total = mediaPlayer.getDuration();

            final Timer mTimer = new Timer();
            final TimerTask mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    int currpos = mediaPlayer.getCurrentPosition(); //有时候获取不完全就停了？
                    sendHandlePercentMsg(mHandler,(int)(currpos*100 / total));
                }
            };
            mTimer.schedule(mTimerTask,  300,300);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    sendHandlePercentMsg(mHandler,100);
                    mTimer.cancel();
                    mTimerTask.cancel();
                }
            });

            mediaPlayer.start();
        } catch (IOException ex) {
            String s = ex.toString();
            ex.printStackTrace();
        }
    }
// rushing 发一个普通遥控器按键数据
    public boolean RushingKey(TxtBtnInfo arush){
        if(arush==null){
            Toast.makeText(instance, "error ir data!", Toast.LENGTH_SHORT)
                    .show();
            return false;
        }
        if(arush.gsno<0 || arush.param==null){
            Toast.makeText(instance, "error ir data!", Toast.LENGTH_SHORT)
                    .show();
            return false;
        }
        long[] mp= new long[arush.param.length];
        for (int i = 0; i < mp.length; i++) {
            mp[i] = arush.param[i]&0xffffffff;
        }
        int[] wave = MainActivity.irGsGetData(mp,arush.param.length,arush.gsno);//. LongerMain.getWavesByGsId(arush.gsno,arush.param);
        if(wave==null){
            Toast.makeText(instance, "error ir data!", Toast.LENGTH_SHORT)
                    .show();
            return false;
        }
        dorushWave(wave,MainActivity.irGetCurrentFreq());
        return true;
    }

    public boolean irpowerturx=false;
    public boolean RushAc(String acdata, byte[] statusArr,int btn,int btnval) {
        byte[] rawdata = android.util.Base64.decode(acdata,android.util.Base64.DEFAULT);
        return RushAc(rawdata,statusArr,btn,btnval);
    }
    public boolean RushAc(byte[] rawdata, byte[] statusArr,int btn,int btnval) {
        if(rawdata.length<=26){
            Toast.makeText(instance, "error ir data!", Toast.LENGTH_SHORT)
                    .show();
            return false;
        }
        ///
        irpowerturx = !irpowerturx;
        MainActivity.irSetStatus(statusArr);
        int[] irhl = MainActivity.irGetIRDataByRaw(rawdata,rawdata.length,btn,
                btnval);
        //
        if(irhl==null){
            Toast.makeText(instance, "error ir data!", Toast.LENGTH_SHORT)
                    .show();
            return false;
        }
        dorushWave(irhl,MainActivity.irGetCurrentFreq());
        return true;
    }
    //必须把正负搞掉？
    public void dorushWave(int[] wave,int freq){
        mVibrator.vibrate(new long[]{30,30}, -1);
        switch (CfgData.selectIr) {
            case 0:
            case 1:
                SsSerivce.getInstance().setRushing(freq,wave);
                break;
            case 2:
                //if(wave.length)
                ConsumerIrManager ir=(ConsumerIrManager)getSystemService(CONSUMER_IR_SERVICE);
                ir.transmit(freq, wave);
                break;
        }
    }

    public interface OnDataEventer{
        void onSuc(byte[] data);
    }
    private void BackgroundPut(String urlparam,String body,final OnDataEventer aev){
        showwait();
        webHttpClientCom.getInstance(instance).RestkHttpCall(urlparam,body,"POST", new webHttpClientCom.RestOnWebPutEvent() {
            @Override
            public void onSuc(final byte[] out) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        hidedialog();
                        aev.onSuc(out);
                    }
                });
            }
            @Override
            public void onFail(final boolean isnet, final String res) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        hidedialog();
                        if(isnet){
                            ShowDialog(getString(R.string.str_err_net));
                        }else
                            ShowDialog(res);
                    }
                });
            }
        });
    }

    public void getAudioData(List<IrButton> buttons,int remoteType,int remoteid,OnDataEventer aev){
        if(buttons.size()==0){
            Toast.makeText(instance, "Err founded.Can not made PRC's data!", Toast.LENGTH_SHORT).show();
            return;
        }
        String s = CfgData.getButtonsString(buttons);
        if (s == null) {
            Toast.makeText(instance, "Err founded.Can not made PRC's data!", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.w(TAG_SS,"===>eep sorce:"+s+";"+remoteType+",remoteid="+remoteid);
        BackgroundPut("getAudioData?chip="+remoteType+"&force=0"+"&rmtid="+remoteid,s,aev);
    }

    public void getAudioWData(byte[] data,int addr,int remoteid,OnDataEventer aev){
        byte[] ret=null;
        if(data.length==0){
            Toast.makeText(instance, "Err founded.Can not made PRC's data!", Toast.LENGTH_SHORT).show();
            return;
        }
        String s ="";
        for (int i = 0; i < data.length; i++) {
            String sub = Integer.toBinaryString(data[i]&0xff);
            if(sub.length()==1)
                sub = "0"+sub;
            s+= sub;
        }
        Log.w(TAG_SS,"===>get audio write:"+s);
        BackgroundPut("getAudioWData?addr="+addr+"&rmtid="+remoteid,s,aev);
    }
}
