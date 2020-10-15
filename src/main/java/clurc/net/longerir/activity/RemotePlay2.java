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
import clurc.net.longerir.view.RemoteBtnView;
import clurc.net.longerir.view.ViewDragGrid;
import clurc.net.longerir.view.ViewDragGridReadOnly;

public class RemotePlay2 extends BaseActivity {
    private ViewDragGrid DGViewNew;
    private RemoteInfo aremote;
    private List<BtnInfo> btnlist;
    private int remoteidx;

    @Override
    public void getViewId(){
        layid = R.layout.activity_remote_play;
        remoteidx = getIntent().getExtras().getInt("pos");
        aremote = CfgData.myremotelist.get(remoteidx);
        title = aremote.pp;
    }

    private int getBtnRowsCount(int arow){
        int m = 0;
        for (int i = 0; i < btnlist.size(); i++) {
            if(btnlist.get(i).row==arow){
                m++;
            }
        }
        return m;
    }

    private int getMaxRowsCount(){
        int m = 0;
        for (int i = 0; i < btnlist.size(); i++) {
            if(btnlist.get(i).row>m){
                m = btnlist.get(i).row;
            }
        }
        return m;
    }

    @Override
    public void DoInit(){
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        btnlist = CfgData.getBtnInfo(instance,aremote.id);
        DGViewNew = findViewById(R.id.vgv);
        if(!CfgData.myremotelist.get(remoteidx).islearned) {
            DGViewNew.setAline(true);
            // ---去除空行
            int maxrow = getMaxRowsCount();
            int p = 0;
            for (int i = 0; i <= maxrow; i++) {
                if(getBtnRowsCount(i)==0)
                    continue;
                for (int j = 0; j < btnlist.size(); j++) {
                    if(btnlist.get(j).row==i){
                        btnlist.get(j).row = p;
                    }
                }
                p++;
            }
        }
        //
        int col_int = 0;
        for(int i=0;i<btnlist.size();i++){
            if(btnlist.get(i).col>col_int)
                col_int = btnlist.get(i).col;
        }
        col_int++;
        DGViewNew.setCOL_CNT(col_int);
        //
        for(int i=0;i<btnlist.size();i++){
            RemoteBtnView image = new RemoteBtnView(instance,
                    getResources().getColor(android.R.color.darker_gray),
                    getResources().getColor(android.R.color.holo_blue_dark),true);
            image.setBtnName(btnlist.get(i).btnname);
            image.setCustomName(btnlist.get(i).btnname);
            image.setRownCol(btnlist.get(i).col,btnlist.get(i).row);
            image.setmShapeKinds(RemoteBtnView.ShapeKinds.values()[btnlist.get(i).shapekinds]);
            image.setKeyidx(i);
            if(btnlist.get(i).gsno>=0)
                image.setmSelected(true);
            else
                image.setmSelected(false);
            DGViewNew.addView(image);
        }

        DGViewNew.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!CheckIrDaPort(SsSerivce.getInstance().getHidState(),SsSerivce.getInstance().getBlvState())){
                    showMessage(null,getString(R.string.str_nodev));
                    return;
                }
                int idx = ((RemoteBtnView)v).getKeyidx();
                if(idx>btnlist.size())return;
                //--------------  rushing
                BtnInfo btn = btnlist.get(idx);
                if(btn.gsno<0)return;
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
        super.onStop();
    }
}
