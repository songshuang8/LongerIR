package clurc.net.longerir.adapt;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import clurc.net.longerir.R;

public class AcTempShowAdapt extends BaseAdapter {
    private Context context = null;
    private LayoutInflater mInflater;
    private int[] sta;

    public AcTempShowAdapt(Context context, int[] sta) {
        this.context = context;
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
        return 15;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = mInflater.inflate(R.layout.listview_singletxt_2, parent, false);
        TextView tv = (TextView) convertView.findViewById(R.id.singleid);
        tv.setText(String.valueOf(position+16));
        tv.setTag(position);
        GradientDrawable gd = new GradientDrawable();//创建drawable
        if(sta[1]==position)
            gd.setColor(context.getResources().getColor(R.color.app_color_theme_6));
        else
            gd.setColor(Color.WHITE);
        gd.setCornerRadius(8);
        gd.setStroke(1, context.getResources().getColor(R.color.app_color_theme_1));
        tv.setBackground(gd);
        return convertView;
    }
}
