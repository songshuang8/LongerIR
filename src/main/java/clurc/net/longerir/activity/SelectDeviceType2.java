package clurc.net.longerir.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.qmuiteam.qmui.util.QMUILangHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import clurc.net.longerir.R;
import clurc.net.longerir.adapt.SingleTextAdapt;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.webHttpClientCom;

public class SelectDeviceType2 extends BaseActivity {
    private ListView devlist;
    private int looktype;
    private List<String> arr;
    private String sel;

    @Override
    public void getViewId() {
        layid = R.layout.sel_device2;
        title = getString(R.string.str_sel_device);
        looktype = getIntent().getExtras().getInt("look",0);
    }

    @Override
    public void DoInit() {
        devlist = findViewById(R.id.listview);
        arr = new ArrayList<String>();
        devlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                if(i==0){
                    sel = "ALL";
                }else {
                    sel = arr.get(i);
                }
                if(looktype==0) {
                    intent.putExtra("devname", sel);
                    intent.setClass(instance, SelectARemote.class);
                    startActivityForResult(intent, 1);
                }else {
                    intent.setClass(instance, Search_Brand2.class);
                    intent.putExtra("devname",sel);
                    startActivityForResult(intent, 1);
                }
            }
        });
        if(CfgData.devitems.size()==0) {
            webHttpClientCom.getInstance(instance).RestkHttpCall("search_devs?data=" + CfgData.dataidx,null,"GET", new webHttpClientCom.WevEvent_NoErrString() {
                @Override
                public void onSuc() {
                    CopyThereItems();
                }

                @Override
                public boolean onDodata(String res) {
                    if (CfgData.DoChkdevs(res)) {
                        return true;
                    }else {
                        return false;
                    }
                }
            });
        }else{
            CopyThereItems();
        }
    }

    private void CopyThereItems(){
        if(CfgData.devitems==null)return;
        arr.clear();
        arr.add(getString(R.string.str_all));
        for (int i = 0; i < CfgData.devitems.size(); i++) {
            arr.add(CfgData.devitems.get(i));
        }
        SingleTextAdapt adpt = new SingleTextAdapt(instance,R.layout.listview_singletext,R.id.textView,arr);
        devlist.setAdapter(adpt);//用android内置布局，或设计自己的样式
        adpt.notifyDataSetChanged();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            setResult(Activity.RESULT_OK, new Intent());
            finish();
        }
    }
}
