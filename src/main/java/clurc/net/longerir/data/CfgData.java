package clurc.net.longerir.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.qmuiteam.qmui.util.QMUILangHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import clurc.net.longerir.BaseApplication;
import clurc.net.longerir.R;
import clurc.net.longerir.Utils.MoudelFile;
import clurc.net.longerir.Utils.SysFun;
import clurc.net.longerir.data.modeldata.DataModelBtnInfo;
import clurc.net.longerir.ircommu.DesRemote;
import clurc.net.longerir.ircommu.DesRemoteBtn;
import clurc.net.longerir.manager.QDPreferenceManager;

import static clurc.net.longerir.manager.UiUtils.getString;

public class CfgData {
    public static final int AcPro = 10;
    public static final int AcLear = 11;
    public static final int Ac_Lear_Sta_Count = 8;
    public static final String sdcard_paht = "/sdcard/longer/";
    public static final String txtFile_End = "********device file end********";
    public static String mobileserial;
    public static int dataidx = 0; //数据库序号
    public static int selectIr;  //0:usb 1:ble t, 2:ble remote 3:mobile
    //---------------------------------------
    public static int userid=-1;
    public static String username=null;
    public static int usertype=0;
    //---------------------------------------
    private static String myremotefile = "/dbfile.db";
    public static List<MoudelFile.ModelStru> modellist;

    public static List<RemoteInfo> myremotelist=new ArrayList<RemoteInfo>();
    public static List<DesRemote> prclist = new ArrayList<DesRemote>();
    //设备类型列表
    public static List<String> devitems =  new ArrayList<String>();
    //
    public static List<IrButton> desbuttons;//作为prc跨activity临时使用
    //-----分析服务器数据--------------------------------
    public static boolean ParseBrandArr(List<String> brandlist,String src){
        if(brandlist==null)
            brandlist = BaseApplication.getMyApplication().getShrremote_brands();
        brandlist.clear();
        try {
            JSONArray jsonAll = new JSONArray(src);
            for(int i=0;i<jsonAll.length();i++){
                JSONObject jsonsingle = (JSONObject)jsonAll.get(i);
                String astr = jsonsingle.getString("pp").toUpperCase();
                if(!QMUILangHelper.isNullOrEmpty(astr))
                    brandlist.add(astr);
            }
            return true;
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void readMyRemote(Context context,List<RemoteInfo> rmtlist){
        rmtlist.clear();
        String basefile=context.getFilesDir().getAbsolutePath()+myremotefile;
        SQLiteDatabase mSQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(basefile,null);
        CreatemyTable(mSQLiteDatabase);
        Cursor cursor =mSQLiteDatabase.rawQuery("select * from myremote", null);
        if(cursor!=null) {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    RemoteInfo ainfo = new RemoteInfo();
                    ainfo.id = cursor.getInt(cursor.getColumnIndex("id"));
                    ainfo.descname = cursor.getString(cursor.getColumnIndex("showstr"));
                    ainfo.pp = cursor.getString(cursor.getColumnIndex("pp"));
                    ainfo.xh = cursor.getString(cursor.getColumnIndex("xh"));
                    ainfo.dev = cursor.getString(cursor.getColumnIndex("dev"));
                    ainfo.codecannot = cursor.getInt(cursor.getColumnIndex("codecannot"))==1?true:false;
                    int v = cursor.getInt(cursor.getColumnIndex("islearned"));
                    ainfo.islearned = ((v % 2) == 1) ? true : false;
                    ainfo.isAc = (int)(v / 10);
                    ainfo.fav = cursor.getInt(cursor.getColumnIndex("fav"))==1?true:false;
                    ainfo.acdata = cursor.getString(cursor.getColumnIndex("keys"));
                    ainfo.rid = cursor.getInt(cursor.getColumnIndex("rid"));
                    rmtlist.add(ainfo);
                }
            }
            cursor.close();
        }
        mSQLiteDatabase.close();
    }

