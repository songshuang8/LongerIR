package clurc.net.longerir.activity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import clurc.net.longerir.MainActivity;
import clurc.net.longerir.R;
import clurc.net.longerir.Utils.SysFun;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.ClassAcStatus;
import clurc.net.longerir.data.RemoteInfo;
import clurc.net.longerir.uicomm.SsSerivce;

public class RemotePlayAc extends BaseActivity{
    private RemoteInfo aremote;
    private ClassAcStatus sta;
    private boolean bchanged=false; //调节参数改变
    byte[]  rawdata;

    private static int[] rid_btn= {R.id.tools0,R.id.tools1,R.id.tools2,R.id.tools3,R.id.subtemper,R.id.addtemper,R.id.btn_power};
    private ImageView mode0ImgView =null,mode1ImgView =null,mode2ImgView =null,mode3ImgView =null;
    private TextView temperTxtView=null;
    private Button powerBtn;

    @Override
    public void getViewId(){
        layid = R.layout.activity_remote_playac;
        int pos = getIntent().getExtras().getInt("pos");
        aremote = CfgData.myremotelist.get(pos);
        title = aremote.pp;
    }

    @Override
    public void DoInit(){
        String strdata = aremote.acdata;
        sta = new ClassAcStatus();
        rawdata = android.util.Base64.decode(strdata,android.util.Base64.DEFAULT);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        mode0ImgView = (ImageView) findViewById(R.id.mode0);
        mode1ImgView = (ImageView) findViewById(R.id.mode1);
        mode2ImgView = (ImageView) findViewById(R.id.mode2);
        mode3ImgView = (ImageView) findViewById(R.id.mode3);
        temperTxtView= (TextView) findViewById(R.id.wendutxt);
        for (int i = 0; i < rid_btn.length; i++) {
            ((Button)findViewById(rid_btn[i])).setOnClickListener(buttonlis);
        }
        powerBtn = (Button)findViewById(rid_btn[6]);
        if(sta.getBtnStaus(0)==0){
            powerBtn.setBackground(getResources().getDrawable(R.drawable.btn_selected_power));
        }else{
            powerBtn.setBackground(getResources().getDrawable(R.drawable.btn_unselected_power));
        }
        datatoUI();
        if(SysFun.getSensorState(instance) == 0){
            Toast.makeText(instance, getString(R.string.str_openrotate), Toast.LENGTH_SHORT).show();
            // Settings.System.putInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION, 1);
        }
        mTopBar.addRightImageButton(R.mipmap.icon_topbar_overflow, R.id.topbar_right_change_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setClass(instance, SelectIrPort.class);
                        intent.putExtra("baudio",true);
                        startActivity(intent);
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public byte[] ToClick(int which) {
        byte[] staval = null;
        byte[] statusArr = new byte[ClassAcStatus.BTN_COUNT];
        for (int i = 0; i < ClassAcStatus.BTN_COUNT; i++)
            statusArr[i] = (byte)sta.getBtnStaus(i);
        if(RushAc(rawdata,statusArr,which,sta.getPressVal())){
            staval = MainActivity.irGetStatus();
        }
        return staval;
    }

    private View.OnClickListener buttonlis = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(!CheckIrDaPort(SsSerivce.getInstance().getHidState(),SsSerivce.getInstance().getBlvState())){
                showToast(getString(R.string.str_nodev));
                return;
            }
            int select = Integer.valueOf((String) view.getTag());
            sta.setPressBtn(select);
            if(select==1){  //temp-
                sta.setPressval(1);
                sta.setPressBtn(1);
                sta.setBtnDec(1);
            }else
            if(select==88){ //temp +
                select = 1;
                sta.setPressval(0);
                sta.setPressBtn(1);
                sta.setBtnInc(1,false);
            }else{
                sta.setPressval(0);
                sta.setBtnInc(select,true);
            }
            if(select==0){
                if(sta.getBtnStaus(0)==0){
                    powerBtn.setBackground(getResources().getDrawable(R.drawable.btn_selected_power));
                }else{
                    powerBtn.setBackground(getResources().getDrawable(R.drawable.btn_unselected_power));
                }
            }
            //--
            byte[] currstatusval = ToClick(select);
            if(currstatusval!=null){
                for (int i = 0; i < currstatusval.length; i++) {
                    sta.setBtnStaus(i,currstatusval[i]);
                }
            }
            bchanged=true;
            datatoUI();
        }
    };

    private void datatoUI(){
        switch(sta.getBtnStaus(2)){
            case 0:
                mode0ImgView.setBackground(getResources().getDrawable(R.drawable.yk_air_a));
                temperTxtView.setText("--℃");
                break;
            case 1:
                mode0ImgView.setBackground(getResources().getDrawable(R.drawable.yk_air_r));
                temperTxtView.setText(String.valueOf(sta.getBtnStaus(1)+16)+"℃");
                break;
            case 2:
                mode0ImgView.setBackground(getResources().getDrawable(R.drawable.yk_air_d));
                temperTxtView.setText("--℃");
                break;
            case 3:
                mode0ImgView.setBackground(getResources().getDrawable(R.drawable.yk_air_w));
                temperTxtView.setText("--℃");
                break;
            case 4:
                mode0ImgView.setBackground(getResources().getDrawable(R.drawable.yk_air_h));
                temperTxtView.setText(String.valueOf(sta.getBtnStaus(1)+16)+"℃");
                break;
        }

        switch(sta.getBtnStaus(3)){
            case 0:
                mode1ImgView.setBackground(getResources().getDrawable(R.drawable.yk_air_s0));
                break;
            case 1:
                mode1ImgView.setBackground(getResources().getDrawable(R.drawable.yk_air_s3));
                break;
            case 2:
                mode1ImgView.setBackground(getResources().getDrawable(R.drawable.yk_air_s2));
                break;
            case 3:
                mode1ImgView.setBackground(getResources().getDrawable(R.drawable.yk_air_s1));
                break;
        }
        mode2ImgView.setBackground(getResources().getDrawable(R.drawable.yk_air_u0+sta.getBtnStaus(4)));
        mode3ImgView.setBackground(getResources().getDrawable(R.drawable.yk_air_l0+sta.getBtnStaus(5)));
    }

    @Override
    public void DoServiceMesg(int cmd, Intent intent) {
        switch (cmd) {
            // 在线情况
            case SsSerivce.BROD_CMDUI_HIDSTATE:
                //busb = (boolean) intent.getSerializableExtra("hid");
                //bble = (boolean) intent.getSerializableExtra("blv");
                break;
        }
    }
}