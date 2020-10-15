package clurc.net.longerir.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.qmuiteam.qmui.util.QMUILangHelper;

import java.util.ArrayList;
import java.util.List;

import clurc.net.longerir.data.CfgData;

public class QDPreferenceManager {
    private static SharedPreferences sPreferences;
    private static QDPreferenceManager sQDPreferenceManager = null;

    private static final String APP_autologin = "app_autologin";
    private static final String APP_username = "app_login";
    private static final String APP_userpassword = "app_passwd";
    private static final String APP_userid = "app_userid";
    private static final String APP_usertype = "app_passtype";

    private static final String APP_SearchStr = "searchhis";
    private static final int APP_SearchStrMax = 32;

    private static final String APP_app_ir_port = "app_irport";

    private static final String APP_showtype = "app_showdestype";
    private static final String APP_seldes = "app_seldes";



    private QDPreferenceManager(Context context) {
        sPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public static final QDPreferenceManager getInstance(Context context) {
        if (sQDPreferenceManager == null) {
            sQDPreferenceManager = new QDPreferenceManager(context);
        }
        return sQDPreferenceManager;
    }

    public boolean getAutoLogin() {
        return sPreferences.getBoolean(APP_autologin, false);
    }

    public void setAutoLogin(boolean aval) {
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.putBoolean(APP_autologin, aval);
        editor.apply();
    }

    public String getLoginUser() {
        return sPreferences.getString(APP_username, "");
    }

    public void setLoginUser(String aval) {
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.putString(APP_username, aval);
        editor.apply();
    }

    public String getLoginPswd() {
        return sPreferences.getString(APP_userpassword, "");
    }

    public void setLoginPswd(String aval) {
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.putString(APP_userpassword, aval);
        editor.apply();
    }

    public String getCustomVal(String id) {
        return sPreferences.getString(id, "null");
    }

    public void setCustomVal(String id,String aval) {
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.putString(id, aval);
        editor.apply();
    }

    public int geSelectIrPort() {
        return sPreferences.getInt(APP_app_ir_port,0);
    }

    public void setIrPort(int aval) {
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.putInt(APP_app_ir_port, aval);
        editor.apply();
    }

    //-----
    public boolean getShowType() {
        return sPreferences.getBoolean(APP_showtype, false);
    }

    public void setShowType(boolean aval) {
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.putBoolean(APP_showtype, aval);
        editor.apply();
    }

    public int geSelectDes() {
        return sPreferences.getInt(APP_seldes,0);
    }

    public void setSelDes(int aval) {
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.putInt(APP_seldes, aval);
        editor.apply();
    }
    //----------
    public int getAppUserId() {
        return sPreferences.getInt(APP_userid,-1);
    }

    public void setAPP_userid(int aval) {
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.putInt(APP_userid, aval);
        editor.apply();
    }
    //----------
    public int getAppUserType() {
        return sPreferences.getInt(APP_usertype,-1);
    }

    public void setAPP_usertype(int aval) {
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.putInt(APP_usertype, aval);
        editor.apply();
    }
    //---------------------
    public void App_AutoLogin(){
        if(getAutoLogin()) {
            CfgData.userid = getAppUserId();
            CfgData.usertype = getAppUserType();
            CfgData.username = getLoginUser();
            if(QMUILangHelper.isNullOrEmpty(CfgData.username))
                CfgData.userid = -1;
        }
    }

   // -----------his array
   public List<String> getSearHis() {
        List<String> ret = new ArrayList<String>();
       for (int i = 0; i < APP_SearchStrMax; i++) {
           String s = sPreferences.getString(APP_SearchStr+i,"");
           if(s.length()==0)break;
           ret.add(s);
       }
       return ret;
   }

    public void AppendSearchHis(String s) {
        List<String> ret = getSearHis();
        ret.remove(s);
        ret.add(0,s);

        SharedPreferences.Editor editor = sPreferences.edit();
        for (int i = 0; i < ret.size(); i++) {
            if(i>=APP_SearchStrMax)break;
            editor.putString(APP_SearchStr+i, ret.get(i));
        }
        editor.apply();
    }

    public void ClearSearchHis() {
        SharedPreferences.Editor editor = sPreferences.edit();
        for (int i = 0; i < APP_SearchStrMax; i++) {
            editor.putString(APP_SearchStr+i, "");
        }
        editor.apply();
    }
}
