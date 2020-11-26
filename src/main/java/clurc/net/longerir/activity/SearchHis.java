package clurc.net.longerir.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

import clurc.net.longerir.R;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.BM_ModelData;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.RemoteInfo;
import clurc.net.longerir.data.webHttpClientCom;
import clurc.net.longerir.manager.QDPreferenceManager;


public class SearchHis  extends BaseActivity {
    private RecyclerView hisview;
    private HisSearchAdapt hisadatpe;
    private List<String> hisarr;

    private List<BM_ModelData> topdata=new ArrayList<BM_ModelData>();
    private ListViewAdapter adapter;

    private EditText edtinput;
    private QMUIRoundButton btns;
    @Override
    public void getViewId() {
        layid = R.layout.activity_bar_search;
        title = getString(R.string.str_search);
    }
    @Override
    public void DoInit() {
        //View view = LayoutInflater.from(instance).inflate(R.layout.bar_input_search,null, false);
        //RelativeLayout.LayoutParams editTextLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //editTextLP.addRule(RelativeLayout.CENTER_VERTICAL);
        //mTopBar.addRightView(view,R.id.main_bar_seach,editTextLP);
        edtinput = findViewById(R.id.editsearch);
        btns =findViewById(R.id.btnseach);
        btns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = edtinput.getText().toString();
                if (QMUILangHelper.isNullOrEmpty(s)) {
                    return;
                }
                DoBtnClick(s);
            }
        });
        ((ImageButton)findViewById(R.id.btn_his_delete)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QDPreferenceManager.getInstance(instance).ClearSearchHis();
                hisarr.clear();
                hisadatpe.notifyDataSetChanged();
            }
        });

        hisview = findViewById(R.id.recy_seachlist);

        ListView mlist = (ListView)findViewById(R.id.listview);
        adapter = new ListViewAdapter(instance);
        mlist.setAdapter(adapter);
        mlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (topdata.size() < 1) return;
                if (position >= topdata.size()) return;
                final BM_ModelData axh = topdata.get(position);
                //--------------------------------------------------------
                String param = CfgData.getRmtUrl(axh.getType(),axh.getId());
                if(param==null){
                    ShowDialog("error data type!");
                    return;
                }
                RemoteInfo aremote = new RemoteInfo();
                aremote.pp = axh.getPp();
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
    }

    @Override
    public void DoShowing(){
        updateData();
        getSvrTopData();
    }


    private void updateData(){
        hisarr = QDPreferenceManager.getInstance(instance).getSearHis();
        hisview.setLayoutManager(new GridLayoutManager(instance,3));
        hisadatpe = new HisSearchAdapt();
        hisview.setAdapter(hisadatpe);
    }

    private void DoBtnClick(String s){
        QDPreferenceManager.getInstance(instance).AppendSearchHis(s);

        Intent intent=new Intent();
        intent.putExtra("searchstr",s);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void getSvrTopData(){
        String param = "topremote?top=30";
        webHttpClientCom.getInstance(instance).RestkHttpCall(param,null,"GET", new webHttpClientCom.WevEvent_NoErr() {
            @Override
            public void onSuc(byte[] data) {
                adapter.notifyDataSetChanged();

            }
            @Override
            public boolean onDoData(byte[] res) {
                topdata.clear();
                try {
                    Object json = new JSONTokener(new String(res)).nextValue();
                    if(json instanceof JSONArray) {
                        JSONArray jsonAll = (JSONArray) json;
                        for (int i = 0; i < jsonAll.length(); i++) {
                            JSONObject jsonsingle = (JSONObject) jsonAll.get(i);
                            BM_ModelData axh = new BM_ModelData(
                                    jsonsingle.getInt("ID"),
                                    jsonsingle.getString("pp").toUpperCase(),
                                    jsonsingle.getString("xh").toUpperCase(),
                                    jsonsingle.getString("dev"),
                                    jsonsingle.getInt("ct"),
                                    jsonsingle.getInt("gt"),
                                    jsonsingle.getInt("bt"),
                                    jsonsingle.getInt("ty"),
                                    jsonsingle.getInt("isAc")
                            );
                            topdata.add(axh);
                        }
                    }
                    return true;
                }catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }

    class HisSearchAdapt extends RecyclerView.Adapter<HisSearchAdapt.VH> {
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            //LayoutInflater.from指定写法
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_singletxt_2, parent, false);
            return new VH(v);
        }
        // 创建ViewHolder
        class VH extends RecyclerView.ViewHolder{
            public TextView mtitle;
            public VH(View v) {
                super(v);
                mtitle = (TextView)v.findViewById(R.id.singleid);
            }
        }

        @Override
        public int getItemCount() {
            return hisarr.size();
        }

        public HisSearchAdapt() {

        }

        @Override
        public void onBindViewHolder(final VH holder, final int position) {
            holder.mtitle.setText(hisarr.get(position));
            holder.mtitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DoBtnClick(((TextView)v).getText().toString());
                }
            });
        }
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
            return topdata.size();
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
                view = mInflater.inflate(R.layout.listview_top_search, parent, false);//  android.R.layout.simple_list_item_1
            } else {
                view = convertView;
            }
            BM_ModelData amodel = topdata.get(position);
            ((TextView) view.findViewById(R.id.tvdev)).setText(amodel.getDev());
            ((TextView) view.findViewById(R.id.tvxh)).setText(amodel.getXh());
            ((TextView) view.findViewById(R.id.tvpp)).setText(amodel.getPp());
            ((TextView) view.findViewById(R.id.tvdct)).setText(String.valueOf(amodel.getDowncnt()));

            return view;
        }
    }
}
