package clurc.net.longerir.activity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

import clurc.net.longerir.Utils.SysFun;
import clurc.net.longerir.data.TxtBtnInfo;
import clurc.net.longerir.R;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.BtnInfo;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.RemoteInfo;
import clurc.net.longerir.uicomm.SsSerivce;
import clurc.net.longerir.view.DraggableGridViewNew;
import clurc.net.longerir.view.OnRearrangeListener;

public class RemotePlay extends BaseActivity {
    private DraggableGridViewNew DGViewNew;
    private RemoteInfo aremote;
    private List<BtnInfo> btnlist;

    @Override
    public void getViewId(){
        layid = R.layout.activity_remote_play;
        int pos = getIntent().getExtras().getInt("pos");
        aremote = CfgData.myremotelist.get(pos);
        title = aremote.pp;
    }

    @Override
    public void DoInit(){
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        btnlist = CfgData.getBtnInfo(instance,aremote.id);
        DGViewNew = findViewById(R.id.vgv);
        DGViewNew.setViewInfoList(btnlist);
        int size=btnlist.size();
        for(int i=0;i<size;i++){
            if(btnlist.get(i).gsno<0 || btnlist.get(i).params==null)
                continue;
            ImageView image = new ImageView(instance);
            Bitmap bmp=CfgData.getBtnBmp(instance,btnlist.get(i).btnname,btnlist.get(i).imgpath,false);
            image.setImageBitmap(bmp);
            image.setTag(i);
            DGViewNew.addView(image);
        }
        DGViewNew.setOnRearrangeListener(new OnRearrangeListener() {
            public void onRearrange(int oldIndex, int newIndex) {
            }
        });
        DGViewNew.setOnClickListener(new View.OnClickListener() {//----------发码
            @Override
            public void onClick(View v) {
                if(!CheckIrDaPort(SsSerivce.getInstance().getHidState(),SsSerivce.getInstance().getBlvState())){
                    showMessage(null,getString(R.string.str_nodev));
                    return;
                }
                if(v.getTag()==null)return;
                int idx = (int)v.getTag();
                if(idx>btnlist.size())return;
                //--------------  rushing
                BtnInfo btn = btnlist.get(idx);
                TxtBtnInfo arush = new TxtBtnInfo();
                arush.param = btn.params;
                arush.gsno = btn.gsno;
                arush.keyidx = btn.keyidx;
                RushingKey(arush);
            }
        });

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
        if(DGViewNew.getChanged()){
            CfgData.SaveBtnposChanged(instance,btnlist);
        }
        super.onStop();
    }
}
