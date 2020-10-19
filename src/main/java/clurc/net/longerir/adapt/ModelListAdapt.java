package clurc.net.longerir.adapt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import clurc.net.longerir.R;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.modeldata.DataModelInfo;
import clurc.net.longerir.view.RemoteBtnView;
import clurc.net.longerir.view.ViewDragGridReadOnly;

public class ModelListAdapt extends RecyclerView.Adapter<ModelListAdapt.VH> {
    private List<DataModelInfo> models;
    private Context context;
    private OnItemClickListener click,longclick;
    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        //LayoutInflater.from指定写法
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_model_adapt_list, parent, false);
        return new VH(v);
    }
    // 创建ViewHolder
    public static class VH extends RecyclerView.ViewHolder{
        public ViewDragGridReadOnly dragview;
        public TextView mtitle;
        public VH(View v) {
            super(v);
            dragview = v.findViewById(R.id.btnpreview);
            mtitle = (TextView)v.findViewById(R.id.tvTitle);
        }
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public ModelListAdapt(List<DataModelInfo> models,Context context,OnItemClickListener click,OnItemClickListener lclick) {
        this.context = context;
        this.models = models;
        this.click = click;
        this.longclick = lclick;
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    @Override
    public void onBindViewHolder(final VH holder, final int position) {
        holder.setIsRecyclable(false);
//        if(models.size()==0 || position>=models.size())return;
        if(holder.dragview.getChildCount()>0)
            holder.dragview.removeAllViews();
        final DataModelInfo item = models.get(position);
        holder.mtitle.setText(item.strdesc);
        if(item.id<0) {  //作为空调
            holder.dragview.setIsacview(true);
            holder.dragview.setCOL_CNT(1);
            holder.dragview.setTag(position);
            View acview = LayoutInflater.from(context).inflate(R.layout.ac_view_model, null,false);
            holder.dragview.addView(acview);
            holder.dragview.invalidate();
        }else {
            holder.dragview.setCOL_CNT(item.colcnt);
            holder.dragview.setTag(position);
            for (int i = 0; i < item.btns.size(); i++) {
                RemoteBtnView image = new RemoteBtnView(context,
                        context.getResources().getColor(android.R.color.darker_gray),
                        context.getResources().getColor(android.R.color.holo_blue_dark), false);
                image.setBtnName(item.btns.get(i).btnname);
                image.setCustomName(item.btns.get(i).btnname);
                image.setRownCol(item.btns.get(i).cols, item.btns.get(i).rows);
                image.setmShapeKinds(RemoteBtnView.ShapeKinds.values()[item.btns.get(i).kinds]);
                image.setKeyidx(item.btns.get(i).keyidx);
                image.setSno(item.btns.get(i).sid);
                image.setmSelected(true);
                holder.dragview.addView(image);
            }
        }
        holder.dragview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(click!=null)
                    click.onItemClick(holder.getLayoutPosition());
            }
        });
        holder.dragview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(longclick!=null) {
                    longclick.onItemClick(holder.getLayoutPosition());
                    return true;
                }
                return false;
            }
        });
    }
}
