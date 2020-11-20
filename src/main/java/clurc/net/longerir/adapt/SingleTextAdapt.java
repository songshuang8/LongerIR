package clurc.net.longerir.adapt;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import clurc.net.longerir.R;

public class SingleTextAdapt extends BaseAdapter {
    private Context context = null;
    private LayoutInflater mInflater;
    private int lay,vid;
    private List<String> arr;
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(R.layout.listview_singletext, parent, false);
        if (arr.size() > 0 && position < arr.size()) {
            ((TextView) convertView.findViewById(vid)).setText(arr.get(position));
        }
        if((position % 2)==0){
            convertView.setBackgroundColor(Color.WHITE);
        }else{
            convertView.setBackgroundColor(context.getResources().getColor(R.color.infobak));
        }
        return convertView;
    }

    public SingleTextAdapt(Context context,int Resource,int viewID,List<String> arr) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        lay = Resource;
        vid = viewID;
        this.arr = arr;
    }

    @Override
    public int getCount() {
        return arr.size();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return arr.get(position);
    }
}