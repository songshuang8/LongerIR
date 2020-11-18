package clurc.net.longerir.activity;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import java.math.BigDecimal;

import clurc.net.longerir.R;
import clurc.net.longerir.Utils.MoudelFile;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.CfgData;

public class UniversalCode extends BaseActivity {
    private class PageInfoUI{
        private LinearLayout pg;
        private TextView pagename;
        private EditText edt;
        private TextView dosel;
    }
    private PageInfoUI[] pageui;
    private String[] pages;
    private int desidx;
    @Override
    public void getViewId() {
        layid = R.layout.universal_main;
        title = getString(R.string.str_sel_universal);
    }

    @Override
    public void DoInit() {
        desidx = getIntent().getExtras().getInt("desidx");
        ((TextView)findViewById(R.id.tvdes)).setText(CfgData.modellist.get(desidx).name);
        pages = CfgData.modellist.get(desidx).pageName;
        pageui = new PageInfoUI[4];
        for (int i = 0; i < 4; i++) {
            pageui[i] = new PageInfoUI();
        }
        pageui[0].pg = findViewById(R.id.pge1);
        pageui[1].pg = findViewById(R.id.pge2);
        pageui[2].pg = findViewById(R.id.pge3);
        pageui[3].pg = findViewById(R.id.pge4);

        pageui[0].pagename = findViewById(R.id.pagename1);
        pageui[1].pagename = findViewById(R.id.pagename2);
        pageui[2].pagename = findViewById(R.id.pagename3);
        pageui[3].pagename = findViewById(R.id.pagename4);

        pageui[0].edt = findViewById(R.id.edtcode1);
        pageui[1].edt = findViewById(R.id.edtcode2);
        pageui[2].edt = findViewById(R.id.edtcode3);
        pageui[3].edt = findViewById(R.id.edtcode4);

        pageui[0].dosel = findViewById(R.id.tvselect1);
        pageui[1].dosel = findViewById(R.id.tvselect2);
        pageui[2].dosel = findViewById(R.id.tvselect3);
        pageui[3].dosel = findViewById(R.id.tvselect4);

        for (int i = 0; i < 4; i++) {
            if(i<pages.length){
                pageui[i].pg.setVisibility(View.VISIBLE);
                pageui[i].pagename.setText(pages[i]);
            }else{
                pageui[i].pg.setVisibility(View.GONE);
            }
        }
        QMUIRoundButton btn = findViewById(R.id.btnlogin);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (QMUILangHelper.isNullOrEmpty(pageui[0].edt.getText().toString())) {
                    showMessage(getString(android.R.string.dialog_alert_title),getString(R.string.str_firstpage));
                    return;
                }
                int[] codes = new int[4];
                for (int i = 0; i < 4; i++) {
                    codes[i] = -1;
                }
                for (int i = 0; i < pages.length; i++) {
                    if (QMUILangHelper.isNullOrEmpty(pageui[i].edt.getText().toString()))
                            continue;
                    String in = pageui[i].edt.getText().toString().trim();
                    if(!isNumeric(in)){
                        showMessage(getString(android.R.string.dialog_alert_title),getString(R.string.str_universal_invalid));
                        return;
                    }
                    codes[i] = Integer.valueOf(in);
                    if(codes[i]>9999){
                        showMessage(getString(android.R.string.dialog_alert_title),getString(R.string.str_universal_invalid));
                        return;
                    }
                }
                Intent intent = new Intent();
                intent.setClass(instance, IrUpdateCode.class);
                for (int i = 0; i < 4; i++) {
                    intent.putExtra("codes"+i,codes[i]);
                }
                intent.putExtra("des",desidx);
                startActivity(intent);
            }
        });
    }

    private static boolean isNumeric(String str) {
        String bigStr;
        try {
            bigStr = new BigDecimal(str).toString();
        } catch (Exception e) {
            return false;//异常 说明包含非数字。
        }
        return true;
    }
}
