package clurc.net.longerir.activity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import clurc.net.longerir.R;
import clurc.net.longerir.Utils.AcUltils;
import clurc.net.longerir.Utils.SysFun;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.BtnInfo;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.RemoteInfo;
import clurc.net.longerir.ircommu.MadeRushData;
import clurc.net.longerir.uicomm.SsSerivce;

public class RemotePlayAcLearn extends BaseActivity {
    private RemoteInfo aremote;
    private List<BtnInfo> btnlist;

    private static int[] rid_btn= {R.id.tools0,R.id.tools1,R.id.tools2,R.id.tools3,R.id.subtemper,R.id.addtemper,R.id.btn_power,R.id.tools6,R.id.tools7};
    private ImageView mode0ImgView =null,mode1ImgView =null,mode2ImgView =null,mode3ImgView =null;
    private ImageView mode6ImgView =null,mode7ImgView =null;
    private TextView temperTxtView=null;
    private Button powerBtn;
    private int[] sta;

    private int btnidx;
    @Override
    public void getViewId(){
        layid = R.layout.activity_remote_playac_learn;
        int pos = getIntent().getExtras().getInt("pos");
        aremote = CfgData.myremotelist.get(pos);
        btnlist = CfgData.getBtnInfo(instance,aremote.id);
        title = aremote.pp;
    }

    @Override
    public void DoInit(){
        sta = new int[CfgData.Ac_Lear_Sta_Count];
        mode0ImgView = (ImageView) findViewById(R.id.mode0);
        mode1ImgView = (ImageView) findViewById(R.id.mode1);
        mode2ImgView = (ImageView) findViewById(R.id.mode2);
        mode3ImgView = (ImageView) findViewById(R.id.mode3);
        mode6ImgView = (ImageView) findViewById(R.id.mode6);
        mode7ImgView = (ImageView) findViewById(R.id.mode7);
        temperTxtView= (TextView) findViewById(R.id.wendutxt);
        for (int i = 0; i < rid_btn.length; i++) {
            ((Button)findViewById(rid_btn[i])).setOnClickListener(butnclick);
        }
        powerBtn = (Button)findViewById(rid_btn[6]);
        datatoUI();
        if(SysFun.getSensorState(instance) == 0){
            Toast.makeText(instance, getString(R.string.str_openrotate), Toast.LENGTH_SHORT).show();
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
        btnidx = -1;
        if(btnlist.size()>0){
            btnidx = 0;
            //寻找关的地方
            for (int i = 0; i < btnlist.size(); i++) {
                if (btnlist.get(i).params[0] == 0) {
                    btnidx = i;
                    break;
                }
            }
            for (int j = 0; j < btnlist.get(btnidx).params.length; j++)
                sta[j] = btnlist.get(btnidx).params[j];
        }
        datatoUI();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private int getIncValue(int btn,int v){
        int r;
        if(v>= (AcUltils.btnmax[btn]-1)){
            r = 0;
        }else{
            r = v+1;
        }
        return r;
    }
    private int lookPowIndex(int look_pow_value){
        int idx = -1;
        for (int i = 0; i < btnlist.size(); i++) {
            if(btnlist.get(i).params[0]==look_pow_value){
                idx = i;
                break;
            }
        }
        return idx;
    }
    //
    private int lookModeIndex(int curr_value){
        int idx = -1;
        int v = curr_value;
        while(true) {
            int next_value = getIncValue(2,v);
            if(next_value==curr_value)break;
            for (int i = 0; i < btnlist.size(); i++) {
                if (btnlist.get(i).params[0] == 1)
                if (btnlist.get(i).params[2] == next_value) {
                    idx = i;
                    break;
                }
            }
            if(idx>=0)break;
            v = next_value;
        }
        return idx;
    }
    //必须为打开，模式相同的才能调节
    private int lookOtherIndex(int btn,int curr_value){
        int idx = -1;
        int v = curr_value;
        while(true) {
            int next_value = getIncValue(btn,v);
            if(next_value==curr_value)break;
            for (int i = 0; i < btnlist.size(); i++) {
                BtnInfo abtn = btnlist.get(i);
                if (abtn.params[0] == 1 && abtn.params[2] == sta[2])
                    if (abtn.params[btn] == next_value) {
                        idx = i;
                        break;
                    }
            }
            if(idx>=0)break;
            v = next_value;
        }
        return idx;
    }
    //温度增加寻找
    private int lookTempInc(int curr_value){
        int idx = -1;
        int next_value = curr_value;
        while(true) {
            next_value++;
            if(next_value>14)break;
            for (int i = 0; i < btnlist.size(); i++) {
                BtnInfo abtn = btnlist.get(i);
                if (abtn.params[0] == 1 && abtn.params[2] == sta[2])
                    if (abtn.params[1] == next_value) {
                        idx = i;
                        break;
                    }
            }
            if(idx>=0)break;
        }
        return idx;
    }
    private int lookTempDec(int curr_value){
        int idx = -1;
        int next_value = curr_value;
        while(true) {
            next_value--;
            if(next_value<0)break;
            for (int i = 0; i < btnlist.size(); i++) {
                BtnInfo abtn = btnlist.get(i);
                if (abtn.params[0] == 1 && abtn.params[2] == sta[2])
                    if (abtn.params[1] == next_value) {
                        idx = i;
                        break;
                    }
            }
            if(idx>=0)break;
        }
        return idx;
    }

    private View.OnClickListener butnclick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int btn = Integer.valueOf((String) view.getTag());
            //----------------用户操作按键改变sta--------------------
            int idx=-1;
            switch (btn){
                case 0:
                    idx = lookPowIndex(getIncValue(0,sta[btn]));
                    if(idx>=0)btnidx=idx;
                    break;
                case 2:
                    idx = lookModeIndex(sta[btn]);
                    if(idx>=0)btnidx=idx;
                    break;
                case 1:  //temp-
                    idx = lookTempDec(sta[btn]);
                    if(idx>=0)btnidx=idx;
                    break;
                case 88:  //temp+
                    btn = 1;
                    idx = lookTempInc(sta[btn]);
                    if(idx>=0)btnidx=idx;
                    break;
                default:
                    idx = lookOtherIndex(btn,sta[btn]);
                    if(idx>=0)btnidx=idx;
                    break;
            }
            if(btnidx>=0 && btnidx<btnlist.size()) {
                BtnInfo abtn = btnlist.get(btnidx);
                if (CheckIrDaPort(SsSerivce.getInstance().getHidState(), SsSerivce.getInstance().getBlvState())) {
                    String[] w = abtn.wave.split(",");
                    int[] wave =  new int[w.length];
                    for (int i = 0; i < w.length; i++) {
                        wave[i] = Math.abs(Integer.parseInt(w[i]));
                    }
                    dorushWave(wave,abtn.keyidx);
                } else {
                    showToast(getString(R.string.str_nodev));
                }
                for (int i = 0; i < sta.length; i++) {
                    sta[i] = abtn.params[i];
                }
            }else{
                showToast("null data!");
            }
            //--------------------根据改变后的sta设置各状态显示-------------------------
            datatoUI();
        }
    };

