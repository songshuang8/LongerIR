package clurc.net.longerir.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.qmuiteam.qmui.util.QMUILangHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import clurc.net.longerir.Utils.MoudelFile;
import clurc.net.longerir.data.modeldata.DataModelBtnInfo;
import clurc.net.longerir.data.modeldata.DataModelInfo;
import clurc.net.longerir.view.RemoteBtnView;

public class BtnModelData {
    public static String mymodelfile = "/model.db";

    private static void CreatemyTable(SQLiteDatabase mSQLiteDatabase){
        String sql= "create table if not exists mymodel(id integer PRIMARY KEY AUTOINCREMENT,strdesc text,colcnt int,issys int)";
        mSQLiteDatabase.execSQL(sql);
        sql= "create table if not exists modelbtnlist(id integer PRIMARY KEY AUTOINCREMENT,pid int,keyidx int,sid int,btnname text,cols int,rows int,kinds int)";
        mSQLiteDatabase.execSQL(sql);
    }

    public static List<DataModelInfo> readMyModels(Context context){
        List<DataModelInfo> ret=new ArrayList<DataModelInfo>();
        String basefile=context.getFilesDir().getAbsolutePath()+mymodelfile;
        SQLiteDatabase mSQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(basefile,null);
        CreatemyTable(mSQLiteDatabase);
        Cursor cursor =mSQLiteDatabase.rawQuery("select * from mymodel", null);
        if(cursor!=null) {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    DataModelInfo ainfo = new DataModelInfo();
                    ainfo.id = cursor.getInt(cursor.getColumnIndex("id"));
                    ainfo.strdesc = cursor.getString(cursor.getColumnIndex("strdesc"));
                    ainfo.colcnt = cursor.getInt(cursor.getColumnIndex("colcnt"));
                    ainfo.issys = cursor.getInt(cursor.getColumnIndex("issys"));
                    ret.add(ainfo);
                }
            }
            cursor.close();
        }
        mSQLiteDatabase.close();
        return ret;
    }

    public static DataModelInfo readAMyModel(Context context,int id){
        DataModelInfo ret=new DataModelInfo();
        String  basefile=context.getFilesDir().getAbsolutePath()+mymodelfile;
        SQLiteDatabase mSQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(basefile,null);
        CreatemyTable(mSQLiteDatabase);
        Cursor cursor =mSQLiteDatabase.rawQuery("select * from mymodel where id="+id, null);
        if(cursor!=null) {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    ret.id = cursor.getInt(cursor.getColumnIndex("id"));
                    ret.strdesc = cursor.getString(cursor.getColumnIndex("strdesc"));
                    ret.colcnt = cursor.getInt(cursor.getColumnIndex("colcnt"));
                    break;
                }
            }
            cursor.close();
        }
        mSQLiteDatabase.close();
        return ret;
    }

    public static List<DataModelBtnInfo> getBtnInfo(Context context, int id){
        List<DataModelBtnInfo> ret = new ArrayList<DataModelBtnInfo>();
        String basefile=context.getFilesDir().getAbsolutePath()+mymodelfile;
        SQLiteDatabase mSQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(basefile,null);
        CreatemyTable(mSQLiteDatabase);
        Cursor cursor =mSQLiteDatabase.rawQuery("select * from modelbtnlist where pid="+id, null);
        if(cursor!=null) {
            if (cursor.getCount() > 0) {//,mid int,btnname text,gsno int,param text,col int,rows int,img text)";
                while (cursor.moveToNext()) {
                    DataModelBtnInfo abtn = new DataModelBtnInfo();
                    abtn.keyidx = cursor.getInt(cursor.getColumnIndex("keyidx"));
                    abtn.sid = cursor.getInt(cursor.getColumnIndex("sid"));
                    abtn.btnname = cursor.getString(cursor.getColumnIndex("btnname"));
                    abtn.cols = cursor.getInt(cursor.getColumnIndex("cols"));
                    abtn.rows = cursor.getInt(cursor.getColumnIndex("rows"));
                    abtn.kinds = cursor.getInt(cursor.getColumnIndex("kinds"));
                    ret.add(abtn);
                }
            }
            cursor.close();
        }
        mSQLiteDatabase.close();
        return ret;
    }

    public static  int getMaxCols(List<DataModelBtnInfo> btns){
        int ret = 0;
        for (int i = 0; i < btns.size(); i++) {
            DataModelBtnInfo v = btns.get(i);
            if(v.cols>ret){
                ret = v.cols;
            }
        }
        ret++;
        return ret;
    }

    //当前行个数等于总列
    private static int get_ARowCnt(List<DataModelBtnInfo> btns,int arow){
        int nowrow = 0;
        for (int i = 0; i < btns.size(); i++) {
            DataModelBtnInfo v = btns.get(i);
            if(v.rows==arow){
                nowrow++;
            }
        }
        return nowrow;
    }

    public static void xiuzhengButtons(List<DataModelBtnInfo> btns,int col_int){
        int maxrow = 0;
        for (int i = 0; i < btns.size(); i++) {
            if(btns.get(i).rows>maxrow)
                maxrow = btns.get(i).rows;
        }
        maxrow++;
        for (int i = 0; i < maxrow; i++) {
            int arowCnt = get_ARowCnt(btns,i);
            if(arowCnt==col_int){
                continue;
            }
            //
            List<DataModelBtnInfo> tempbtns = new ArrayList<DataModelBtnInfo>();
            for (int j = 0; j < btns.size(); j++) {
                if(btns.get(j).rows==i)
                    tempbtns.add(btns.get(j));
            }
            Collections.sort(tempbtns,new Comparator<DataModelBtnInfo>() {
                public int compare(DataModelBtnInfo obj1, DataModelBtnInfo obj2) {
                    return obj1.cols - obj2.cols;
                }
            });
            for (int j = 0; j < tempbtns.size(); j++) {
                tempbtns.get(j).cols=j;
            }
        }
    }

    //保存新建学习的数据到磁盘{
    public  static  boolean SaveLearMyFile(Context context,DataModelInfo amodel){
        xiuzhengButtons(amodel.btns,amodel.colcnt);
        String basefile=context.getFilesDir().getAbsolutePath()+mymodelfile;
        SQLiteDatabase mSQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(basefile,null);
        CreatemyTable(mSQLiteDatabase);

        ContentValues values = new ContentValues();
        values.put("strdesc", amodel.strdesc);
        values.put("colcnt", amodel.colcnt);
        if(amodel.id>=0){
            mSQLiteDatabase.update("mymodel", values,"id=?",new String[]{String.valueOf(amodel.id)});
        }else {
            amodel.id = (int)mSQLiteDatabase.insert("mymodel", null, values);
        }
        if(amodel.id<0)return false;
        //---------save info database----
        mSQLiteDatabase.execSQL("delete from modelbtnlist where pid="+amodel.id);
        for (int i = 0; i < amodel.btns.size(); i++) {
            DataModelBtnInfo abtn = (DataModelBtnInfo)amodel.btns.get(i);
            values = new ContentValues();
            values.put("pid", amodel.id);
            values.put("btnname", abtn.btnname);
            values.put("keyidx", abtn.keyidx);
            values.put("sid", abtn.sid);
            values.put("rows", abtn.rows);
            values.put("cols", abtn.cols);
            values.put("kinds", abtn.kinds);
            mSQLiteDatabase.insert("modelbtnlist", null, values);
        }
        mSQLiteDatabase.close();
        //-------
        return true;
    }
    // 从下载的mod文件抽取保存到用户模板里面
    public  static  void SaveMdFromMoudel(Context context){
        if(CfgData.modellist==null){
            return;
        }

        String basefile=context.getFilesDir().getAbsolutePath()+mymodelfile;
        SQLiteDatabase mSQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(basefile,null);
        CreatemyTable(mSQLiteDatabase);
        Cursor cursor =mSQLiteDatabase.rawQuery("select * from mymodel", null);
        if(cursor!=null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                mSQLiteDatabase.close();
                return;
            }else
                cursor.close();
        }

        for (int i = 0; i < CfgData.modellist.size(); i++) {
            MoudelFile.ModelStru title = CfgData.modellist.get(i);
            if(title.chip == MoudelFile.Con_Ac_Chip)continue; //去除空调
            //不要shift
            List<DataModelBtnInfo> MBtns = MoudelFile.GetMBtns(context,i,false);
            int colcnt = getMaxCols(MBtns);
            if(colcnt<2)colcnt= 3; //数据会有错误
            xiuzhengButtons(MBtns,colcnt);
//
            ContentValues values = new ContentValues();
            values.put("strdesc", title.name);
            values.put("colcnt", colcnt);
            int id = (int)mSQLiteDatabase.insert("mymodel", null, values);
            if(id<0)continue;
            //---------save info database----
            //mSQLiteDatabase.execSQL("delete from modelbtnlist where pid="+amodel.id);
            for (int j = 0; j < MBtns.size(); j++) {
                DataModelBtnInfo abtn = (DataModelBtnInfo)MBtns.get(j);
                values = new ContentValues();
                values.put("pid", id);
                values.put("btnname", abtn.btnname);
                values.put("keyidx", abtn.keyidx);
                values.put("sid", abtn.sid);
                values.put("rows", abtn.rows);
                values.put("cols", abtn.cols);
                values.put("kinds", abtn.kinds);
                mSQLiteDatabase.insert("modelbtnlist", null, values);
            }
        }
        mSQLiteDatabase.close();
    }

    public static void DelAMyModel(Context context,int id){
        DataModelInfo ret=new DataModelInfo();
        String basefile=context.getFilesDir().getAbsolutePath()+mymodelfile;
        SQLiteDatabase mSQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(basefile,null);
        CreatemyTable(mSQLiteDatabase);
        mSQLiteDatabase.execSQL("delete from modelbtnlist where pid="+id);
        mSQLiteDatabase.execSQL("delete from mymodel where id="+id);
        mSQLiteDatabase.close();
    }
}
