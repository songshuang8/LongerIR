package clurc.net.longerir.activity;

import android.content.Intent;
import android.hardware.ConsumerIrManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.widget.QMUIProgressBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import clurc.net.longerir.R;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.webHttpClientCom;
import clurc.net.longerir.uicomm.MobileIrPort;
import clurc.net.longerir.uicomm.SsSerivce;
import pl.droidsonroids.gif.GifImageView;

import static clurc.net.longerir.uicomm.SsSerivce.TAG_SS;

public class PrcComuni extends BaseActivity {
    private QMUIProgressBar mCircleProgressBar;
    private Button mbtn;
    private GifImageView donghua;
    private TextView currremote;
    private TextView tips;
    private ProgressBar mgotble;

    private boolean busb = false;
    private boolean bble = false;

    private int desidx;
    private boolean notaudio=false;
    private boolean nobleremote = false;
    private int chipkind;

    private MobileIrPort amoibleir = null;

    @Override
    public void getViewId() {
        layid = R.layout.activity_hid_commu;
        desidx = getIntent().getIntExtra("desidx",-1);
        if(desidx<0){
            Toast.makeText(instance, "Unknown err", Toast.LENGTH_SHORT).show();
            return;
        }
        chipkind = CfgData.modellist.get(desidx).chip;
        if(chipkind == 11){
            Toast.makeText(instance, "Unknown err", Toast.LENGTH_SHORT).show();
            return;
        }
        notaudio = (chipkind == 1 || chipkind == 4 || chipkind == 5 || chipkind == 7 || chipkind == 9) ? false : true;
        if (notaudio) {
            if (CfgData.selectIr == 3)
                CfgData.selectIr = 0;
        }
        nobleremote =false;// (chipkind==10)?false:true; //蓝牙也可以
        title = getString(R.string.str_data_download);
    }

    @Override
    public void DoInit() {
        if(CfgData.desbuttons.size()==0){
            Toast.makeText(instance, "Err founded.Can not made PRC's data!", Toast.LENGTH_SHORT).show();
            return;
        }
        mCircleProgressBar = findViewById(R.id.circleProgressBar);
        mbtn = findViewById(R.id.startBtn);
        donghua = findViewById(R.id.imgdonghua);
        currremote = findViewById(R.id.remoteid);
        tips = findViewById(R.id.tips);

        if(chipkind==10){
            currremote.setText(CfgData.modellist.get(desidx).name + "　　" + getString(R.string.str_ble_guide));
            currremote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new QMUIDialog.MessageDialogBuilder(instance)
                            .setMessage(getString(R.string.str_ble_guide_desc))
                            .addAction("Ok", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                }
                            })
                            .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
                }
            });
        }else {
            currremote.setText(CfgData.modellist.get(desidx).name);
        }

        mgotble = findViewById(R.id.blebar);

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
                    mCircleProgressBar.setVisibility(View.VISIBLE);
                    donghua.setVisibility(View.GONE);
                    mCircleProgressBar.setProgress(0,false);
                    if(CfgData.selectIr==0){
                        SsSerivce.getInstance().irReadInfo();
                        tips.setText("");
                    }else if(CfgData.selectIr==1) {
                        PrepareDataAndDown(10);
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
                        intent.putExtra("baudio",notaudio);
                        intent.putExtra("nobleremote",nobleremote);
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
                //获取到下载遥控器的信息
            case SsSerivce.BROD_CMDUI_RMTINFO:
                int rmtid = (int) intent.getSerializableExtra("rmtid");
                final int rmttype = (int) intent.getSerializableExtra("rmttype");

                int desid = CfgData.modellist.get(desidx).id;
                if(rmtid==desid){
                    PrepareDataAndDown(rmttype);
                    break;
                }
                //
                String srcname = CfgData.getMougleName(rmtid);
                if(srcname==null){
                    PrepareDataAndDown(rmttype);
                    break;
                }

                new QMUIDialog.MessageDialogBuilder(instance)
                            .setTitle("Infomation")
                            .setMessage(String.format(getString(R.string.str_notthis),srcname))
                            .addAction("Cancel", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                    finish();
                                }
                            })
                            .addAction("Ok", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                    PrepareDataAndDown(rmttype);
                                }
                            })
                            .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
                break;
            case SsSerivce.BROD_CMDUI_BLUEMODEL:
                String modelstr = (String) intent.getSerializableExtra("model");
                PrepareDataAndDown(10);
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

        if(notaudio){
            if (CfgData.selectIr == 3)
                CfgData.selectIr = 0;
        }
        if(nobleremote){
            if (CfgData.selectIr == 1)
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
        SsSerivce.getInstance().setStateBroadcast();
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
        int chip =CfgData.modellist.get(desidx).chip;
        mCircleProgressBar.setVisibility(View.VISIBLE);
        donghua.setVisibility(View.GONE);
        ///开始调用主转换函数
        webHttpClientCom.getInstance(instance).WebGetEepData(CfgData.desbuttons, chip, new webHttpClientCom.RestOnEEpData() {
            @Override
            public void onSuc(byte[] eep) {
                mCircleProgressBar.setProgress(0,false);
                amoibleir = new MobileIrPort(instance,eep,mHandler);
                amoibleir.DoStart();
            }
        });
    }

    private void AudioPortDown(){
        int chipkind = CfgData.modellist.get(desidx).chip;
        int remotekey = CfgData.modellist.get(desidx).id;
        mCircleProgressBar.setVisibility(View.VISIBLE);
        donghua.setVisibility(View.GONE);
        ///开始调用主转换函数
        Log.w(TAG_SS, "芯片类型："+chipkind+","+Integer.toHexString(remotekey));
        getAudioData(CfgData.desbuttons, chipkind, remotekey, new OnDataEventer() {
            @Override
            public void onSuc(byte[] data) {
                mCircleProgressBar.setProgress(0,false);
                playMp3(data,mHandler);
            }
        });
    }

    private void PrepareDataAndDown(int rmttype){
        webHttpClientCom.getInstance(instance).WebGetEepData(CfgData.desbuttons, rmttype, new webHttpClientCom.RestOnEEpData() {
            @Override
            public void onSuc(byte[] eep) {
                mCircleProgressBar.setProgress(0,false);
                SsSerivce.getInstance().setRRCStart(eep);
            }
        });
    }

    @Override
    public void onStop() {
        SsSerivce.getInstance().irStop();
        if(amoibleir!=null)
            if(amoibleir.getActived())
                amoibleir.setCancel();
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
}