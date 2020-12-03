package clurc.net.longerir.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import clurc.net.longerir.R;
import clurc.net.longerir.Utils.AcUltils;
import clurc.net.longerir.Utils.DialogRemoteInfo;
import clurc.net.longerir.adapt.AcRecordAdapt;
import clurc.net.longerir.adapt.AcTempShowAdapt;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.BtnInfo;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.RemoteInfo;
import clurc.net.longerir.uicomm.SsSerivce;

public class IrLearAc extends BaseActivity {
    private LinearLayout pnlsta;
    private List<TextView[]>tvbtn=new ArrayList<TextView[]>();
    private ListView recordlist,templist;
    private AcTempShowAdapt tempadpt;
    private AcRecordAdapt recordadpt;
    private RemoteInfo aremote;
    private List<BtnInfo> btnslist;
    private int[] sta = {1,0,1,0,0,0,0,0};

    private boolean filechanged = false;
    @Override
    public void getViewId() {
        layid = R.layout.activity_learn_ac;
        title = getString(R.string.str_ac_learn);
        showback = false;
        int pos = getIntent().getIntExtra("pos",-1);
        if(pos<0) {
            aremote = new RemoteInfo();
            aremote.islearned = true;
            aremote.isAc = CfgData.AcLear;
            aremote.dev = "AC";
            btnslist = new ArrayList<BtnInfo>();
        }else{
            aremote = CfgData.myremotelist.get(pos);
            btnslist = CfgData.getBtnInfo(instance,aremote.id);
        }
    }

    @Override
    public void DoInit() {
        pnlsta = findViewById(R.id.pnlsta);
        tempadpt = new AcTempShowAdapt(instance,sta);
        templist = findViewById(R.id.templist);
        templist.setAdapter(tempadpt);
        //点击温度
        templist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(sta[1] != i){
                   sta[1] = i;

                    StaChanged();
                   tempadpt.notifyDataSetChanged();
                    recordadpt.SetCurrent(sta);
                    AcUltils.SetListViewPos(recordlist,recordadpt.getCurrpos());
                }
            }
        });

        recordadpt = new AcRecordAdapt(instance,btnslist,sta);
        recordlist = findViewById(R.id.recrdlist);
        recordlist.setAdapter(recordadpt);
        // 点击学习的记录
        recordlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if(btnslist.size()==0  || position>=(btnslist.size()))return;

                recordadpt.SetCurrentDr(position);
                BtnInfo abtn = btnslist.get(position);
                for (int i = 0; i < sta.length; i++) {
                    sta[i] = abtn.params[i];
                }
                StaChanged();
                SetColorChanged(-1);
                AcUltils.SetListViewPos(templist,sta[1]);
            }
        });

        for (int i = 0; i < AcUltils.rid.length; i++) {
            TextView[] sub = new  TextView[AcUltils.rid[i].length];
            for (int j = 0; j < sub.length; j++) {
                sub[j] = findViewById(AcUltils.rid[i][j]);
                sub[j].setTag(new Point(i,j-1));
                //点击各状态
                if(j>0)
                sub[j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Point info = (Point) view.getTag();
                        sta[info.x] = info.y;

                        StaChanged();
                        SetColorChanged(info.x);
                        recordadpt.SetCurrent(sta);
                        AcUltils.SetListViewPos(recordlist,recordadpt.getCurrpos());
                    }
                });
            }
            tvbtn.add(sub);
        }
        //test
