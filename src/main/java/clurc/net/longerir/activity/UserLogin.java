package clurc.net.longerir.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import clurc.net.longerir.MainActivity;
import clurc.net.longerir.R;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.manager.QDPreferenceManager;

public class UserLogin extends BaseActivity {
    private QMUITopBar mTopBar;
    private QMUIRoundButton mBtnReg;
    private QMUIRoundButton mBtnLog;
    private EditText mEdtName;
    private EditText mEdtPasswd;
    private TextView mGotPassword;
    private CheckBox mAutoLogin;
    private int delayms = 1500;

    @Override
    public void getViewId() {
        layid = R.layout.user_activity_login;
        title = getString(R.string.str_login);
    }

    @Override
    public void DoInit() {
        mTopBar = findViewById(R.id.topbar);
        mBtnLog = findViewById(R.id.btnlogin);
        mBtnReg = findViewById(R.id.btnreg);
        mEdtName = findViewById(R.id.edtname);
        mEdtPasswd = findViewById(R.id.edtmodel);
        mGotPassword = findViewById(R.id.gotpassword);
        mAutoLogin = findViewById(R.id.auto_login);
        mBtnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(instance, UserRegister.class);
                intent.putExtra("isregister", true);
                startActivity(intent);
            }
        });
        //
        mBtnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (QMUILangHelper.isNullOrEmpty(mEdtName.getText().toString())) {
                    Toast.makeText(instance,
                            getString(R.string.str_input_validname), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (QMUILangHelper.isNullOrEmpty(mEdtPasswd.getText().toString())) {
                    Toast.makeText(instance,
                            getString(R.string.str_input_password), Toast.LENGTH_SHORT).show();
                    return;
                }
                final String u = mEdtName.getText().toString();
                String p = mEdtPasswd.getText().toString();
                String urlparam = "";
                try {
                    urlparam = "ClientLoginHz?LOGONNAME=" + URLEncoder.encode(u, "UTF-8")
                            + "&PASSWORD=" + URLEncoder.encode(p, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    urlparam = "ClientLoginHz?LogonName=" + u
                            + "&Password=" + p;
                }
                BackgroundRest(urlparam,null,"GET", new OnActivityEventer() {
                    @Override
                    public void onSuc() {
                        QDPreferenceManager.getInstance(instance).setAutoLogin(mAutoLogin.isChecked());
                        QDPreferenceManager.getInstance(instance).setLoginUser(mEdtName.getText().toString());
                        QDPreferenceManager.getInstance(instance).setLoginPswd(mEdtPasswd.getText().toString());
                        tipDialog = new QMUITipDialog.Builder(instance)
                                .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                .setTipWord(getString(R.string.str_login_ok))
                                .create();
                        tipDialog.show();
                        mBtnLog.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tipDialog.dismiss();
                                Intent intent=new Intent();
                                setResult(Activity.RESULT_OK, intent);
                                instance.finish();
                            }
                        }, delayms);
                    }

                    @Override
                    public boolean onDodata(String res) {
                        try {
                            JSONObject jsonObj = new JSONObject(res);
                            CfgData.userid = jsonObj.getInt("id");
                            CfgData.usertype = jsonObj.getInt("type");
                            if(CfgData.userid>0){
                                CfgData.username = u;
                                QDPreferenceManager.getInstance(instance).setAPP_userid(CfgData.userid);
                                QDPreferenceManager.getInstance(instance).setAPP_usertype(CfgData.usertype);
                                return true;
                            }
                            CfgData.userid = -1;
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                        errstr = getString(R.string.str_try);
                        return false;
                    }
                });
            }
        });
        //
        mGotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(instance, UserRegister.class);
                intent.putExtra("isregister", false);
                startActivity(intent);
            }
        });
        //
        mAutoLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                QDPreferenceManager.getInstance(instance).setAutoLogin(isChecked);
            }
        });

        mAutoLogin.setChecked(QDPreferenceManager.getInstance(instance).getAutoLogin());
        if (mAutoLogin.isChecked()) {
            mEdtName.setText(QDPreferenceManager.getInstance(instance).getLoginUser());
            mEdtPasswd.setText(QDPreferenceManager.getInstance(instance).getLoginPswd());
        }
    }
}
