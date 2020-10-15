package clurc.net.longerir.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;
import com.zhouwei.mzbanner.MZBannerView;
import com.zhouwei.mzbanner.holder.MZHolderCreator;
import com.zhouwei.mzbanner.holder.MZViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import clurc.net.longerir.R;
import clurc.net.longerir.Utils.GlideImageUtils;
import clurc.net.longerir.Utils.MoudelFile;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.RemoteInfo;
import clurc.net.longerir.manager.QDPreferenceManager;
import clurc.net.longerir.view.AllListView;

public class SelectDesRemote extends BaseActivity {
    public static List<Integer> showidx;
    public static List<String> showname;

    private  int selecttype;

    private int selectindex;

    private MZBannerView banner;
    private RecyclerView collist;
    private Rv2Adapter rv2;
    private LinearLayout llshowtype;
    @Override
    public void getViewId() {
        layid = R.layout.sel_des_remote;
        title = getString(R.string.str_sel_desremote);
    }

    @Override
    public void DoInit() {
        llshowtype = findViewById(R.id.showtype);
        if(CfgData.modellist==null){
            CfgData.modellist = MoudelFile.getMoudleArr(instance);
        }
        showidx = new ArrayList<Integer>();//当前过滤的
        showname = new ArrayList<String>();
        selecttype = getIntent().getIntExtra("iseltype",-1);
        for (int i = 0; i < CfgData.modellist.size(); i++) {
            if(CfgData.modellist.get(i).prgtype!=0)continue;
            int chipint = CfgData.modellist.get(i).chip;

            if(selecttype<0){ // -1 普通非空调遥控器
                if (chipint == 11)continue;
            }else {
                if (chipint == 1 || chipint == 4 || chipint == 5 || chipint == 7) {
                    if (selecttype != 1) continue;
                } else if (chipint == 10) {
                    if (selecttype != 2) continue;
                } else if (chipint == 11) {
                    if (selecttype != 3) continue;
                } else {
                    if (selecttype != 0) continue;
                }
            }
            showidx.add(i);
            showname.add(CfgData.modellist.get(i).name);
        }

        banner = findViewById(R.id.banner);
        collist = findViewById(R.id.rightview);

        changeTypeShow();

        mTopBar.addRightImageButton(R.mipmap.icon_topbar_overflow, R.id.topbar_right_change_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DoShowing();
                    }
                });
    }

    @Override
    public void DoShowing(){
        TranslateAnimation mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mShowAction.setDuration(500);
        llshowtype.startAnimation(mShowAction);
        llshowtype.setVisibility(View.VISIBLE);
    }

    private void changeTypeShow(){
        //int p = QDPreferenceManager.getInstance(instance).geSelectDes();
        //if(p>=showidx.size())p=0;
        if(QDPreferenceManager.getInstance(instance).getShowType()) {
            collist.setLayoutManager(new GridLayoutManager(instance,4));
            rv2 = new Rv2Adapter(R.layout.mall_classic_view2, showname);
            collist.setAdapter(rv2);
            banner.setVisibility(View.GONE);
            collist.setVisibility(View.VISIBLE);
        }else{
            // 设置数据
            banner.setPages(showidx, new MZHolderCreator<BannerViewHolder>() {
                @Override
                public BannerViewHolder createViewHolder() {
                    return new BannerViewHolder();
                }
            });
            banner.setIndicatorVisible(false);
            banner.setCanLoop(true);
            collist.setVisibility(View.GONE);
            banner.setVisibility(View.VISIBLE);
        }
    }

    private void doselClick(int idx){
        selectindex = idx;
        QDPreferenceManager.getInstance(instance).setSelDes(selectindex);

            Intent at = new Intent();
            at.putExtra("desidx", selectindex);
            setResult(Activity.RESULT_OK, at);
            finish();
    }

    class BannerViewHolder implements MZViewHolder<Integer> {
        private ImageView mImageView;
        private TextView mdesc;
        @Override
        public View createView(Context context) {
            // 返回页面布局
            View view = LayoutInflater.from(context).inflate(R.layout.sel_des_remote_a,null);
            mImageView = (ImageView) view.findViewById(R.id.apic);
            mdesc = view.findViewById(R.id.rdes);
            return view;
        }

        @Override
        public void onBind(Context context,final int position, Integer data) {
            // 数据绑定
            mImageView.setImageBitmap(createBitmapFromByteData(showidx.get(position)));
            mdesc.setText(CfgData.modellist.get(showidx.get(position)).name);
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    doselClick(showidx.get(position));
                }
            });
            //GlideImageUtils.display(context,data,mImageView);
        }
    }

    class Rv2Adapter extends BaseQuickAdapter<String, BaseViewHolder> {
        public Rv2Adapter(int layoutResId, @Nullable List<String> data) {
            super(layoutResId, data);
        }
        @Override
        protected void convert(final BaseViewHolder helper, String item) {
            int pos = helper.getAdapterPosition(); //从1开始？
            helper.setText(R.id.tvTitle,showname.get(pos));
            ImageView image = (ImageView)helper.getView(R.id.iv_photo);
            image.setTag(pos);
            image.setImageBitmap(createBitmapFromByteData(showidx.get(pos)));
            helper.setOnClickListener(R.id.iv_photo, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int p = (int)view.getTag();
                    doselClick(showidx.get(p));
                }
            });
        }
    }

    private Bitmap createBitmapFromByteData(int idx){
        byte[] data = MoudelFile.getMoudleJpg(instance,idx,CfgData.modellist);
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inSampleSize = 2;
        //op.inJustDecodeBounds = true; //它仅仅会把它的宽，高取回来给你，这样就不会占用太多的内存，也就不会那么频繁的发生OOM了。
        //op.inPreferredConfig = Bitmap.Config.ARGB_4444;    // 默认是Bitmap.Config.ARGB_8888
        return BitmapFactory.decodeByteArray(data, 0, data.length, op);
    }

    private void dofinnishThis(int myidx,int pageidx){
        Intent at=new Intent();
        at.putExtra("desidx", selectindex);
        at.putExtra("myidx", myidx);
        at.putExtra("pagesel",pageidx);
        setResult(Activity.RESULT_OK, at);
        finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            dofinnishThis(data.getIntExtra("myidx",0),data.getIntExtra("pagesel",0));
        }
    }

    private void hideshowType(){
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                -1.0f);
        mHiddenAction.setDuration(500);
        llshowtype.startAnimation(mHiddenAction);
        llshowtype.setVisibility(View.GONE);
    }

    public void showSingle(View v){
        QDPreferenceManager.getInstance(instance).setShowType(false);
        changeTypeShow();
        hideshowType();
    }

    public void showFour(View v){
        QDPreferenceManager.getInstance(instance).setShowType(true);
        changeTypeShow();
        hideshowType();
    }
}
