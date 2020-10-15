package clurc.net.longerir.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.widget.QMUILoadingView;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.concurrent.ThreadLocalRandom;

import clurc.net.longerir.R;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.webHttpClientCom;

import static clurc.net.longerir.manager.UiUtils.getString;
import static clurc.net.longerir.uicomm.SsSerivce.TAG_SS;
import static java.lang.Math.abs;

public class UserCenter extends BaseActivity {
    private mSynThread threadsyn;
    private int[] func_strid = {
            R.string.str_myshared,
            R.string.str_changepswd,
            R.string.str_model_btn,
            R.string.str_synremote,
            R.string.str_about,
            R.string.str_logout
    };
    private QMUICommonListItemView[] item_myshare;
    private QMUIGroupListView mGroupListView;
    private TextView tvuser,tvusertype;
    private QMUILoadingView loadingView =null;

    @Override
    public void getViewId() {
        layid = R.layout.user_center_qm;
        title = getString(R.string.str_setting);
    }

    private void Dologin(){
        if(CfgData.userid<0) {
            Intent intent = new Intent();
            intent.setClass(instance, UserLogin.class);
            ((Activity) instance).startActivityForResult(intent, 1);
        }
        showLoginInfomaion();
    }

    @Override
    public void DoInit() {
        mGroupListView = findViewById(R.id.groupListView);
        tvuser = findViewById(R.id.txt_user);
        tvusertype = findViewById(R.id.txt_usertype);
        tvuser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dologin();
            }
        });
        ((LinearLayout)findViewById(R.id.pnl_user)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  //点击登陆
                Dologin();
            }
        });
        //--------------
        int size = QMUIDisplayHelper.dp2px(instance, 20);
        QMUIGroupListView.Section asection = QMUIGroupListView.newSection(instance)
                .setLeftIconSize(size, ViewGroup.LayoutParams.WRAP_CONTENT);
        item_myshare = new QMUICommonListItemView[func_strid.length];
        for (int i = 0; i < func_strid.length; i++) {
            item_myshare[i] = mGroupListView.createItemView(
                    ContextCompat.getDrawable(instance, R.mipmap.about),
                    getString(func_strid[i]),
                    null,
                    QMUICommonListItemView.HORIZONTAL,
                    QMUICommonListItemView.ACCESSORY_TYPE_NONE);
            item_myshare[i].setOrientation(QMUICommonListItemView.VERTICAL);
            if(i==3){
                item_myshare[i].setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CUSTOM);
            }else {
                item_myshare[i].setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
            }
            item_myshare[i].setTag(i);
            asection.addItemView(item_myshare[i],funcClickListener);
        }
        asection
                .setMiddleSeparatorInset(QMUIDisplayHelper.dp2px(instance, 16), 0)
                .setUseTitleViewForSectionSpace(false).addTo(mGroupListView);
    }

    private void showLoginInfomaion(){
        if(CfgData.userid<0) {
            tvuser.setText("[点击登录]");
            tvusertype.setText("");
        }else{
            tvuser.setText(CfgData.username);
            switch (CfgData.usertype) {
                case 0:
                    tvusertype.setText("超级用户");
                    break;
                case 1:
                    tvusertype.setText("VIP用户");
                    break;
                default:
                    tvusertype.setText("普通用户");
                    break;
            }
        }
    }

    View.OnClickListener funcClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v instanceof QMUICommonListItemView) {
                Intent intent;
                int funcid = (int)v.getTag();
                switch (funcid){
                    case 0:
                    case 1:
                        if(CfgData.userid<0) {
                            ShowDialog(getString(R.string.str_pls_login));
                            return;
                        }
                        if(funcid==0)
                            intent = new Intent(instance, MyShareList.class);
                        else{
                            intent = new Intent(instance, UserReset.class);
                            intent.putExtra("usrname", CfgData.username);
                        }
                        startActivity(intent);
                        break;
                    case 2:
                        intent = new Intent(instance, ActivityModelList.class);
                        startActivity(intent);
                        break;
                    case 3:
                        if(CfgData.userid<0) {
                            ShowDialog(getString(R.string.str_pls_login));
                            return;
                        }
                        if (loadingView == null) {
                            threadsyn = new mSynThread();
                            threadsyn.start();
                        }else{
                            if(threadsyn!=null && threadsyn.isAlive())
                                threadsyn.interrupt();
                        }
                        break;
                    case 4:
                        intent = new Intent(instance, ActivityAbout.class);
                        startActivity(intent);
                        break;
                    case 5:
                        if(CfgData.userid>0) {
                            CfgData.userid = -1;
                        }
                        showLoginInfomaion();
                        break;
                }
            }
        }
    };

    @Override
    public void DoShowing(){
        showLoginInfomaion();
    }

    public String BugJSONTokener(String in) {
        // consume an optional byte order mark (BOM) if it exists
        if (in != null && in.startsWith("\ufeff")) {
            in = in.substring(1);
        }
        return in;
    }

    private void DoMobileLogin(String token){
        webHttpClientCom.getInstance(instance).RestkHttpCall("getMobile?serial=" + CfgData.mobileserial, token, "POST", new webHttpClientCom.RestOnWebPutEvent() {
                    @Override
                    public void onSuc(byte[] out) {
                        String json = new String(out);
                        Log.w(TAG_SS,json);
                        try {
                            JSONObject jb = new JSONObject(BugJSONTokener(json));
                            if(jb.getInt("result")==0){
                                Log.w(TAG_SS,jb.getString("mobile"));
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFail(boolean netfaulre, String res) {
                        if(res!=null && res.length()>0)
                            Log.w(TAG_SS,res);
                    }
                });
    }
    //--------------------------------------我的数据库-----------------------------------------------------------------------
    //Handler静态内部类
    private static class MyUIHandler extends Handler {
        //弱引用
        WeakReference<UserCenter> weakReference;
        public MyUIHandler(UserCenter activity) {
            weakReference = new WeakReference<UserCenter>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            UserCenter activity = weakReference.get();
            if (activity != null) {
                if(msg.what==1) {
                    if (activity.loadingView == null) {
                        activity.loadingView = new QMUILoadingView(activity);
                        activity.item_myshare[3].addAccessoryCustomView(activity.loadingView);
                    }
                }else if (activity.loadingView != null) {
                    activity.item_myshare[3].getAccessoryContainerView().removeView(activity.loadingView);
                    activity.loadingView = null;
                }
            }
        }
    }

    private MyUIHandler uihandler = new MyUIHandler(this);
    public class mSynThread extends Thread{
        public void run(){
            Message uimsg = new Message();
            uimsg.what = 1;
            uihandler.sendMessage(uimsg);
            long txtime = SystemClock.uptimeMillis();
            try {
                // push
                for (int i = 0; i < CfgData.myremotelist.size(); i++) {
                    if(isInterrupted())return;
                    if(CfgData.myremotelist.get(i).rid<1)
                        webHttpClientCom.getInstance(UserCenter.this).Slient_UploadRemote(CfgData.myremotelist.get(i));
                }

                if(isInterrupted())return;
//                //delete
//                String res = webHttpClientCom.getInstance(UserCenter.this).ThreadHttpCall("appRemote?where=Author="+ CfgData.userid,null,"GET",null);
//                if(res==null)return;
//                if(isInterrupted())return;
//
//                try {
//                    JSONArray jsonAll = new JSONArray(res);
//                    for(int i=0;i<jsonAll.length();i++){
//                        JSONObject jsonsingle = (JSONObject)jsonAll.get(i);
//                        int aid = jsonsingle.getInt("ID");
//                        boolean localhas = false;
//                        for (int j = 0; j < CfgData.myremotelist.size(); j++) {
//                            if(CfgData.myremotelist.get(j).rid == aid){
//                                localhas = true;
//                                break;
//                            }
//                        }
//                        if(!localhas){
//                            webHttpClientCom.getInstance(UserCenter.this).ThreadHttpCall("appRemote/"+ aid,null,"DELETE",null);
//                        }
//                        if(isInterrupted())return;
//                    }
//                }catch (JSONException e) {
//                    e.printStackTrace();
//                }
                //pull
                String res = webHttpClientCom.getInstance(UserCenter.this).ThreadHttpCall("appRemote?where=Author="+ CfgData.userid,null,"GET",null);
                if(res==null)return;
                if(isInterrupted())return;
                try {
                    JSONArray jsonAll = new JSONArray(res);
                    for(int i=0;i<jsonAll.length();i++){
                        JSONObject jsonsingle = (JSONObject)jsonAll.get(i);
                        int aid = jsonsingle.getInt("ID");
                        boolean localhas = false;
                        for (int j = 0; j < CfgData.myremotelist.size(); j++) {
                            if(CfgData.myremotelist.get(j).rid == aid){
                                localhas = true;
                                break;
                            }
                        }
                        if(!localhas){
                            String param = "appRemote?select=id,xh,dev,downloads as ct,GoodCount as gt,BadCount as bt,'' as Au,isAc,1 as ty&where=id="+aid;
                            webHttpClientCom.getInstance(UserCenter.this).ThreadHttpCall("appRemote/"+ aid,null,"DELETE",null);
                        }
                        if(isInterrupted())break;
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }
                //--------------------------
                long alen = abs(SystemClock.uptimeMillis()-txtime);
                if(alen<2000) {
                    try{
                        Thread.sleep(1500);
                    }catch (InterruptedException e){
                    }
                }
            }finally {
                Message uimsg2 = new Message();
                uimsg2.what = 0;
                uihandler.sendMessage(uimsg2);
            }
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if(threadsyn!=null && threadsyn.isAlive())
            threadsyn.interrupt();
    }
}
