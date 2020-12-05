package clurc.net.longerir.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.alpha.QMUIAlphaImageButton;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import clurc.net.longerir.BaseApplication;
import clurc.net.longerir.activity.PrcAcCommu;
import clurc.net.longerir.activity.PrcAdjust;
import clurc.net.longerir.activity.UserCenter;

import clurc.net.longerir.R;
import clurc.net.longerir.Utils.MoudelFile;
import clurc.net.longerir.activity.PrcComuni;
import clurc.net.longerir.activity.SelectDesType;
import clurc.net.longerir.activity.SelectMyRemote;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.RemoteInfo;
import clurc.net.longerir.data.webHttpClientCom;
import clurc.net.longerir.ircommu.DesRemote;
import clurc.net.longerir.ircommu.PrcFunction;

import static clurc.net.longerir.manager.UiUtils.getString;

public class PrcFragment extends BaseFragment {
    private DesRemote prcinfo;
    private class PageInfoUI{
        private TextView pagename;
        private TextView pp;
        private TextView xh;
        private TextView edt;
        private TextView rmv;
        private LinearLayout pg;
        private View split;
    }
    private int desidx;
    private int currpagepos;
    private TextView tvmodename;
    private TextView tvwizard;
    private PageInfoUI[] pageui;
    private QMUIRoundButton btngo;

    private QMUIAlphaImageButton leftuserbtn;
    private boolean isac = false;

    public PrcFragment(Context context,View root){
        super(context,root);
    }

    private boolean CanShowUniversalCode(){
        if(desidx<0)return false;
        int chipint = CfgData.modellist.get(desidx).chip;
        if (chipint == 4 || chipint == 5 || chipint == 7 || chipint == 9)
            return true;
        return false;
    };

    @Override
    public  void OnGetView(){
        view = LayoutInflater.from(context).inflate(R.layout.home_prclist, (ViewGroup) root, false);
        title = context.getString(R.string.str_program);
        prcinfo = BaseApplication.getMyApplication().getPrcinfo();
    }

