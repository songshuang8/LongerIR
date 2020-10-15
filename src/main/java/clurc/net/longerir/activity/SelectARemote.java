package clurc.net.longerir.activity;

import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListPopupWindow;
import android.widget.ProgressBar;
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

import clurc.net.longerir.R;
import clurc.net.longerir.Utils.MoudelFile;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.BM_ModelData;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.RemoteInfo;
import clurc.net.longerir.data.webHttpClientCom;

public class SelectARemote extends BaseActivity{
    private EditText metBrand,metModel;
    private ProgressBar busyBrand,busyModel;
    private String str_searchpp,str_searchxh,selpp;
    private  String[]  pplist;
    private String[]  xhlist;
    private String[]  devlist;
    private int[] isAc;
    private int[] ty;
    private static int[]  idlist;
    private ListPopupWindow listPopupWindow;
    private boolean buichangectrl;
    private String seldev;
    private int selidx;
    private QMUIRoundButton mbtndo;
    @Override
    public void getViewId() {
        layid = R.layout.select_remote;
        title = getString(R.string.str_pls_sel_remote);
    }

    @Override
    public void DoInit() {
        selidx = -1;
        seldev =  getIntent().getExtras().getString("devname");
        metBrand = findViewById(R.id.edtbrand);
        metModel = findViewById(R.id.edtmodel);
        busyBrand = findViewById(R.id.progressBar1);
        busyModel = findViewById(R.id.progressBar2);
        listPopupWindow = new ListPopupWindow(this);
        buichangectrl = false;
        str_searchpp = "";
        str_searchxh = "";
        metBrand.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(buichangectrl)return;
                String currstr = metBrand.getText().toString().trim();
                if(currstr.length()>2) {
                    if(currstr==str_searchpp)return;
                    getSysBrandData(currstr);
                    str_searchpp = currstr;
                }else{
                    selpp = "";
                    str_searchpp = "";
                }
                metModel.setText("");
                selidx = -1;
            }
        });
        metModel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(buichangectrl)return;
                String currstr = metModel.getText().toString().trim();
                if(currstr.length()>2) {
                    if(currstr==str_searchxh)return;
                    getSyModelData(currstr);
                    str_searchxh = currstr;
                }else{
                    selidx = -1;
                    str_searchxh = "";
                }
            }
        });
        mbtndo = findViewById(R.id.btnlogin);
        mbtndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(xhlist==null)return;
                if (selidx<0) return;;
                getIrData();
            }
        });
    }

    private void showListPopulWindow(final boolean showpp) {
        final ListViewAdapter adapter;
        listPopupWindow = new ListPopupWindow(this);
        if(showpp) {
            listPopupWindow.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pplist));//用android内置布局，或设计自己的样式
            listPopupWindow.setAnchorView(metBrand);//以哪个控件为基准，在该处以mEditText为基准
        }else {
            adapter = new ListViewAdapter();
            listPopupWindow.setAdapter(adapter);
            listPopupWindow.setAnchorView(metModel);//以哪个控件为基准，在该处以mEditText为基准
        }
        listPopupWindow.setModal(true);

        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {//设置项点击监听
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(showpp) {
                    buichangectrl = true;
                    selpp = pplist[i];
                    metBrand.setText(selpp);
                    buichangectrl = false;
                }else {
                    buichangectrl = true;
                    selidx = i;
                    metModel.setText(xhlist[i]);
                    buichangectrl = false;
                }
                listPopupWindow.dismiss();//如果已经选择了，隐藏起来
            }
        });
        listPopupWindow.show();//把ListPopWindow展示出来
    }

    private void getSysBrandData(String schstr){
        String param = "search_brands2?data="+CfgData.dataidx+"&pp=";
        try{
            param += URLEncoder.encode(schstr,"UTF-8");
        }catch (UnsupportedEncodingException e){
            param += schstr;
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
                if(pplist==null)return;
                if(pplist.length==0)return;
                showListPopulWindow(true);
            }
            @Override
            public boolean onDodata(String res) {
                if(ParseSysBrand(res))
                    return true;
                else{
                    errstr = getString(R.string.str_try);
                    return false;
                }
            }
        });
    }

    private void getSyModelData(String schstr){
        String param = "search_models2?data="+CfgData.dataidx+"&xh=";
        try{
            param += URLEncoder.encode(schstr,"UTF-8");
        }catch (UnsupportedEncodingException e){
            param += schstr;
        }
        param += "&dev=";
        try{
            param += URLEncoder.encode(seldev,"UTF-8");
        }catch (UnsupportedEncodingException e){
            param += seldev;
        }
        if(QMUILangHelper.isNullOrEmpty(str_searchpp)==false){
            param += "&pp=";
            try{
                param += URLEncoder.encode(str_searchpp,"UTF-8");
            }catch (UnsupportedEncodingException e){
                param += str_searchpp;
            }
        }
        BackgroundRest(param,null,"GET", new OnActivityEventer() {
            @Override
            public void onSuc() {
                if(idlist==null)return;
                if(xhlist.length==0)return;
                showListPopulWindow(false);
            }
            @Override
            public boolean onDodata(String res) {
                if(ParseSysModel(res))
                    return true;
                else{
                    errstr = getString(R.string.str_try);
                    return false;
                }
            }
        });
    }

    private boolean ParseSysBrand(String src){
        pplist = null;
        try {
            Object json = new JSONTokener(src).nextValue();
            if(json instanceof JSONArray) {
                JSONArray jsonAll = (JSONArray) json;
                pplist = new String[jsonAll.length()];
                for (int i = 0; i < jsonAll.length(); i++) {
                    JSONObject jsonsingle = (JSONObject) jsonAll.get(i);
                    pplist[i] = jsonsingle.getString("pp").toUpperCase();
                }
            }
            return true;
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean ParseSysModel(String res){
        idlist = null;
        xhlist = null;
        try {
            Object json = new JSONTokener(res).nextValue();
            if(json instanceof JSONArray) {
                JSONArray jsonAll = (JSONArray) json;
                xhlist = new String[jsonAll.length()];
                idlist = new int[jsonAll.length()];
                devlist = new  String[jsonAll.length()];
                ty = new int[jsonAll.length()];
                isAc = new int[jsonAll.length()];
                if(QMUILangHelper.isNullOrEmpty(selpp)){
                    pplist = new String[jsonAll.length()];
                    for (int i = 0; i < jsonAll.length(); i++) {
                        JSONObject jsonsingle = (JSONObject) jsonAll.get(i);
                        xhlist[i] = jsonsingle.getString("xh").toUpperCase();
                        idlist[i] = jsonsingle.getInt("ID");
                        pplist[i] = jsonsingle.getString("pp").toUpperCase();
                        devlist[i] = jsonsingle.getString("dev").toUpperCase();
                        ty[i] = jsonsingle.getInt("ty");
                        isAc[i] = jsonsingle.getInt("isAc");
                    }
                }else{
                    for (int i = 0; i < jsonAll.length(); i++) {
                        JSONObject jsonsingle = (JSONObject) jsonAll.get(i);
                        xhlist[i] = jsonsingle.getString("xh").toUpperCase();
                        idlist[i] = jsonsingle.getInt("ID");
                        devlist[i] = jsonsingle.getString("dev").toUpperCase();
                        ty[i] = jsonsingle.getInt("ty");
                        isAc[i] = jsonsingle.getInt("isAc");
                    }
                }
            }
            return true;
        }catch (JSONException e) {
            e.printStackTrace();
            errstr = e.getMessage();
        }
        return false;
    }

    private class ListViewAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public ListViewAdapter() {
            mInflater = LayoutInflater.from(instance);
        }

        @Override
        public int getCount() {
            if(xhlist==null)
                return 0;
            else
                return xhlist.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.listview_modellist2, parent, false);//  android.R.layout.simple_list_item_1
            } else {
                view = convertView;
            }
            ((TextView) view.findViewById(R.id.tvdev)).setText(devlist[position]);
            ((TextView) view.findViewById(R.id.tvxh)).setText(xhlist[position]);
            TextView pp = ((TextView) view.findViewById(R.id.tvpp));
            if(QMUILangHelper.isNullOrEmpty(selpp)) {
                pp.setText(pplist[position]);
                pp.setVisibility(View.VISIBLE);
            }else{
                pp.setVisibility(View.GONE);
            }
            return view;
        }
    }

    private void getIrData(){
        if (selidx >= xhlist.length) return;

        String param = CfgData.getRmtUrl(ty[selidx],idlist[selidx]);
        if(param==null){
            ShowDialog("error data type!");
            return;
        }
        RemoteInfo aremote = new RemoteInfo();
        aremote.pp = pplist[selidx];
        aremote.xh = xhlist[selidx];
        aremote.dev = devlist[selidx];
        aremote.isAc = isAc[selidx];
        webHttpClientCom.getInstance(instance).Rest_DownAndSaveMys(aremote,param,new webHttpClientCom.RestOnAppEvent() {
            @Override
            public void onSuc() {
                setResult(Activity.RESULT_OK, new Intent());
                instance.finish();
            }
        });
    }
}
