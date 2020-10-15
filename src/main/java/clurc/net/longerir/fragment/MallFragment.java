package clurc.net.longerir.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;

import java.util.Arrays;
import java.util.List;

import clurc.net.longerir.R;
import clurc.net.longerir.Utils.GlideImageUtils;
import clurc.net.longerir.Utils.ResCcb;
import clurc.net.longerir.activity.MallDetail;
import clurc.net.longerir.activity.MallCart;

public class MallFragment extends BaseFragment{
    private static String classifys[] = {"PRC", "Tools"};
    private static String Titlepic[] = {"http://www.cldic.com/pic/flash/SPPRCAPP_0.jpg",
            "http://www.cldic.com/pic/flash/4.jpg?max_age=31536000&d=05231747"};
    private static String goodsimages[][] ={
            {"http://www.cldic.com/pic/big/34_0.jpg",
                    "http://www.cldic.com/pic/big/35_0.jpg",
                    "http://www.cldic.com/pic/big/36_0.jpg",
                    "http://www.cldic.com/pic/big/37_0.jpg",
                    "http://www.cldic.com/pic/big/38_0.jpg",
                    "http://www.cldic.com/pic/big/39_0.jpg",
                    "http://www.cldic.com/pic/big/40_0.jpg",
                    "http://www.cldic.com/pic/big/41_0.jpg",},
            {"http://www.cldic.com/pic/big/111_0.jpg",
                    "http://www.cldic.com/pic/big/51_0.jpg"}
    };
    private static String goodsdes[][] = {
            {
                    "CLR79825-L",
                    "CLR79826-TV",
                    "CLR79826-DVB",
                    "CLR79827-TV",

                    "CLR79827-DVB",
                    "CLR79828-SAT",
                    "CLR79828-DVD",
                    "CLR79829-E4",
            },
            {
                    "TYPEC/USB Programmer",
                    "USB Programmer",
            }
    };

    private RecyclerView leftview;
    private MallClassLeftAdapt leftAdapter;

    private RecyclerView rightview;
    private ImageView ivHead;
    private Rv2Adapter rv2;

    public MallFragment(Context context, View root){
        super(context,root);
    }

    @Override
    public  void Init() {
        view = LayoutInflater.from(context).inflate(R.layout.mall_list, (ViewGroup) root, false);
        QMUITopBar mTopBar = (QMUITopBar) view.findViewById(R.id.topbar);
        mTopBar.setBackgroundColor(ContextCompat.getColor(context, R.color.app_color_theme_1));
        mTopBar.setTitle(context.getString(R.string.str_mall));

        leftview = view.findViewById(R.id.rvLeft);
        rightview = view.findViewById(R.id.rightview);
        leftview.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false));
        leftview.setItemAnimator(new DefaultItemAnimator());
        leftAdapter = new MallClassLeftAdapt(ResCcb.getClassifys());
        leftview.setAdapter(leftAdapter);
//        //创建Fragment对象
        //rightview.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false));
        rightview.setLayoutManager(new GridLayoutManager(context,2));
        rv2 = new Rv2Adapter(R.layout.mall_classic_view2, Arrays.asList(goodsimages[0]),0);
        rightview.setAdapter(rv2);

        View hview = View.inflate(context,R.layout.mall_classic_default,null);
        ivHead = hview.findViewById(R.id.iv_pic);
        rv2.setHeaderView(hview);

        GlideImageUtils.DisplayRoundCorner(context,Titlepic[0],ivHead,10);
        mTopBar.addRightImageButton(R.mipmap.icon_topbar_overflow, R.id.topbar_right_change_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        QMUIBottomSheet.BottomListSheetBuilder bd = new QMUIBottomSheet.BottomListSheetBuilder(context);
                        bd.addItem("Cart");
                        bd.addItem("My Orders");
                        bd.setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                            @Override
                            public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                                switch (position) {
                                    case 0:
                                        context.startActivity(new Intent(context, MallCart.class));
                                        break;
                                    case 1:

                                }
                                dialog.dismiss();
                            }
                        });
                        bd.build().show();
                    }
                });
    }

    class Rv2Adapter extends BaseQuickAdapter<String,BaseViewHolder>{
        private int kinds=0;
        public Rv2Adapter(int layoutResId, @Nullable List<String> data, int akinds) {
            super(layoutResId, data);
            kinds = akinds;
        }
        public void setKinds(int akinds){
            kinds = akinds;
        }

        @Override
        protected void convert(final BaseViewHolder helper, String item) {
            int pos = helper.getAdapterPosition() - 1; //从1开始？
            helper.setText(R.id.tvTitle,goodsdes[kinds][pos]);
            //if(pos<goodsimages[kinds].length)
            GlideImageUtils.display(mContext, goodsimages[kinds][pos],(ImageView)helper.getView(R.id.iv_photo));
            //helper.setImageResource(R.id.iv_photo,R.mipmap.spc4);
            helper.setOnClickListener(R.id.iv_photo, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(mContext, MallDetail.class));
                }
            });
        }
    }

    public class MallClassLeftAdapt extends RecyclerView.Adapter<MallClassLeftAdapt.ViewHolder> {
        private List<String> list;

        public MallClassLeftAdapt(List<String> list) {
            this.list = list;
        }

        @Override
        public MallClassLeftAdapt.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View av = LayoutInflater.from(parent.getContext()).inflate(R.layout.mall_classic_left, parent, false);
            ViewHolder viewHolder = new MallClassLeftAdapt.ViewHolder(av);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final MallClassLeftAdapt.ViewHolder holder,final int position) {
            holder.mText.setText(list.get(position));
            if (position == holder.getAdapterPosition()){
                holder.mText.setTextColor(context.getResources().getColor(R.color.app_color_theme_2));
                holder.mText.setBackgroundColor(Color.WHITE);
            }else{
                holder.mText.setTextColor(context.getResources().getColor(R.color.qmui_config_color_black));
                holder.mText.setBackgroundColor(Color.WHITE);
            }
            holder.mText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notifyDataSetChanged();
                    rv2.setKinds(position);
                    rv2.setNewData(Arrays.asList(goodsimages[position]));
                    GlideImageUtils.DisplayRoundCorner(context, Titlepic[position], ivHead, 10);
                    rightview.scrollToPosition(0); //回到顶部 不需要可以注释
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView mText;
            ViewHolder(View itemView) {
                super(itemView);
                mText = itemView.findViewById(R.id.tv_commclass);
            }
        }
    }
}
