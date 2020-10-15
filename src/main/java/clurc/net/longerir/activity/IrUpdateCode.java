package clurc.net.longerir.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.widget.QMUIProgressBar;

import clurc.net.longerir.R;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.uicomm.SsSerivce;
import pl.droidsonroids.gif.GifImageView;

import static clurc.net.longerir.uicomm.SsSerivce.TAG_SS;

public class IrUpdateCode extends BaseActivity {
    private QMUIProgressBar mCircleProgressBar;
    private Button mbtn;
    private GifImageView donghua;
    private TextView currremote;
    private TextView tips;
    private ProgressBar mgotble;

    private boolean busb = false;
    private boolean bble = false;

    private int[] codes;
    private int desidx;
    @Override
    public void getViewId() {
        layid = R.layout.activity_hid_commu;
        title = "Ir Communication";
    }

    @Override
    public void DoInit() {
        mCircleProgressBar = findViewById(R.id.circleProgressBar);
        mbtn = findViewById(R.id.startBtn);
        donghua = findViewById(R.id.imgdonghua);
        currremote = findViewById(R.id.remoteid);
        tips = findViewById(R.id.tips);
        desidx = getIntent().getExtras().getInt("des");
        currremote.setText(CfgData.modellist.get(desidx).name);
        mgotble = findViewById(R.id.blebar);
//
        codes = new int[4];
        for (int i = 0; i < 4; i++) {
            codes[i] = getIntent().getExtras().getInt("codes"+i);
        }
        //
        mCircleProgressBar.setQMUIProgressBarTextGenerator(new QMUIProgressBar.QMUIProgressBarTextGenerator() {
            @Override
            public String generateText(QMUIProgressBar progressBar, int value, int maxValue) {
                return 100 * value / maxValue + "%";
            }
        });
        mbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CheckIrDaPort(busb,bble)){
                    mCircleProgressBar.setVisibility(View.GONE);
                    donghua.setVisibility(View.VISIBLE);
                    mCircleProgressBar.setProgress(0,false);
                    if(CfgData.selectIr==0 || CfgData.selectIr==1){
                        PrepareDataAndDown();
                        tips.setText("");
                    }else if(CfgData.selectIr==2){
                        MobilePortDown();
                    }else{
                        AudioPortDown();
                    }
                    mbtn.setVisibility(View.GONE);
                }else{
                    tips.setText(getString(R.string.str_nodev));
                    showMessage(null,getString(R.string.str_nodev));
                }
            }
        });
        mTopBar.addRightImageButton(R.mipmap.icon_topbar_overflow, R.id.topbar_right_change_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setClass(instance, SelectIrPort.class);
                        intent.putExtra("baudio",false);
                        intent.putExtra("upcode",true);
                        startActivity(intent);
                    }
                });
    }

    @Override
    public void DoServiceMesg(int cmd, Intent intent) {
        switch (cmd) {
            // 在线情况
            case SsSerivce.BROD_CMDUI_HIDSTATE:
                busb = (boolean) intent.getSerializableExtra("hid");
                bble = (boolean) intent.getSerializableExtra("blv");
                if(CheckIrDaPort(busb,bble)){
                    tips.setText(getString(R.string.str_ready));
                    mgotble.setVisibility(View.GONE);
                }else{
                    tips.setText(getString(R.string.str_nodev));
                    if(bble==false && (CfgData.selectIr==1)){
                        mgotble.setVisibility(View.VISIBLE);
                    }
                }
                break;
            // 百分比显示
            case SsSerivce.BROD_CMDUI_PRCPERCENT:
                setProgressBar((int) intent.getSerializableExtra("obj"));
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        donghua.setVisibility(View.VISIBLE);
        mCircleProgressBar.setVisibility(View.GONE);
        mbtn.setVisibility(View.VISIBLE);
        if(CfgData.selectIr==1){
            CfgData.selectIr = 0;
        }
        if(bble==false && (CfgData.selectIr==1)){
            mgotble.setVisibility(View.VISIBLE);
        }else{
            mgotble.setVisibility(View.GONE);
        }
        if(CfgData.selectIr==0){
            donghua.setImageResource(R.mipmap.prc_donghua);
        }else if(CfgData.selectIr==1){
            donghua.setImageResource(R.mipmap.prc_ble);
        }else if(CfgData.selectIr==2){
            donghua.setImageResource(R.mipmap.prc_mobile);
        }else{
            donghua.setImageResource(R.mipmap.prc_audio);
        }
        if(CfgData.selectIr<2) {
            SsSerivce.getInstance().setStateBroadcast();
        }
//        ConsumerIrManager ir=(ConsumerIrManager)getSystemService(CONSUMER_IR_SERVICE);
//        ConsumerIrManager.CarrierFrequencyRange[] afre= ir.getCarrierFrequencies();
//        String s = "";
//        for (int i = 0; i < afre.length; i++) {
//            s = s + " :"+afre[i].getMinFrequency() + "," + afre[i].getMaxFrequency();
//        }
//        tips.setText(s);

        super.onResume();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            setProgressBar(msg.arg1);
            //
            super.handleMessage(msg);
        }
    };

    private void MobilePortDown(){
        mCircleProgressBar.setVisibility(View.VISIBLE);
        donghua.setVisibility(View.GONE);
        ///开始调用主转换函数
        byte[] data = getPrcData();
        mCircleProgressBar.setProgress(0,false);
        DoIrCodes(data, mHandler);
    }

    private void AudioPortDown(){
        int remotekey = CfgData.modellist.get(desidx).id;
        mCircleProgressBar.setVisibility(View.VISIBLE);
        donghua.setVisibility(View.GONE);
        ///开始调用主转换函数
        Log.w(TAG_SS, "开锁键："+Integer.toHexString(remotekey));
        byte[] src = getPureData();
        getAudioWData(src,0x40,remotekey, new OnDataEventer() {
            @Override
            public void onSuc(byte[] data) {
                mCircleProgressBar.setProgress(0,false);
                playMp3(data,mHandler);
            }
        });
    }

    private void PrepareDataAndDown(){
        byte[] data = getPrcData();
        if(data==null)return;
        mCircleProgressBar.setProgress(0,false);
        SsSerivce.getInstance().setCodeStart(data);
    }

    @Override
    public void onStop() {
        SsSerivce.getInstance().irStop();
        super.onStop();
    }

    private void setProgressBar(int percent){
        mCircleProgressBar.setProgress(percent,false);
        if(percent>=100){
            mCircleProgressBar.setVisibility(View.GONE);
            donghua.setVisibility(View.VISIBLE);
            mbtn.setVisibility(View.VISIBLE);
            Toast.makeText(instance, getString(R.string.str_prcsuc), Toast.LENGTH_SHORT).show();
        }else if(percent>0){
            if(mCircleProgressBar.getVisibility()==View.GONE)
                mCircleProgressBar.setVisibility(View.VISIBLE);
            if(donghua.getVisibility()==View.VISIBLE)
                donghua.setVisibility(View.GONE);
        }
    }

    private byte[] getPrcData(){
        byte[] buf = new byte[16];

        buf[0] = (byte) 0xcf;
        buf[1] = (byte) 0x40;
        buf[2] = (byte) 0x0;
        //--
        buf[3] = (byte) (codes[0]&0xff);
        buf[4] = (byte) ((codes[0] >> 8)&0xff);
        buf[5] = (byte) (codes[1]&0xff);
        buf[6] = (byte) ((codes[1] >> 8)&0xff);
//---
        int crc = 0;
        for (int i = 0; i < 7; i++) {
            crc += (buf[i]&0xff);
        }
        buf[7] = (byte)(crc & 0xff);
//-----------
        buf[8] = (byte) 0xcf;
        buf[9] = (byte) 0x44;
        buf[10] = (byte) 0x0;
        //--
        buf[11] = (byte) (codes[2]&0xff);
        buf[12] = (byte) ((codes[2] >> 8)&0xff);
        buf[13] = (byte) (codes[3]&0xff);
        buf[14] = (byte) ((codes[3] >> 8)&0xff);
//---
        crc = 0;
        for (int i = 8; i < 15; i++) {
            crc += (buf[i]&0xff);
        }
        buf[15] = (byte)(crc & 0xff);

        return buf;
    }

    private byte[] getPureData(){
        byte[] buf = new byte[8];

        buf[0] = (byte) (codes[0]&0xff);
        buf[1] = (byte) ((codes[0] >> 8)&0xff);
        buf[2] = (byte) (codes[1]&0xff);
        buf[3] = (byte) ((codes[1] >> 8)&0xff);

        buf[4] = (byte) (codes[2]&0xff);
        buf[5] = (byte) ((codes[2] >> 8)&0xff);
        buf[6] = (byte) (codes[3]&0xff);
        buf[7] = (byte) ((codes[3] >> 8)&0xff);
        return buf;
    }
}
