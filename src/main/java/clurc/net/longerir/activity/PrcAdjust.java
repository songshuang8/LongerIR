package clurc.net.longerir.activity;

import android.content.DialogInterface;
import android.view.View;
import android.widget.Toast;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import java.util.List;

import clurc.net.longerir.BaseApplication;
import clurc.net.longerir.R;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.BtnInfo;
import clurc.net.longerir.data.BtnModelData;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.RemoteInfo;
import clurc.net.longerir.data.modeldata.DataModelBtnInfo;
import clurc.net.longerir.data.modeldata.DataModelInfo;
import clurc.net.longerir.ircommu.DesRemote;
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

        int remoteid = CfgData.modellist.get(desidx).id;
        int modelid = 3;
        switch (remoteid){
            case 0x1A00:
            case 0x1A01:
            case 0x1A02:
                modelid = 3;
                break;
            case 0x1A10:
                modelid = 4;
                break;
            case 0x1A32:
                modelid = 5;
                break;
            case 0x1A34:
            case 0x1A60:
                modelid = 6;
                break;
            case 0x1A71:
            case 0x1A72:
                modelid = 7;
                break;
            case 0x1A80:
                modelid = 8;
                break;
            case 0x1B07:
                modelid = 9;
                break;
            case 0x7:
            case 0x8:
            case 0x5:
            case 0x1:
            case 0x2:
                modelid = 10;
                break;
            case 0x9:
            case 0xA:
            case 0x6:
                modelid = 11;
                break;
            case 0x1995:
            case 0x199A:
            case 0x199B:
            case 0x1999:
                modelid = 12;
                break;
        }
        List<DataModelInfo> models = BtnModelData.readMyModels(instance,false);
        DataModelInfo amodel = null;
        for (int i = 0; i < models.size(); i++) {
            if(models.get(i).id == modelid){
                amodel = models.get(i);
            }
        }
        if(amodel==null)return;
        amodel.btns = BtnModelData.getBtnInfo(instance,amodel.id,false);
        DGViewNew.setCOL_CNT(amodel.colcnt);

        for (int j = 0; j < prcinfo.src.size(); j++) {
            if (prcinfo.src.get(j).pageidx == pagesel) {
                srcremote = prcinfo.src.get(j);
                break;
            }
        }
        if(srcremote==null)return;
        //
        int size = amodel.btns.size();
        int alllen = size;
        int maxrow = 0;
        if(CfgData.modellist.get(desidx).hasShift) {
            alllen = size * 2;
            for(int i=0;i<amodel.btns.size();i++){
                if(maxrow<amodel.btns.get(i).rows){
                    maxrow = amodel.btns.get(i).rows;
                }
            }
        }
        maxrow++;
        for(int i=0;i<alllen;i++){
            boolean ishift = (i>=size)?true:false;
            int idx = i;
            if(ishift)idx-=size;
            DataModelBtnInfo dmbtn = amodel.btns.get(idx);            
            RemoteBtnView image = new RemoteBtnView(instance,
                    getResources().getColor(android.R.color.darker_gray),
                    getResources().getColor(android.R.color.holo_blue_dark),true);
            image.setBtnName(dmbtn.btnname);
            if(ishift)
                image.setRownCol(dmbtn.cols,dmbtn.rows+maxrow);
            else
                image.setRownCol(dmbtn.cols,dmbtn.rows);
            image.setmShapeKinds(RemoteBtnView.ShapeKinds.values()[dmbtn.kinds]);
            image.setKeyidx(dmbtn.keyidx);
            image.setTag(i);
            image.setSno(dmbtn.sid);
            image.setMadjust(true);
            image.setMshift(ishift);
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
