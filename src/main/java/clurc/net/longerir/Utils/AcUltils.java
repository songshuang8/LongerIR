package clurc.net.longerir.Utils;

import android.content.Context;
import android.widget.ListView;

import clurc.net.longerir.R;

public class AcUltils {
    public static int[][] rid = {
            {R.id.tvbtn00,R.id.tvbtn01,R.id.tvbtn02},  //pow
            {R.id.tvbtn10},  //temp
            {R.id.tvbtn20,R.id.tvbtn21,R.id.tvbtn22,R.id.tvbtn23,R.id.tvbtn24,R.id.tvbtn25}, //mode
            {R.id.tvbtn30,R.id.tvbtn31,R.id.tvbtn32,R.id.tvbtn33,R.id.tvbtn34}, //fan
            {R.id.tvbtn40,R.id.tvbtn41,R.id.tvbtn42}, //swing
            {R.id.tvbtn50,R.id.tvbtn51,R.id.tvbtn52,R.id.tvbtn53,R.id.tvbtn54,R.id.tvbtn55,R.id.tvbtn56}, //vane
            {R.id.tvbtn60,R.id.tvbtn61,R.id.tvbtn62}, //strong
            {R.id.tvbtn70,R.id.tvbtn71,R.id.tvbtn72}, //agu
    };
    private static int[][] btnstaus_nameid= {
            {R.string.str_ac_01,R.string.str_ac_02},
            {},
            {R.string.str_ac_21,R.string.str_ac_22,R.string.str_ac_23,R.string.str_ac_24,R.string.str_ac_25},
            {R.string.str_ac_31,R.string.str_ac_32,R.string.str_ac_33,R.string.str_ac_34},
            {R.string.str_ac_41,R.string.str_ac_42},
            {R.string.str_ac_51,R.string.str_ac_52,R.string.str_ac_53,R.string.str_ac_54,R.string.str_ac_55,R.string.str_ac_56},
            {R.string.str_ac_61,R.string.str_ac_62},
            {R.string.str_ac_71,R.string.str_ac_72}
    };
    public static int[] btnmax = {2,15,5,4,2,6,2,2};
    public static String getStatusName(Context context,int btn, int sta){
        String ret = "";
        if(btn==1){
            ret = String.valueOf(sta+16);
        }else{
            ret = context.getResources().getString(btnstaus_nameid[btn][sta]);
        }
        return ret;
    }

    public static int getUiCol(int dcol){
        if(dcol==0)
            return 0;
        else
        if(dcol==1)
            return 7;
        else
            return dcol-1;
    }
    public static int getDCol(int uicol){
        if(uicol==0)
            return 0;
        else
        if(uicol==7)
            return 1;
        else
            return uicol+1;
    }

    public static void SetListViewPos(ListView lv, int pos){
        int apos = pos;
        if(apos==-1)apos = lv.getCount()-1;
        if(lv.getCount()==0)return;
        if(apos>(lv.getCount()-1))return;
        int first = lv.getFirstVisiblePosition();
        int last = lv.getLastVisiblePosition();
        if(apos>=first && apos<=last){
            return;
        }
        lv.setSelection(apos);
    }
}
