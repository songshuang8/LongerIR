package clurc.net.longerir.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import clurc.net.longerir.R;
import clurc.net.longerir.Utils.SysFun;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.manager.QDPreferenceManager;
import clurc.net.longerir.uicomm.SsSerivce;

public class SelectIrPort extends BaseActivity {
    private ImageView[] mchk ;
    private TextView[] mdes;
    private TextView[] mtitle;
    private View[] view_split;
    private int oldsel;
    private boolean notaudio;
    private boolean nobleremote;
    private boolean nomobileir;
    private TableRow tbaudio;
    @Override
    public void getViewId() {
        layid = R.layout.sel_irport;
        title = getString(R.string.str_pls_sel_irport);
        Bundle abundle = getIntent().getExtras();
        notaudio = true;
        if(abundle!=null) {
            if(abundle.containsKey("baudio"))
                notaudio = getIntent().getExtras().getBoolean("baudio");
        }
        nobleremote = false;
        if(abundle!=null) {
            if(abundle.containsKey("nobleremote"))
                nobleremote = getIntent().getExtras().getBoolean("nobleremote");
        }
    }

    @Override
    public void DoInit() {
        oldsel = CfgData.selectIr;
        mchk = new ImageView[4];
        mchk[0] = ((ImageView)findViewById(R.id.imgchk1));
        mchk[1] = ((ImageView)findViewById(R.id.imgchk2));
        mchk[2] = ((ImageView)findViewById(R.id.imgchk3));
        mchk[3] = ((ImageView)findViewById(R.id.imgchk4));
        mdes = new TextView[4];
        mdes[0] = (TextView)findViewById(R.id.tx13);
        mdes[1] = (TextView)findViewById(R.id.tx23);
        mdes[2] = (TextView)findViewById(R.id.tx33);
        mdes[3] = (TextView)findViewById(R.id.tx43);
        view_split = new View[4];
        view_split[0] = findViewById(R.id.sp1);
        view_split[1] = findViewById(R.id.sp2);
        view_split[2] = findViewById(R.id.sp3);
        view_split[3] = findViewById(R.id.sp4);
        mtitle = new TextView[4];
        mtitle[0] = findViewById(R.id.tx11);
        mtitle[1] = findViewById(R.id.tx21);
        mtitle[2] = findViewById(R.id.tx31);
        mtitle[3] = findViewById(R.id.tx41);

        setSelected();

        if(!SysFun.IfHasIrDaPort(instance)){
            mtitle[2].setTextColor(getResources().getColorStateList(R.color.bar_divider));
            mdes[2].setText(getString(R.string.str_unsupported));
            mdes[2].setTextColor(getResources().getColorStateList(R.color.bar_divider));
            if(CfgData.selectIr==2)CfgData.selectIr = 0;
        }else{
            mdes[2].setText(getString(R.string.str_ready));
        }
        tbaudio = findViewById(R.id.rowaudio);
        if(notaudio){
            if(CfgData.selectIr==3)CfgData.selectIr = 0;
            tbaudio.setVisibility(View.GONE);
            view_split[3].setVisibility(View.GONE);
        }

        if(!SysFun.IfhasBle(instance) || nobleremote){
            mtitle[1].setTextColor(getResources().getColorStateList(R.color.bar_divider));
            mdes[1].setTextColor(getResources().getColorStateList(R.color.bar_divider));

            mdes[1].setText(getString(R.string.str_unsupported));
            mdes[1].setOnClickListener(null);
        }else{
            mdes[1].setText(getString(R.string.str_notfound));
            mdes[1].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DoClick1(mdes[1]);
                }
            });
        }

        mdes[0].setText(getString(R.string.str_notfound));
        setHidAndBle(SsSerivce.getInstance().getHidState(),SsSerivce.getInstance().getBlvState());
        mdes[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse("http://www.clurc.cn/?p=437");
                intent.setData(uri);
                startActivity(intent);
            }
        });
        mdes[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse("http://www.clurc.cn/?p=437");
                intent.setData(uri);
                startActivity(intent);
            }
        });
    }

    @Override
    public void DoShowing(){
        if(CfgData.selectIr==1) {
            SysFun.BleCheckOpend(instance);
        }
    }

    public void DoClick0(View v){
        CfgData.selectIr = 0;
        setSelected();
    }

    public void DoClick1(View v){
        if(nobleremote)return;
        if(!SysFun.IfhasBle(instance)){
            ShowDialog(getString(R.string.str_unsupported));
            return;
        }
        SysFun.BleCheckOpend(instance);
        CfgData.selectIr = 1;
        setSelected();
    }

    public void DoClick2(View v){
        if(!SysFun.IfHasIrDaPort(instance)){
            return;
        }
        CfgData.selectIr = 2;
        setSelected();
    }

    public void DoClick3(View v){
        if(notaudio)return;
        CfgData.selectIr = 3;
        setSelected();
    }

    private void setSelected(){
        for (int i = 0; i < mchk.length; i++) {
            if(i== CfgData.selectIr){
                mchk[i].setBackgroundResource(R.mipmap.selected);
            }else{
                mchk[i].setBackgroundResource(R.mipmap.blank64);
            }
        }
    }

    @Override
    public void DoServiceMesg(int cmd,Intent intent) {
        switch (cmd) {
            case SsSerivce.BROD_CMDUI_HIDSTATE:
                setHidAndBle((boolean) intent.getSerializableExtra("hid"),(boolean) intent.getSerializableExtra("blv"));
                break;
        }
    }

    @Override
    public boolean DoBack(){
        if(oldsel!=CfgData.selectIr)
            QDPreferenceManager.getInstance(instance).setIrPort(CfgData.selectIr);
        return true;
    }

    private void setHidAndBle(boolean bhid,boolean bblv){
        if (bhid) {
            mdes[0].setText(getString(R.string.str_ready));
        } else {
            mdes[0].setText(getString(R.string.str_notfound));
        }
        if (bblv) {
            mdes[1].setText(getString(R.string.str_ready));
        } else {
            mdes[1].setText(getString(R.string.str_notfound));
        }
    }
}
