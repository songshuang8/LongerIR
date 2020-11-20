package clurc.net.longerir.activity;

import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUILangHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import clurc.net.longerir.R;
import clurc.net.longerir.adapt.SingleTextAdapt;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.webHttpClientCom;
import clurc.net.longerir.manager.CharacterParser;
import clurc.net.longerir.view.SideBar;

public class Search_Brand2 extends BaseActivity {
    private SingleTextAdapt adapter;
    private EditText medit;
    private SideBar mbar;
    private ListView mlist;
    private  String[]  pplist;
    private List<String> brandModeVec=new ArrayList<String>();

    private String strFilterBrand;
    private String selectdev;

    @Override
    public void getViewId(){
        layid = R.layout.activity_search_brand2;
        title = instance.getString(R.string.str_selectbrand);
        selectdev = getIntent().getExtras().getString("devname");
    }

    @Override
    public void DoInit(){
        mlist = (ListView)findViewById(R.id.listview);
        adapter = new SingleTextAdapt(instance,R.layout.listview_singletext,R.id.textView,brandModeVec);
        mlist.setAdapter(adapter);
        mlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (brandModeVec.size() < 1) return;
                if (position >= brandModeVec.size()) return;

                String selbrand = brandModeVec.get(position);
                if(QMUILangHelper.isNullOrEmpty(selbrand)){
                    ShowDialog(getString(R.string.str_errorunknown));
                    return;
                }

                Intent intent=new Intent();
                intent.setClass(instance, Search_model2.class);
                intent.putExtra("pp",selbrand);
                intent.putExtra("dev",selectdev);
                startActivityForResult(intent, 1);
            }
        });
        // 根据输入框输入值的改变来过滤搜索
        medit = (EditText) findViewById(R.id.edt);
        medit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showUIdata();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        // 设置右侧触摸监听
        mbar = (SideBar) findViewById(R.id.sidrbar);
        mbar.setTextView((TextView) findViewById(R.id.dialog));
        mbar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            /**
             * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
             */
            public int getPositionForSection(int section) {
                for (int i = 0; i < brandModeVec.size(); i++) {
                    String sortStr = CharacterParser.getInstance().getFirstLetter(brandModeVec.get(i));
                    char firstChar = sortStr.toUpperCase().charAt(0);
                    if (firstChar == section) {
                        return i;
                    }
                }
                return -1;
            }

            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mlist.setSelection(position);
                }
            }
        });
        showUIdata();
    }

    private void showUIdata(){
        if(pplist==null || pplist.length==0)
            getSysBrandData();
        else
            showSysListView();
    }

    private void getSysBrandData(){
        String param = "search_brands2?data="+CfgData.dataidx+"&pp=&dev=";
        try{
            param += URLEncoder.encode(selectdev,"UTF-8");
        }catch (UnsupportedEncodingException e){
            param += selectdev;
        }
        webHttpClientCom.getInstance(instance).RestkHttpCall(param,null,"GET", new webHttpClientCom.WevEvent_NoErrString() {
            @Override
            public void onSuc() {
                showSysListView();
            }
            @Override
            public boolean onDodata(String res) {
                if(ParseSysBrand(res))
                    return true;
                else{
                    return false;
                }
            }
        });
    }

    private boolean ParseSysBrand(String src){
        pplist = new String[0];
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

    private void showSysListView(){
        strFilterBrand = medit.getText().toString().toUpperCase();
        brandModeVec.clear();

        for (int i = 0; i < pplist.length; i++) {
            if(!QMUILangHelper.isNullOrEmpty(strFilterBrand)) {
                if (!pplist[i].contains(strFilterBrand))
                    continue;
            }
            brandModeVec.add(pplist[i]);
        }
        CharacterParser.sortListIgnoreCase(brandModeVec);
        adapter.notifyDataSetChanged();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            setResult(Activity.RESULT_OK, new Intent());
            finish();
        }
    }
}
