package clurc.net.longerir.adapt;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import clurc.net.longerir.R;
import clurc.net.longerir.Utils.AcUltils;
import clurc.net.longerir.data.BtnInfo;

import static clurc.net.longerir.uicomm.SsSerivce.TAG_SS;

public class AcRecordAdapt extends BaseAdapter {
    private int[] rcd  = {R.id.lab1,R.id.lab2,R.id.lab3,R.id.lab4,R.id.lab5,R.id.lab6,R.id.lab7,R.id.lab8};
    private Context context;
    private LayoutInflater mInflater;
    private int currpos=-1;
    private List<BtnInfo> btnslist;
    private int[] sta;

    public AcRecordAdapt(Context context, List<BtnInfo> btnslist,int[] sta) {
        this.context = context;
        this.btnslist = btnslist;
        this.sta = sta;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public Object getItem(int position) {
        return null;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        if(currpos<0)
            return btnslist.size()+1;
        else
            return btnslist.size();
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = mInflater.inflate(R.layout.activity_learn_ac_record, parent, false);

        TextView v = (TextView) convertView.findViewById(R.id.labline);
        if (position >= btnslist.size())
            v.setText("**");
        else
            v.setText(getTwoIntString(position+1));
        SetTextColor(v,position,position>=btnslist.size());

        for (int i = 0; i < rcd.length; i++) {
            TextView tv = (TextView) convertView.findViewById(rcd[i]);
            int dcol = AcUltils.getDCol(i);

            String s;
            if (position >= btnslist.size()) {
                if (sta[0] == 0 && dcol > 0)
                    s = "-";
                else {
                    Log.w(TAG_SS,"===>get btn name:"+dcol+":"+sta[dcol]);
                    s = AcUltils.getStatusName(context, dcol, sta[dcol]);
                }
            } else {
                BtnInfo abtn = btnslist.get(position);
                if (abtn.params[0] == 0 && dcol > 0)
                    s = "-";
                else {
                    Log.w(TAG_SS,"===>get btn name2:"+dcol+":"+":"+abtn.params[dcol]);
                    s = AcUltils.getStatusName(context, dcol, abtn.params[dcol]);
                }
            }
            tv.setText(s);
            SetTextColor(tv, position,position>=btnslist.size());
        }

        return convertView;
    }

    public void SetCurrent(int[] asta){
        SetCurrentDr(getNowRecrdBySta(asta));
    }

    public void SetCurrentDr(int idx){
        if(currpos!=idx){
            currpos = idx;
            notifyDataSetChanged();
        }else if(currpos<0){
            notifyDataSetChanged();
        }
    }

    public int getCurrpos() {
        return currpos;
    }

    private String getTwoIntString(int p){
        if(p>9){
            return String.valueOf(p);
        }else
            return "0"+p;
    }

    private void SetTextColor(TextView tv,int pos,boolean isnew){
        GradientDrawable gd = new GradientDrawable();//创建drawable
        if(isnew){
            gd.setColor(context.getResources().getColor(R.color.app_color_theme_4));
        }else
        if(currpos==pos) {
            gd.setColor(context.getResources().getColor(R.color.app_color_theme_6));
        }else {
            gd.setColor(Color.WHITE);
        }
        gd.setCornerRadius(0);
        gd.setStroke(1, context.getResources().getColor(R.color.app_color_theme_1));
        tv.setBackground(gd);
    }

    public int getNowRecrdBySta(int[] asta){
        int sel=-1;
        for (int i = 0; i < btnslist.size(); i++) {
            boolean b = true;
            for (int j = 0; j < asta.length; j++) {
                if(btnslist.get(i).params[j]!=asta[j]){
                    b = false;
                    break;
                }
            }
            if(b){
                sel = i;
                break;
            }
        }
        return sel;
    }
}
