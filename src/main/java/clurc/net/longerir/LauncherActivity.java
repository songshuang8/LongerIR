package clurc.net.longerir;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import clurc.net.longerir.manager.QDPreferenceManager;

public class LauncherActivity extends Activity {
    private Handler waitHandler=null;
    private Runnable waitRunnable=null;
    private  boolean bwaitingRight = false;
    private  boolean bcanexit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        QDPreferenceManager.getInstance(this).App_AutoLogin();
        setContentView(R.layout.activity_launcher);
        waitHandler=new Handler();
        RequestMyPermission();
        waitRunnable=new Runnable() {
            @Override
            public void run() {
                if(bwaitingRight) {
                    if(bcanexit==false)
                        waitHandler.postDelayed(waitRunnable, 2000);
                }else{
                    startActivity(new Intent(LauncherActivity.this, MainActivity.class));
                    LauncherActivity.this.finish();
                }
            }
        };
        waitHandler.postDelayed(waitRunnable, 2000);
    }

    String[] permissions = new String[]{
            Manifest.permission.INTERNET,

            Manifest.permission.READ_EXTERNAL_STORAGE,

            //Manifest.permission.READ_PHONE_STATE,
            //Manifest.permission.READ_PHONE_NUMBERS,

            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,

            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.VIBRATE,

            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
    };
    List<String> mPermissionList = new ArrayList<>();
    private void RequestMyPermission(){
        if(Build.VERSION.SDK_INT>=23) {
            mPermissionList.clear();
            for (int i = 0; i < permissions.length; i++) {
                if (checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }
            if (mPermissionList.size()>0) {
                bwaitingRight = true;
                String[] perno = mPermissionList.toArray(new String[mPermissionList.size()]);
                requestPermissions(perno,11);
            }else{
                bwaitingRight = false;
            }
        }else{
            bwaitingRight = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 11:
                boolean haveallright = true;
                boolean bforbiddon = false;
                String aqx="";
                if(Build.VERSION.SDK_INT>=23) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            haveallright = false;
                            //判断是否勾选禁止后不再询问
                            if (shouldShowRequestPermissionRationale(permissions[i])) {//
                                //重新申请权限
                            } else {
                                bforbiddon = true;//已经禁止
                                aqx = aqx+"/"+permissions[i];
                                //break;
                            }
                        }
                    }
                }
                if(!haveallright){
                    if(bforbiddon){
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage("您已经禁止所需权限:"+aqx);
                        builder.setTitle("软件权限");
                        builder.setCancelable(false);
                        builder.setPositiveButton("退出",
                                new android.content.DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        bcanexit = true;
                                        dialog.dismiss();
                                        System.exit(0);
                                    }
                                });
                        builder.setNegativeButton("继续",
                                new android.content.DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        bwaitingRight = false;
                                    }
                                });
                        builder.create().show();
                    }else
                        RequestMyPermission();
                }else{
                    bwaitingRight = false;
                }
                break;
            default:
                break;
        }
    }


}
