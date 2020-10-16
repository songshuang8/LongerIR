package clurc.net.longerir.activity;

import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.alpha.QMUIAlphaImageButton;
import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import clurc.net.longerir.R;
import clurc.net.longerir.Utils.DialogRemoteInfo;
import clurc.net.longerir.Utils.DialogTxtEditor;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.BM_ModelData;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.RemoteInfo;
import clurc.net.longerir.data.webHttpClientCom;

import static clurc.net.longerir.manager.UiUtils.getString;

public class MyShareList extends BaseActivity {
    private ShareItemAdapter deviceAdapter;
    private EditText filter;
    private LinearLayout pnlsearch;

    private QMUIAlphaImageButton searchbtn;
    private boolean uictrl=false;
    private int currindex;

    private List<BM_ModelData> mysharelist; //所有
    private  String mtxtstr;
    @Override
    public void getViewId() {
        layid = R.layout.my_shared_list;
        title = getString(R.string.str_myshared);
        mysharelist=new ArrayList<BM_ModelData>();
    }

    @Override
    public void DoInit() {
        searchbtn = mTopBar.addRightImageButton(android.R.drawable.ic_menu_search,R.id.topbar_searchbtn);
        searchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pnlsearch.setVisibility(View.VISIBLE);
            }
        });
        pnlsearch = findViewById(R.id.pnl_search);
        filter = findViewById(R.id.myremotefilter);
        ImageButton delsear = findViewById(R.id.imageButton2);
        delsear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pnlsearch.setVisibility(View.GONE);
                filter.setText("");
                deviceAdapter.setFilterstr("");
                deviceAdapter.notifyDataSetChanged();
            }
        });
        filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(uictrl)return;
                deviceAdapter.setFilterstr(filter.getText().toString().toUpperCase());
                deviceAdapter.notifyDataSetChanged();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        pnlsearch.setVisibility(View.GONE);
        //-------------
        ListView mlist = findViewById(R.id.listview);
        deviceAdapter = new ShareItemAdapter();
        mlist.setAdapter(deviceAdapter);
        //ListView长按监听
        mlist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, final long id) {
                currindex = deviceAdapter.getIdx(position);
                QMUIBottomSheet.BottomListSheetBuilder bd = new QMUIBottomSheet.BottomListSheetBuilder(instance);
                bd.addItem(getString(R.string.str_delete));
                bd.addItem(getString(R.string.str_rename));
                bd.addItem(getString(R.string.str_addremote));
                //加入的遥控器列表点击
                bd.setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, final int pos, String tag) {
                        dialog.dismiss();
                        switch (pos) {
                            case 0: // delete
                                new QMUIDialog.MessageDialogBuilder(instance)
                                        .setTitle(getString(R.string.str_info))
                                        .setMessage(getString(R.string.str_suredelete))
                                        .addAction(getString(R.string.str_cancel), new QMUIDialogAction.ActionListener() {
                                            @Override
                                            public void onClick(QMUIDialog dialog, int index) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .addAction(0, getString(R.string.str_Ok), QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener() {
                                            @Override
                                            public void onClick(QMUIDialog dialog, int index) {
                                                dialog.dismiss();
                                                String url;
                                                if(CfgData.usertype==2)
                                                    url = "rawinfo/";
                                                else
                                                    url = "upinfo/";
                                                webHttpClientCom.getInstance(instance).RestkHttpCall(url+mysharelist.get(currindex).getId(),null,"DELETE", new webHttpClientCom.WevEvent_SucString() {
                                                    @Override
                                                    public void onSuc(String res) {
                                                        mysharelist.remove(currindex);
                                                        deviceAdapter.notifyDataSetChanged();
                                                    }
                                                });
                                            }
                                        })
                                        .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
                                break;
                            case 1: // rename 修改 遥控器的名称 不包括按键
                                final RemoteInfo aremote = new RemoteInfo();
                                aremote.pp = mysharelist.get(currindex).getPp();
                                aremote.xh = mysharelist.get(currindex).getXh();
                                aremote.dev = mysharelist.get(currindex).getDev();
                                aremote.isAc =  mysharelist.get(currindex).getIsAc();
                                DialogRemoteInfo aputdig = new DialogRemoteInfo(instance,aremote);
                                aputdig.setTitle(getString(R.string.str_info));
                                aputdig.CustomShow(new DialogRemoteInfo.OnRemoteInfoSuc() {
                                    @Override
                                    public void onSuc() {
                                        String vip = "0";
                                        if(CfgData.usertype!=2)vip="1";
                                        String body = CfgData.getRemoteTxtFile(aremote,null);
                                        webHttpClientCom.getInstance(instance).RestkHttpCall("ClientEditUpload?RMTID="+mysharelist.get(currindex).getId()+"&vip="+vip,body,"POST", new webHttpClientCom.WevEvent_SucData() {
                                            @Override
                                            public void onSuc(byte[] dta) {
                                                mysharelist.get(currindex).setPp(aremote.pp);
                                                mysharelist.get(currindex).setXh(aremote.xh);
                                                mysharelist.get(currindex).setDev(aremote.dev);
                                                deviceAdapter.notifyDataSetChanged();
                                                Toast.makeText(instance,"Save ok", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                                break;
                            case 2:
                                String url;
                                if(CfgData.usertype==2)
                                    url = "rawcodelist?id="+ mysharelist.get(currindex).getId() + "&NOJSON=1";
                                else
                                    url = "ulcodelist?id="+ mysharelist.get(currindex).getId() + "&NOJSON=1";
                                RemoteInfo rmt = new RemoteInfo();
                                rmt.dev = mysharelist.get(currindex).getDev();
                                rmt.pp = mysharelist.get(currindex).getPp();
                                rmt.xh = mysharelist.get(currindex).getXh();
                                rmt.isAc = mysharelist.get(currindex).getIsAc();
                                webHttpClientCom.getInstance(instance).Rest_DownAndSaveMys(rmt,url, new webHttpClientCom.RestOnAppEvent() {
                                    @Override
                                    public void onSuc() {
                                        Toast.makeText(instance, getString(R.string.str_add_ok), Toast.LENGTH_SHORT).show();
                                        deviceAdapter.notifyDataSetChanged();
                                    }
                                });
                                break;
                        }
                    }
                }).build().show();
                return true;
            }
        });
        //-------点击
        mlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,final int position, long id) {
                currindex = deviceAdapter.getIdx(position);
                final int rid = mysharelist.get(currindex).getId();
                String url;
                if(CfgData.usertype==2)
                    url = "rawinfo/data/"+ rid;
                else
                    url = "upinfo/data/"+ rid;
                webHttpClientCom.getInstance(instance).RestkHttpCall(url,null,"GET", new webHttpClientCom.WevEvent_SucData() {
                    @Override
                    public void onSuc(byte[] data) {
                        final DialogTxtEditor adialog = new DialogTxtEditor(instance);
                        adialog.addAction(getString(R.string.str_Ok), new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(final QMUIDialog dialog, int index) {
                                String url;
                                if(CfgData.usertype==2)
                                    url = "0";
                                else
                                    url = "1";
                                webHttpClientCom.getInstance(instance).RestkHttpCall("ClientEditUpload?rmtid="+rid+"&vip="+url,adialog.getEditText().getText().toString(),"PUT", new webHttpClientCom.WevEvent_SucString() {
                                    @Override
                                    public void onSuc(String res) {
                                        dialog.dismiss();
                                        RemoteInfo armt = new RemoteInfo();
                                        CfgData.GetRemoteFromText(armt,adialog.getEditText().getText().toString());
                                        if(armt.pp!=null)
                                            mysharelist.get(currindex).setPp(armt.pp);
                                        if(armt.xh!=null)
                                            mysharelist.get(currindex).setXh(armt.xh);
                                        if(armt.dev!=null)
                                            mysharelist.get(currindex).setDev(armt.dev);
                                        deviceAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        });
                        adialog.showEditor(mtxtstr);
                    }
                });
            }
        });
        getModelData(); //网络加载
    }

    private void getModelData(){
        webHttpClientCom.getInstance(instance).RestkHttpCall("clientGetList?userid="+String.valueOf(CfgData.userid),null,"GET", new webHttpClientCom.WevEvent_NoErr() {
            @Override
            public void onSuc(byte[] out) {
                deviceAdapter.notifyDataSetChanged();
            }

            @Override
            public boolean onDoData(byte[] res) {
                mysharelist.clear();
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
                            mysharelist.add(axh);
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


    //---------------------------------------------------------------------------------------------------------
    public class ShareItemAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<Integer> myidx;
        private String filterstr;

        public ShareItemAdapter() {
            mInflater = LayoutInflater.from(instance);
            myidx = new ArrayList<Integer>();
            setFilterstr("");
        }

        @Override
        public int getCount() {
            String s = filterstr;
            setFilterstr(s);
            return myidx.size();
        }

        @Override
        public Object getItem(int position) {
            Object abj = null;
            if (position < mysharelist.size())
                abj = mysharelist.get(position);
            return abj;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.listview_myremote, parent, false);
            int pos = myidx.get(position);
            BM_ModelData adev = mysharelist.get(pos);
            ((TextView) convertView.findViewById(R.id.tvdev)).setText(adev.getDev());
            ((TextView) convertView.findViewById(R.id.tvbrand)).setText(adev.getPp());
            ((TextView) convertView.findViewById(R.id.tvxh)).setText(adev.getXh());

            return convertView;
        }

        private boolean ManzuFilter(String f, BM_ModelData adev) {
            if (f == null) return true;
            if (f.length() == 0) return true;
            if (adev.getDev() != null)
                if (adev.getDev().contains(f)) {
                    return true;
                }
            if (adev.getPp() != null)
                if (adev.getPp().contains(f)) {
                    return true;
                }
            if (adev.getXh() != null)
                if (adev.getXh().contains(f)) {
                    return true;
                }
            return false;
        }

        public void setFilterstr(String filterstr) {
            this.filterstr = filterstr;
            myidx.clear();
            int len = mysharelist.size();
            for (int i = 0; i < len; i++) {
                BM_ModelData adev = mysharelist.get(i);
                if (ManzuFilter(filterstr, adev)) {
                    myidx.add(i);
                }
            }
        }

        public int getIdx(int pos) {
            return myidx.get(pos);
        }
    }
}
