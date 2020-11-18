package clurc.net.longerir.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.net.Uri;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import clurc.net.longerir.R;
import clurc.net.longerir.Utils.MoudelFile;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.webHttpClientCom;
import clurc.net.longerir.manager.QDPreferenceManager;

public class SelectDesRemote extends BaseActivity {
    public static List<Integer> showidx;
    public static List<MoudelFile.ModelStru> showcurr;

    private  int selecttype;
    private int selectindex;

    private QDRecyclerViewAdapter mRecyclerViewAdapter;
    private SnapHelper mSnapHelper;

    private RecyclerView mRecyclerView;

    private LinearLayout llshowtype;
    private boolean showtype;
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
        showcurr = new ArrayList<MoudelFile.ModelStru>();
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
            showcurr.add(CfgData.modellist.get(i));
        }
        mTopBar.addRightImageButton(R.mipmap.icon_topbar_overflow, R.id.topbar_right_change_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DoShowing();
                    }
                });
        //------------------------
        mRecyclerView = findViewById(R.id.remotepic);
        //------------------------------------
        showtype = QDPreferenceManager.getInstance(instance).getShowType();
        changeTypeShow();
        mRecyclerViewAdapter = new QDRecyclerViewAdapter();
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        mSnapHelper = new PagerSnapHelper();
        mSnapHelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    public void DoShowing(){
        TranslateAnimation mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mShowAction.setDuration(500);
        mShowAction.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mRecyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        llshowtype.startAnimation(mShowAction);
        llshowtype.setVisibility(View.VISIBLE);
    }

    private void changeTypeShow(){
        if(showtype) {
            mRecyclerView.setLayoutManager(new GridLayoutManager(instance,4));
        }else{
            mRecyclerView.setLayoutManager(new LinearLayoutManager(instance, LinearLayoutManager.HORIZONTAL, false));
        }
    }

    private void doselClick(int idx){
        selectindex = idx;
        Intent at = new Intent();
        at.putExtra("desidx", selectindex);
        setResult(Activity.RESULT_OK, at);
        finish();
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
        mHiddenAction.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mRecyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        llshowtype.startAnimation(mHiddenAction);
        llshowtype.setVisibility(View.GONE);
    }

    public void showSingle(View v){
        QDPreferenceManager.getInstance(instance).setShowType(false);
        showtype = false;
        changeTypeShow();
        hideshowType();
    }

    public void showFour(View v){
        QDPreferenceManager.getInstance(instance).setShowType(true);
        showtype = true;
        changeTypeShow();
        hideshowType();
    }

    class QDRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//        private AdapterView.OnItemClickListener mOnItemClickListener;
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            if(i==0) {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sel_des_remote_a, viewGroup, false);
                return new ViewHolder1(v, QDRecyclerViewAdapter.this);
            }else{
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.mall_classic_view2, viewGroup, false);
                return new ViewHolder4(v,QDRecyclerViewAdapter.this);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            holder.setIsRecyclable(false);
            MoudelFile.ModelStru astu = showcurr.get(position);
            if (holder instanceof ViewHolder1) {
                ((ViewHolder1)holder).mTextView.setText(astu.name);
                setLoadPicInfo(((ViewHolder1)holder).mImageView,
                        webHttpClientCom.baseurl + "mod_export?flag=1&rmtid=" + astu.id + "&cnt=" + astu.imageCount);
            }else{
                ((ViewHolder4)holder).mTextView.setText(astu.name);
                setLoadPicInfo(((ViewHolder4)holder).mImageView,
                        webHttpClientCom.baseurl + "mod_export?flag=1&rmtid=" + astu.id + "&cnt=" + astu.imageCount);
            }
        }

        /**
         * 决定元素的布局使用哪种类型
         *
         * @param position 数据源的下标
         * @return 一个int型标志，传递给onCreateViewHolder的第二个参数 */
        @Override
        public int getItemViewType(int position) {
            if(showtype)
                return 1;
            else
                return 0;
        }

        @Override
        public int getItemCount() {
            return showidx.size();
        }

        private void onItemHolderClick(RecyclerView.ViewHolder itemHolder) {
            int position = itemHolder.getAdapterPosition();
            doselClick(showidx.get(position));
        }

        public class ViewHolder1 extends RecyclerView.ViewHolder implements View.OnClickListener {
            private TextView mTextView;
            private SimpleDraweeView mImageView;
            private QDRecyclerViewAdapter mAdapter;

            public ViewHolder1(View itemView,QDRecyclerViewAdapter adapter) {
                super(itemView);
                itemView.setOnClickListener(this);
                mAdapter = adapter;
                mTextView = itemView.findViewById(R.id.rdes);
                mImageView = itemView.findViewById(R.id.apic);
            }

            @Override
            public void onClick(View v) {
                mAdapter.onItemHolderClick(this);
            }
        }
        public class ViewHolder4 extends RecyclerView.ViewHolder implements View.OnClickListener {
            private TextView mTextView;
            private SimpleDraweeView mImageView;
            private QDRecyclerViewAdapter mAdapter;

            public ViewHolder4(View itemView,QDRecyclerViewAdapter adapter) {
                super(itemView);
                itemView.setOnClickListener(this);
                mAdapter = adapter;
                mTextView = itemView.findViewById(R.id.tvTitle);
                mImageView = itemView.findViewById(R.id.my_image_view);
            }

            @Override
            public void onClick(View v) {
                mAdapter.onItemHolderClick(this);
            }
        }
    }

    private void setLoadPicInfo(SimpleDraweeView imageview,String url){
        Uri uri = Uri.parse(url);
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setProgressiveRenderingEnabled(true)
//                .disableDiskCache()
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setOldController(imageview.getController())
                .build();
        GenericDraweeHierarchyBuilder builder =
                new GenericDraweeHierarchyBuilder(getResources());
        GenericDraweeHierarchy hierarchy = builder
                .setFadeDuration(300)
                .setPlaceholderImage(R.drawable.placeholder)
                .setPlaceholderImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                .setProgressBarImage(new ProgressBarDrawable())
                .build();
        imageview.setHierarchy(hierarchy);
        imageview.setController(controller);
    }
}
