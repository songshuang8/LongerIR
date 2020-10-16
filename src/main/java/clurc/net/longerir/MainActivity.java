package clurc.net.longerir;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUIProgressBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.tab.QMUIBasicTabSegment;
import com.qmuiteam.qmui.widget.tab.QMUITab;
import com.qmuiteam.qmui.widget.tab.QMUITabBuilder;
import com.qmuiteam.qmui.widget.tab.QMUITabSegment;
import com.qmuiteam.qmui.widget.tab.QMUITabView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import clurc.net.longerir.Utils.MobileUUID;
import clurc.net.longerir.data.BtnModelData;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.modeldata.DataModelBtnInfo;
import clurc.net.longerir.data.modeldata.DataModelInfo;
import clurc.net.longerir.data.webHttpClientCom;
import clurc.net.longerir.fragment.BaseFragment;
import clurc.net.longerir.fragment.MallFragment;
import clurc.net.longerir.fragment.PrcFragment;
import clurc.net.longerir.fragment.RemoteFragment;

import clurc.net.longerir.uicomm.SsSerivce;

import static clurc.net.longerir.manager.UiUtils.getContext;
import static clurc.net.longerir.uicomm.SsSerivce.*;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("native-lib");
    }

    private final static String TAG = MainActivity.class.getSimpleName();
    private ViewPager mViewPager;
    private QMUITabSegment mTabSegment;
    private View root;
    private Vector<BaseFragment> mPages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        root = LayoutInflater.from(this).inflate(R.layout.activity_main, null);
        setContentView(root);
        mViewPager = (ViewPager)findViewById(R.id.pager);
        mTabSegment = (QMUITabSegment)findViewById(R.id.tabs);
        QMUIStatusBarHelper.translucent(this, getResources().getColor(R.color.app_color_theme_1));

        mPages = new Vector<>();
        initTabs();
        initPagers();
        //
        TimeZone azone = TimeZone.getDefault();
        String  strid = azone.getID();
        if(strid.contains("Shanghai")){  //Asia/Shanghai
            CfgData.dataidx = 4;
        }else if(strid.contains("America")){
            CfgData.dataidx = 3;
        }
        // data
        SsSerivce.getInstance().Start(this);
        CfgData.readSystBtnInfo(this);
        //
        FileInitCheck();
        //
        ModelFileCHeck();
        //
        CfgData.OpenConfig(this);
        mCircleProgressBar = findViewById(R.id.circleProgressBar);
        dochkUpdate(strid);
        CfgData.mobileserial = MobileUUID.getUniquePsuedoID2(this);
        //
        QMUISkinManager skinManager = QMUISkinManager.defaultInstance(this);
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            skinManager.register(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            for (int i = 0; i < mPages.size(); i++) {
                ((BaseFragment)mPages.get(i)).doResultOk(requestCode,data);
            }
        }
    }

    private void initTabs() {
        int normalColor = QMUIResHelper.getAttrColor(this, R.attr.qmui_config_color_gray_6);
        int selectColor = QMUIResHelper.getAttrColor(this, R.attr.qmui_config_color_blue);
        //mTabSegment.setDefaultNormalColor(normalColor);
        //mTabSegment.setDefaultSelectedColor(selectColor);

        QMUITabBuilder builder = mTabSegment.tabBuilder();
        builder.setTypeface(null, Typeface.DEFAULT_BOLD);
        builder.setSelectedIconScale(1.2f)
                .setTextSize(QMUIDisplayHelper.sp2px(getContext(), 13), QMUIDisplayHelper.sp2px(getContext(), 15))
                .setDynamicChangeIconColor(false);
        QMUITab mys = builder
                .setNormalDrawable(ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_component))
                .setSelectedDrawable(ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_component_selected))
                .setText(getText(R.string.str_remote))
                .build(getContext());
        QMUITab prc = builder
                .setNormalDrawable(ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_util))
                .setSelectedDrawable(ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_util_selected))
                .setText(getText(R.string.str_prc))
                .build(getContext());
        mTabSegment.addTab(mys)
                .addTab(prc);
