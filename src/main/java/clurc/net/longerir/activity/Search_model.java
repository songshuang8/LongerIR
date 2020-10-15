package clurc.net.longerir.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.util.QMUILangHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import clurc.net.longerir.R;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.BM_ModelData;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.RemoteInfo;
import clurc.net.longerir.data.webHttpClientCom;
import clurc.net.longerir.manager.CharacterParser;
import clurc.net.longerir.view.SideBar;

import static clurc.net.longerir.manager.UiUtils.getString;

public class Search_model extends BaseActivity {
    private ListViewAdapter adapter;
    private EditText medit;
    private SideBar mbar;
    private ListView mlist;

    private List<BM_ModelData> dataall=new ArrayList<BM_ModelData>(); //某品牌下的所有
    private List<BM_ModelData> datafilter=new ArrayList<BM_ModelData>();//当前过滤的

    private String strselectedpp;
    private String strFilterXh;
    private int selidx,dataid;
    private int selvip;
    @Override
    public void getViewId(){
        layid = R.layout.activity_search_model;
        strselectedpp = getIntent().getExtras().getString("pp");
        title = strselectedpp+"/"+instance.getString(R.string.str_selectmodel);
    }

    @Override
    public void DoInit() {
        selidx = getIntent().getExtras().getInt("kinds");
        selvip = getIntent().getExtras().getInt("selvip");
        if(selidx==0){
            dataid = CfgData.dataidx;
        }else if(selidx==2){
            dataid = 2;
            ((TextView)findViewById(R.id.tvidno)).setText(R.string.str_shared);
        }
        mlist = (ListView)findViewById(R.id.listview);
        adapter = new ListViewAdapter(instance);
        mlist.setAdapter(adapter);
        mlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (datafilter.size() < 1) return;
                if (position >= datafilter.size()) return;
                final BM_ModelData axh = datafilter.get(position);
                //--------------------------------------------------------
                String param = CfgData.getRmtUrl(axh.getType(),axh.getId());
                if(param==null){
                    ShowDialog("error data type!");
                    return;
                }
                RemoteInfo aremote = new RemoteInfo();
                if(axh.getType()==1){
                    aremote.codecannot = true;
                }
                aremote.pp = strselectedpp;
                aremote.xh = axh.getXh();
                aremote.dev = axh.getDev();
                aremote.isAc = axh.getIsAc();
                webHttpClientCom.getInstance(instance).Rest_DownAndSaveMys(aremote,param,new webHttpClientCom.RestOnAppEvent() {
                    @Override
                    public void onSuc() {
                        setResult(Activity.RESULT_OK, new Intent());
                        instance.finish();
                    }
                });
            }
        });
        // 根据输入框输入值的改变来过滤搜索
        medit = (EditText) findViewById(R.id.edt);
        medit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showListView();
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
                for (int i = 0; i < datafilter.size(); i++) {
                    String sortStr = CharacterParser.getInstance().getFirstLetter(datafilter.get(i).getXh());
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
        getModelData();
    }

    private void getModelData(){
        if(QMUILangHelper.isNullOrEmpty(strselectedpp)){
            ShowDialog("the string error!");
            return;
        }
        String param=null;
        if(selidx==1){
            if(selvip>=0){
                param = "upinfo?select=id,xh,dev,downloads as ct,GoodCount as gt,BadCount as bt,'' as Au,isAc,1 as ty&where=author="+selvip+" and pp='";
            }else {
                param = "upinfo?select=id,xh,dev,downloads as ct,GoodCount as gt,BadCount as bt,'' as Au,isAc,1 as ty&where=pp='";
            }
            try {
                param += URLEncoder.encode(strselectedpp, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                param += strselectedpp;
            }
            param +="' order by dev,xh";
        }else {
            param = "search_models?data=" + dataid + "&pp=";
            try {
                param += URLEncoder.encode(strselectedpp, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                param += strselectedpp;
            }
        }
        BackgroundRest(param,null,"GET", new OnActivityEventer() {
            @Override
            public void onSuc() {
                showListView();
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
                                    jsonsingle.getInt("ct"),
                                    jsonsingle.getInt("gt"),
                                    jsonsingle.getInt("bt"),
                                    jsonsingle.getString("Au"),
                                    jsonsingle.getInt("ty"),
                                    jsonsingle.getInt("isAc")
                            );
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

    private void showListView(){
        strFilterXh = medit.getText().toString().toUpperCase();
        datafilter.clear();
        for (int i = 0; i < dataall.size(); i++) {
            if(!QMUILangHelper.isNullOrEmpty(strFilterXh)) {
                if (!dataall.get(i).getXh().contains(strFilterXh))
                    continue;
            }
            datafilter.add(dataall.get(i));
        }
        //sortListIgnoreCase(datafilter);
        adapter.notifyDataSetChanged();
    }

    private class ListViewAdapter extends BaseAdapter {
        private Activity context;
        private LayoutInflater mInflater;

        public ListViewAdapter(Activity context) {
            this.context = context;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return datafilter.size();
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
                view = mInflater.inflate(R.layout.listview_modellist, parent, false);//  android.R.layout.simple_list_item_1
            } else {
                view = convertView;
            }
            BM_ModelData amodel = datafilter.get(position);
            ((TextView) view.findViewById(R.id.tvdev)).setText(amodel.getDev());
            ((TextView) view.findViewById(R.id.tvxh)).setText(amodel.getXh());
            if(selidx==2){
                ((TextView) view.findViewById(R.id.tvid)).setText(String.valueOf(amodel.getProvider()));
            }else {
                ((TextView) view.findViewById(R.id.tvid)).setText(String.valueOf(amodel.getId()));
            }
            ((TextView) view.findViewById(R.id.tvdct)).setText(String.valueOf(amodel.getDowncnt()));
            ((TextView) view.findViewById(R.id.tvgct)).setText(String.valueOf(amodel.getGoodcnt()));
            ((TextView) view.findViewById(R.id.tvbct)).setText(String.valueOf(amodel.getBadcnt()));

            return view;
        }
    }

    private void sortListIgnoreCase(List<BM_ModelData> list) {
        Collections.sort(list, new Comparator<BM_ModelData>() {
            @Override
            public int compare(BM_ModelData s1, BM_ModelData s2) {
                return s1.getXh().compareToIgnoreCase(s2.getXh());
            }
        });
    }
}
