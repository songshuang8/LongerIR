package clurc.net.longerir.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import clurc.net.longerir.R;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.webHttpClientCom;

import static clurc.net.longerir.manager.UiUtils.getContext;

public class BaseFragment{
    public QMUITopBar mTopBar;
    public QMUITipDialog tipDialog;
    public Context context;
    public View root;
    public View view;
    public String title;
    private HomeControlListener mHomeControlListener;

    public BaseFragment(Context context,View root) {
        this.context = context;
        this.root = root;
    }

    public  void OnGetView(){
    }
    public  void viewInit(){
    }
    public  void Init(){
        OnGetView();
        mTopBar = (QMUITopBar)view.findViewById(R.id.topbar);
        mTopBar.setBackgroundColor(ContextCompat.getColor(context, R.color.app_color_theme_1));
        mTopBar.setTitle(title);
        viewInit();
    }

    public View getApgeview() {
        return view;
    }

    public void doResultOk(int which, Intent data){

    }

    public void showMessage(String title,String contxt){
        new QMUIDialog.MessageDialogBuilder(context)
                //.setTitle(title)
                .setMessage(contxt)
                .setSkinManager(QMUISkinManager.defaultInstance(getContext()))
                .addAction("Ok", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public  void DoClickShow(){
    }

    public void DoOnResume(){
    }

    public interface HomeControlListener {
        void startFragment(BaseFragment fragment);
    }

    public void ShowDialog(String str){
        tipDialog = new QMUITipDialog.Builder(context)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                .setTipWord(str)
                .create();
        tipDialog.show();
        mTopBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                tipDialog.dismiss();
            }
        }, 1500);
    }

    public void showwait(){
        tipDialog = new QMUITipDialog.Builder(context)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(context.getString(R.string.str_wait))
                .create();
        tipDialog.show();
    };

    public interface OnActivityEventer {
        void onSuc(byte[] out);
    }
}
