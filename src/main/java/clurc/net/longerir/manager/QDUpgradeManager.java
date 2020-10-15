package clurc.net.longerir.manager;

import android.app.Activity;
import android.content.Context;

public class QDUpgradeManager {
    public static final int INVALIDATE_VERSION_CODE = -1;
    public static final int sCurrentVersion = 1;
    private static QDUpgradeManager sQDUpgradeManager = null;
    private UpgradeTipTask mUpgradeTipTask;

    private Context mContext;

    private QDUpgradeManager(Context context) {
        mContext = context.getApplicationContext();
    }

    public static final QDUpgradeManager getInstance(Context context) {
        if (sQDUpgradeManager == null) {
            sQDUpgradeManager = new QDUpgradeManager(context);
        }
        return sQDUpgradeManager;
    }

    public void check() {
//        int oldVersion = QDPreferenceManager.getInstance(mContext).getVersionCode();
//        if (sCurrentVersion > oldVersion) {
//            if (oldVersion == INVALIDATE_VERSION_CODE) {
//                onNewInstall(sCurrentVersion);
//            } else {
//                onUpgrade(oldVersion, sCurrentVersion);
//            }
//            QDPreferenceManager.getInstance(mContext).setAppVersionCode(sCurrentVersion);
//        }
    }

    private void onUpgrade(int oldVersion, int currentVersion) {
        mUpgradeTipTask = new UpgradeTipTask(oldVersion, currentVersion);
    }

    private void onNewInstall(int currentVersion) {
        mUpgradeTipTask = new UpgradeTipTask(INVALIDATE_VERSION_CODE, currentVersion);
    }

    public void runUpgradeTipTaskIfExist(Activity activity) {
        if (mUpgradeTipTask != null) {
            mUpgradeTipTask.upgrade(activity);
            mUpgradeTipTask = null;
        }
    }
}
