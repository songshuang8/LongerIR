package clurc.net.longerir.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import clurc.net.longerir.R;
import clurc.net.longerir.Utils.DialogRemoteInfo;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.BtnInfo;
import clurc.net.longerir.data.BtnModelData;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.RemoteInfo;
import clurc.net.longerir.data.modeldata.DataModelBtnInfo;
import clurc.net.longerir.data.modeldata.DataModelInfo;
import clurc.net.longerir.data.webHttpClientCom;
import clurc.net.longerir.uicomm.SsSerivce;
import clurc.net.longerir.view.RemoteBtnView;
import clurc.net.longerir.view.ViewDragGrid;

public class IrLearn extends BaseActivity {
    private ViewDragGrid DGViewNew;
    private RemoteInfo aremote;
    private List<BtnInfo> btnlist;
    private boolean devsta=false;
    private boolean filechanged = false;
    private int col_int;
    private RemoteBtnView mselectbtn = null;

    @Override
    public void getViewId(){
        layid = R.layout.activity_learning;
        int pos = getIntent().getIntExtra("pos",-1);
        if(pos<0) {
            int selmodelidx = getIntent().getExtras().getInt("selmodelidx",0);

            List<DataModelInfo> models = BtnModelData.readMyModels(instance);
            DataModelInfo amodel = models.get(selmodelidx);
            amodel.btns = BtnModelData.getBtnInfo(instance,amodel.id);

            aremote = new RemoteInfo();
            aremote.islearned = true;
            btnlist = new ArrayList<BtnInfo>();
            col_int = 0;
            for (int i = 0; i < amodel.btns.size(); i++) {
                BtnInfo abtn = new BtnInfo();
                DataModelBtnInfo src = amodel.btns.get(i);
                abtn.keyidx = src.keyidx;
                abtn.btnname = src.btnname;
                abtn.gsno = -1;
                abtn.shapekinds = src.kinds;
                abtn.row = src.rows;
                abtn.col = src.cols;
                if((abtn.col+1)>col_int)
                    col_int = abtn.col+1;
                btnlist.add(abtn);
            }
        }else{
            aremote = CfgData.myremotelist.get(pos);
            btnlist = CfgData.getBtnInfo(instance,aremote.id);
            col_int = 0;
            for (int i = 0; i < btnlist.size(); i++) {
                if((btnlist.get(i).col+1)>col_int)
                    col_int = btnlist.get(i).col+1;
            }
        }
        title = getString(R.string.str_addlearn);
        showback = false;
    }

