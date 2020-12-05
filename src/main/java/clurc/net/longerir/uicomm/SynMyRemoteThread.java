package clurc.net.longerir.uicomm;

import android.app.Activity;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import clurc.net.longerir.R;
import clurc.net.longerir.data.BtnInfo;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.IrPrcDownComm;
import clurc.net.longerir.data.RemoteInfo;
import clurc.net.longerir.data.webHttpClientCom;
import clurc.net.longerir.ircommu.BleLearData;
import clurc.net.longerir.ircommu.MadeBleRushData;
import clurc.net.longerir.manager.BlueDeiceCommu;

import static clurc.net.longerir.manager.UiUtils.getString;
import static clurc.net.longerir.uicomm.SsSerivce.BROD_CMDUI_BLUEMODEL;
import static clurc.net.longerir.uicomm.SsSerivce.TAG_SS;
import static clurc.net.longerir.uicomm.SsSerivce.brod_ui;

public class SynMyRemoteThread extends Thread{
    private Context ctx;
    private volatile boolean cancel = false;
    private volatile boolean going = false;

    public SynMyRemoteThread(Context actx){
        Log.i(TAG_SS, "创建同步数据库线程类对象");
        this.ctx = actx;
    }

    private static SynMyRemoteThread instance=null;
    public static SynMyRemoteThread getInstance(Context actx){
        if(instance==null) {
            instance = new SynMyRemoteThread(actx);
        }
        return instance;
    }

    private OnSynMyStaEvents mevents;
    public interface OnSynMyStaEvents{
        public abstract void OnStateChanged(boolean Activeed);
    }
    public void setOnMySynThreadEvents(OnSynMyStaEvents aev){
        mevents = aev;
    }

    public void setCancel() {
        this.cancel = true;
    }

    public boolean getWorking(){
        return going;
    }

    public void run(){
        while(true){
            try {
                while(true){
                    if(going){
                        if(CfgData.userid>0) {
                            if (mevents != null) mevents.OnStateChanged(true);
                            doSynWork();
                            going = false;
                            if (mevents != null) mevents.OnStateChanged(false);
                        }
                    }
                    Thread.sleep(2000);
                }
            }catch(InterruptedException e){
                //e.printStackTrace();
                break;
            }
        }
    }

    private void doSynWork(){
        List<RemoteInfo> localremote=new ArrayList<RemoteInfo>();
        CfgData.readMyRemote(ctx,localremote);
        for (int i = 0; i < localremote.size(); i++) {
            if(cancel){
                cancel = false;
                return;
            }
            RemoteInfo src = localremote.get(i);
            if(src.rid>0)continue;
            thread_UploadRemote(src);
        }
    }

    //同步遥控器
    public void thread_UploadRemote(final RemoteInfo remote){
        List<BtnInfo> btnlist = CfgData.getBtnInfo(ctx,remote.id);
        if(remote.isAc!=CfgData.AcPro) {
            int validcount = 0;
            for (int i = 0; i < btnlist.size(); i++) {
                if (CfgData.BtnHasIr(btnlist.get(i)))validcount++;
            }
            if (validcount < 2) {
                return;
            }
        }
        String txtstr = CfgData.getRemoteTxtFile(remote,btnlist);
        webHttpClientCom.HttpRet ret = webHttpClientCom.getInstance(null).ThreadHttpCall("appUpload?userid="+ CfgData.userid,txtstr,"POST");
        if(ret.result){
               try {
                    JSONObject jsonObj = new JSONObject(new String(ret.data));
                    remote.rid = jsonObj.getInt("id");
                    CfgData.AppendoOrEditMyFile(ctx,remote,null);
                }catch (JSONException e) {
                }
        };
    }
}