    public static List<BtnInfo> getBtnInfo(Context context,int id){
        List<BtnInfo> btnlist = new ArrayList<BtnInfo>();
        String basefile=context.getFilesDir().getAbsolutePath()+myremotefile;
        SQLiteDatabase mSQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(basefile,null);
        CreatemyTable(mSQLiteDatabase);
        Cursor cursor =mSQLiteDatabase.rawQuery("select * from mybtnlist where mid="+id, null);
        if(cursor!=null) {
            if (cursor.getCount() > 0) {//,mid int,btnname text,gsno int,param text,col int,rows int,img text)";
                while (cursor.moveToNext()) {
                    BtnInfo abtn = new BtnInfo();
                    abtn.id = cursor.getInt(cursor.getColumnIndex("id"));
                    abtn.param16 = cursor.getString(cursor.getColumnIndex("param"));
                    abtn.btnname = cursor.getString(cursor.getColumnIndex("btnname"));
                    abtn.gsno = cursor.getInt(cursor.getColumnIndex("gsno"));
                    abtn.wave = cursor.getString(cursor.getColumnIndex("wave"));
                    abtn.col = cursor.getInt(cursor.getColumnIndex("col"));
                    abtn.row = cursor.getInt(cursor.getColumnIndex("rows"));
                    abtn.imgpath = cursor.getString(cursor.getColumnIndex("img"));
                    abtn.keyidx = cursor.getInt(cursor.getColumnIndex("keyidx"));
                    if (abtn.param16 != null) {
                        String[] item = abtn.param16.split(",");
                        abtn.params = new int[item.length];
                        for (int i = 0; i < item.length; i++) {
                            abtn.params[i] = Integer.parseInt(item[i],16);
                        }
                    }
                    btnlist.add(abtn);
                }
            }
            cursor.close();
        }
        mSQLiteDatabase.close();
        return btnlist;
    }

    public static  int getMaxCols(List<BtnInfo> btns){
        int ret = 0;
        for (int i = 0; i < btns.size(); i++) {
            BtnInfo v = btns.get(i);
            if(v.col>ret){
                ret = v.col;
            }
        }
        ret++;
        return ret;
    }

    private static void CreatemyTable(SQLiteDatabase mSQLiteDatabase){
        String sql= "create table if not exists myremote(id integer PRIMARY KEY AUTOINCREMENT,showstr text,pp text,xh text,dev text,keys text,islearned int,rid int,codecannot int,fav int)";
        mSQLiteDatabase.execSQL(sql);
        //增加字段
        sql = "SELECT sql FROM sqlite_master where type='table' and tbl_name='myremote'";
        Cursor cursor =mSQLiteDatabase.rawQuery(sql,null);
        if(cursor!=null) {
            if (cursor.getCount() > 0) {
                cursor.moveToNext();
                String s = cursor.getString(0);
                if(!s.contains("rid")){
                    mSQLiteDatabase.execSQL("ALTER TABLE 'myremote' ADD 'rid' int DEFAULT 0");
                }
                if(!s.contains("codecannot")){
                    mSQLiteDatabase.execSQL("ALTER TABLE 'myremote' ADD 'codecannot' int DEFAULT 0");
                }
                if(!s.contains("fav")){
                    mSQLiteDatabase.execSQL("ALTER TABLE 'myremote' ADD 'fav' int DEFAULT 0");
                }
            }
        }
        //
        sql= "create table if not exists mybtnlist(id integer PRIMARY KEY AUTOINCREMENT,mid int,btnname text,gsno int,param text,col int,rows int,img text,wave text,keyidx int)";
        mSQLiteDatabase.execSQL(sql);
    }

    //服务器上的数据保存我的数据到磁盘{
    public  static boolean AppendoOrEditMyFile(Context context,RemoteInfo armt,List<BtnInfo> btnlist){
        String basefile=context.getFilesDir().getAbsolutePath()+myremotefile;
        SQLiteDatabase mSQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(basefile,null);
        CreatemyTable(mSQLiteDatabase);

        ContentValues values = new ContentValues();
        values.put("showstr", armt.descname);
        values.put("pp", armt.pp);
        values.put("xh", armt.xh);
        values.put("dev", armt.dev);
        values.put("rid", armt.rid);
        values.put("codecannot", armt.codecannot?1:0);
        values.put("fav", armt.fav?1:0);
        int v = 0;
        if(armt.islearned)v = 1;
        v+= armt.isAc*10;
        values.put("islearned", v);
        if(armt.isAc==AcPro)
            values.put("keys",armt.acdata);

        if(armt.id>=0){
            mSQLiteDatabase.update("myremote", values,"id=?",new String[]{String.valueOf(armt.id)});
        }else {
            armt.id = (int)mSQLiteDatabase.insert("myremote", null, values);
        }

        if(armt.id<0){
            mSQLiteDatabase.close();
            return false;
        }
        if(armt.isAc==AcPro){
            mSQLiteDatabase.close();
            return true;
        }
        //---------save info database----
        if(btnlist!=null) {
            mSQLiteDatabase.execSQL("delete from mybtnlist where mid=" + armt.id);
            for (int i = 0; i < btnlist.size(); i++) {
                values = new ContentValues();
                values.put("mid", armt.id);
                values.put("btnname", btnlist.get(i).btnname);
                values.put("gsno", btnlist.get(i).gsno);
                values.put("param", btnlist.get(i).param16);
                values.put("col", btnlist.get(i).col);
                values.put("rows", btnlist.get(i).row);
                values.put("img", btnlist.get(i).imgpath);
                values.put("keyidx", btnlist.get(i).keyidx);
                values.put("wave", btnlist.get(i).wave);
                mSQLiteDatabase.insert("mybtnlist", null, values);
            }
        }
        mSQLiteDatabase.close();
        //-------
        return true;
    }