//        mTopBar.addRightImageButton(R.mipmap.circle, R.id.topbar_right_test_button)
//                .setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        int idx = recordadpt.getNowRecrdBySta(sta);
//                        BtnInfo abtn;
//                        if(idx<0) {
//                            abtn = new BtnInfo();
//                            abtn.params = new  int[sta.length];
//                        }else
//                            abtn = btnslist.get(idx);
//                        for (int j = 0; j < sta.length; j++) {
//                            abtn.params[j] = sta[j];
//                        }
//                        abtn.keyidx = 38000;
//                        abtn.wave = "9024,-4512,564,-1692,564,-564,564,-1692,564,-564,564,-1692,564,-564,564,-1692,564,-564,564,-564,564,-1692,564,-564,564,-1692,564,-564,564,-1692,564,-564,564,-1692,564,-1692,564,-564,564,-1692,564,-564,564,-1692,564,-564,564,-1692,564,-564,564,-564,564,-1692,564,-564,564,-1692,564,-564,564,-1692,564,-564,564,-1692,564,-39756,9024,-2256,564,-96156";
//                        btnslist.add(abtn);
//                        filechanged = true;
//
//                        incsta(1);
//
//                        StaChanged();
//                        SetColorChanged(-1);
//                        recordadpt.SetCurrent(sta);
//                        AcUltils.SetListViewPos(recordlist,recordadpt.getCurrpos());
//                        AcUltils.SetListViewPos(templist,sta[1]);
//                    }
//                });
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
                                int id = aremote.id;
                                toBtnParam16();
                                if(CfgData.AppendoOrEditMyFile(instance,aremote,btnslist)) {
                                    filechanged = false;
                                    Toast.makeText(instance, getString(R.string.str_save_ok), Toast.LENGTH_SHORT).show();
                                    if(id<0)
                                        CfgData.myremotelist.add(aremote);
                                }
                            }
                        });
                    }
                });
        //
        SetColorChanged(-1);
    }

    private void dofinnish(){
        if(aremote.id>0){
            Intent intent=new Intent();
            setResult(Activity.RESULT_OK, intent);
        }
        finish();
        overridePendingTransition(com.qmuiteam.qmui.R.anim.abc_slide_in_bottom, com.qmuiteam.qmui.R.anim.shrink_from_bottom);
    }

    private void incsta(int btn){
        if(sta[0]==0){
            sta[0] = 1;
            return;
        }
        if(btn>=sta.length)return;
        if(sta[btn]>= (AcUltils.btnmax[btn]-1)){
            sta[btn] = 0;
            incsta(btn+1);
        }else{
            sta[btn]++;
        }
    }

    private void SetColorChanged(int col){
        if(col==1){
            tempadpt.notifyDataSetChanged();
            return;
        }
        for (int i = 0; i < tvbtn.size(); i++) {
            if(col>=0 && i!=col)continue;
            for (int j = 0; j < tvbtn.get(i).length; j++) {
                GradientDrawable gd = new GradientDrawable();//创建drawable

                if(j>0 && sta[i]==(j-1))
                    gd.setColor(getResources().getColor(R.color.app_color_theme_6));
                else
                    gd.setColor(Color.WHITE);
                if(j>0)
                    gd.setCornerRadius(8);
                else
                    gd.setCornerRadius(0);
                gd.setStroke(1, getResources().getColor(R.color.app_color_theme_1));
                tvbtn.get(i)[j].setBackground(gd);
            }
        }
        if(col<0)
            tempadpt.notifyDataSetChanged();
    }

    //实现off时其它隐藏
    private int powsta=-1;
    private void StaChanged(){
        if(sta[0]!=powsta){
            powsta = sta[0];
            if(powsta==0){
                int h = pnlsta.getMeasuredHeight();
                pnlsta.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, h));
                for (int i = 1; i < tvbtn.size(); i++)
                    for (int j = 1; j < tvbtn.get(i).length; j++) {
                        tvbtn.get(i)[j].setVisibility(View.GONE);
                    }
                templist.setVisibility(View.GONE);
            }else{
                pnlsta.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                for (int i = 1; i < tvbtn.size(); i++)
                    for (int j = 1; j < tvbtn.get(i).length; j++) {
                        tvbtn.get(i)[j].setVisibility(View.VISIBLE);
                    }
                templist.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void DoServiceMesg(int cmd,Intent intent) {
        switch (cmd) {
            case SsSerivce.BROD_CMDUI_LEARNDATA:
                String wavestr = (String) intent.getSerializableExtra("wavestr");
                int freq = (int) intent.getSerializableExtra("freq");
                //-------------------------
                int idx = recordadpt.getNowRecrdBySta(sta);
                BtnInfo abtn;
                if(idx<0) {
                    abtn = new BtnInfo();
                    abtn.params = new  int[sta.length];
                }else
                    abtn = btnslist.get(idx);
                for (int j = 0; j < sta.length; j++) {
                    abtn.params[j] = sta[j];
                }
                abtn.keyidx = freq;
                abtn.wave = wavestr;
                btnslist.add(abtn);

                incsta(1);

                StaChanged();
                SetColorChanged(-1);
                recordadpt.SetCurrent(sta);
                AcUltils.SetListViewPos(recordlist,recordadpt.getCurrpos());
                AcUltils.SetListViewPos(templist,sta[1]);
                //-----------------------
                filechanged = true;
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

    @Override
    public void onResume() {
        super.onResume();
        SsSerivce.getInstance().setStateBroadcast();
    }

    @Override
    public void onStop() {
        SsSerivce.getInstance().setHIDLearn(false);
        SsSerivce.getInstance().setBlvLearn(false);
        super.onStop();
    }

    //btn的params 到param16
    private void toBtnParam16(){
        for (int i = 0; i < btnslist.size(); i++) {
            BtnInfo abtn = btnslist.get(i);
            abtn.param16="";
            for (int j = 0; j < abtn.params.length; j++) {
                abtn.param16 += Integer.toHexString(abtn.params[j])+",";
            }
        }
    }
}
