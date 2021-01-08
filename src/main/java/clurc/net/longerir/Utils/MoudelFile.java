package clurc.net.longerir.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.qmuiteam.qmui.util.QMUILangHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import clurc.net.longerir.R;
import clurc.net.longerir.data.BM_ModelData;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.modeldata.DataModelBtnInfo;
import clurc.net.longerir.ircommu.DesRemote;
import clurc.net.longerir.ircommu.DesRemoteBtn;

import static clurc.net.longerir.uicomm.SsSerivce.TAG_SS;

public class MoudelFile {
    public static int Con_Ac_Chip = 11;
    public static String getModeFile(Context context){
        return context.getFilesDir().getAbsolutePath() + "/remotemod";
    }
    public static boolean saveFile(Context context,byte[] bytes){
        try {
            String filename = MoudelFile.getModeFile(context);
            OutputStream os = new FileOutputStream(filename);
            os.write(bytes);
            os.close();
            os.flush();
            return true;
        }catch (Exception e){

        }
        return false;
    }
    public static boolean isExist(Context context){
        String filename = getModeFile(context);
        File modfile = new File(filename);
        if(!modfile.exists())
            Log.w(TAG_SS,"===>model file not exist ");
        return modfile.exists();
    }

    public static List<ModelStru> getMoudleArr(Context context) {
        List<ModelStru> ret=new ArrayList<ModelStru>();//当前过滤的
        if(isExist(context)==false){
            return ret;
        }
        try {
            String filename = MoudelFile.getModeFile(context);
            JSONArray jsonAll = new JSONArray(CfgData.getStringFromFile(filename));
            for(int i=0;i<jsonAll.length();i++){
                JSONObject jsonsingle = (JSONObject)jsonAll.get(i);
                ModelStru astru = new ModelStru();
                astru.chip = jsonsingle.getInt("ChipKind");
                astru.name = jsonsingle.getString("ModelType");
                //astru.keycount = jsonsingle.getInt("ChipKind");
                astru.id = jsonsingle.getInt("ModelID");
                astru.prgtype = jsonsingle.getInt("PrgType");
                astru.hasShift = (jsonsingle.getInt("HasShift")==1)?true:false;
                astru.guest = jsonsingle.getInt("guest");
                astru.pri = (jsonsingle.getInt("pri")==1)?true:false;
                JSONArray jpage = (JSONArray)jsonsingle.get("PageName");
                astru.pageName = new String[jpage.length()];
                astru.imageCount = jsonsingle.getInt("ImageCount");
                for (int j = 0; j < jpage.length(); j++) {
                    astru.pageName[j] = jpage.getString(j);
                }
                ret.add(astru);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static List<DesRemoteBtn> GetBtns(Context context, int index) {
        List<DesRemoteBtn> result = new ArrayList<DesRemoteBtn>();
        if(isExist(context)==false){
            return result;
        }
        try {
            String filename = MoudelFile.getModeFile(context);
            JSONArray jsonAll = new JSONArray(CfgData.getStringFromFile(filename));
            if(index>=jsonAll.length())return result;

            JSONObject jsonsingle = (JSONObject)jsonAll.get(index);
            JSONArray jkeys = (JSONArray)jsonsingle.get("btnInfo");
            for (int i = 0; i < jkeys.length(); i++) {
                DesRemoteBtn abtn = new DesRemoteBtn();
                JSONObject jabtns = (JSONObject)jkeys.get(i);
                abtn.s = jabtns.getInt("KNo");
                abtn.keyidx = jabtns.getInt("KeyIdx");
                abtn.name = jabtns.getString("Name");
                abtn.follow = jabtns.getInt("Folow");
                abtn.isstudy = (jabtns.getInt("isstudy")==1)?true:false;
                abtn.col = jabtns.getInt("Col");
                abtn.row = jabtns.getInt("Row");
                result.add(abtn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static List<DataModelBtnInfo> GetMBtns(Context context, int index, boolean hasshift) {
        List<DataModelBtnInfo> result = new ArrayList<DataModelBtnInfo>();
        if(isExist(context)==false){
            return result;
        }
        try {
            String filename = MoudelFile.getModeFile(context);
            JSONArray jsonAll = new JSONArray(CfgData.getStringFromFile(filename));
            if(index>=jsonAll.length())return result;

            JSONObject jsonsingle = (JSONObject)jsonAll.get(index);
            int mShift = jsonsingle.getInt("HasShift");
            JSONArray jkeys = (JSONArray)jsonsingle.get("btnInfo");
            int btnscount = jkeys.length();
            if(hasshift==false && mShift==1)
                btnscount /= 2;
            for (int i = 0; i < btnscount; i++) {
                DataModelBtnInfo abtn = new DataModelBtnInfo();
                JSONObject jabtns = (JSONObject)jkeys.get(i);
                abtn.sid = jabtns.getInt("KNo");
                abtn.keyidx = jabtns.getInt("KeyIdx");
                abtn.btnname = jabtns.getString("Name");
                abtn.kinds = 0;
                abtn.cols = jabtns.getInt("Col");
                abtn.rows = jabtns.getInt("Row");
                result.add(abtn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static int readmyint(InputStream in) throws Exception{
        int b0 = in.read();
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        return (int)(b0 | b1<<8 | b2<<16 | b3<<24);
    }
    private static int readmyshort(InputStream in) throws Exception{
        int b0 = in.read();
        int b1 = in.read();
        return (int)(b0 | b1<<8);
    }

    public static class ModelStru{
        public String name;
        public int prgtype;
        public boolean hasShift;
        public int id;
        public int chip;
        public String[] pageName;
        public int imageCount;
        public int guest;
        public boolean pri;

        public ModelStru() {
        }
    }
}