    @Override
    public void DoInit(){
        DGViewNew = findViewById(R.id.vgv);
        DGViewNew.setCOL_CNT(col_int);

        for(int i=0;i<btnlist.size();i++){
            RemoteBtnView image = new RemoteBtnView(instance,
                    getResources().getColor(android.R.color.darker_gray),
                    getResources().getColor(android.R.color.holo_blue_dark),true);
            image.setBtnName(btnlist.get(i).btnname);
            image.setCustomName(btnlist.get(i).btnname);
            image.setRownCol(btnlist.get(i).col,btnlist.get(i).row);
            image.setmShapeKinds(RemoteBtnView.ShapeKinds.values()[btnlist.get(i).shapekinds]);
            image.setKeyidx(btnlist.get(i).keyidx);
            image.setMlearning(true);
            image.gsno = -1;
            image.wave = "";
            image.setTag(i);
            image.gsno = btnlist.get(i).gsno;
            image.params = btnlist.get(i).params;
            image.param16 = btnlist.get(i).param16;
            image.wave = btnlist.get(i).wave;
            DGViewNew.addView(image);
        }
        if(DGViewNew.getChildCount()>0) {
            mselectbtn = (RemoteBtnView)(DGViewNew.getChildAt(0));
            DGViewNew.setSelectedChildView(0);
        }
        DGViewNew.setSelectChangedListen(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mselectbtn = (RemoteBtnView) v;
            }
        });
        //选择红外口
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
        //退出
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(filechanged){
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
                }else {
                    dofinnish();
                }
            }
        });
        //保存按钮
        mTopBar.addRightImageButton(R.mipmap.select_button, R.id.topbar_right_save_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DialogRemoteInfo aputdig = new DialogRemoteInfo(instance,aremote);
                        aputdig.setTitle(getString(R.string.str_info));
                        aputdig.CustomShow(new DialogRemoteInfo.OnRemoteInfoSuc() {
                            @Override
                            public void onSuc() {
                                uiToData();
                                int id = aremote.id;
                                if(CfgData.AppendoOrEditMyFile(instance,aremote,btnlist)) {
                                    filechanged = false;
                                    Toast.makeText(instance, "Save ok", Toast.LENGTH_SHORT).show();
                                    if(id<0)
                                        CfgData.myremotelist.add(aremote);
                                }
                            }
                        });
                    }
                });
    }

    private void dofinnish(){
        finish();
        overridePendingTransition(com.qmuiteam.qmui.R.anim.abc_slide_in_bottom, com.qmuiteam.qmui.R.anim.shrink_from_bottom);
    }

    @Override
    public void DoServiceMesg(int cmd,Intent intent) {
        switch (cmd) {
            case SsSerivce.BROD_CMDUI_LEARNDATA:
                if(mselectbtn==null)return;
                String wavestr = (String) intent.getSerializableExtra("wavestr");
                int freq = (int) intent.getSerializableExtra("freq");

                webHttpClientCom.getInstance(instance).RestkHttpCall("DoLearPro2?freq=" + freq, wavestr,"POST", new webHttpClientCom.WevEvent_SucString() {
                    @Override
                    public void onSuc(String res) {
                        String codestr = null;
                        try {
                            JSONObject jsonObj = new JSONObject(res);
                            if(jsonObj.getInt("result")==0){
                                codestr = jsonObj.getString("code");
                            }
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(codestr!=null) {
                            filechanged = true;
                            setCurrIrData(freq,codestr);
                            AutoSelectNextButton();
                        }else {
                            Toast.makeText(instance, wavestr, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case SsSerivce.BROD_CMDUI_HIDSTATE:
                if(CfgData.selectIr==0){
                    if((boolean) intent.getSerializableExtra("hid")){
                        SsSerivce.getInstance().setHIDLearn(true);
                    }else{
                        Toast.makeText(instance, getString(R.string.str_usbout), Toast.LENGTH_SHORT).show();
                    }
                }else if(CfgData.selectIr==1){
                    if((boolean) intent.getSerializableExtra("blv")){
                        SsSerivce.getInstance().setBlvLearn(true);
                    }else{
                        Toast.makeText(instance, getString(R.string.str_usbout), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                break;
        }
    }

    public void setCurrIrData(int freq,String decode){
        if(mselectbtn==null)return;
        String[] item = decode.split(" ");
        if(item.length>2) {
            mselectbtn.freq = freq;
            mselectbtn.gsno = Integer.valueOf(item[0]);
            int paramlen = item.length-1;
            mselectbtn.param16 = "";
            mselectbtn.params = new int[paramlen];
            for (int i = 0; i < paramlen; i++) {
                try {
                    mselectbtn.params[i] = Integer.parseInt(item[i+1],16);
                }catch (NumberFormatException e){
                }
                mselectbtn.param16 += item[i+1] + ",";
            }
        }
    }

    private void AutoSelectNextButton(){
        int curr = 0;
        if(mselectbtn!=null)
            curr = (int)mselectbtn.getTag();
        curr++;
        if(curr<DGViewNew.getChildCount()) {
            DGViewNew.setSelectedChildView(curr);
            mselectbtn = (RemoteBtnView)DGViewNew.getChildAt(curr);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //request hid state
        SsSerivce.getInstance().setStateBroadcast();
    }

    @Override
    public void onStop() {
        if(DGViewNew.getChanged()){
            CfgData.SaveBtnposChanged(instance,btnlist);
        }
        SsSerivce.getInstance().setHIDLearn(false);
        SsSerivce.getInstance().setBlvLearn(false);
        super.onStop();
    }

    public interface OnClicOkkListener {
        void onClick(CharSequence in);
    }

    private void uiToData(){
        btnlist.clear();
        for (int i = 0; i < DGViewNew.getChildCount(); i++) {
            BtnInfo abtn = new BtnInfo();
            RemoteBtnView src = (RemoteBtnView)(DGViewNew.getChildAt(i));
            abtn.keyidx = src.getKeyidx();
            abtn.btnname = src.getBtnName();
            abtn.gsno = src.gsno;
            abtn.shapekinds = src.getmShapeKinds().ordinal();;
            abtn.row = src.getRow();
            abtn.col = src.getCol();
            abtn.gsno = src.gsno;
            abtn.param16 = src.param16;
            abtn.wave = src.wave;
            abtn.id=-1;
            btnlist.add(abtn);
        }
    }
}
