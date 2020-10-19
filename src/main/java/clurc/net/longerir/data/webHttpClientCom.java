package clurc.net.longerir.data;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import clurc.net.longerir.R;
import clurc.net.longerir.base.BaseActivity;

import static clurc.net.longerir.manager.UiUtils.getContext;
import static clurc.net.longerir.manager.UiUtils.getString;
import static clurc.net.longerir.uicomm.SsSerivce.TAG_SS;

public class webHttpClientCom {
    private Activity context;
    private static String baseurl = "https://data.clurc.net/root/";
    //private static String baseurl = "http://192.168.1.8:65500/root/";
    public static final int CONN_TIMEOUT = 60;
    public static final int READ_TIMEOUT = 60;
    public static final int WRITE_TIMEOUT = 6;

    private static webHttpClientCom webinstace = null;

    public webHttpClientCom(Activity context) {
        this.context = context;
    }

    public static final webHttpClientCom getInstance(Activity context) {
        if (webinstace == null) {
            webinstace = new webHttpClientCom(context);
        }else if(context!=null)
            webinstace.context = context;
        return webinstace;
    }

    public interface OnDownEventer {
        void onSuc(String body);
        void onFail(String res);
        void onPosition(int curr, int total);
    }

    private byte[] streamToByteArr(InputStream is) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            baos.close();
            is.close();
            byte[] byteArray = baos.toByteArray();
            return byteArray;
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] GzipstreamToByteArr(InputStream inis) {
        try {
            GZIPInputStream input = new GZIPInputStream(inis);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = input.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            baos.close();
            input.close();
            byte[] byteArray = baos.toByteArray();
            return byteArray;
        } catch (Exception e) {
            return null;
        }
    }

    public void BackHttpGetFile(final String urlstr, final OnDownEventer aev) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlstr); //URLEncoder.encode(paramsMap.get(key),"utf-8")));
                    // 打开一个HttpURLConnection连接
                    HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                    urlConn.setConnectTimeout(CONN_TIMEOUT * 1000);
                    urlConn.setReadTimeout(120 * 1000);
                    urlConn.setUseCaches(false);
                    urlConn.setRequestMethod("GET");
                    urlConn.setRequestProperty("Content-Type", "application/json");
                    //设置客户端与服务连接类型
                    urlConn.connect();
                    InputStream is = urlConn.getInputStream();
                    int total = urlConn.getContentLength();//获取文件长度
                    aev.onPosition(-1, total);
                    String filename = context.getCacheDir().getPath() + "/temp.apk"; //手机存储地址
                    OutputStream os = new FileOutputStream(filename);
                    int length;
                    int lengtsh = 0;
                    byte[] bytes = new byte[1024];
                    while ((length = is.read(bytes)) != -1) {
                        os.write(bytes, 0, length);
                        lengtsh += length; //获取当前进度值
                        aev.onPosition(lengtsh, -1);
                    }
                    //关闭流
                    is.close();
                    os.close();
                    os.flush();
                    if (urlConn.getResponseCode() == 200 && lengtsh >= total) {
                        aev.onSuc(filename);
                    } else
                        aev.onFail("NetWork Err! Please try again");
                } catch (Exception e) {
                    e.printStackTrace();
                    aev.onFail("Please check netword,retry again!");
                }
            }
        }).start();
    }

    public HttpRet ThreadHttpCall(String urlparam, String body,String method) {
        HttpRet ret = new HttpRet();
        try {
            URL url = new URL(baseurl + urlparam);
            // 打开一个HttpURLConnection连接
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setConnectTimeout(CONN_TIMEOUT * 1000);
            urlConn.setReadTimeout(READ_TIMEOUT * 1000);
            urlConn.setUseCaches(false);
            urlConn.setRequestMethod(method);
            urlConn.setDoInput(true);
            urlConn.setRequestProperty("Connection","close");
            if(body==null) {
                urlConn.setDoOutput(false);
                urlConn.setRequestProperty("Accept-Encoding","gzip");
                //urlConn.setRequestProperty("Accept-Encoding","synlz");
                Log.w(TAG_SS, urlparam + "===>http request:" + urlparam);
            }else {
                urlConn.setDoOutput(true);
                Log.w(TAG_SS, urlparam + "===>http request:" + urlparam + " body=" + body);
            }

            urlConn.connect();
            if(body!=null) {
                PrintWriter pw = new PrintWriter(urlConn.getOutputStream());
                pw.print(body);
                pw.flush();
            }
            // 先判断code 因为这个client的规则某些code，就不接收body
            int rescode= urlConn.getResponseCode();
            String encoding = urlConn.getContentEncoding();
            if (rescode == 200) {
                byte[] bdata;
                if(encoding==null || encoding.equals("gzip")==false){
                    bdata = streamToByteArr(urlConn.getInputStream());
                }else {
                    bdata = GzipstreamToByteArr(urlConn.getInputStream());
                }
                Log.w(TAG_SS,"recv len = "+ bdata.length);
                Log.w(TAG_SS,urlparam+"===>http code="+rescode+" body="+new String(bdata));
                ret.data = bdata;
                ret.result = true;
            } else {
                ret.err ="Error Code = " + rescode;
                Log.w(TAG_SS,urlparam+"===>http code="+rescode);
            }
            urlConn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG_SS,"===>http err="+e.getMessage());
        }
        return ret;
    }

    public interface WevEvent {
        void onSuc(byte[] out); //ui
        void onFail(String res); //ui
        boolean onDoData(byte[] out); //线程内处理
    }
    public void RestkHttpCallBase(final String urlparam, final String body, final String method, final WevEvent aev) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final HttpRet ret = ThreadHttpCall(urlparam, body,method);
                if(ret.result){
                    if(aev!=null) {
                        if(aev.onDoData(ret.data)) {
                            context.runOnUiThread(new Runnable() {
                                public void run() {
                                    aev.onSuc(ret.data);
                                }
                            });
                        }else
                            aev.onFail("Unknown err founded!");
                    }
                }else{
                    if(aev!=null) {
                        context.runOnUiThread(new Runnable() {
                            public void run() {
                                aev.onFail(ret.err);
                            }
                        });
                    }
                }
            }
        }).start();
    }
    private void RestkHttpCallBaseUI(final String urlparam, final String body, final String method, final WevEvent aev) {
        final QMUITipDialog tipDialog = new QMUITipDialog.Builder(context)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(context.getString(R.string.str_wait))
                .create();
        if(aev!=null)
            tipDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final HttpRet ret = ThreadHttpCall(urlparam, body,method);
                if(ret.result){
                    if(aev!=null) {
                        if(aev.onDoData(ret.data)) {
                            context.runOnUiThread(new Runnable() {
                                public void run() {
                                    tipDialog.dismiss();
                                    aev.onSuc(ret.data);
                                }
                            });
                        }else {
                            context.runOnUiThread(new Runnable() {
                                public void run() {
                                    tipDialog.dismiss();
                                    aev.onFail("Unknown err founded!");
                                }
                            });
                        }
                    }
                }else{
                    if(aev!=null) {
                        context.runOnUiThread(new Runnable() {
                            public void run() {
                                tipDialog.dismiss();
                                aev.onFail(ret.err);
                            }
                        });
                    }
                }
            }
        }).start();
    }
    public interface WevEvent_NoErr{
        void onSuc(byte[] out); //ui
        boolean onDoData(byte[] out); //线程内处理
    }
    public void RestkHttpCall(String urlparam, String body, String method, final WevEvent_NoErr aev) {
        RestkHttpCallBaseUI(urlparam, body, method, new WevEvent() {
            @Override
            public void onSuc(byte[] out) {
                aev.onSuc(out);
            }

            @Override
            public void onFail(String res) {
//                if (res==null)
                    Toast.makeText(context,  getString(R.string.str_err_net), Toast.LENGTH_SHORT).show();
//                if (res.length() > 0)
//                    Toast.makeText(context,  getString(R.string.str_err), Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean onDoData(byte[] out) {
                return aev.onDoData(out);
            }
        });
    }

    public interface WevEvent_NoErrString{
        void onSuc(); //ui
        boolean onDodata(String res); //线程内处理
    }
    public void RestkHttpCall(String urlparam, String body, String method, final WevEvent_NoErrString aev) {
        RestkHttpCall(urlparam, body, method, new WevEvent_NoErr() {
            @Override
            public void onSuc(byte[] out) {
                aev.onSuc();
            }

            @Override
            public boolean onDoData(byte[] out) {
                return aev.onDodata(new String(out));
            }
        });
    }

    public interface WevEvent_SucData{
        void onSuc(byte[] data); //ui
    }
    public void RestkHttpCall(final String urlparam, final String body, final String method, final WevEvent_SucData aev) {
        RestkHttpCall(urlparam, body, method, new WevEvent_NoErr() {
            @Override
            public void onSuc(byte[] out) {
                aev.onSuc(out);
            }

            @Override
            public boolean onDoData(byte[] out) {
                return true;
            }
        });
    }
    public interface WevEvent_SucString {
        void onSuc(String res); //ui
    }
    public void RestkHttpCall(final String urlparam, final String body, final String method, final WevEvent_SucString aev) {
        RestkHttpCall(urlparam, body, method, new WevEvent_NoErr() {
            @Override
            public void onSuc(byte[] out) {
                aev.onSuc(new String(out));
            }

            @Override
            public boolean onDoData(byte[] out) {
                return true;
            }
        });
    }
    //-------------------upload remote
    public void static_showMessage(String title,String contxt){
        new QMUIDialog.MessageDialogBuilder(context)
                //.setTitle(title)
                .setMessage(contxt)
                .setSkinManager(QMUISkinManager.defaultInstance(getContext()))
                .addAction("Ok", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
    //静默上传或覆盖我的遥控器
    public int thread_UploadRemote(RemoteInfo remote,boolean ispub){
        List<BtnInfo> btnlist = CfgData.getBtnInfo(context,remote.id);
        if(remote.isAc!=CfgData.AcPro){
            int validcount = 0;
            for (int i = 0; i < btnlist.size(); i++) {
                if (btnlist.get(i).gsno >= 0) validcount++;
            }
            if (validcount < 2) {
                return -1;
            }
        }
        String txtstr = CfgData.getRemoteTxtFile(remote,btnlist);
        String p;
        if(ispub){
            p = "ClientUpload?USERID=" + CfgData.userid;
        }else
        if(remote.rid>0){
            p = "appEditUpload?id=="+remote.rid;
        }else{
            if(CfgData.userid<0)return -2;
            p = "appUpload?userid="+CfgData.userid;
        }
        HttpRet ret = ThreadHttpCall(p,txtstr,"POST");
        if(ret.result==false)return -3;
        if(ispub){
            String res = new String(ret.data);
            if(res.contains("ok"))
                return 0;
            else {
                static_showMessage(getString(R.string.str_err),getString(R.string.str_share_err));
            }
        }
        if(remote.rid<1) {
            try {
                JSONObject jsonObj = new JSONObject(new String(ret.data));
                remote.rid = jsonObj.getInt("id");
                CfgData.AppendoOrEditMyFile(context, remote, null);
                return 0;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -4;
    }
    //上传遥控器
    public interface RestOnAppEvent {
        void onSuc();
    }
    public void Rest_UploadRemote(final RemoteInfo remote, final boolean ismy, final  RestOnAppEvent aev){
        if(CfgData.userid<0){
            static_showMessage(getString(R.string.str_info),getString(R.string.str_pls_login));
            return;
        }
        final QMUITipDialog tipDialog = new QMUITipDialog.Builder(context)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(context.getString(R.string.str_wait))
                .create();
        tipDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int ret = thread_UploadRemote(remote,ismy);
                context.runOnUiThread(new Runnable() {
                    public void run() {
                        tipDialog.dismiss();
                        switch (ret){
                            case -1:
                                static_showMessage(getString(R.string.str_info), getString(R.string.str_tips_btnlititle));
                                break;
                            case 0:
                                aev.onSuc();
                                break;
                            default:
                                static_showMessage(getString(R.string.str_err), getString(R.string.str_err_net));
                                break;
                        }
                    }
                });
            }
        }).start();
    }

    //下载遥控器数据
    public void Rest_DownRemote(final RemoteInfo remote,final List<TxtBtnInfo> txtbtns,String params, final  RestOnAppEvent aev) {
        final QMUITipDialog tipDialog = new QMUITipDialog.Builder(context)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(context.getString(R.string.str_wait))
                .create();
        tipDialog.show();
        RestkHttpCall(params, null, "GET", new webHttpClientCom.WevEvent_NoErr() {
            @Override
            public void onSuc(byte[] out) {
                context.runOnUiThread(new Runnable() {
                    public void run() {
                        tipDialog.dismiss();
                        aev.onSuc();
                    }
                });
            }

            @Override
            public boolean onDoData(byte[] out) {
                String errstr = "Unknown err";
                boolean suc = false;
                String res = new String(out);
                if (QMUILangHelper.isNullOrEmpty(res)) {
                    errstr = "err in the codes";
                } else {
                    if (remote.pp == null)
                        CfgData.GetRemoteFromText(remote, res);
                    remote.descname = remote.pp+"/"+remote.xh;
                    if (remote.isAc == CfgData.AcPro) {
                        remote.acdata = res;
                    } else {
                        CfgData.GetBtnsFromText(txtbtns, res);
                    }
                    suc = true;
                }
                return suc;
            }
        });
    }

    public void Rest_DownAndSaveMys(final RemoteInfo remote,String params, final  RestOnAppEvent aev){
        final List<TxtBtnInfo> txtbtns = new ArrayList<TxtBtnInfo>();
        Rest_DownRemote(remote, txtbtns, params, new RestOnAppEvent() {
            @Override
            public void onSuc() {
                List<BtnInfo> btns = new ArrayList<BtnInfo>();
                CfgData.TransTxtToBtns(txtbtns, btns, remote.dev,remote.isAc);
                if(CfgData.AppendoOrEditMyFile(context, remote, btns)) {
                    CfgData.myremotelist.add(remote);
                    aev.onSuc();
                }else{
                    Toast.makeText(context, "Unknown err", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public boolean thread_DownAndSaveMys(RemoteInfo remote,String params){
        final List<TxtBtnInfo> txtbtns = new ArrayList<TxtBtnInfo>();
        HttpRet ret = ThreadHttpCall(params, null,"GET");
        if(!ret.result)return false;
        String errstr = "Unknown err";
        boolean suc = false;
        String res = new String(ret.data);
        if (QMUILangHelper.isNullOrEmpty(res)) return false;
        if (remote.pp == null)
            CfgData.GetRemoteFromText(remote, res);
        remote.descname = remote.pp+"/"+remote.xh;
        if (remote.isAc == CfgData.AcPro) {
            remote.acdata = res;
        } else {
            CfgData.GetBtnsFromText(txtbtns, res);
        }
        List<BtnInfo> btns = new ArrayList<BtnInfo>();
        CfgData.TransTxtToBtns(txtbtns, btns, remote.dev,remote.isAc);
        if(CfgData.AppendoOrEditMyFile(context, remote, btns)) {
            CfgData.myremotelist.add(remote);
            return true;
        }
        return false;
    }

    //下载遥控器数据 品牌 型号从json里面取
    public void Rest_DownRemoteMulty(String[] itm, final  RestOnAppEvent aev) {
        final QMUITipDialog tipDialog = new QMUITipDialog.Builder(context)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(context.getString(R.string.str_wait))
                .create();
        tipDialog.show();
        final boolean iscodecant = itm[0].equals("upinfo");
        RestkHttpCallBase(itm[0]+"?select=data,pp,xh,dev,isAc&where=id in "+itm[1], null, "GET", new webHttpClientCom.WevEvent() {
            @Override
            public void onSuc(byte[] out) {
                String errstr = "Unknown err";
                boolean suc = false;
                String res = new String(out);
                if (QMUILangHelper.isNullOrEmpty(res)) {
                    errstr = "err in the codes";
                } else {
                    try {
                        Object json = new JSONTokener(res).nextValue();
                        if(json instanceof JSONArray) {
                            JSONArray jsonAll = (JSONArray) json;
                            for (int i = 0; i < jsonAll.length(); i++) {
                                JSONObject jsonsingle = (JSONObject) jsonAll.get(i);
                                RemoteInfo armt = new RemoteInfo();
                                armt.codecannot = iscodecant;
                                armt.pp = jsonsingle.getString("pp");
                                armt.dev = jsonsingle.getString("dev");
                                armt.xh = jsonsingle.getString("xh");
                                armt.isAc = jsonsingle.getInt("isAc");
                                armt.descname = armt.pp+"/"+armt.xh;
                                List<TxtBtnInfo> txtbtns = new ArrayList<TxtBtnInfo>();
                                String codestr = jsonsingle.getString("Data");
                                byte[] rawdata = android.util.Base64.decode(codestr,android.util.Base64.DEFAULT);
                                if (armt.isAc == CfgData.AcPro) { //这里的专业数据 读取txt的data
                                    armt.acdata = CfgData.GetAcProFromText(new String(rawdata));
                                } else {
                                    CfgData.GetBtnsFromText(txtbtns,new String(rawdata));
                                }
                                List<BtnInfo> btns = new ArrayList<BtnInfo>();
                                CfgData.TransTxtToBtns(txtbtns, btns, armt.dev,armt.isAc);
                                if(CfgData.AppendoOrEditMyFile(context, armt, btns)) {
                                    CfgData.myremotelist.add(armt);
                                }else{
                                    Toast.makeText(context, "Unknown err", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                        }
                        suc = true;
                    }catch (JSONException e) {
                        e.printStackTrace();
                        errstr = e.getMessage();
                    }
                }
                tipDialog.dismiss();
                if (suc) {
                    aev.onSuc();
                } else {
                    Toast.makeText(context, errstr, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFail(final String res) {
                        tipDialog.dismiss();
                        if (res==null)
                            static_showMessage( getString(R.string.str_err), getString(R.string.str_err_net));
                        else if (res != null && res.length() > 0)
                            static_showMessage( getString(R.string.str_err), res);
            }

            @Override
            public boolean onDoData(byte[] out) {
                return true;
            }
        });
    }

    public class HttpRet{
        public boolean result;
        public byte[] data;
        public String err;

        public HttpRet(){
            result = false;
            err = null;
        }
    }
}
