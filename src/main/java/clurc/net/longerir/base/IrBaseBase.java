package clurc.net.longerir.base;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import clurc.net.longerir.R;
import clurc.net.longerir.data.webHttpClientCom;

public class IrBaseBase extends Activity {
    public QMUITipDialog tipDialog;
    public static final String WEBTAG = "http_request";
    private Handler mHandler;
    private boolean mWebCancel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HandlerThread thread = new HandlerThread("webhttp");
        thread.start();//创建一个HandlerThread并启动它
        mHandler = new Handler(thread.getLooper());//使用HandlerThread的looper对象创建Handler，如果使用默认的构造方法，很有可能阻塞UI线程
    }

    public interface OnActivityEventer {
        void onSuc();
        boolean onDodata(String res);
    }

    public void showwait() {
        tipDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(getString(R.string.str_wait))
                .create();
        tipDialog.show();
    }

    //实现耗时操作的线程
    Runnable mBackgroundRunnable = new Runnable() {
        @Override
        public void run() {
            //----------模拟耗时的操作，开始---------------
            Log.i(WEBTAG, "web thread running!");

            //mUIHandler.sendMessage();
            //----------模拟耗时的操作，结束---------------
            Log.i(WEBTAG, "web thread stop!");
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mWebCancel = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mWebCancel = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //销毁线程
        mHandler.removeCallbacks(mBackgroundRunnable);
    }
}
