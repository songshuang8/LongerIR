package clurc.net.longerir.activity;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;

import java.util.List;

import clurc.net.longerir.Utils.SysFun;
import clurc.net.longerir.data.BtnModelData;
import clurc.net.longerir.data.TxtBtnInfo;
import clurc.net.longerir.R;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.BtnInfo;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.RemoteInfo;
import clurc.net.longerir.uicomm.SsSerivce;
import clurc.net.longerir.view.OnRearrangeListener;
import clurc.net.longerir.view.RemoteBtnView;
import clurc.net.longerir.view.ViewBtnPlaying;
import clurc.net.longerir.view.ViewPlayGird;

public class RemotePlay extends BaseActivity {
    private ViewPlayGird DGViewNew;
    private RemoteInfo aremote;
    private List<BtnInfo> btnlist;
    private int remoteidx;

    @Override
    public void getViewId(){
        layid = R.layout.activity_remote_play;
        remoteidx = getIntent().getExtras().getInt("pos");
        aremote = CfgData.myremotelist.get(remoteidx);
        title = aremote.pp + " " + aremote.xh;
    }

    @Override
    public void DoInit(){
        DGViewNew = findViewById(R.id.vgv);
        DGViewNew.setFixdcolrow(false);
        dataToUI();
        DGViewNew.setOnPlayEvent(new ViewPlayGird.OnBtnPlay() {
            @Override
            public boolean onPlay(int idx) {
                if(!CheckIrDaPort(SsSerivce.getInstance().getHidState(),SsSerivce.getInstance().getBlvState())){
                    Toast.makeText(instance,
                            getString(R.string.str_nodev), Toast.LENGTH_SHORT).show();
                    return false;
                }
                //--------------  rushing
                if(idx>btnlist.size())return false;
                //--------------  rushing
                BtnInfo btn = btnlist.get(idx);
                if(btn.gsno<0)return false;
                TxtBtnInfo arush = new TxtBtnInfo();
                arush.param = btn.params;
                arush.gsno = btn.gsno;
                arush.keyidx = btn.keyidx;
                RushingKey(arush);
                return true;
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
                        final QMUIBottomSheet.BottomListSheetBuilder bd = new QMUIBottomSheet.BottomListSheetBuilder(instance);
                        bd.addItem(getString(R.string.str_edit));
                        bd.addItem(getString(R.string.str_irport_select));
                        //加入的遥控器列表点击
                        bd.setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                            @Override
                            public void onClick(QMUIBottomSheet dialog, View itemView, final int pos, String tag) {
                                dialog.dismiss();
                                Intent intent = new Intent();
                                switch (pos) {
                                    case 0:
                                        intent.setClass(instance, RemotePlayAdjust.class);
                                        intent.putExtra("pos",remoteidx);
                                        startActivityForResult(intent,101);
                                        break;
                                    case 1:
                                        intent.setClass(instance, SelectIrPort.class);
                                        intent.putExtra("baudio",true);
                                        startActivity(intent);
                                        break;
                                }
                            }
                        }).build().show();
                    }
                });
    }

    private void dataToUI(){
        DGViewNew.removeAllViews();
        btnlist = CfgData.getBtnInfo(instance,aremote.id);
        if(!CfgData.myremotelist.get(remoteidx).islearned) {
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
        DGViewNew.setCOL_CNT(CfgData.getMaxCols(btnlist));
        //
        for(int i=0;i<btnlist.size();i++){
            if(btnlist.get(i).gsno<0)continue;
            BtnInfo abtn = btnlist.get(i);
            ViewBtnPlaying image = new ViewBtnPlaying(instance,abtn.col,abtn.row,abtn.btnname);
            image.setmShapeKinds(RemoteBtnView.ShapeKinds.values()[btnlist.get(i).shapekinds]);
            image.idx = i;
            DGViewNew.addView(image);
        }
        DGViewNew.invalidate();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            if(requestCode==101){
                //更新按键
                dataToUI();
            }
        }
    }

    @Override
    public void onStop() {
//        if(DGViewNew.getChanged()){
//            CfgData.SaveBtnposChanged(instance,btnlist);
//        }
        super.onStop();
    }

    private int getBtnRowsCount(int arow){
        int m = 0;
        for (int i = 0; i < btnlist.size(); i++) {
            if(btnlist.get(i).row==arow){
                if(btnlist.get(i).gsno>=0)
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
}
