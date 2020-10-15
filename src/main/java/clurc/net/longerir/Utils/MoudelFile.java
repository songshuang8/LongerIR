package clurc.net.longerir.Utils;

import android.content.Context;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import clurc.net.longerir.data.BM_ModelData;
import clurc.net.longerir.ircommu.DesRemote;
import clurc.net.longerir.ircommu.DesRemoteBtn;
/*   512
  TMyModelTitle = record
          Counts:Integer;
          customName:string[255];
          modelver:word;
          memo:string[63];
          date:TDateTime;
          ReSved:array[0..170] of byte;
          end;
          */
/*
  TMyModelRemote = packed record   2733
    key:array[0..110] of TMyModelkeyV1;   // 23*111    2553
    chipkind:byte //2554

    descr:string[24];                25

    hide:boolean;
    keycouts:byte;
    id:DWORD;
    PagesCount:byte;                      7     2554+25+7
    PageName:array[0..7] of string[15];  //16*8== 128

    jpgsize:dword;   //135
    progrmmType:byte;  //135+4
    hasShift:boolean;
    tvCount:byte;                     7
    //  14+128 = 142
    gdcodeCount:dword;
    pidCount:dword;                8
    allsize:dword;// jpgsezie+ 4*gdcodecount + 2*pidCount
  end;
 */

public class MoudelFile {
    public static String[] getMoudlePage(Context context,int index,List<ModelStru> src) {
        String[] result = null;
        try {
            InputStream in = context.getResources().getAssets().open("model.RMF");
            int count = readmyint(in);
            if(count==0) return result;
            in.skip(512-4);
            for (int i = 0; i < src.size(); i++) {
                if (index == i) {
                    in.skip(2585);  //2554 +25 +6
                    int pagecount = in.read();
                    if(pagecount<0)pagecount=0;
                    result = new String[pagecount];
                    for (int j = 0; j < pagecount; j++) {
                        int strlen = in.read();
                        byte[] buffer = new byte[strlen];
                        in.read(buffer);
                        result[j] = new String(buffer,"UTF-8");
                        in.skip(15-strlen);
                    }
                    break;
                }else {
                    in.skip(2733);
                    in.skip(src.get(i).allsize);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static List<ModelStru> getMoudleArr(Context context) {
        List<ModelStru> ret=new ArrayList<ModelStru>();//当前过滤的
        try {
            InputStream in = context.getResources().getAssets().open("model.RMF");
            int count = readmyint(in);
            if(count==0) return ret;
            in.skip(512-4);
            for (int i = 0; i < count; i++) {
                ModelStru astru = new ModelStru();
                in.skip(2553);
                astru.chip = in.read();
                int strlen = in.read();
                byte[] buffer = new byte[strlen];
                in.read(buffer);
                astru.name = new String(buffer,"UTF-8");
                in.skip(24-strlen);
                //
                in.skip(1);
                astru.keycount = in.read();
                astru.id = readmyshort(in);
                //
                in.skip(131);
                astru.jpgsize = readmyint(in);
                astru.prgtype = in.read();
                int atemp = in.read();
                astru.hasShift = (atemp==1)?true:false;

                in.skip(9); //142+8
                astru.allsize = readmyint(in);
                in.skip(astru.allsize);
                ret.add(astru);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static byte[] getMoudleJpg(Context context,int index,List<ModelStru> src) {
        byte[] result = null;
        try {
            InputStream in = context.getResources().getAssets().open("model.RMF");
            int count = readmyint(in);
            if(count==0) return result;
            in.skip(512-4);
            for (int i = 0; i < src.size(); i++) {
                in.skip(2733);
                if (index == i) {
                    result = new byte[src.get(i).jpgsize];
                    in.read(result);
                    break;
                }else {
                    in.skip(src.get(i).allsize);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /*
  TMyModelRemote = packed record   2733
    key:array[0..110] of TMyModelkeyV1;   // 23*111    2553
    chipkind:byte //2554

    descr:string[24];                25

    hide:boolean;
    keycouts:byte;
    id:DWORD;
    PagesCount:byte;                      7     2554+25+7
    PageName:array[0..7] of string[15];  //16*8== 128

    jpgsize:dword;   //135
    progrmmType:byte;  //135+4
    hasShift:boolean;
    tvCount:byte;                     7
    //  14+128 = 142
    gdcodeCount:dword;
    pidCount:dword;                8
    allsize:dword;// jpgsezie+ 4*gdcodecount + 2*pidCount
  end;

    TMyModelkeyV1 = packed record
    keyName:string[15];
    S:Byte;
    KeyIdx:word;
    isGud:Boolean;  //跟随哪个s号的编码
    gudSNumber:Byte;
    ChangeID:Byte;
    isstudy:boolean;
    end;
    */
    public static List<DesRemoteBtn> GetBtns(Context context, int index, List<ModelStru> src) {
        List<DesRemoteBtn> result = new ArrayList<DesRemoteBtn>();
        try {
            InputStream in = context.getResources().getAssets().open("model.RMF");
            int count = readmyint(in);
            if(count==0) return result;
            in.skip(512-4);
            for (int i = 0; i < src.size(); i++) {
                if (index == i) {
                    int keyccount = src.get(i).keycount;
                    for (int j = 0; j < keyccount; j++) {
                        DesRemoteBtn abtn = new DesRemoteBtn();
                        in.skip(16);
                        abtn.s = in.read();
                        abtn.keyidx = readmyshort(in) - 1;
                        in.skip(3);
                        int isstudy = in.read();
                        if (isstudy == 0)
                            result.add(abtn);
                    }
                    break;
                }else {
                    in.skip(2733);
                    in.skip(src.get(i).allsize);
                }
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
//        public int idx;
        public int keycount;
        public String name;
        public int prgtype;
        public boolean hasShift;
        public int jpgsize;
        public int allsize;
        public int id;
        public int chip;

        public ModelStru() {
        }
    }
}
