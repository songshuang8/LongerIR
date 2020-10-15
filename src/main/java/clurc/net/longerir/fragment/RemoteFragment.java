package clurc.net.longerir.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;


import com.google.zxing.client.android.MNScanManager;
import com.google.zxing.client.android.other.MNScanCallback;
import com.google.zxing.client.android.utils.ZXingUtils;
import com.qmuiteam.qmui.alpha.QMUIAlphaImageButton;
import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.widget.QMUIRadiusImageView;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import clurc.net.longerir.Utils.DialogShowImage;
import clurc.net.longerir.Utils.DialogTxtEditor;
import clurc.net.longerir.Utils.SysFun;
import clurc.net.longerir.activity.IrLearAc;
import clurc.net.longerir.activity.MyShareList;
import clurc.net.longerir.activity.PrcAcCommu;
import clurc.net.longerir.activity.RemotePlay2;
import clurc.net.longerir.activity.RemotePlayAcLearn;
import clurc.net.longerir.activity.SearchHis;
import clurc.net.longerir.data.IrButton;
import java.util.ArrayList;
import java.util.List;
import clurc.net.longerir.Utils.MoudelFile;
import clurc.net.longerir.activity.IrLearn;
import clurc.net.longerir.activity.PrcComuni;
import clurc.net.longerir.activity.RemotePlayAc;
import clurc.net.longerir.R;
import clurc.net.longerir.activity.SelectDesRemote;
import clurc.net.longerir.activity.SelectIrPort;
import clurc.net.longerir.activity.SelectSearchMode;
import clurc.net.longerir.activity.UserCenter;
import clurc.net.longerir.adapt.DevicesItemAdapter;
import clurc.net.longerir.data.BtnInfo;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.RemoteInfo;
import clurc.net.longerir.data.TxtBtnInfo;
import clurc.net.longerir.data.webHttpClientCom;
import clurc.net.longerir.ircommu.DesRemoteBtn;
import clurc.net.longerir.ircommu.PrcFunction;

import static clurc.net.longerir.manager.UiUtils.getResources;
import static clurc.net.longerir.manager.UiUtils.getString;
import static clurc.net.longerir.uicomm.SsSerivce.TAG_SS;

public class RemoteFragment extends BaseFragment {
    private static String qrcodeTitle = "longerir://add/";
    private DevicesItemAdapter deviceAdapter = null;
    private EditText filter;
    private LinearLayout pnlsearch;

    public RemoteFragment(Context context,View root){
        super(context,root);
    }
    private QMUIDialog.EditTextDialogBuilder builder;
    private int currindex = -1;
    private QMUIAlphaImageButton leftuserbtn,searchbtn;
    private boolean uictrl=false;

    @Override
    public  void OnGetView(){
        view = LayoutInflater.from(context).inflate(R.layout.home_remotelist, (ViewGroup) root, false);
        title = context.getString(R.string.str_remote);
    }