    public  static void MyRemoteSaveFav(Context context,RemoteInfo armt){
        String basefile=context.getFilesDir().getAbsolutePath()+myremotefile;
        SQLiteDatabase mSQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(basefile,null);
        CreatemyTable(mSQLiteDatabase);

        ContentValues values = new ContentValues();
        values.put("fav", armt.fav?1:0);
        mSQLiteDatabase.update("myremote", values,"id=?",new String[]{String.valueOf(armt.id)});
        mSQLiteDatabase.close();
    }

    public static void TransTxtToBtns(List<TxtBtnInfo> src,List<BtnInfo> des,String dev,int isAc){
        des.clear();
        if(isAc==AcPro){
            return;
        }else
        if(isAc==AcLear){
            for (int i = 0; i < src.size(); i++) {
                BtnInfo desbtn = new BtnInfo();
                TxtBtnInfo srcbtn = src.get(i);

                desbtn.keyidx = srcbtn.gsno; // freq

                String s = String.valueOf(srcbtn.keyidx);
                while (s.length()<Ac_Lear_Sta_Count)
                    s="0"+s;
                desbtn.params = new int[Ac_Lear_Sta_Count];
                desbtn.param16="";
                for (int j = 0; j < desbtn.params.length; j++) {
                    try {
                        desbtn.params[j] = Integer.parseInt(String.valueOf(s.charAt(j)));
                        desbtn.param16 += s.charAt(j)+",";
                    }catch (NumberFormatException e){
                        continue;
                    }
                }
                desbtn.wave ="";
                for (int j = 0; j < srcbtn.wave.length; j++) {
                    desbtn.wave += String.valueOf(srcbtn.wave[j])+",";
                }
                des.add(desbtn);
            }
        }else {
            int devidx = getTypeIDByStr(dev);
            for (int i = 0; i < src.size(); i++) {
                SysbtnInfo sysbtn = getSysBtnByIdx(devidx, src.get(i).keyidx);
                if (sysbtn == null) //先排序号给出的 0...86
                    continue;
                BtnInfo viewInfo = new BtnInfo();
                viewInfo.btnname = src.get(i).keyname;
                if (QMUILangHelper.isNullOrEmpty(viewInfo.btnname))
                    viewInfo.btnname = sysbtn.btnname;

                viewInfo.row = sysbtn.row;
                viewInfo.col = sysbtn.col;
                viewInfo.imgpath = sysbtn.img;
                viewInfo.gsno = src.get(i).gsno;
                viewInfo.param16 = "";
                viewInfo.keyidx = src.get(i).keyidx;
                for (int j = 0; j < src.get(i).param.length; j++) {
                    viewInfo.param16 += Integer.toHexString(src.get(i).param[j] & 0xffffffff) + ",";
                }
                //-------------
                src.get(i).flag = true;
                des.add(viewInfo);
            }
            //不在0.。86之间，把它按在空的地方
            for (int i = 0; i < src.size(); i++) {
                if (src.get(i).flag) continue;

                BtnInfo viewInfo = new BtnInfo();
                viewInfo.btnname = src.get(i).keyname;
                if (QMUILangHelper.isNullOrEmpty(viewInfo.btnname))
                    viewInfo.btnname = "NULL";
                viewInfo.imgpath = "button/def.png";
                for (int j = 0; j < 255; j++) {
                    boolean notfound = true;
                    for (int k = 0; k < des.size(); k++) {
                        if ((des.get(k).row * 4 + des.get(k).col) == j) {
                            notfound = false;
                            break;
                        }
                    }
                    if (notfound) {
                        viewInfo.row = j / 4;
                        viewInfo.col = j % 4;
                        break;
                    }
                }
                viewInfo.gsno = src.get(i).gsno;
                viewInfo.param16 = "";
                for (int j = 0; j < src.get(i).param.length; j++) {
                    viewInfo.param16 += Integer.toHexString(src.get(i).param[j] & 0xffffffff) + ",";
                }
                viewInfo.keyidx = src.get(i).keyidx;

                des.add(viewInfo);
                if (des.size() > 1024) break;//按键超过255，放弃
            }
        }
    }
     //
    public static void SaveBtnposChanged(Context context,List<BtnInfo> btns){
        String basefile=context.getFilesDir().getAbsolutePath()+myremotefile;
        SQLiteDatabase mSQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(basefile,null);
        CreatemyTable(mSQLiteDatabase);
        for (int i = 0; i < btns.size(); i++) {
            ContentValues values = new ContentValues();
            values.put("col", btns.get(i).col);
            values.put("rows", btns.get(i).row);
            mSQLiteDatabase.update("mybtnlist", values,"id=?",new String[]{String.valueOf(btns.get(i).id)});
        }
    }
    //
    public static void DeleteMyRemote(Context context,int idx){
        String basefile=context.getFilesDir().getAbsolutePath()+myremotefile;
        SQLiteDatabase mSQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(basefile,null);
        CreatemyTable(mSQLiteDatabase);
        mSQLiteDatabase.execSQL("delete from mybtnlist where mid="+myremotelist.get(idx).id);
        mSQLiteDatabase.execSQL("delete from myremote where id="+myremotelist.get(idx).id);
        mSQLiteDatabase.close();
        myremotelist.remove(idx);
    }
    //--------------------------------------------------------------
    /**
     * 从assets 文件夹中获取文件并读取数据
     * @return
     */
    public static String getStringFromAssets(String filePath,Context context) {
        String result = "";
        try {
            InputStream in = context.getResources().getAssets().open(filePath);
            int lenght = in.available();
            byte[] buffer = new byte[lenght];
            in.read(buffer);
            result = new String(buffer,"UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    public static String getStringFromFile(String filePath) {
        String result = "";
        try {
            InputStream in = new FileInputStream(filePath);
            int lenght = in.available();
            byte[] buffer = new byte[lenght];
            in.read(buffer);
            result = new String(buffer,"UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 按键信息数据集
     * MBI=ModelButtonInfo
     */
    public static List<TemplateRemote> remotetemps =new ArrayList<TemplateRemote>();
    public static void readSystBtnInfo(Context context){
        remotetemps.clear();
        String[] infoArr = getStringFromAssets("config/template.txt",context).split("\r\n");
        for (int i = 0; i < infoArr.length; i++) {
            TemplateRemote atr = new TemplateRemote();
            String[] curArr=infoArr[i].split(":");
            atr.name = curArr[3].toUpperCase();
            atr.img = curArr[4];
            //--------------
            String[] posarr = curArr[5].split(";");
            for (int j = 0; j < posarr.length; j++) {
                String[] itm = posarr[j].split(",");
                SysbtnInfo abtn = new SysbtnInfo();
                abtn.keyidx = Integer.parseInt(itm[0]) - i*86;
                abtn.row = Integer.parseInt(itm[1]);
                abtn.col = Integer.parseInt(itm[2]);
                atr.btns.add(abtn);
            }
            //--------
            remotetemps.add(atr);
        }
        infoArr = getStringFromAssets("config/button.txt",context).split("\r\n");
        for(int i=0;i<infoArr.length;i++){
            String[] curArr=infoArr[i].split(";");

            int devindex = Integer.parseInt(curArr[1]);
            int keyidx = Integer.parseInt(curArr[2]);
            SysbtnInfo abtn = getSysBtnByIdx(devindex,keyidx);
            if(abtn==null) {
                abtn = new SysbtnInfo();
                abtn.btnname = curArr[5];
                abtn.img = curArr[6];
                remotetemps.get(devindex).btns.add(abtn);
            }else{
                abtn.btnname = curArr[5];
                abtn.img = curArr[6];
            }
        }
    }

    public static SysbtnInfo getSysBtnByIdx(int devidx,int idx){
        for (int i = 0; i < remotetemps.get(devidx).btns.size(); i++) {
            SysbtnInfo abtn=remotetemps.get(devidx).btns.get(i);
            if(idx==abtn.keyidx){
                return abtn;
            }
        }
        return null;
    }
    //设备名称获取相近的模板
    public static int getTypeIDByStr(String str){
        if(QMUILangHelper.isNullOrEmpty(str))return 0;
        String tempStr=str.toUpperCase().trim();
        for (int i = 0; i < remotetemps.size(); i++) {
            if(remotetemps.get(i).name.equals(tempStr))
                return i;
        }
        return 0;
    }
    //从txt中获取按键
    public static List<TxtBtnInfo> GetBtnsFromText(String txtstr){
        List<TxtBtnInfo> btns = new ArrayList<TxtBtnInfo>();
        String[] linestr = txtstr.split("\r\n");
        if(linestr.length<2)
            linestr = txtstr.split("\n");
        int from=0;
        for (int i = 0; i < linestr.length; i++) {
            if(linestr[i].contains("Button's counts")){
                from = i+1;
                break;
            }
        }
        for(int i=from;i<linestr.length;i++){
            String[] lr=linestr[i].split("=");
            if(lr.length!=2)continue;

            String[] left = lr[0].split(",");
            TxtBtnInfo abtn = new TxtBtnInfo();
            try {
                abtn.keyidx = Integer.parseInt(left[0]);
            }catch (NumberFormatException e){
                continue;
            }
            abtn.keyname=null;
            if(left.length>1)
                abtn.keyname = left[1];

            String[] right = lr[1].split(" ");
            int paramlen = right.length-1;
            if(paramlen<1)continue;
            abtn.gsno = Integer.parseInt(right[0]);
            abtn.param = new int[paramlen];
            int p=0;
            for (int j = 0; j < paramlen; j++) {
                try {
                    abtn.param[p] = Integer.parseInt(right[j+1],16);
                    p++;
                }catch (NumberFormatException e){
                }
            }
            btns.add(abtn);
        }
        return btns;
    }

    //从txt中获取按键
    public static void GetBtnsFromText(List<TxtBtnInfo> btns,String txtstr){
        if(btns==null)
            btns = new ArrayList<TxtBtnInfo>();
        else
            btns.clear();
        String[] linestr = txtstr.split("\r\n");
        if(linestr.length<2)
            linestr = txtstr.split("\n");
        int from=0;
        for (int i = 0; i < linestr.length; i++) {
            if(linestr[i].contains("Button's counts")){
                from = i+1;
                break;
            }
        }
        for(int i=from;i<linestr.length;i++){
            String[] lr=linestr[i].split("=");
            if(lr.length!=2)continue;

            String[] left = lr[0].split(",");
            TxtBtnInfo abtn = new TxtBtnInfo();
            try {
                abtn.keyidx = Integer.parseInt(left[0]);
            }catch (NumberFormatException e){
                continue;
            }
            abtn.keyname=null;
            if(left.length>1)
                abtn.keyname = left[1];
            if(lr[1].contains(",")){  //fre wavestr
                String[] right = lr[1].split(" ");
                if(right.length!=2)
                    continue;
                try {
                    abtn.gsno = Integer.parseInt(right[0]);
                }catch (NumberFormatException e){
                    continue;
                }
                String[] wave = right[1].split(",");
                abtn.wave = new int[wave.length];
                for (int j = 0; j < wave.length; j++) {
                    try {
                        abtn.wave[j] = Integer.parseInt(wave[j]);
                    }catch (NumberFormatException e){
                        break;
                    }
                }
            }else {
                String[] right = lr[1].split(" ");
                int paramlen = right.length - 1;
                if (paramlen < 1) continue;
                abtn.gsno = Integer.parseInt(right[0]);
                abtn.param = new int[paramlen];
                int p = 0;
                for (int j = 0; j < paramlen; j++) {
                    try {
                        abtn.param[p] = Integer.parseInt(right[j + 1], 16);
                        p++;
                    } catch (NumberFormatException e) {
                    }
                }
            }
            btns.add(abtn);
        }
    }

    // 从ｔｘｔ获取ｄａｔａ　空调专业数据
    public static String GetAcProFromText(String txtstr){
        String[] linestr = txtstr.split("\r\n");
        if(linestr.length<2)
            linestr = txtstr.split("\n");
        for(int i=0;i<linestr.length;i++){
            String [] lr=linestr[i].split("=");
            if(lr.length!=2)continue;

            if(lr[0].equals("data")){
                return lr[1];
            }
        }
        return null;
    }

    public static void GetRemoteFromText(RemoteInfo aremote,String txtstr){
        String[] linestr = txtstr.split("\r\n");
        if(linestr.length<2)
            linestr = txtstr.split("\n");
        aremote.dev = null;
        aremote.xh = null;
        aremote.pp = null;
        for(int i=0;i<linestr.length;i++){
            String[] lr=linestr[i].split("=");
            if(lr.length!=2)continue;

            if(lr[0].equals("  Device")){
                aremote.dev = lr[1];
            }else
            if(lr[0].equals("  Brand")){
                aremote.pp = lr[1];
            }else
            if(lr[0].equals("  Model")){
                aremote.xh = lr[1];
            }
        }
    }
      /**
     * 从Assets中读取图片
     * @return
     */
    public static Bitmap getImage4Assets(Context context, String filePath) {
        Bitmap image = null;
        try {
            InputStream is = context.getResources().getAssets().open(filePath);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public static Bitmap getBtnBmp(Context context,String btnname, String path, boolean iscustom) {
        Bitmap viewBmp = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(viewBmp);
        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        Bitmap bmp=null;
        if(!iscustom){
            if(QMUILangHelper.isNullOrEmpty(path))
                bmp= getImage4Assets(context,"button/def.png");
            else
                bmp= getImage4Assets(context, path);
//				bmp=BitmapFactory.decodeResource(getResources(), R.drawable.def);
        }else{
            if(QMUILangHelper.isNullOrEmpty(path))
                bmp= getImage4Assets(context,"button/def.png");
            else{
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize =0;
                bmp = BitmapFactory.decodeFile(path, options);
                if(bmp==null)
                    bmp= getImage4Assets(context,"button/def.png");
//					bmp=BitmapFactory.decodeResource(instance.getResources(), R.drawable.def);
            }
        }
        int w=bmp.getWidth();
        int h=bmp.getHeight();
        canvas.drawBitmap(bmp, (100-w)/2,0, paint);
        paint.setTextSize(25);
        paint.setColor(Color.WHITE);
        int fontWidth=(int) paint.measureText(btnname);
        if(fontWidth>100){
            canvas.drawText(btnname, 0, h+25, paint);
        }else{
            canvas.drawText(btnname, (100-fontWidth)/2, h+25, paint);
        }
        return viewBmp;
    }

    public static int[] getMyRemoteIdList(boolean isac){
        int len = 0;
        for (int i = 0; i < myremotelist.size(); i++) {
            if(CfgData.myremotelist.get(i).isAc==0){
                if(!isac)len++;
            }else{
                if(isac)len++;
            }
        }
        int[] acj = new int[len];
        int n=0;
        for (int i = 0; i < myremotelist.size(); i++) {
            if(CfgData.myremotelist.get(i).isAc==0) {
                if(!isac) {
                    acj[n] = i;
                    n++;
                }
            }else{
                if(isac) {
                    acj[n] = i;
                    n++;
                }
            }
        }
        return acj;
    }
    public static String[] getMyRemoteStrings(int[] myid){
        String[] acj = new String[myid.length];
        for (int i = 0; i < myid.length; i++) {
            int n = myid[i];
            String s = CfgData.myremotelist.get(n).descname;
            if (QMUILangHelper.isNullOrEmpty(s)) {
                s = CfgData.myremotelist.get(n).pp + CfgData.myremotelist.get(n).xh;
            }
            acj[i] = s;
        }
        return acj;
    }

    public static byte[] StringsToByteArr(String src){
        byte[] arr = null;
        int temp = src.length();
        int counts = temp / 2;
        if (counts<=0)return null;
        arr = new byte[counts];
        int pos = 0;
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (byte)Integer.parseInt(src.substring(pos, pos+2), 16);
            pos+=2;
        }
        return arr;
    }

    /**
     * 16进制字符串转换
     */
    public static String getHexStr(int num){
        String str=Integer.toHexString(num&0xff);
        if(str.length()<=1){
            str="0"+str;
        }
        while(str.length()>2)
            str = str.substring(1);
        return str;
    }

    public static String ByteArrToString(byte[] data){
        if(data==null)return null;
        String valstr="";
        for(int i=0;i<data.length;i++) {
            String str= getHexStr(data[i] & 0xff);
            if(str.length()<=1){
                str="0"+str;
            }
            valstr +=str;
        }
        return valstr;
    }

    public static byte[] intArrToByte(int[] data){
        byte[] ret = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            ret[i] = (byte) (data[i] & 0xff);
        }
        return ret;
    }

    public static boolean CheckSum(byte[] data){
        int he=0;
        int len = data.length-1;
        for (int i = 0; i < len; i++) he += (data[i]&0xff);
        if((he & 0xff)==(data[len]&0xff))
            return true;
        else
            return false;
    }

    public static boolean ComPareMydata(byte[] d1,int[] d2){
        for (int i = 0; i < d1.length; i++) {
            if((d1[i] & 0xff)!= (d2[i]&0xff)){
                return false;
            }
        }
        return true;
    }
    public static boolean ComPareMydata(byte[] d1,byte[] d2){
        for (int i = 0; i < d1.length; i++) {
            if(d1[i]!= d2[i]){
                return false;
            }
        }
        return true;
    }


    //生成验证码
    public static String CreateVeryCode(){
        int[] array = {0,1,2,3,4,5,6,7,8,9};
        Random rand = new Random();
        for (int i = 10; i > 1; i--) {
            int index = rand.nextInt(i);
            int tmp = array[index];
            array[index] = array[i - 1];
            array[i - 1] = tmp;
        }
        String result = "";
        for(int i = 0; i < 6; i++)
            result+=array[i];
        return result;
    }

    public static int getWordValue(byte[] buf,int pos){
        int ret = buf[pos+1]&0xff;
        ret <<=8;
        ret +=(int)(buf[pos]&0xff);
        return ret;
    }

    public static int getDWordValue(byte[] buf,int pos){
        int ret1 = getWordValue(buf,pos);
        int ret2 = getWordValue(buf,pos+2);
        ret2 <<=16;
        ret1 +=ret2;
        return ret1;
    }

    //拷贝遥控器数据
    public static RemoteInfo CopyNewRemote(RemoteInfo src){
        RemoteInfo des= new RemoteInfo();
        des.descname = src.descname;
        des.pp = src.pp;
        des.dev = src.dev;
        des.xh = src.xh;
        des.id =src.id;
        des.isAc =src.isAc;
        des.acdata =src.acdata;
        des.islearned =src.islearned;
        return des;
    }

    //设置配置
    public static void OpenConfig(Context context){
        selectIr = QDPreferenceManager.getInstance(context).geSelectIrPort();
        if(selectIr>3)CfgData.selectIr = 0;
        if(selectIr==2 && SysFun.IfHasIrDaPort(context)==false)
            selectIr = 0;
    }

    //根据遥控器ID，获取名称
    public static String getMougleName(int id){
        String ret = null;
        for (int i = 0; i < modellist.size(); i++) {
            if(modellist.get(i).id==id){
                ret = modellist.get(i).name;
                break;
            }
        }
        return ret;
    }

    public static String getDesTypeDesc(int types){
        String s="";
        switch (types){
            case 0:s=getString(R.string.str_type_noaudio);break;
            case 1:s=getString(R.string.str_type_hasaudio);break;
            case 2:s=getString(R.string.str_type_ble);break;
            case 3:s=getString(R.string.str_type_ac);break;
        }
        return s;
    }

    //分解服务器的设备列表
    public static boolean DoChkdevs(String src){
        try {
            devitems.clear();
            JSONArray jsonAll = new JSONArray(src);
            for(int i=0;i<jsonAll.length();i++){
                JSONObject jsonsingle = (JSONObject)jsonAll.get(i);
                String astr = jsonsingle.getString("dev").toUpperCase();
                if(!QMUILangHelper.isNullOrEmpty(astr))
                    devitems.add(astr);
            }
            return true;
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }


    private static String getStringFromObject(String s){
        if(s==null || s.length()==0){
            return "";
        }else return s;
    }
    //保存txt文件
    public static String getRemoteTxtFile(RemoteInfo aremote,List<BtnInfo> btnlist){
        String ret = "";
        switch (aremote.isAc) {
            case AcLear:
                ret += "FileFormat=PRCACTOOL\r\n";
                break;
            case AcPro:
                ret += "FileFormat=PRCACTOOLPRO\r\n";
                break;
            default:
                ret += "FileFormat=PRCTOOL\r\n";
                break;
        }
        ret += "Remote control descripe:  \r\n";
        ret += "keymap=universal\r\n";
        ret += "  Device="+getStringFromObject(aremote.dev)+"\r\n";
        ret += "  Brand="+getStringFromObject(aremote.pp)+"\r\n";
        ret += "  Model="+getStringFromObject(aremote.xh)+"\r\n";
        ret +="\r\n";
        if(btnlist==null)return ret;

        int btncount = 0;
        for (int i = 0; i < btnlist.size(); i++) {
            BtnInfo abtn = btnlist.get(i);
            if(aremote.isAc==AcLear) {
                if (abtn.wave==null) continue;
            }else{
                if (abtn.gsno < 0) continue;
                if (abtn.param16 == null || abtn.param16.length() == 0) continue;
            }
            btncount++;
        }
        if(aremote.isAc==AcPro)
            btncount = 1;
        ret += "Button's counts="+String.valueOf(btncount)+"\r\n";
        for (int i = 0; i < btnlist.size(); i++) {
            BtnInfo abtn = btnlist.get(i);
            if(aremote.isAc==AcLear) {
                if (abtn.wave==null) continue;
                //
                String stastr = "";
                for (int j = 0; j < abtn.params.length; j++) {
                    stastr +=Integer.toHexString(abtn.params[j]);
                }
                ret += stastr+"="+String.valueOf(abtn.keyidx) + " " + abtn.wave + "\r\n";
            }else{
                if (abtn.gsno < 0) continue;
                if (abtn.param16 == null || abtn.param16.length() == 0) continue;
                //
                String gsstr = String.valueOf(abtn.gsno);
                while (gsstr.length() < 4)
                    gsstr = "0" + gsstr;

                for (int j = 0; j < abtn.params.length; j++) {
                    gsstr += " " + Integer.toHexString(abtn.params[j]);
                }
                ret += String.valueOf(abtn.keyidx) + "," + abtn.btnname + "=" + gsstr + "\r\n";
            }
        }
        if(aremote.isAc==AcPro){
            ret += "Data="+ aremote.acdata + "\r\n";
        }
        ret +="\r\n";
        ret +=txtFile_End+"\r\n";
        return ret;
    }

    //获取遥控器的数据， 获取网址
    public static String getRmtUrl(int type,int id){
        String param = null;
        switch (type){
            case 0:
                param = "gbs_codes?code="+id;
                break;
            case 1:
                param = "ulcodelist?id="+ id + "&NOJSON=1";
                break;
            case 2:
                param = "rawcodelist?id="+ id+"&NOJSON=1";
                break;
            case 3:
                param = "accodelist?id="+ id+"&NOJSON=0";
                break;
            case 4:
                param = "philipcodes?id="+ id+"&NOJSON=1";
                break;
            case 5:
                param = "getChinaCodes?id="+ id +"&NOJSON=1";
                break;
        }
        return param;
    }

    public static boolean SaveFile(Bitmap bm,String filename){
        try {
            File path = new File(sdcard_paht);
            //文件
            final String filepath = sdcard_paht + filename;
            final File file = new File(filepath);
            if (!path.exists()) {
                path.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fos = null;
            fos = new FileOutputStream(file);
            if (null != fos) {
                bm.compress(Bitmap.CompressFormat.PNG, 0, fos);
                fos.flush();
                fos.close();
                return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    //保存按键字符串，提交给服务器 编码
    public static String getButtonsString(List<IrButton> buttons){
        String s ="";
        for (int i = 0; i < buttons.size(); i++) {
            IrButton abtn = buttons.get(i);
            s+=abtn.getPage()+" "+abtn.getSnumber() +" "+ String.valueOf(abtn.getProtocol())+" ";
            if(abtn.getProtocol()>=0) {
                int[] params = abtn.getParams();
                if (params == null) {
                    return null;
                }
                for (int j = 0; j < params.length; j++) {
                    s += String.valueOf(params[j]) + " ";
                }
            }else{
                int[] wave = abtn.getWave();
                if (wave == null)
                    continue;
                if (wave.length < 3) {
                    return null;
                }
                s +=String.valueOf(abtn.getFreq()) + " ";
                for (int j = 0; j < wave.length; j++) {
                    s += String.valueOf(wave[j]) + " ";
                }
            }
            s+="\r\n";
        }
        return s;
    }

    public static void getDataVersion(Context context,boolean clearche){
        int olddata = CfgData.dataidx;
        int d = QDPreferenceManager.getInstance(context).getDataSet();
        if (d<0){
            CfgData.dataidx = 0 ;
            TimeZone azone = TimeZone.getDefault();
            String  strid = azone.getID();
            if(strid.contains("Shanghai")){  //Asia/Shanghai
                CfgData.dataidx = 4;
            }else if(strid.contains("America")){
                CfgData.dataidx = 3;
            }
        }else{
            CfgData.dataidx = 0;
            switch (d){
                case 1:
                    CfgData.dataidx = 3;
                    break;
                case 2:
                    CfgData.dataidx = 4;
                    break;
            }
        }
        if(olddata!=CfgData.dataidx && clearche){
            BaseApplication.getMyApplication().ClearRemoteCache();
        }
    }
}
