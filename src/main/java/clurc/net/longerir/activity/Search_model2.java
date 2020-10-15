package clurc.net.longerir.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import clurc.net.longerir.MainActivity;
import clurc.net.longerir.R;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.BM_ModelData;
import clurc.net.longerir.data.BtnInfo;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.ClassAcStatus;
import clurc.net.longerir.data.RemoteInfo;
import clurc.net.longerir.data.TxtBtnInfo;
import clurc.net.longerir.data.webHttpClientCom;
import clurc.net.longerir.uicomm.SsSerivce;

public class Search_model2 extends BaseActivity {
    private String selpp;
    private String seldev;
    private TextView tvPercent;
    private ImageButton mleft,mright,mkey;
    private RelativeLayout mbotom;
    private QMUIRoundButton mno,myes;
    private List<BM_ModelData> dataall=new ArrayList<BM_ModelData>(); //某品牌下的所有
    private int currpos=1;
    private BM_ModelData axh;
    private RemoteInfo aremote;
    private List<TxtBtnInfo> txtbtns = new ArrayList<TxtBtnInfo>();;
    private boolean pow = false;//空调开关
    @Override
    public void getViewId(){
        layid = R.layout.activity_search_model2;
        selpp = getIntent().getExtras().getString("pp");
        seldev = getIntent().getExtras().getString("dev");
        title = selpp+" | "+seldev;
    }
    @Override
    public void DoInit() {
        tvPercent = findViewById(R.id.tvpercent);
        mleft =findViewById(R.id.btnleft);
        mright = findViewById(R.id.btnright);
        mkey = findViewById(R.id.btnpow);
        mbotom = findViewById(R.id.layout_bottom);
        mno = findViewById(R.id.btnno);
        myes = findViewById(R.id.btnyes);
        getModelData();
        mleft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currpos--;
                if(currpos<1)currpos=1;
                setLeftAndRight();
            }
        });
        mright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currpos++;
                if(currpos>dataall.size())currpos=dataall.size();
                setLeftAndRight();
            }
        });
        mkey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dodownAndRush();
            }
        });
        myes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<BtnInfo> btns = new ArrayList<BtnInfo>();
                CfgData.TransTxtToBtns(txtbtns, btns, aremote.dev,aremote.isAc);
                if(CfgData.AppendoOrEditMyFile(instance, aremote, btns)) {
                    CfgData.myremotelist.add(aremote);
                    setResult(Activity.RESULT_OK, new Intent());
                    instance.finish();
                }else{
                    Toast.makeText(instance, "Unknown err", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currpos++;
                if(currpos>dataall.size())currpos=dataall.size();
                setLeftAndRight();
                mbotom.setVisibility(View.INVISIBLE);
            }
        });
        mTopBar.addRightImageButton(R.mipmap.icon_topbar_overflow, R.id.topbar_right_change_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setClass(instance, SelectIrPort.class);
                        intent.putExtra("baudio",true);
                        startActivity(intent);
                    }
                });
    }

    private void getModelData(){
        if(QMUILangHelper.isNullOrEmpty(selpp)){
            ShowDialog("the string error!");
            return;
        }
        String param = "search_models2?data="+CfgData.dataidx+"&xh=&pp=";
        try{
            param += URLEncoder.encode(selpp,"UTF-8");
        }catch (UnsupportedEncodingException e){
            param += selpp;
        }
        param += "&dev=";
        try{
            param += URLEncoder.encode(seldev,"UTF-8");
        }catch (UnsupportedEncodingException e){
            param += seldev;
        }
        BackgroundRest(param,null,"GET", new OnActivityEventer() {
            @Override
            public void onSuc() {
                currpos = 1;
                setLeftAndRight();
            }
            @Override
            public boolean onDodata(String res) {
                dataall.clear();
                try {
                    Object json = new JSONTokener(res).nextValue();
                    if(json instanceof JSONArray) {
                        JSONArray jsonAll = (JSONArray) json;
                        for (int i = 0; i < jsonAll.length(); i++) {
                            JSONObject jsonsingle = (JSONObject) jsonAll.get(i);
                            BM_ModelData axh = new BM_ModelData(
                                    jsonsingle.getInt("ID"),
                                    jsonsingle.getString("xh").toUpperCase(),
                                    jsonsingle.getString("dev"),
                                    0,0,0,
                                    "",
                                    jsonsingle.getInt("ty"),
                                    jsonsingle.getInt("isAc")
                            );
                            axh.setPp(jsonsingle.getString("pp"));
                            dataall.add(axh);
                        }
                    }
                    return true;
                }catch (JSONException e) {
                    e.printStackTrace();
                    errstr = e.getMessage();
                }
                return false;
            }
        });
    }

    private void setLeftAndRight(){
        if(currpos==1){
            mleft.setVisibility(View.INVISIBLE);
        }else{
            mleft.setVisibility(View.VISIBLE);
        }
        if(currpos==dataall.size()){
            mright.setVisibility(View.INVISIBLE);
        }else{
            mright.setVisibility(View.VISIBLE);
        }
        tvPercent.setText(getString(R.string.str_rush_tip3)+"("+currpos+"/"+dataall.size()+")");
    }

    private void dodownAndRush(){
        if (dataall.size() < 1) return;
        if (currpos > dataall.size()) return;
        axh = dataall.get(currpos-1);
        //--------------------------------------------------------
        String param = CfgData.getRmtUrl(axh.getType(),axh.getId());
        if(param==null){
            ShowDialog("error data type!");
            return;
        }
        aremote = new RemoteInfo();
        aremote.pp = axh.getPp();
        aremote.xh = axh.getXh();
        aremote.dev = axh.getDev();
        aremote.isAc = axh.getIsAc();
        webHttpClientCom.getInstance(instance).Rest_DownRemote(aremote,txtbtns,param,new webHttpClientCom.RestOnAppEvent() {
            @Override
            public void onSuc() {
                if(aremote.isAc==CfgData.AcLear){
                    if(CheckIrDaPort(busb,bble)) {
                        TxtBtnInfo abtn = getPowBtn(pow);
                        if(abtn!=null)
                            dorushWave(abtn.wave,abtn.gsno);
                        pow = !pow;
                    }else{
                        showMessage(null,getString(R.string.str_nodev));
                    }
                    mbotom.setVisibility(View.VISIBLE);
                }else
                if(aremote.isAc==CfgData.AcPro){
                    byte[] statusArr = new byte[ClassAcStatus.BTN_COUNT];
                    for (int i = 0; i < ClassAcStatus.BTN_COUNT; i++)
                        statusArr[i] = 0;
                    if(irpowerturx) {
                        statusArr[0] = 1;
                    }else{
                        statusArr[0] = 0;
                    }
                    statusArr[1] = 8;
                    statusArr[2] = 1;
                    if(CheckIrDaPort(busb,bble)) {
                        RushAc(aremote.acdata,statusArr,0,0);
                    }else{
                        showMessage(null,getString(R.string.str_nodev));
                    }
                    mbotom.setVisibility(View.VISIBLE);
                }else {
                    if (txtbtns.size() == 0) {
                        errstr = "err in the server";
                        ShowDialog(errstr);
                        return;
                    }
                    TxtBtnInfo arush = foundInKeys(txtbtns);
                    if(CheckIrDaPort(busb,bble)) {
                        RushingKey(arush);
                    }else{
                        showMessage(null,getString(R.string.str_nodev));
                    }
                    mbotom.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private TxtBtnInfo foundInKeys(List<TxtBtnInfo> btns){
        // pow
        for (int i = 0; i < btns.size(); i++) {
            if(btns.get(i).keyidx==0){
                return btns.get(i);
            }
        }
        // val
        for (int i = 0; i < btns.size(); i++) {
            if(btns.get(i).keyidx==27 || btns.get(i).keyidx==28){
                return btns.get(i);
            }
        }
        return null;
    }

    private boolean busb = false;
    private boolean bble = false;
    @Override
    public void DoServiceMesg(int cmd, Intent intent) {
        switch (cmd) {
            // 在线情况
            case SsSerivce.BROD_CMDUI_HIDSTATE:
                busb = (boolean) intent.getSerializableExtra("hid");
                bble = (boolean) intent.getSerializableExtra("blv");
                break;
        }
    }

    @Override
    public void onResume() {
        SsSerivce.getInstance().setStateBroadcast();
        super.onResume();
    }

    private TxtBtnInfo getPowBtn(boolean b){
        for (int i = 0; i < txtbtns.size(); i++) {
            TxtBtnInfo abtn = txtbtns.get(i);
            String s = String.valueOf(abtn.keyidx);
            if(b) {
                if (s.length() == CfgData.Ac_Lear_Sta_Count) {
                    return abtn;
                }
            }else{
                if(s.length()<CfgData.Ac_Lear_Sta_Count)
                    return abtn;
            }
        }
        if(txtbtns.size()>0)return txtbtns.get(0);
        else return null;
    }
}
