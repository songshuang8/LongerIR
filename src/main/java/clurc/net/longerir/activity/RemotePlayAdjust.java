package clurc.net.longerir.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import java.util.List;

import clurc.net.longerir.R;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.BtnInfo;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.RemoteInfo;
import clurc.net.longerir.manager.UiUtils;
import clurc.net.longerir.view.RemoteBtnView;
import clurc.net.longerir.view.ViewBtnPlaying;
import clurc.net.longerir.view.ViewPlayAdjust;

public class RemotePlayAdjust extends BaseActivity {
    private ViewPlayAdjust DGViewNew;
    private RemoteInfo aremote;
    private List<BtnInfo> btnlist;
    private int remoteidx;

    private boolean hadShowSave = false;
    private boolean truechang = false;
    @Override
    public void getViewId(){
        layid = R.layout.activity_remote_play_adjust;
        remoteidx = getIntent().getExtras().getInt("pos");
        aremote = CfgData.myremotelist.get(remoteidx);
        title = aremote.pp + " " + aremote.xh;
        showback = false;
    }

    @Override
    public void DoInit(){
        btnlist = CfgData.getBtnInfo(instance,aremote.id);
        DGViewNew = findViewById(R.id.vgv);
        dataToUI();
        //退出
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(DGViewNew.isFilechanged()){
                    needToQuit();
                }else {
                    dofinnish();
                }
            }
        });
        DGViewNew.setOnAdjustEvent(new ViewPlayAdjust.OnBtnAjust() {
            @Override
            public void onAdjust() {
                showSaveButtons();
            }
        });
    }

    private void showSaveButtons(){
        if(hadShowSave)return;
        //保存按钮
        mTopBar.addRightImageButton(R.mipmap.select_button, R.id.topbar_right_save_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        uiToData();
                        if(CfgData.AppendoOrEditMyFile(instance,aremote,btnlist)) {
                            DGViewNew.setFilechanged(false);
                            Toast.makeText(instance, getString(R.string.str_save_ok), Toast.LENGTH_SHORT).show();
                            truechang = true;
                        }
                    }
                });
        hadShowSave = true;
    }

    private void uiToData(){
        for (int i = 0; i < btnlist.size(); i++) {
            BtnInfo abtn = btnlist.get(i);
            abtn.flag = false;
        }
        for (int i = 0; i < DGViewNew.getChildCount(); i++) {
            ViewBtnPlaying src = (ViewBtnPlaying)(DGViewNew.getChildAt(i));
            BtnInfo abtn = getOldBtn(src.idx);
            if(abtn==null)continue;
            abtn.btnname = src.btnName;
            abtn.shapekinds = src.getmShapeKinds().ordinal();;
            abtn.row = src.row;
            abtn.col = src.col;
            abtn.flag = true;
        }
        int i = 0;
        while(i<btnlist.size()){
            BtnInfo abtn = btnlist.get(i);
            if(abtn.flag==false){
                btnlist.remove(i);
            }else{
                i++;
            }
        }
    }

    private BtnInfo getOldBtn(int aid){
        for (int i = 0; i < btnlist.size(); i++) {
            BtnInfo abtn = btnlist.get(i);
            if(abtn.id==aid){
                return abtn;
            }
        }
        return null;
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
            if(!CfgData.BtnHasIr(btnlist.get(i)))continue;
            BtnInfo abtn = btnlist.get(i);
            ViewBtnPlaying image = new ViewBtnPlaying(instance,abtn.col,abtn.row,abtn.btnname);
            image.setmShapeKinds(RemoteBtnView.ShapeKinds.values()[btnlist.get(i).shapekinds]);
            image.idx = abtn.id;
            DGViewNew.addView(image);
        }
        DGViewNew.invalidate();
    }

    private void needToQuit(){
        new QMUIDialog.MessageDialogBuilder(instance)
                .setTitle(instance.getString(R.string.str_info))
                .setMessage(instance.getString(R.string.str_exitqury))
                .addAction(instance.getString(R.string.str_cancel), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(0,instance.getString(R.string.str_Ok), QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        dofinnish();
                    }
                })
                .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
    }
    private void dofinnish(){
        if(truechang){
            Intent intent=new Intent();
            setResult(Activity.RESULT_OK, intent);
        }
        finish();
        overridePendingTransition(com.qmuiteam.qmui.R.anim.abc_slide_in_bottom, com.qmuiteam.qmui.R.anim.shrink_from_bottom);
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

    @Override
    public void onBackPressed() {
        needToQuit();
    }
}