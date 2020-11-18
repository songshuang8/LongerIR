package clurc.net.longerir.activity;

import android.content.DialogInterface;
import android.view.View;
import android.widget.Toast;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import java.util.ArrayList;
import java.util.List;

import clurc.net.longerir.BaseApplication;
import clurc.net.longerir.R;
import clurc.net.longerir.Utils.MoudelFile;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.BtnInfo;
import clurc.net.longerir.data.BtnModelData;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.RemoteInfo;
import clurc.net.longerir.data.modeldata.DataModelBtnInfo;
import clurc.net.longerir.data.modeldata.DataModelInfo;
import clurc.net.longerir.ircommu.DesRemote;
import clurc.net.longerir.ircommu.DesRemoteBtn;
import clurc.net.longerir.view.RemoteBtnView;
import clurc.net.longerir.view.ViewDragGridAdjust;

public class PrcAdjust extends BaseActivity {
    private DesRemote prcinfo;
    private ViewDragGridAdjust DGViewNew;
    private int desidx;
    private int pagesel;
    private RemoteInfo srcremote = null;
    private boolean filechanged = false;
    @Override
    public void getViewId() {
        layid = R.layout.activity_prc_adjust;
        title = getString(R.string.str_prc_adjust);
        prcinfo = BaseApplication.getMyApplication().getPrcinfo();
    }

    @Override
    public void DoInit() {
        desidx = getIntent().getIntExtra("desidx", 0);
        pagesel = getIntent().getIntExtra("pagesel", -1);
        DGViewNew = findViewById(R.id.vgv);

        List<DataModelBtnInfo> dmbtns =  MoudelFile.GetMBtns(instance,desidx,true);
        DGViewNew.setCOL_CNT(BtnModelData.getMaxCols(dmbtns));

        for (int j = 0; j < prcinfo.src.size(); j++) {
            if (prcinfo.src.get(j).pageidx == pagesel) {
                srcremote = prcinfo.src.get(j);
                break;
            }
        }
        if(srcremote==null)return;
        int normalcount = dmbtns.size() / 2 ;
        MoudelFile.ModelStru mdstr = CfgData.modellist.get(desidx);
        //
        for(int i=0;i<dmbtns.size();i++){
            DataModelBtnInfo dmbtn = dmbtns.get(i);
            RemoteBtnView image = new RemoteBtnView(instance,
                    getResources().getColor(android.R.color.darker_gray),
                    getResources().getColor(android.R.color.holo_blue_dark),true);
            image.setBtnName(dmbtn.btnname);
            image.setRownCol(dmbtn.cols,dmbtn.rows);
            image.setmShapeKinds(RemoteBtnView.ShapeKinds.values()[dmbtn.kinds]);
            image.setKeyidx(dmbtn.keyidx);
            image.setTag(i);
            image.setSno(dmbtn.sid);
            image.setMadjust(true);
            if(CfgData.modellist.get(desidx).hasShift) {
                if(i>=normalcount){
                    image.setMshift(true);
                }else{
                    image.setMshift(false);
                }
            }else
                image.setMshift(false);
            // look for src info
            BtnInfo srcbtn=null;
            for (int j = 0; j < srcremote.btns.size(); j++) {
                if(srcremote.btns.get(j).desidx == i){
                    srcbtn = srcremote.btns.get(j);
                    image.srcidx = j;
                    break;
                }
            }
            if(srcbtn!=null) {
                image.setCustomName(srcbtn.btnname);
                image.gsno = srcbtn.gsno;
                image.wave = srcbtn.wave;

                image.gsno = srcbtn.gsno;
                image.params = srcbtn.params;
                image.param16 = srcbtn.param16;
            }else{
                image.gsno = -1;
            }
            DGViewNew.addView(image);
        }
        //
        DGViewNew.setDataChangedListen(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filechanged = true;
            }
        });
        // save
        mTopBar.addRightImageButton(R.mipmap.select_button, R.id.topbar_right_save_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dosave();
                        filechanged = false;
                        showMessage(getString(R.string.str_info),getString(R.string.str_save_ok));
                    }
                });
    }

    private void dosave(){
        if(srcremote==null)return;
        for (int j = 0; j < srcremote.btns.size(); j++) {
            srcremote.btns.get(j).desidx = -1;
        }

        for (int i = 0; i < DGViewNew.getChildCount(); i++) {
            RemoteBtnView desbtn = (RemoteBtnView)(DGViewNew.getChildAt(i));
            int desidx = (int)desbtn.getTag();
            int srcidx = desbtn.srcidx;

            if(srcidx<0)continue;
            if(srcidx>=srcremote.btns.size())continue;

            srcremote.btns.get(srcidx).desidx = i;
        }
    }

    @Override
    public boolean DoBack(){
        if(filechanged==false)return true;
        new QMUIDialog.MessageDialogBuilder(instance)
                .setTitle(getString(R.string.str_info))
                .setMessage(getString(R.string.str_changed_save))
                .addAction(getString(R.string.str_no), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        finish();
                        overridePendingTransition(com.qmuiteam.qmui.R.anim.abc_slide_in_bottom, com.qmuiteam.qmui.R.anim.shrink_from_bottom);
                    }
                })
                .addAction(0, getString(R.string.str_yes), QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        dosave();
                        filechanged = false;
                        finish();
                        overridePendingTransition(com.qmuiteam.qmui.R.anim.abc_slide_in_bottom, com.qmuiteam.qmui.R.anim.shrink_from_bottom);
                    }
                })
                .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
        return false;
    }
}
