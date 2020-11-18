package clurc.net.longerir.adapt;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
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
    private List<MyremoteListClass> myremotelistsearc;
    private String filterstr;

    public DevicesItemAdapter(Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        myremotelistsearc=new ArrayList<MyremoteListClass>();
        setFilterstr("");
    }

    @Override
    public int getCount() {
//        String s = filterstr;
//        setFilterstr(s);
        return myremotelistsearc.size();
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

    public View getView(int position, View convertView, ViewGroup parent) {
        if(myremotelistsearc.get(position).istitle){
            //if (convertView == null)
                convertView = mInflater.inflate(R.layout.listview_title, parent, false);
            if (myremotelistsearc.size() > 0 && position < myremotelistsearc.size()) {
                ((TextView) convertView.findViewById(R.id.tvdev)).setText(myremotelistsearc.get(position).devName);
            }
        }else {
            //if (convertView == null)
                convertView = mInflater.inflate(R.layout.listview_myremote, parent, false);
            if (myremotelistsearc.size() > 0 && position < myremotelistsearc.size()) {
                RemoteInfo adev = myremotelistsearc.get(position).remote;
                //((TextView) convertView.findViewById(R.id.tvdev)).setText(adev.dev);
                ((TextView) convertView.findViewById(R.id.tvbrand)).setText(adev.pp);
                ((TextView) convertView.findViewById(R.id.tvxh)).setText(adev.xh);
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

    public void setFilterstr(String filterstr) {
        this.filterstr = filterstr;
        CheckChanged();
    }

    public int getIdx(int  pos){
        return myremotelistsearc.get(pos).idx;
    }
    public boolean isTitle(int  pos){
        return myremotelistsearc.get(pos).istitle;
    }

    public void CheckChanged(){
        List<MyremoteListClass> tempremote = new ArrayList<MyremoteListClass>();
        for (int i = 0; i < CfgData.myremotelist.size(); i++) {
            RemoteInfo adev = CfgData.myremotelist.get(i);
            if (ManzuFilter(filterstr, adev)) {
                MyremoteListClass abj = new MyremoteListClass(false);
                abj.remote = adev;
                abj.idx = i;
                tempremote.add(abj);
            }
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
        myremotelistsearc.clear();
        List<String> devlist = new ArrayList<String>();
        for (int i = 0; i < tempremote.size(); i++) {
            RemoteInfo adev = tempremote.get(i).remote;
            if (devlist.indexOf(adev.dev)<0) {
                MyremoteListClass abj = new MyremoteListClass(true);
                if(adev.dev==null){
                    abj.devName = "";
                }else {
                    abj.devName = adev.dev;
                }
                devlist.add(abj.devName);
                myremotelistsearc.add(abj);
                myremotelistsearc.add(tempremote.get(i));
            }else{
                myremotelistsearc.add(tempremote.get(i));
            }
        }
    }

    private class MyremoteListClass {
        public boolean istitle;
        public int idx; //myremote的序号
        public RemoteInfo remote;
        public String devName;
        public MyremoteListClass(boolean b){
            istitle = b;
            idx = 0;
            remote = null;
            devName = null;
        }
    }
}
