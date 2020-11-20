package clurc.net.longerir;

import android.app.Application;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.ArrayList;
import java.util.List;

import clurc.net.longerir.Utils.MoudelFile;
import clurc.net.longerir.data.BtnModelData;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.IrButton;
import clurc.net.longerir.ircommu.DesRemote;
import clurc.net.longerir.manager.QDSkinManager;

import static clurc.net.longerir.uicomm.SsSerivce.TAG_SS;

public class BaseApplication extends Application {
    private static BaseApplication instance;
    //品牌列表　　，更新一次
    private List<String> sysremote_brands;
    private List<String> shrremote_brands;
    //
    private DesRemote prcinfo;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG_SS,"===>BaseApplication Creating.....");
        Fresco.initialize(this);
        instance = this;
        sysremote_brands = new ArrayList<String>();
        shrremote_brands = new ArrayList<String>();
        prcinfo = new DesRemote();

        QDSkinManager.install(this);
    }

    @Override
    public void onTerminate() {
        // 程序终止的时候执行
        super.onTerminate();
    }
    @Override
    public void onLowMemory() {
        // 低内存的时候执行
        super.onLowMemory();
    }

    public static BaseApplication getMyApplication() {
        return instance;
    }

    public List<String> getSysremote_brands(){
        return sysremote_brands;
    }

    public void ClearRemoteCache(){
        sysremote_brands.clear();
        shrremote_brands.clear();
    }

    public List<String> getShrremote_brands() {
        return shrremote_brands;
    }

    public DesRemote getPrcinfo(){
        return prcinfo;
    }
}