    @Override
    public  void viewInit(){
        desidx = -1;
        leftuserbtn = mTopBar.addRightImageButton(R.mipmap.user,R.id.topbar_left_users);
        leftuserbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity) context).startActivity(new Intent(context, UserCenter.class));
            }
        });
        //------
        tvmodename = view.findViewById(R.id.modename);
        pageui = new PageInfoUI[4];
        for (int i = 0; i < 4; i++) {
            pageui[i] = new PageInfoUI();
        }
        pageui[0].pagename = view.findViewById(R.id.pagename1);
        pageui[0].pp = view.findViewById(R.id.page1pp);
        pageui[0].xh = view.findViewById(R.id.page1xh);
        pageui[1].pagename = view.findViewById(R.id.pagename2);
        pageui[1].pp = view.findViewById(R.id.page2pp);
        pageui[1].xh = view.findViewById(R.id.page2xh);
        pageui[2].pagename = view.findViewById(R.id.pagename3);
        pageui[2].pp = view.findViewById(R.id.page3pp);
        pageui[2].xh = view.findViewById(R.id.page3xh);
        pageui[3].pagename = view.findViewById(R.id.pagename4);
        pageui[3].pp = view.findViewById(R.id.page4pp);
        pageui[3].xh = view.findViewById(R.id.page4xh);
        pageui[0].pg = view.findViewById(R.id.pge1);
        pageui[1].pg = view.findViewById(R.id.pge2);
        pageui[2].pg = view.findViewById(R.id.pge3);
        pageui[3].pg = view.findViewById(R.id.pge4);

        pageui[0].edt = view.findViewById(R.id.page1edt);
        pageui[1].edt = view.findViewById(R.id.page2edt);
        pageui[2].edt = view.findViewById(R.id.page3edt);
        pageui[3].edt = view.findViewById(R.id.page4edt);
        pageui[0].rmv = view.findViewById(R.id.page1del);
        pageui[1].rmv = view.findViewById(R.id.page2del);
        pageui[2].rmv = view.findViewById(R.id.page3del);
        pageui[3].rmv = view.findViewById(R.id.page4del);

        pageui[0].split = view.findViewById(R.id.sp1);
        pageui[1].split = view.findViewById(R.id.sp2);
        pageui[2].split = view.findViewById(R.id.sp3);
        pageui[3].split = view.findViewById(R.id.sp4);

        tvmodename.setText("");
        for (int i = 0; i < 4; i++) {
            pageui[i].pagename.setText("");
            pageui[i].pp.setText("");
            pageui[i].xh.setText("");
            pageui[i].pagename.setTag(i);
            pageui[i].pp.setTag(i);
            pageui[i].xh.setTag(i);
            pageui[i].edt.setTag(i);
            pageui[i].rmv.setTag(i);

            pageui[i].rmv.setVisibility(View.INVISIBLE);
            pageui[i].edt.setVisibility(View.INVISIBLE);

            pageui[i].pagename.setOnClickListener(AdjustBtnClick);
            pageui[i].pp.setOnClickListener(AdjustBtnClick);
            pageui[i].xh.setOnClickListener(PageClick);

            pageui[i].edt.setOnClickListener(PageEdit);
            pageui[i].rmv.setOnClickListener(PageRemove);
        }
        //型号选择
        tvmodename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //doSelAll();
                doSelDesRemote();
            }
        });
        tvmodename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSelDesRemote();
            }
        });
        ((RelativeLayout)view.findViewById(R.id.pnltitle)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSelDesRemote();
            }
        });
        //下载按钮
        btngo = ((QMUIRoundButton)view.findViewById(R.id.btnadd));
        btngo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(prcinfo.src.size()==0){
                    showMessage(getString(R.string.str_info),getString(R.string.str_no_append));
                    return;
                }
                //检查第一个页面是否有
                int firstidx = -1;
                for (int i = 0; i < prcinfo.src.size(); i++) {
                    if(prcinfo.src.get(i).pageidx==0){
                        firstidx = i;
                        break;
                    }
                }
                if(firstidx<0){
                    showMessage(context.getString(android.R.string.dialog_alert_title),context.getString(R.string.str_firstpage));
                    return;
                }

                //获取目标遥控器的按键信息
                if (prcinfo.btns.size() == 0){
                    showMessage("Error","Err found,cant not found prcinfo remote control's template");
                    return;
                }

                final Intent intent = new Intent();
                intent.putExtra("desidx",desidx);
                RemoteInfo armt = prcinfo.src.get(firstidx);
                if(armt.isAc==CfgData.AcPro){
                    byte[] prcdata =  (new PrcFunction()).getEepData(armt.acdata);
                    intent.setClass(context, PrcAcCommu.class);
                    intent.putExtra("prcdata",CfgData.ByteArrToString(prcdata));
                    ((Activity)context).startActivity(intent);
                }else if(armt.isAc==CfgData.AcLear){
                    webHttpClientCom.getInstance((Activity)context).RestkHttpCall("getTransEepAc", CfgData.getRemoteTxtFile(armt,armt.btns), "POST", new webHttpClientCom.WevEvent_SucData() {
                        @Override
                        public void onSuc(byte[] out) {
                            intent.setClass(context, PrcAcCommu.class);
                            intent.putExtra("prcdata",CfgData.ByteArrToString(out));
                            ((Activity)context).startActivity(intent);
                        }
                    });
                }else{
                    String s = CfgData.createPrcText(prcinfo);
                    String[] k = s.split("\r\n");
                    if(k.length>=2) {
                        intent.setClass(context, PrcComuni.class);
                        ((Activity) context).startActivity(intent);
                    }else{
                        showMessage("Error","Err found,buttons is short");
                    }
                }
            }
        });
        tvwizard = (TextView)view.findViewById(R.id.wizard);
        tvwizard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSelDesRemote();
            }
        });
        btngo.setVisibility(View.GONE);
    }

    @Override
    public void doResultOk(int which,Intent data){
        switch (which){
            case 94: {
                desidx = data.getIntExtra("desidx",0);
                tvmodename.setText(CfgData.modellist.get(desidx).name);
                prcinfo.pagename = CfgData.modellist.get(desidx).pageName;
                if(CfgData.myremotelist.size()>0) {
                    CfgData.AppendRemoteToPrc(context,data.getIntExtra("myidx", 0), data.getIntExtra("pagesel", 0));
                }
                //
                reFreshUI();
            }
                break;
            case 95:
                CfgData.AppendRemoteToPrc(context,data.getIntExtra("myidx",-1),data.getIntExtra("pagesel", 0));
                reFreshUI();
                break;
            case 96: {
                desidx = data.getIntExtra("desidx", 0);
                boolean oldisac = isac;
                isac = CfgData.modellist.get(desidx).chip == 11?true:false;
                //获取目标遥控器的按键信息
                prcinfo.btns = MoudelFile.GetBtns(context, desidx);
                if (prcinfo.btns.size() == 0) {
                    showMessage("Error", "Err found,cant not found prcinfo remote control's template");
                    return;
                }

                int typeint = data.getIntExtra("destype", 0);
                tvmodename.setText(CfgData.getDesTypeDesc(typeint) + " " + CfgData.modellist.get(desidx).name);
                prcinfo.pagename =CfgData.modellist.get(desidx).pageName;

                if (oldisac != isac) {  //剔除所有
                    while (prcinfo.src.size()>0) {
                        prcinfo.src.remove(0);
                    }
                } else{ //剔除超过的设备
                    int p = 0;
                    while (p < prcinfo.src.size()) {
                        if (prcinfo.src.get(p).pageidx >= prcinfo.pagename.length) {
                            prcinfo.src.remove(p);
                        } else {
                            p++;
                        }
                    }
                }
                reFreshUI();
            }
                break;
        }
    }

    @Override
    public  void DoClickShow(){
        if(desidx<0) {
            doSelDesRemote();
        }
    }

    //选择永伟牌型号
    private void doSelDesRemote(){
        Intent intent = new Intent();
        intent.setClass(context, SelectDesType.class);
        intent.putExtra("pagesel",-1);
        intent.putExtra("bonly",true);
        ((Activity)context).startActivityForResult(intent, 96);
    }

    private void doSelectSrcRemote(){
        int[] myid =CfgData.getMyRemoteIdList(isac);
        if(myid.length==0){
            Toast.makeText(context,getString(R.string.str_nomyremote), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.setClass(context, SelectMyRemote.class);
        intent.putExtra("desidx",desidx);
        intent.putExtra("pagesel",currpagepos);
        ((Activity)context).startActivityForResult(intent, 95);
    }

    private View.OnClickListener PageClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(desidx<0)return;
            currpagepos = (Integer) v.getTag();
            doSelectSrcRemote();
        }
    };

    private void DoAppendOrAdjust(){
        boolean bhavesrc = false;
        for (int i = 0; i < prcinfo.src.size(); i++) {
            if (prcinfo.src.get(i).pageidx == currpagepos) {
                bhavesrc = true;
                break;
            }
        }

        if(bhavesrc){
            Intent intent = new Intent();
            intent.setClass(context, PrcAdjust.class);
            intent.putExtra("desidx", desidx);
            intent.putExtra("pagesel", currpagepos);
            ((Activity) context).startActivity(intent);
        }else {
            doSelectSrcRemote();
        }
    }

    private View.OnClickListener AdjustBtnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(desidx<0)return;
            currpagepos = (Integer) v.getTag();
            DoAppendOrAdjust();
        }
    };

    private View.OnClickListener PageEdit = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(desidx<0)return;
            currpagepos = (Integer) v.getTag();
            DoAppendOrAdjust();
        }
    };

    private View.OnClickListener PageRemove = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int p = (Integer) v.getTag();
            //剔除原有的
            for (int i = 0; i < prcinfo.src.size(); i++) {
                if (prcinfo.src.get(i).pageidx == p) {
                    prcinfo.src.remove(i);
                    break;
                }
            }
            reFreshUI();
        }
    };

    @Override
    public void DoOnResume(){
        if(desidx<0){
            tvmodename.setText(context.getString(R.string.str_sel_desremote));
        }
    }

    private void reFreshUI(){
        if(desidx<0){
            for (int i = 0; i < pageui.length; i++) {
                pageui[i].pg.setVisibility(View.GONE);
                pageui[i].split.setVisibility(View.GONE);
            }
            return;
        }
        for (int i = 0; i < 4; i++) {
            if(i<prcinfo.pagename.length){
                pageui[i].pg.setVisibility(View.VISIBLE);
                pageui[i].split.setVisibility(View.VISIBLE);
                pageui[i].pagename.setText(prcinfo.pagename[i]);
                //
                RemoteInfo asrc = null;
                for (int j = 0; j < prcinfo.src.size(); j++) {
                    if (prcinfo.src.get(j).pageidx == i) {
                        asrc = prcinfo.src.get(j);
                        break;
                    }
                }
                if(asrc!=null){
                    pageui[i].edt.setVisibility(View.VISIBLE);
                    pageui[i].rmv.setVisibility(View.VISIBLE);
                    pageui[i].edt.setText(context.getString(R.string.str_edit));

                    pageui[i].pp.setText(asrc.pp);
                    pageui[i].xh.setText(asrc.xh);
                }else {
                    pageui[i].edt.setVisibility(View.VISIBLE);
                    pageui[i].rmv.setVisibility(View.INVISIBLE);
                    pageui[i].edt.setText(context.getString(R.string.str_append));
                    pageui[i].pp.setText("");
                    pageui[i].xh.setText("");
                }
            }else{
                pageui[i].pg.setVisibility(View.GONE);
                pageui[i].split.setVisibility(View.GONE);
            }
        }
        if(prcinfo.src.size()>0) {
            btngo.setVisibility(View.VISIBLE);
        }else{
            btngo.setVisibility(View.GONE);
        }
    }
}