//                .addTab(mall);

        mTabSegment.setOnTabClickListener(new QMUIBasicTabSegment.OnTabClickListener() {
            @Override
            public boolean onTabClick(QMUITabView tabView,int index) {
                BaseFragment abase = mPages.get(index);
                abase.DoClickShow();
                return false;
            }
        });
    }



    private void initPagers() {
        mPages.clear();
        BaseFragment rmt = new RemoteFragment(MainActivity.this,root);
        rmt.Init();
        mPages.add(rmt);

        BaseFragment prc = new PrcFragment(MainActivity.this,root);
        prc.Init();
        mPages.add(prc);

//        BaseFragment mall = new MallFragment(MainActivity.this,root);
//        mall.Init();
//        mPages.add(mall);

        mViewPager.setAdapter(mPagerAdapter);
        mTabSegment.setupWithViewPager(mViewPager, false);
    }

    enum Pager {
        RMT, PRC,MALL;

        public static Pager getPagerFromPositon(int position) {
            switch (position) {
                case 0:
                    return RMT;
                case 1:
                    return PRC;
                case 2:
                    return MALL;
                default:
                    return RMT;
            }
        }
    }

    private PagerAdapter mPagerAdapter = new PagerAdapter() {
        private int mChildCount = 0;
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getCount() {
            return mPages.size();
        }

        @Override
        public Object instantiateItem(final ViewGroup container, int position) {
            BaseFragment apages = mPages.get(position);
            View view =apages.getApgeview();
            view.setTag(position);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            container.addView(view, params);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(Object object) {
            if (mChildCount == 0) {
                return POSITION_NONE;
            }
            return super.getItemPosition(object);
        }

        @Override
        public void notifyDataSetChanged() {
            mChildCount = getCount();
            super.notifyDataSetChanged();
        }
    };

    @Override
    public void onDestroy(){
//        Intent intent = new Intent();
//        intent.setAction(MyServices.brod_SvrName);
//        intent.putExtra("cmd", MyServices.BROD_STOPSERV);
//        sendBroadcast(intent);
//        unbindService(conn);
        //stopService(intentsvr);
        SsSerivce.getInstance().stopped();
        //((BaseApplication) getApplication()).SvronTerminate();
        super.onDestroy();
    }

    private void dochkUpdate(String zonestr){
        Log.w(TAG_SS,"===>chkupdate...");
        webHttpClientCom.getInstance(MainActivity.this).RestkHttpCallBase("www/longerir.txt?"+zonestr, null, "GET", new webHttpClientCom.WevEvent() {
            @Override
            public void onSuc(byte[] out) {
                String src = new String(out);
                final String[] sarr  = src.split(" ");
                if(sarr.length!=2)return;
                Log.w(TAG_SS,"==============>update:sss");
                int m = Integer.valueOf(sarr[0]);

                int versioncode = 0;
                try {
                    PackageManager pm = MainActivity.this.getPackageManager();
                    PackageInfo pi = pm.getPackageInfo(MainActivity.this.getPackageName(), 0);
                    versioncode = pi.versionCode;
                } catch (Exception e) {
                    Log.e(TAG_SS, "Err get version", e);
                }
                Log.w(TAG_SS,"==============>update:"+m+","+versioncode);
                if(m<=versioncode)return;
                new QMUIDialog.MessageDialogBuilder(MainActivity.this)
                        .setTitle(MainActivity.this.getString(R.string.str_info))
                        .setMessage(MainActivity.this.getString(R.string.str_foundnewversion))
                        .addAction(MainActivity.this.getString(R.string.str_cancel), new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.dismiss();
                            }
                        })
                        .addAction(0, MainActivity.this.getString(R.string.str_Ok), QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.dismiss();
                                checkIsAndroid(sarr[1]);
                            }
                        })
                        .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
            }

            @Override
            public void onFail(String res) {

            }

            @Override
            public boolean onDoData(byte[] out) {
                return true;
            }
        });
    }

    //** * 判断是否是8.0,8.0需要处理未知应用来源权限问题,否则直接安装 */
    private void checkIsAndroid(String url){
        if (Build.VERSION.SDK_INT >= 26) {
            boolean b = getPackageManager().canRequestPackageInstalls();
            if (b) {
                dodownFileAndInstall(url);
            } else {
                //请求安装未知应用来源的权限
                if (this.checkSelfPermission(Manifest.permission.REQUEST_INSTALL_PACKAGES) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, 12);
                    dodownFileAndInstall(url);
                }else{
                    //dodownFileAndInstall(url);
                }
            }
        }else {
            dodownFileAndInstall(url);
        }
    }

    private QMUIProgressBar mCircleProgressBar;
    private void dodownFileAndInstall(String url){
        mCircleProgressBar.setQMUIProgressBarTextGenerator(new QMUIProgressBar.QMUIProgressBarTextGenerator() {
            @Override
            public String generateText(QMUIProgressBar progressBar, int value, int maxValue) {
                return 100 * value / maxValue + "%";
            }
        });
        mCircleProgressBar.setVisibility(View.VISIBLE);
        mCircleProgressBar.setProgress(0,false);
        webHttpClientCom.getInstance(this).BackHttpGetFile(url, new webHttpClientCom.OnDownEventer() {
            @Override
            public void onSuc(final String file) {
                Log.w(TAG_SS,"===>do install file "+file);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Intent intent = new Intent();
                        // 执行动作
                        intent.setAction(Intent.ACTION_VIEW);
                        // 执行的数据类型
                        //判断是否是AndroidN以及更高的版本
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            Uri contentUri = FileProvider.getUriForFile(MainActivity.this,"clurc.net.longerir.fileProvider", new File(file));
                            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                        } else {
                            intent.setDataAndType(Uri.fromFile(new File(file)), "application/vnd.android.package-archive");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }
                        MainActivity.this.startActivity(intent);
                    }
                });
            }

            @Override
            public void onFail(final String res) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        mCircleProgressBar.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, res, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onPosition(final int curr,final int total) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if(curr>=0)
                            mCircleProgressBar.setProgress(curr,false);
                        if(total>=0)
                            mCircleProgressBar.setMaxValue(total);
                    }
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        for (int i = 0; i < mPages.size(); i++) {
            BaseFragment abase = mPages.get(i);
            if(abase!=null)
                abase.DoOnResume();
        }
    }

    public native void Init();
    public native void Cleanup();
    //--------------jini------------------------------------------------------------------------------
    public native static int[] acGetIRData(String p0,int powval);
    public native static int[] irGetIRDataByRaw(byte[] rawbuf,int rawlen,int pressbtn,int pressval);
    public native static void irSetStatus(byte[] val);
    public native static byte[] irGetStatus();
    //---------------------------------------------------
    private native void setMobileId(String astr);
    private native void setLangugeId(String alan);

    public native int irFileInit(Context context_object, String filedir);
    //--remote-----------------------------------
    public native static int[] irGsGetData(long[] param,int paralen,int pid);
    public native static int irGetCurrentFreq();
    //
    public native static byte[] cHttpGet(String p0);

    private void FileInitCheck(){
        if(!copyAssetFileToFiles("modelsys.db")){
            Log.v("sungle","copy file err");
        }
        if(!copyAssetFileToFiles("irapp.db")){
            Log.v("sungle","copy file err");
            return;
        }
        int gsVer = irFileInit(this,getFilesDir().getAbsolutePath()+ File.separator);
        return;
    }
    private boolean copyAssetFileToFiles(String filename){
        String desname = getFilesDir().getAbsolutePath()+File.separator + filename;
/*
        File file = new File(desname);
        if (file.isFile() && file.exists()) {
            file.delete();
        }
*/
        if (new File(desname).exists()){
            return true;
        }
        FileOutputStream fos;
        try{
            fos = new FileOutputStream(desname);
        }catch(FileNotFoundException e){
            Log.e("IR_Data", "can't create FileOutputStream");
            return false;
        }
        try {
            InputStream is = getAssets().open(filename);//getResources().openRawResource(R.raw.irapp);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = is.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
            fos.close();
            is.close();
        } catch (Exception e) {
            Log.e("DB_ERROR", "数据文件读写失败");
        }
        return  true;
    }

    private boolean copyFileToFiles(String srcname,String desname){
        if (new File(desname).exists()){
            return true;
        }
        FileOutputStream fos;
        try{
            fos = new FileOutputStream(desname);
        }catch(FileNotFoundException e){
            Log.e("IR_Data", "can't create FileOutputStream");
            return false;
        }
        try {
            FileInputStream is = new FileInputStream(srcname);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = is.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
            fos.close();
            is.close();
        } catch (Exception e) {
            Log.e("DB_ERROR", "数据文件读写失败");
        }
        return  true;
    }

    private void ModelFileCHeck(){
        String src=  getFilesDir().getAbsolutePath()+ BtnModelData.mymodelfilebak;
        String des=  getFilesDir().getAbsolutePath()+ BtnModelData.mymodelfile;
        copyFileToFiles(src,des);
    }
}
