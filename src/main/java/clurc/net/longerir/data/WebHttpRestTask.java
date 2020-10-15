package clurc.net.longerir.data;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import clurc.net.longerir.base.BaseActivity;

import static clurc.net.longerir.uicomm.SsSerivce.TAG_SS;

// a. Params：开始异步任务执行时传入的参数类型，对应excute（）中传递的参数
// b. Progress：异步任务执行过程中，返回下载进度值的类型
// c. Result：异步任务执行完成后，返回的结果类型，与doInBackground()的返回值类型保持一致
public class WebHttpRestTask extends AsyncTask<String, Void, byte[]> {
    private static String baseurl = "https://data.clurc.net/root/";
    public static final int CONN_TIMEOUT = 60;
    public static final int READ_TIMEOUT = 60;
    private Handler mhandle;

    public void setUiHanler(Handler mhandle) {
        this.mhandle = mhandle;
    }
    // 作用：执行 线程任务前的操作
    // 注：根据需求复写
    @Override
    protected void onPreExecute() {

    }

    // 作用：接收输入参数、执行任务中的耗时操作、返回 线程任务执行的结果
    // 注：必须复写，从而自定义线程任务
    @Override
    protected byte[] doInBackground(String... params) {
        byte[] bdata = null;
        try {
            URL url = new URL(baseurl + params[0]);
            // 打开一个HttpURLConnection连接
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setConnectTimeout(CONN_TIMEOUT * 1000);
            urlConn.setReadTimeout(READ_TIMEOUT * 1000);
            urlConn.setUseCaches(false);
            urlConn.setRequestMethod(params[2]);
            urlConn.setDoInput(true);
            urlConn.setRequestProperty("Connection","close");
            if(params[1]==null) {
                urlConn.setDoOutput(false);
                urlConn.setRequestProperty("Accept-Encoding","gzip");
                //urlConn.setRequestProperty("Accept-Encoding","synlz");
                Log.w(TAG_SS, params[0] + "===>http request:" );
            }else {
                urlConn.setDoOutput(true);
                Log.w(TAG_SS, params[0] + "===>http request:" +  params[1]);
            }

            urlConn.connect();
            if(params[1]!=null) {
                PrintWriter pw = new PrintWriter(urlConn.getOutputStream());
                pw.print(params[1]);
                pw.flush();
            }
            // 先判断code 因为这个client的规则某些code，就不接收body
            int rescode= urlConn.getResponseCode();
            String encoding = urlConn.getContentEncoding();
            if (rescode == 200) {
                if(encoding==null || encoding.equals("gzip")==false){
                    bdata = streamToByteArr(urlConn.getInputStream());
                }else {
                    bdata = GzipstreamToByteArr(urlConn.getInputStream());
                }
                Log.w(TAG_SS,"recv len = "+ bdata.length);
                Log.w(TAG_SS,"===>http code="+rescode+" body="+new String(bdata));
            } else {
                Log.w(TAG_SS,"===>http code="+rescode);
            }
            urlConn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bdata;
    }

    // 作用：接收线程任务执行结果、将执行结果显示到UI组件
    // 注：必须复写，从而自定义UI操作
    @Override
    protected void onPostExecute(byte[] result) {
        if(isCancelled())return;
      // UI操作
        Message amsg = new Message();
        amsg.obj = result;
        mhandle.sendMessage(amsg);
    }

    // 作用：将异步任务设置为：取消状态
    @Override
    protected void onCancelled() {

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
}
