package clurc.net.longerir.adapt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import clurc.net.longerir.R;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.ircommu.DesRemote;

public class PrcShadueAdapt extends BaseAdapter {
    private int[] pageid = {R.id.pge3,R.id.pge3,R.id.pge3,R.id.pge4};
    private int[] remotenameid = {R.id.fillremote1,R.id.fillremote2,R.id.fillremote3,R.id.fillremote4};
    private int[] pagenameid = {R.id.pagename1,R.id.pagename2,R.id.pagename3,R.id.pagename4};
    private RelativeLayout[] pnl = new RelativeLayout[4];
    private TextView[] pagename = new TextView[4];
    private TextView[] remotename = new TextView[4];
    private Context context = null;
    private LayoutInflater mInflater;
    private OnAppendListener lisappend;

    public PrcShadueAdapt(Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return CfgData.prclist.size();
    }

    @Override
    public Object getItem(int position) {
        Object abj = null;
        if (position < CfgData.prclist.size())
            abj = CfgData.prclist.get(position);
        return abj;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        //View view;
        if (convertView == null)
            convertView = mInflater.inflate(R.layout.listview_prclist, parent, false);
        DesRemote abj = CfgData.prclist.get(position);
        TextView modelname = convertView.findViewById(R.id.modename);
        modelname.setText(abj.name);
        modelname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lisappend!=null)lisappend.OnTypeClick(position);
            }
        });
        for (int i = 0; i < 4; i++) {
            pnl[i] = convertView.findViewById(pageid[i]);
            if(i<abj.pagename.length){
                pnl[i].setVisibility(View.VISIBLE);
                pagename[i] = convertView.findViewById(pagenameid[i]);
                pagename[i].setText(abj.pagename[i]);
                int k=-1;
                for (int j = 0; j < abj.src.size(); j++) {
                    if(abj.src.get(j).pageidx==i){
                        k=j;
                        break;
                    }
                }
                remotename[i] = convertView.findViewById(remotenameid[i]);
                if(k<0){
                    remotename[i].setText("Click here select a source remote");
                }else
                    remotename[i].setText(abj.src.get(k).pp);
                pnl[i].setTag(i);
                pnl[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pageno = (int)v.getTag();
                        if(lisappend!=null)lisappend.OnAppendClick(position,pageno);
                    }
                });
                ImageButton btn = convertView.findViewById(R.id.imageButton);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(lisappend!=null)lisappend.OnDownClick(position);
                    }
                });
            }else{
                pnl[i].setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    public interface OnAppendListener {
        public abstract void OnAppendClick(int pos, int pageno);
        public abstract void OnDownClick(int pos);
        public abstract void OnTypeClick(int pos);
    }

    public void setListenClick(OnAppendListener e){
        this.lisappend = e;
    }
}
