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
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import clurc.net.longerir.R;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.CfgData;

import clurc.net.longerir.uicomm.MobileIrPort;
import clurc.net.longerir.uicomm.SsSerivce;
import pl.droidsonroids.gif.GifImageView;

import static clurc.net.longerir.uicomm.SsSerivce.TAG_SS;


public class PrcAcCommu extends BaseActivity {
    private QMUIProgressBar mCircleProgressBar;
    private Button mStarBtn;
    private GifImageView donghua;
    private TextView currremote;
    private TextView tips;
    private ProgressBar mGotBle;

    private boolean busb = false;
    private boolean bble = false;

    private boolean notaudio=false;
    private boolean nobleremote = false;

    private byte[] eepdata;

    private MobileIrPort amoibleir = null;
    @Override
    public void getViewId() {
        layid = R.layout.activity_hid_commu;

        notaudio = true;
        nobleremote = true;
        if(CfgData.selectIr==1)
            CfgData.selectIr = 0;
        title = getString(R.string.str_data_download);
    }

    @Override
    public void DoInit() {
        String prcdatastr = getIntent().getStringExtra("prcdata");
        if(prcdatastr==null || prcdatastr.length()==0){
            Toast.makeText(instance, "Unknown err", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.w(TAG_SS,"===>ac prc data:"+prcdatastr);
        eepdata = CfgData.StringsToByteArr(prcdatastr);

        mCircleProgressBar = findViewById(R.id.circleProgressBar);
        mStarBtn = findViewById(R.id.startBtn);
        donghua = findViewById(R.id.imgdonghua);
        currremote = findViewById(R.id.remoteid);
        tips = findViewById(R.id.tips);
        int desidx = getIntent().getIntExtra("desidx",-1);
        if(desidx<0){
            currremote.setText("");
        }else {
            currremote.setText(CfgData.modellist.get(desidx).name);
        }
        mGotBle = findViewById(R.id.blebar);

        mCircleProgressBar.setQMUIProgressBarTextGenerator(new QMUIProgressBar.QMUIProgressBarTextGenerator() {
            @Override
            public String generateText(QMUIProgressBar progressBar, int value, int maxValue) {
                return 100 * value / maxValue + "%";
            }
        });
        mStarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CheckIrDaPort(busb,bble)){
                    mCircleProgressBar.setVisibility(View.VISIBLE);
                    donghua.setVisibility(View.GONE);
                    mCircleProgressBar.setProgress(0,false);
                    if(CfgData.selectIr==0){
                        SsSerivce.getInstance().irReadInfo();
                        tips.setText("");
                    }else if(CfgData.selectIr==2){
                        mCircleProgressBar.setProgress(0,false);
                        amoibleir = new MobileIrPort(instance,eepdata,mHandler);
                        amoibleir.DoStart();
                    }
                    mStarBtn.setVisibility(View.GONE);
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
                    mGotBle.setVisibility(View.GONE);
                }else{
                    tips.setText(getString(R.string.str_nodev));
                    if(bble==false && (CfgData.selectIr==1)){
                        mGotBle.setVisibility(View.VISIBLE);
                    }
                }
                break;
            //获取到下载遥控器的信息
            case SsSerivce.BROD_CMDUI_RMTINFO:
                int rmtid = (int) intent.getSerializableExtra("rmtid");
                final int rmttype = (int) intent.getSerializableExtra("rmttype");
                //不是空调遥控器
                if(rmttype!=11){
                    Toast.makeText(instance, getString(R.string.str_desremote_error), Toast.LENGTH_SHORT).show();
                    return;
                }

                mCircleProgressBar.setProgress(0,false);
                SsSerivce.getInstance().setRRCStart(eepdata);
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
        super.onResume();

        donghua.setVisibility(View.VISIBLE);
        mCircleProgressBar.setVisibility(View.GONE);
        mStarBtn.setVisibility(View.VISIBLE);

        if(notaudio){
            if (CfgData.selectIr == 3)
                CfgData.selectIr = 0;
        }
        if(nobleremote){
            if (CfgData.selectIr == 1)
                CfgData.selectIr = 0;
        }

        if(bble==false && (CfgData.selectIr==1)){
            mGotBle.setVisibility(View.VISIBLE);
        }else{
            mGotBle.setVisibility(View.GONE);
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
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            setProgressBar(msg.arg1);
            //
            super.handleMessage(msg);
        }
    };

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
            mStarBtn.setVisibility(View.VISIBLE);
            Toast.makeText(instance, getString(R.string.str_prcsuc), Toast.LENGTH_SHORT).show();
        }else if(percent>0){
            if(mCircleProgressBar.getVisibility()==View.GONE)
                mCircleProgressBar.setVisibility(View.VISIBLE);
            if(donghua.getVisibility()==View.VISIBLE)
                donghua.setVisibility(View.GONE);
        }
    }
}