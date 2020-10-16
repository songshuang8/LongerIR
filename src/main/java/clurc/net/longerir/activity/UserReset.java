package clurc.net.longerir.activity;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import java.net.URLEncoder;

import clurc.net.longerir.R;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.webHttpClientCom;

public class UserReset extends BaseActivity {
    EditText mPass1;
    EditText mPass2;
    QMUIRoundButton mBtnChange;

    private String username;

    @Override
    public void getViewId() {
        layid = R.layout.user_activity_reset;
        title = getString(R.string.str_password_reset);
    }
    @Override
    public void DoInit() {
        mPass1 = findViewById(R.id.password1);
        mPass2 = findViewById(R.id.password2);
        mBtnChange = findViewById(R.id.btnreg);
        //
        username = getIntent().getExtras().getString("usrname");
        //
        mBtnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(QMUILangHelper.isNullOrEmpty(mPass1.getText().toString())){
                    Toast.makeText(instance,
                            "invalid password!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(QMUILangHelper.isNullOrEmpty(mPass2.getText().toString())){
                    Toast.makeText(instance,
                            "invalid confirm password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!mPass1.getText().toString().equals(mPass2.getText().toString())){
                    Toast.makeText(instance,
                            "password is not same", Toast.LENGTH_SHORT).show();
                    return;
                }
                //username,mPass1.getText().toString()
                webHttpClientCom.getInstance(instance).RestkHttpCall("newaddorresetpswd?EMAIL=" + username
                        + "&PASWD=" + mPass1.getText().toString(),null,"GET", new webHttpClientCom.WevEvent_NoErrString() {
                    @Override
                    public void onSuc() {
                        tipDialog = new QMUITipDialog.Builder(instance)
                                .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                .setTipWord("Regist successful")
                                .create();
                        tipDialog.show();
                        mBtnChange.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tipDialog.dismiss();
                            }
                        }, 1500);
                    }

                    @Override
                    public boolean onDodata(String res) {
                        if(res.equals("ok"))
                            return true;
                        else {
                            return false;
                        }
                    }
                });
            }
        });
    }
}
