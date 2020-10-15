package clurc.net.longerir.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import clurc.net.longerir.MainActivity;
import clurc.net.longerir.R;
import clurc.net.longerir.base.BaseActivity;

import static clurc.net.longerir.uicomm.SsSerivce.TAG_SS;

public class ActivityAbout extends BaseActivity {
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
    }
}
