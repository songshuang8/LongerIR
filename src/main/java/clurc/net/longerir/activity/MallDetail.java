package clurc.net.longerir.activity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.zhouwei.mzbanner.MZBannerView;
import com.zhouwei.mzbanner.holder.MZHolderCreator;
import com.zhouwei.mzbanner.holder.MZViewHolder;

import java.util.Arrays;

import clurc.net.longerir.R;
import clurc.net.longerir.Utils.GlideImageUtils;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.view.AllListView;

public class MallDetail extends BaseActivity {
    private static String goodsimages[] ={"http://www.cldic.com/pic/big/34_0.jpg",
            "http://www.cldic.com/pic/big/35_0.jpg",
            "http://www.cldic.com/pic/big/36_0.jpg",
            "http://www.cldic.com/pic/big/37_0.jpg",
            "http://www.cldic.com/pic/big/38_0.jpg",
            "http://www.cldic.com/pic/big/39_0.jpg",
            "http://www.cldic.com/pic/big/40_0.jpg",
            "http://www.cldic.com/pic/big/41_0.jpg"};
    @Override
    public void getViewId(){
        layid = R.layout.mall_detail;
        title = "Detail infomation";
    }
    @Override
    public void DoInit() {
        MZBannerView banner = findViewById(R.id.banner);
        AllListView allListView = findViewById(R.id.alv);
        // 设置数据
        banner.setPages(Arrays.asList(goodsimages), new MZHolderCreator<BannerViewHolder>() {
            @Override
            public BannerViewHolder createViewHolder() {
                return new BannerViewHolder();
            }
        });
        mTopBar.addRightImageButton(R.mipmap.icon_topbar_overflow, R.id.topbar_right_change_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        QMUIBottomSheet.BottomListSheetBuilder bd = new QMUIBottomSheet.BottomListSheetBuilder(instance);
                        bd.addItem("Cart");
                        bd.addItem("My Orders");
                        bd.setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                            @Override
                            public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                                switch (position) {
                                    case 0:
                                        instance.startActivity(new Intent(instance, MallCart.class));
                                        instance.finish();
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

    private class BannerViewHolder implements MZViewHolder<String> {
        private ImageView mImageView;
        @Override
        public View createView(Context context) {
            // 返回页面布局
            View view = LayoutInflater.from(context).inflate(R.layout.mall_detail_banner,null);
            mImageView = (ImageView) view.findViewById(R.id.iv);
            return view;
        }

        @Override
        public void onBind(Context context, int position, String data) {
            // 数据绑定
            GlideImageUtils.display(context,data,mImageView);
        }
    }
}
