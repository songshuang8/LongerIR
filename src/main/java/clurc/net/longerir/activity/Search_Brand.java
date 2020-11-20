package clurc.net.longerir.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import clurc.net.longerir.BaseApplication;
import clurc.net.longerir.R;
import clurc.net.longerir.adapt.SingleTextAdapt;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.RemoteInfo;
import clurc.net.longerir.data.webHttpClientCom;
import clurc.net.longerir.manager.CharacterParser;
import clurc.net.longerir.view.SegmentedRadioGroup;
import clurc.net.longerir.view.SideBar;

public class Search_Brand extends BaseActivity {
    private SingleTextAdapt adapter;
    private EditText medit;
    private SideBar mbar;
    private ListView mlist;
    private SegmentedRadioGroup agroup;

    private List<String> brandModeVec=new ArrayList<String>();

    private String strFilterBrand;
    private String mselectedbrand;
    private int setidx = 0;
    private int selvip;
    private boolean uictrl = false;

    private List<String> sysremote_brands;
    private List<String> shrremote_brands;
    private List<String> ownerremote_brands;
    private Point[] ownerinfo;
    @Override
    public void getViewId(){
        layid = R.layout.activity_search_brand;
        title = instance.getString(R.string.str_selectbrand);
    }

    @Override
    public void DoInit(){
        sysremote_brands = BaseApplication.getMyApplication().getSysremote_brands();
        shrremote_brands = BaseApplication.getMyApplication().getShrremote_brands();
        ownerremote_brands = new ArrayList<String>();
        mlist = (ListView)findViewById(R.id.listview);
        adapter = new SingleTextAdapt(instance,R.layout.listview_singletext,R.id.textView,brandModeVec);
        mlist.setAdapter(adapter);
        mlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (brandModeVec.size() < 1) return;
                if (position >= brandModeVec.size()) return;
                mselectedbrand = brandModeVec.get(position);
                if(QMUILangHelper.isNullOrEmpty(mselectedbrand)){
                    ShowDialog(getString(R.string.str_errorunknown));
                    return;
                }

                Intent intent=new Intent(instance,Search_model.class);
                intent.putExtra("pp", mselectedbrand);
                intent.putExtra("kinds", setidx);
                intent.putExtra("selvip", selvip);
                startActivityForResult(intent, 1);
            }
        });
        // 根据输入框输入值的改变来过滤搜索
        medit = (EditText) findViewById(R.id.edt);
        medit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(uictrl)return;
                showUIdata();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        medit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(medit.getText().toString()==null || medit.getText().toString().length()==0)
                    startActivityForResult(new Intent(instance, SearchHis.class),101);
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
        agroup = findViewById(R.id.segment_text);
        agroup.setOnItemChgeListener(new SegmentedRadioGroup.OnItemChangedListener() {
            @Override
            public void onItemChanged(int position) {
                setidx = position;
                uictrl = true;
                medit.getText().clear();
                uictrl = false;
                showUIdata();
            }
        });
        showUIdata();
    }

    private void showUIdata(){
        switch(setidx){
            case 0:
                if(sysremote_brands.size()==0)
                    getSysBrandData();
                else
                    showListView(sysremote_brands);
                break;
            case 1:
                if(ownerinfo==null || ownerinfo.length==0){
                    getOwnerInfo();
                }else{
                    showSelectGuest();
                }
                break;
            case 2:
                if(shrremote_brands.size()==0)
                    getShrBrandData();
                else
                    showListView(shrremote_brands);
                break;
        }
    }

    private void getSysBrandData(){
        webHttpClientCom.getInstance(instance).RestkHttpCall("search_brands?data="+CfgData.dataidx+"&GUESTID=0",null,"GET", new webHttpClientCom.WevEvent_NoErrString() {
            @Override
            public void onSuc() {
                showListView(sysremote_brands);
            }
            @Override
            public boolean onDodata(String res) {
                if(CfgData.ParseBrandArr(sysremote_brands,res))
                    return true;
                else{
                    return false;
                }
            }
        });
    }

    private void getShrBrandData(){
        webHttpClientCom.getInstance(instance).RestkHttpCall("search_brands?data=2&GUESTID=0",null,"GET", new webHttpClientCom.WevEvent_NoErrString() {
            @Override
            public void onSuc() {
                showListView(shrremote_brands);
            }
            @Override
            public boolean onDodata(String res) {
                if(CfgData.ParseBrandArr(shrremote_brands,res))
                    return true;
                else{
                    return false;
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            if(requestCode==101){
                String searchstr = data.getStringExtra("searchstr");
                if (QMUILangHelper.isNullOrEmpty(searchstr))return;
                medit.setText(searchstr);
            }else {
                setResult(Activity.RESULT_OK, new Intent());
                finish();
            }
        }
    }

    private void getOwnerInfo(){
        webHttpClientCom.getInstance(instance).RestkHttpCall("vipGuestInfo",null,"GET", new webHttpClientCom.WevEvent_NoErrString() {
            @Override
            public void onSuc() {
                showSelectGuest();
            }

            @Override
            public boolean onDodata(String res) {
                try {
                    JSONArray jsonAll = new JSONArray(res); //{"guestid":3,"Author":5,"ct":98}
                    ownerinfo = new Point[jsonAll.length()];
                    for(int i=0;i<jsonAll.length();i++){
                        JSONObject jsonsingle = (JSONObject)jsonAll.get(i);
                        ownerinfo[i] = new Point();
                        ownerinfo[i].x = jsonsingle.getInt("guestid");
                        ownerinfo[i].y = jsonsingle.getInt("Author");
                    }
                    return true;
                }catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        });
    }

    private void showSelectGuest(){
        String[] items = new String[ownerinfo.length+1];
        for (int i = 0; i < ownerinfo.length; i++) {
            items[i] = "Guest " + ownerinfo[i].x;
        }
        items[ownerinfo.length] = getString(R.string.str_all);
        new QMUIDialog.CheckableDialogBuilder(instance)
                .addItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if(which>=ownerinfo.length){
                            selvip = -1;
                        }else
                            selvip = which;
                        getVipBrandData(selvip);
                    }
                })
                .setTitle(getString(R.string.str_vip_select))
                .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
    }

    private void getVipBrandData(int authorid){
        String url;
        if(authorid>=0)
            url = "upinfo?select=pp&where=author="+authorid+" group by pp";
        else
            url = "upinfo?select=pp&where=group by pp";
        webHttpClientCom.getInstance(instance).RestkHttpCall(url,null,"GET", new webHttpClientCom.WevEvent_NoErrString() {
            @Override
            public void onSuc() {
                showListView(ownerremote_brands);
            }
            @Override
            public boolean onDodata(String res) {
                if(CfgData.ParseBrandArr(ownerremote_brands,res))
                    return true;
                else{
                    return false;
                }
            }
        });
    }

    private void showListView(List<String> astrlist){
        strFilterBrand = medit.getText().toString().toUpperCase();
        brandModeVec.clear();

        for (int i = 0; i < astrlist.size(); i++) {
            if(!QMUILangHelper.isNullOrEmpty(strFilterBrand)) {
                if (!astrlist.get(i).contains(strFilterBrand))
                    continue;
            }
            brandModeVec.add(astrlist.get(i));
        }
        CharacterParser.sortListIgnoreCase(brandModeVec);
        adapter.notifyDataSetChanged();
    }
}