    @Override
    public  void viewInit(){
        //右侧三点菜单
        leftuserbtn = mTopBar.addRightImageButton(R.mipmap.icon_topbar_overflow,R.id.main_right_menu);
        leftuserbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final QMUIBottomSheet.BottomListSheetBuilder bd = new QMUIBottomSheet.BottomListSheetBuilder(context);
                bd.addItem(context.getString(R.string.str_user_center));
                bd.addItem(context.getString(R.string.str_irport_select));
                bd.addItem(context.getString(R.string.str_myshared));
                bd.addItem(context.getString(R.string.str_qrcode_sacan));
                //加入的遥控器列表点击
                bd.setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, final int pos, String tag) {
                        switch (pos) {
                            case 0:
                                ((Activity) context).startActivity(new Intent(context,UserCenter.class));
                                break;
                            case 1:
                                ((Activity) context).startActivity(new Intent(context,SelectIrPort.class));
                                break;
                            case 2:
                                if(CfgData.userid<0) {
                                    ShowDialog(getString(R.string.str_pls_login));
                                    return;
                                }
                                ((Activity) context).startActivity(new Intent(context, MyShareList.class));
                                break;
                            case 3:
                                MNScanManager.startScan((Activity)context, SysFun.getQrCodeOption(), new MNScanCallback() {
                                    @Override
                                    public void onActivityResult(int resultCode, Intent data) {
                                        switch (resultCode) {
                                            case MNScanManager.RESULT_SUCCESS:
                                                String resultSuccess = data.getStringExtra(MNScanManager.INTENT_KEY_RESULT_SUCCESS);
                                                Log.i(TAG_SS, "scan ok---"+resultSuccess); //longerir://add/appremote/(4)
                                                if(!resultSuccess.contains(qrcodeTitle)){
                                                    ShowDialog(getString(R.string.str_err));
                                                    break;
                                                }
                                                String s1 = resultSuccess.replace(qrcodeTitle,"").trim();
                                                Log.i(TAG_SS, "qrcode---"+s1);
                                                String[] itm = s1.split("/");
                                                if(itm.length!=2){
                                                    ShowDialog(getString(R.string.str_err));
                                                    break;
                                                }
                                                webHttpClientCom.getInstance((Activity)context).Rest_DownRemoteMulty(itm, new webHttpClientCom.RestOnAppEvent() {
                                                    @Override
                                                    public void onSuc() { //RemoteInfo aremote = new RemoteInfo();
                                                        Toast.makeText(context, getString(R.string.str_add_ok), Toast.LENGTH_SHORT).show();
                                                        deviceAdapter.notifyDataSetChanged();
                                                    }
                                                });
                                                break;
                                            case MNScanManager.RESULT_FAIL:
                                                String resultError = data.getStringExtra(MNScanManager.INTENT_KEY_RESULT_ERROR);
                                                Log.i(TAG_SS, "scan err---"+resultError);
                                                break;
                                            case MNScanManager.RESULT_CANCLE:
                                                break;
                                        }
                                    }
                                });
                                break;
                        }
                        dialog.dismiss();
                    }
                }).build().show();
            }
        });
        //搜索
        searchbtn = mTopBar.addRightImageButton(android.R.drawable.ic_menu_search,R.id.topbar_searchbtn);
        searchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity) context).startActivityForResult(new Intent(context, SearchHis.class),101);
            }
        });
        pnlsearch = view.findViewById(R.id.pnl_search);
        filter = view.findViewById(R.id.myremotefilter);
        ImageButton delsear = view.findViewById(R.id.imageButton2);
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
        //读取我的遥控器列表
        CfgData.readMyRemote(context,CfgData.myremotelist);
        ListView mlist = view.findViewById(R.id.listview);
        deviceAdapter = new DevicesItemAdapter(context);
        mlist.setAdapter(deviceAdapter);
        //ListView长按监听
        mlist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, final long id) {
                currindex = deviceAdapter.getIdx(position);
                QMUIBottomSheet.BottomListSheetBuilder bd = new QMUIBottomSheet.BottomListSheetBuilder(context);
                bd.addItem(context.getString(R.string.str_delete),"0");
                if(CfgData.myremotelist.get(currindex).codecannot==false)
                    bd.addItem(context.getString(R.string.str_edit_remote),"1");
                bd.addItem(context.getString(R.string.str_maderemote),"2");
                bd.addItem(context.getString(R.string.str_share_qrcode),"3");
                if(CfgData.myremotelist.get(currindex).islearned) {
                    bd.addItem(context.getString(R.string.str_relearn),"4");
                    bd.addItem(context.getString(R.string.str_shareto),"5");
                }
                //加入的遥控器列表点击
                bd.setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, final int pos, String tag) {
                        int funcid = Integer.parseInt(tag);
                        Intent intent = new Intent();
                        final RemoteInfo src = CfgData.myremotelist.get(currindex);
                        switch (funcid) {
                            case 0: //删除
                                new QMUIDialog.MessageDialogBuilder(context)
                                        .setTitle(context.getString(R.string.str_info))
                                        .setMessage(context.getString(R.string.str_suredelete))
                                        .addAction(context.getString(R.string.str_cancel), new QMUIDialogAction.ActionListener() {
                                            @Override
                                            public void onClick(QMUIDialog dialog, int index) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .addAction(0, context.getString(R.string.str_Ok), QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener() {
                                            @Override
                                            public void onClick(QMUIDialog dialog, int index) {
                                                dialog.dismiss();
                                                CfgData.DeleteMyRemote(context,currindex);
                                                deviceAdapter.notifyDataSetChanged();
                                                if(src.rid>0){
                                                    // syn my data
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            byte[] data = null;
                                                            String err = null;
                                                            webHttpClientCom.getInstance(null).ThreadHttpCall("appRemote/"+ src.rid,null,"DELETE",data,err);
                                                        }
                                                    }).start();
                                                }
                                            }
                                        })
                                        .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
                                break;
                            case 1: //码修改
                                if(src.isAc==CfgData.AcPro){
                                    Toast.makeText(context,getString(R.string.str_ac_cannotedit), Toast.LENGTH_SHORT).show();
                                    break;
                                }
                                String mtxtstr = CfgData.getRemoteTxtFile(src, CfgData.getBtnInfo(context,src.id));
                                final DialogTxtEditor adialog = new DialogTxtEditor(context);
                                adialog.addAction(getString(R.string.str_Ok), new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(final QMUIDialog dialog, int index) {
                                        dialog.dismiss();
                                        String newtxt=adialog.getEditText().getText().toString();
                                        RemoteInfo tempremote = new RemoteInfo();
                                        CfgData.GetRemoteFromText(tempremote, newtxt);
                                        if(tempremote.pp!=null)
                                            src.pp = tempremote.pp;
                                        if(tempremote.xh!=null)
                                            src.xh = tempremote.xh;
                                        if(tempremote.dev!=null)
                                            src.dev = tempremote.dev;

                                        List<TxtBtnInfo> txtbtns = new ArrayList<TxtBtnInfo>();
                                        CfgData.GetBtnsFromText(txtbtns, newtxt);
                                        List<BtnInfo> btns = new ArrayList<BtnInfo>();
                                        CfgData.TransTxtToBtns(txtbtns, btns, src.dev,src.isAc);
                                        if(CfgData.AppendoOrEditMyFile(context, src, btns)) {
                                            // syn my data
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    webHttpClientCom.getInstance(null).Slient_UploadRemote(src);
                                                }
                                            }).start();
                                        }else{
                                            Toast.makeText(context, "Unknown err", Toast.LENGTH_SHORT).show();
                                        }
                                        deviceAdapter.notifyDataSetChanged();
                                    }
                                });
                                adialog.showEditor(mtxtstr);
                                break;
                            case 2: // direct down
                                if(src.isAc==CfgData.AcPro){
                                    byte[] prcdata = (new PrcFunction()).getEepData(src.acdata);
                                    intent.setClass(context, PrcAcCommu.class);
                                    intent.putExtra("prcdata",CfgData.ByteArrToString(prcdata));
                                    ((Activity)context).startActivity(intent);
                                }else if(src.isAc==CfgData.AcLear){
                                    List<BtnInfo> btnlist = CfgData.getBtnInfo(context,src.id);
                                    HttpRest("getTransEepAc", CfgData.getRemoteTxtFile(src, btnlist), "POST", new OnActivityEventer() {
                                        @Override
                                        public void onSuc(byte[] out) {
                                            Intent intent = new Intent();
                                            intent.setClass(context, PrcAcCommu.class);
                                            intent.putExtra("prcdata",CfgData.ByteArrToString(out));
                                            ((Activity)context).startActivity(intent);
                                        }
                                    });
                                }else{
                                    intent = new Intent();
                                    intent.setClass(context, SelectDesRemote.class);
                                    intent.putExtra("pagesel", -1);
                                    intent.putExtra("bonly", true);
                                    ((Activity) context).startActivityForResult(intent, 21);
                                }
                                break;
                            case 3: // share with a qr code
                                if(src.rid>0){
                                    Bitmap qrImage = ZXingUtils.createQRCodeWithLogo(qrcodeTitle+"AppRemote/("+src.rid+")", BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
                                    DialogShowImage adiag = new DialogShowImage((Activity)context,qrImage);
                                    adiag.CustomShow();
                                }else {
                                    webHttpClientCom.getInstance((Activity) context).Rest_UploadRemote(src, "appUpload?userid=", new webHttpClientCom.RestOnAppEvent() {
                                        @Override
                                        public void onSuc() {
                                            if(src.rid<0)return;
                                            Bitmap qrImage = ZXingUtils.createQRCodeWithLogo(qrcodeTitle+"AppRemote/("+src.rid+")", BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
                                            DialogShowImage adiag = new DialogShowImage((Activity)context,qrImage);
                                            adiag.CustomShow();
                                        }
                                    });
                                }
                                break;
                            case 4: // relearn
                                if(src.isAc==CfgData.AcLear)
                                    intent.setClass(context, IrLearAc.class);
                                else
                                    intent.setClass(context, IrLearn.class);
                                intent.putExtra("pos", currindex);
                                ((Activity)context).startActivityForResult(intent, 4);
                                break;
                            case 5: // 共享
                                if(CfgData.userid<0){
                                    showMessage(getString(R.string.str_info),getString(R.string.str_pls_login));
                                    break;
                                }
                                List<BtnInfo> btnlist = CfgData.getBtnInfo(context,src.id);
                                if(src.isAc!=CfgData.AcPro) {
                                    int validcount = 0;
                                    for (int i = 0; i < btnlist.size(); i++) {
                                        if (btnlist.get(i).gsno >= 0) validcount++;
                                    }
                                    if (validcount < 2) {
                                        showMessage(getString(R.string.str_info), getString(R.string.str_tips_btnlititle));
                                        break;
                                    }
                                }
                                HttpRest("ClientUpload?USERID=" + CfgData.userid, CfgData.getRemoteTxtFile(src,btnlist), "POST", new OnActivityEventer() {
                                            @Override
                                            public void onSuc(byte[] out) {
                                                String res = new String(out);
                                                if(res.contains("ok")){
                                                    showMessage(getString(R.string.str_info),getString(R.string.str_share_ok));
                                                }else{
                                                    showMessage(getString(R.string.str_err),getString(R.string.str_share_err));
                                                }
                                            }
                                        });
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }
                })
                        .build().show();
                return true;
            }
        });
        //-------play
        mlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,final int position, long id) {
                Intent intent = new Intent();
                int idx = deviceAdapter.getIdx(position);
                if(CfgData.myremotelist.get(idx).isAc==CfgData.AcPro)
                    intent.setClass(context, RemotePlayAc.class);
                else
                if(CfgData.myremotelist.get(idx).isAc==CfgData.AcLear)
                    intent.setClass(context, RemotePlayAcLearn.class);
                else
                    intent.setClass(context, RemotePlay2.class);
                intent.putExtra("pos", idx);
                ((Activity)context).startActivity(intent);
            }
        });
        //---添加方式
        ((QMUIRadiusImageView)view.findViewById(R.id.btnappend)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity) context).startActivityForResult(new Intent(context,SelectSearchMode.class),23);
            }
        });
    }

    @Override
    public void doResultOk(int which,Intent data){
        Intent intent = new Intent();
        switch (which){
            case 1:
                //intent.setClass(context, UserCenter.class);
                //((Activity)context).startActivity(intent);
                break;
            case 4:
                // syn my data
                final RemoteInfo src = CfgData.myremotelist.get(currindex);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        webHttpClientCom.getInstance(null).Slient_UploadRemote(src);
                    }
                }).start();
                break;
            case 21: //直接下载 非空调遥控器
                int desidx = data.getIntExtra("desidx",0);
                if(!getPrcData(desidx))return;
                intent.setClass(context, PrcComuni.class);
                intent.putExtra("desidx",desidx);
                ((Activity)context).startActivity(intent);
                break;
            case 101: //添加遥控器
                String searchstr = data.getStringExtra("searchstr");
                if (QMUILangHelper.isNullOrEmpty(searchstr))return;
                pnlsearch.setVisibility(View.VISIBLE);
                filter.setText(searchstr);
                break;
        }
    }

    private boolean getPrcData(int desidx) {
        RemoteInfo src = CfgData.myremotelist.get(currindex);
        if(src==null)return false;

        CfgData.desbuttons = new ArrayList<IrButton>();
        //获取目标遥控器的按键信息
        List<DesRemoteBtn> desbtns = MoudelFile.GetBtns(context, desidx, CfgData.modellist);
        if (desbtns.size() == 0) {
            showMessage("Error", "Err found,cant not found des remote control's template");
            return false;
        }

        if(src.pp.equals("test") && src.xh.equals("test") && src.dev.equals("test")){
            String[] pagename = MoudelFile.getMoudlePage(context,desidx,CfgData.modellist);
            for (int i = 0; i < pagename.length; i++)
            for (int j = 0; j < 112; j++) {
                int[] param = new int[3];
                param[0] = i;
                param[1] = j;
                param[2] = Integer.parseInt(String.valueOf(j+1),16);
                IrButton abtn1 = new IrButton(i, j+1, 51, param); //第一个设备，pow按键
                CfgData.desbuttons.add(abtn1);
            }
            return true;
        }

        List<BtnInfo> srclist = CfgData.getBtnInfo(context, src.id);
        //按键使用位 初始
        for (int j = 0; j < srclist.size(); j++) {
            srclist.get(j).flag = false;
        }
        for (int j = 0; j < desbtns.size(); j++) {
            desbtns.get(j).flag = false;
        }
        //选择遥控器的按键到目标遥控器
        for (int j = 0; j < srclist.size(); j++) {
            BtnInfo abtn = srclist.get(j);
            if (abtn.gsno < 0) continue;
            if (abtn.params == null) continue;
            int n = -1;
            for (int k = 0; k < desbtns.size(); k++) {
                if (desbtns.get(k).flag) continue;
                if (desbtns.get(k).keyidx == abtn.keyidx) {
                    n = k;
                    break;
                }
            }
            if (n < 0) continue;
            IrButton abtn1 = new IrButton(0, desbtns.get(n).s, abtn.gsno, abtn.params); //第一个设备，pow按键
            CfgData.desbuttons.add(abtn1);
            abtn.flag = true;
            desbtns.get(n).flag = true;
        }
        //
        for (int j = 0; j < srclist.size(); j++) {
            BtnInfo abtn = srclist.get(j);
            if (abtn.flag) continue;
            if (abtn.gsno < 0) continue;
            if (abtn.params == null) continue;

            int n = -1;
            for (int k = 0; k < desbtns.size(); k++) {
                if (desbtns.get(k).flag) continue;
                n = k;
            }
            if (n < 0) continue;
            IrButton abtn1 = new IrButton(0, desbtns.get(n).s, abtn.gsno, abtn.params); //第一个设备，pow按键
            CfgData.desbuttons.add(abtn1);
            desbtns.get(n).flag = true;
        }

        if (CfgData.desbuttons.size() < 2) {
            showMessage("Error", "Err found,The selected buttons must be more than 2");
            return false;
        }
        return true;
    }

    @Override
    public void DoOnResume(){
        deviceAdapter.notifyDataSetChanged();
    }
}
