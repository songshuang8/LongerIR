package clurc.net.longerir.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;

import clurc.net.longerir.MainActivity;
import clurc.net.longerir.R;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.manager.QDPreferenceManager;

import static clurc.net.longerir.uicomm.SsSerivce.TAG_SS;

public class ActivityAbout extends BaseActivity {
    private TextView database;
    @Override
    public void getViewId() {
        layid = R.layout.activity_about;
        title = getString(R.string.str_about);
    }

    @Override
    public void DoInit() {
        ((TextView)findViewById(R.id.txt3)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.clurc.net/privacy.htm");
                Intent abouturl = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(abouturl);
            }
        });
        String appv = "V 1.0";
        try {
            PackageManager pm = getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
            appv = pi.versionName;
        } catch (Exception e) {
            Log.e(TAG_SS, "Err get version", e);
        }
        ((TextView)findViewById(R.id.appver)).setText(appv);
        database = findViewById(R.id.datachanged);
        showCurrDataName();
        database.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] items = new String[4];
                items[0] = getString(R.string.str_data_auto);
                items[1] = getString(R.string.str_data_europe);
                items[2] = getString(R.string.str_data_American);
                items[3] = getString(R.string.str_data_china);
                int d = QDPreferenceManager.getInstance(instance).getDataSet()+1;
                new QMUIDialog.CheckableDialogBuilder(instance)
                        .setCheckedIndex(d)
                        .addItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                QDPreferenceManager.getInstance(instance).setDataset(which-1);
                                CfgData.getDataVersion(instance,true);
                                showCurrDataName();
                            }
                        })
                        .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
            }
        });
    }

    private void showCurrDataName(){
        String dataname = getString(R.string.str_data_europe);
        if(CfgData.dataidx==3)
            dataname = getString(R.string.str_data_American);
        else if(CfgData.dataidx == 4)
            dataname = getString(R.string.str_data_china);
        database.setText(getString(R.string.str_data_change)+":"+dataname);
    }
}
