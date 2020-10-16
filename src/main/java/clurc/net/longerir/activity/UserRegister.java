package clurc.net.longerir.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import clurc.net.longerir.MainActivity;
import clurc.net.longerir.R;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.webHttpClientCom;
import clurc.net.longerir.manager.QDPreferenceManager;

public class UserRegister extends BaseActivity {
    private QMUITopBar mTopBar;
    private EditText mEdtName;
    private EditText mVeryCode;
    private TextView mGotVertCode;
    private QMUIRoundButton mBtnReg;

    private boolean isnew = false;
    private QMUITipDialog tipDialog;
    private TimeCount time;
    private String vcode=null;

    @Override
    public void getViewId() {
        layid = R.layout.user_activity_register;
        isnew = getIntent().getExtras().getBoolean("isregister");
        if (isnew) {
            title =getString(R.string.str_newuser);
        } else {
            title = getString(R.string.str_password_reset);
        }
    }

    @Override
    public void DoInit() {
        mTopBar = findViewById(R.id.topbar);
        mEdtName = findViewById(R.id.username);
        mVeryCode = findViewById(R.id.edtverycode);
        mGotVertCode = findViewById(R.id.verycode);
        mBtnReg = findViewById(R.id.btnreg);
        //
        mBtnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (QMUILangHelper.isNullOrEmpty(mVeryCode.getText().toString())) {
                    Toast.makeText(instance,
                            getString(R.string.str_verifycode_input), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!mVeryCode.getText().toString().equals(vcode)) {
                    Toast.makeText(instance,
                            getString(R.string.str_invalid_verifycode), Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(instance, UserReset.class);
                intent.putExtra("usrname", mEdtName.getText().toString());
                startActivity(intent);
                instance.finish();
            }
        });
        mGotVertCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (QMUILangHelper.isNullOrEmpty(mEdtName.getText().toString())) {
                    Toast.makeText(instance,
                            getString(R.string.str_input_email), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!mEdtName.getText().toString().contains("@")) {
                    Toast.makeText(instance,
                            getString(R.string.str_input_email), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(vcode==null)
                    vcode = CfgData.CreateVeryCode();
                webHttpClientCom.getInstance(instance).RestkHttpCall("sendveryfycode?EMAIL=" + mEdtName.getText().toString()
                        + "&VCODE=" + vcode,null,"GET", new webHttpClientCom.WevEvent_NoErrString() {
                    @Override
                    public void onSuc() {
                        tipDialog = new QMUITipDialog.Builder(instance)
                                .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                .setTipWord(getString(R.string.str_ok_send_vericode))
                                .create();
                        tipDialog.show();
                        mGotVertCode.postDelayed(new Runnable() {
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

    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {// 计时完毕时触发
            mGotVertCode.setText(getString(R.string.str_getverycode));
            mGotVertCode.setClickable(true);
            //mGotVertCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.redbtn));
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程显示
            mGotVertCode.setClickable(false);
            //mGotVertCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.login_select));
            mGotVertCode.setText(millisUntilFinished / 1000 + " ");
            //ConfigData.TIME=(int) (millisUntilFinished / 1000);
        }
    }
}