package clurc.net.longerir.adapt;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import clurc.net.longerir.R;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.RemoteInfo;
import clurc.net.longerir.data.modeldata.DataModelBtnInfo;

public class DevicesItemAdapter extends BaseAdapter {
    private Context context = null;
    private LayoutInflater mInflater;
    private List<MyremoteListClass> devremotelist;
    private String filterstr;
    private boolean isearch;

    public DevicesItemAdapter(Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        devremotelist=new ArrayList<MyremoteListClass>();
        isearch = false;
        prePreData();
    }

    @Override
    public int getCount() {
        int ret = 0;
        if(isearch){
            ret = devremotelist.size();
        }else {
            for (int i = 0; i < devremotelist.size(); i++) {
                if (devremotelist.get(i).istitle) {
                    ret++;
                } else {
                    if (devremotelist.get(i).iszhedie == false)
                        ret++;
                }
            }
        }
        return ret;
    }

    @Override
    public Object getItem(int position) {
        Object abj = null;
        if (position < CfgData.myremotelist.size())
            abj = CfgData.myremotelist.get(position);
        return abj;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public MyremoteListClass getCurrItemObj(int p){
        if(isearch){
            if(p>=0 && p<devremotelist.size())
                return devremotelist.get(p);
            else
                return null;
        }else {
            int n = 0;
            for (int i = 0; i < devremotelist.size(); i++) {
                if (devremotelist.get(i).istitle) {
                    n++;
                } else {
                    if (devremotelist.get(i).iszhedie == false)
                        n++;
                }
                if ((n - 1) == p) {
                    return devremotelist.get(i);
                }
            }
        }
        return null;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if(isearch) {
            RemoteInfo adev = devremotelist.get(position).remote;
            convertView = mInflater.inflate(R.layout.listview_myremote, parent, false);
            ((TextView) convertView.findViewById(R.id.tvdev)).setText(adev.dev);
            ((TextView) convertView.findViewById(R.id.tvbrand)).setText(adev.pp);
            ((TextView) convertView.findViewById(R.id.tvxh)).setText(adev.xh);
            if((position % 2)==0){
                convertView.setBackgroundColor(Color.WHITE);
            }else{
                convertView.setBackgroundColor(context.getResources().getColor(R.color.infobak));
            }
        }else {
            MyremoteListClass abj = getCurrItemObj(position);
            if (abj == null) return convertView;
            if (abj.istitle) {
                convertView = mInflater.inflate(R.layout.listview_title, parent, false);
                ((TextView) convertView.findViewById(R.id.tvdev)).setText(abj.devName);
                if (abj.iszhedie)
                    ((ImageView) convertView.findViewById(R.id.imageView2)).setImageResource(R.mipmap.drop_up);
            } else {
                convertView = mInflater.inflate(R.layout.listview_myremote_fenlei, parent, false);
                RemoteInfo adev = abj.remote;
                ((TextView) convertView.findViewById(R.id.tvxh)).setText(adev.xh);
                if((abj.devidx % 2)==0){
                    convertView.setBackgroundColor(Color.WHITE);
                }else{
                    convertView.setBackgroundColor(context.getResources().getColor(R.color.infobak));
                }
                ImageView img = convertView.findViewById(R.id.imageView2);
                if (abj.isfav) {
                    img.setImageResource(R.mipmap.faved);
                    ((TextView) convertView.findViewById(R.id.tvbrand)).setText(adev.pp+'/'+adev.dev);
                }else{
                    ((TextView) convertView.findViewById(R.id.tvbrand)).setText(adev.pp);
                }

                img.setTag(abj.idx);
                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int x = (int)v.getTag();
                        RemoteInfo r = CfgData.myremotelist.get(x);
                        r.fav = !r.fav;
                        CfgData.MyRemoteSaveFav(context,r);
                        ChangeInvalid();
                    }
                });
            }
        }
        return convertView;
    }

    private boolean ManzuFilter(String f,RemoteInfo adev){
        if(f==null)return true;
        if(f.length()==0)return true;
        if (adev.dev!=null)
            if(adev.dev.contains(f)){
                return true;
            }
        if (adev.pp!=null)
            if(adev.pp.contains(f)){
                return true;
            }
        if (adev.xh!=null)
            if(adev.xh.contains(f)){
                return true;
            }
        return false;
    }

    public void prePreData(){
        devremotelist.clear();
        if(isearch){
            for (int i = 0; i < CfgData.myremotelist.size(); i++) {
                RemoteInfo adev = CfgData.myremotelist.get(i);
                if(ManzuFilter(filterstr,adev)) {
                    MyremoteListClass abj = new MyremoteListClass(false);
                    abj.remote = adev;
                    abj.idx = i;
                    devremotelist.add(abj);
                }
            }
        }else{
            boolean ishavefav = false;
            List<MyremoteListClass> tempremote = new ArrayList<MyremoteListClass>();
            for (int i = 0; i < CfgData.myremotelist.size(); i++) {
                RemoteInfo adev = CfgData.myremotelist.get(i);
                if(adev.fav) {
                    ishavefav = true;
                    continue;
                }
                MyremoteListClass abj = new MyremoteListClass(false);
                abj.remote = adev;
                abj.idx = i;
                tempremote.add(abj);
            }
            Collections.sort(tempremote, new Comparator<MyremoteListClass>() {
                @Override
                public int compare(MyremoteListClass o1, MyremoteListClass o2) {
                    if (o1.remote.dev == null && o2.remote.dev != null)
                        return 1;
                    else if (o1.remote.dev != null && o2.remote.dev == null)
                        return -1;
                    return o1.remote.dev.compareToIgnoreCase(o2.remote.dev);
                }
            });
            int k = 0;
            List<String> devlist = new ArrayList<String>();

            if(ishavefav) {
                MyremoteListClass abj = new MyremoteListClass(true);
                abj.devName = context.getString(R.string.str_favourite);
                abj.idx = devremotelist.size();
                devlist.add(abj.devName);
                devremotelist.add(abj);

                for (int i = 0; i < CfgData.myremotelist.size(); i++) {
                    RemoteInfo adev = CfgData.myremotelist.get(i);
                    if(adev.fav) {
                        abj = new MyremoteListClass(false);
                        abj.remote = adev;
                        abj.idx = i;
                        abj.isfav = true;
                        devremotelist.add(abj);
                    }
                }
            }
            for (int i = 0; i < tempremote.size(); i++) {
                RemoteInfo adev = tempremote.get(i).remote;

                if (devlist.indexOf(adev.dev)<0) {
                    MyremoteListClass abj = new MyremoteListClass(true);
                    if(adev.dev==null){
                        abj.devName = "";
                    }else {
                        abj.devName = adev.dev;
                    }
                    abj.idx = devremotelist.size();
                    devlist.add(abj.devName);
                    devremotelist.add(abj);
                    //-------------------
                    abj = tempremote.get(i);
                    abj.devidx = 0;
                    devremotelist.add(abj);
                    k = 0;
                }else{
                    MyremoteListClass abj = tempremote.get(i);
                    abj.devidx = ++k;
                    devremotelist.add(abj);
                }
            }
        }
    }

    public void setFilterstr(String filterstr) {
        this.filterstr = filterstr;
        isearch = true;
        prePreData();
        notifyDataSetChanged();
    }

    public void setIsearch(boolean isearch) {
        this.isearch = isearch;
        filterstr = "";
        prePreData();
        notifyDataSetChanged();
    }

    public class MyremoteListClass {
        public boolean istitle;
        public int idx; //myremote的序号
        public String devName;
        public boolean iszhedie;
        public RemoteInfo remote;
        public int devidx; //该设备下的序号
        public boolean isfav;
        public MyremoteListClass(boolean b){
            istitle = b;
            idx = 0;
            devidx = 0;
            devName = null;
            remote = null;
            isfav = false;
        }
    }

    public void setZhedie(int p){
        MyremoteListClass mtitle = devremotelist.get(p);
        mtitle.iszhedie = !mtitle.iszhedie;
        for (int i = p+1; i < devremotelist.size(); i++) {
            MyremoteListClass abj = devremotelist.get(i);
            if(abj.istitle)break;
            abj.iszhedie = mtitle.iszhedie;
        }
        notifyDataSetChanged();
    }

    public void ChangeInvalid(){
        prePreData();
        notifyDataSetChanged();
    }
}