    private int powstate = -1;
    private void datatoUI(){
        if(powstate!=sta[0]){
            powstate = sta[0];
            if(powstate==0){
                mode0ImgView.setVisibility(View.GONE);
                mode1ImgView.setVisibility(View.GONE);
                mode2ImgView.setVisibility(View.GONE);
                mode3ImgView.setVisibility(View.GONE);
                mode6ImgView.setVisibility(View.GONE);
                mode7ImgView.setVisibility(View.GONE);
                temperTxtView.setVisibility(View.GONE);
                return;
            }else{
                mode0ImgView.setVisibility(View.VISIBLE);
                mode1ImgView.setVisibility(View.VISIBLE);
                mode2ImgView.setVisibility(View.VISIBLE);
                mode3ImgView.setVisibility(View.VISIBLE);
                mode6ImgView.setVisibility(View.VISIBLE);
                mode7ImgView.setVisibility(View.VISIBLE);
                temperTxtView.setVisibility(View.VISIBLE);
            }
        }
        switch(sta[2]){
            case 0:
                mode0ImgView.setBackground(getResources().getDrawable(R.drawable.yk_air_a));
                temperTxtView.setText("--℃");
                break;
            case 1:
                mode0ImgView.setBackground(getResources().getDrawable(R.drawable.yk_air_r));
                temperTxtView.setText(String.valueOf(sta[1]+16)+"℃");
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
                temperTxtView.setText(String.valueOf(sta[1]+16)+"℃");
                break;
        }

        switch(sta[3]){
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
        mode2ImgView.setBackground(getResources().getDrawable(R.drawable.yk_air_u0+sta[4]));
        mode3ImgView.setBackground(getResources().getDrawable(R.drawable.yk_air_l0+sta[5]));
        if(sta[6]==1){
            mode6ImgView.setVisibility(View.VISIBLE);
        }else
            mode6ImgView.setVisibility(View.GONE);
        if(sta[7]==1){
            mode7ImgView.setVisibility(View.VISIBLE);
        }else
            mode7ImgView.setVisibility(View.GONE);
        if(sta[0]==0){
            powerBtn.setBackground(getResources().getDrawable(R.drawable.btn_selected_power));
        }else{
            powerBtn.setBackground(getResources().getDrawable(R.drawable.btn_unselected_power));
        }
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
