package clurc.net.longerir.manager;

import android.content.Context;
import android.content.res.Configuration;

import com.qmuiteam.qmui.skin.QMUISkinManager;

import clurc.net.longerir.BaseApplication;
import clurc.net.longerir.R;

public class QDSkinManager {
    public static final int SKIN_BLUE = 1;
    public static final int SKIN_DARK = 2;
    public static final int SKIN_WHITE = 3;


    public static void install(Context context) {
        QMUISkinManager skinManager = QMUISkinManager.defaultInstance(context);
        skinManager.addSkin(SKIN_BLUE, R.style.app_skin_blue);
        skinManager.addSkin(SKIN_DARK, R.style.app_skin_dark);
        skinManager.addSkin(SKIN_WHITE, R.style.app_skin_white);
        boolean isDarkMode = (context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        skinManager.changeSkin(SKIN_BLUE);
    }

    public static void changeSkin(int index) {
        QMUISkinManager.defaultInstance(BaseApplication.getMyApplication()).changeSkin(index);
    }

    public static int getCurrentSkin() {
        return QMUISkinManager.defaultInstance(BaseApplication.getMyApplication()).getCurrentSkin();
    }
}
