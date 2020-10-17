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
import java.util.List;

import clurc.net.longerir.R;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.RemoteInfo;

public class DevicesItemAdapter extends BaseAdapter {
    private Context context = null;
    private LayoutInflater mInflater;
    private List<RemoteInfo> myremotelistsearc;
    private List<Integer> myidx;
    private String filterstr;

    public DevicesItemAdapter(Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        myremotelistsearc=new ArrayList<RemoteInfo>();
        myidx = new ArrayList<Integer>();
        setFilterstr("");
    }

    @Override
    public int getCount() {
        String s = filterstr;
        setFilterstr(s);
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
        //View view;
        if (convertView == null)
            convertView = mInflater.inflate(R.layout.listview_myremote, parent, false);
       // } else {
        //    view = convertView;
       // }
        int pos = myidx.get(position);
        if(myremotelistsearc.size()>0 && pos<myremotelistsearc.size()) {
            RemoteInfo adev = myremotelistsearc.get(pos);
            ((TextView) convertView.findViewById(R.id.tvdev)).setText(adev.dev);
            ((TextView) convertView.findViewById(R.id.tvbrand)).setText(adev.pp);
            ((TextView) convertView.findViewById(R.id.tvxh)).setText(adev.xh);
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
        myremotelistsearc.clear();
        myidx.clear();
        int len = CfgData.myremotelist.size();
        for (int i = 0; i < len; i++) {
            RemoteInfo adev = CfgData.myremotelist.get(i);
            if (ManzuFilter(filterstr, adev)) {
                myidx.add(i);
                myremotelistsearc.add(adev);
            }
        }
    }

    public int getIdx(int  pos){
        return myidx.get(pos);
    }
}
