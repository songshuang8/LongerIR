package clurc.net.longerir.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import java.util.ArrayList;
import java.util.List;

import clurc.net.longerir.R;
import clurc.net.longerir.Utils.MoudelFile;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.manager.QDPreferenceManager;

public class SelectDesRemote extends BaseActivity {
    public static List<Integer> showidx;
    public static List<String> showname;

    private  int selecttype;
    private int selectindex;

    private QDRecyclerViewAdapter mRecyclerViewAdapter;
    private SnapHelper mSnapHelper;

    private RecyclerView mRecyclerView1,mRecyclerView2;

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
        mTopBar.addRightImageButton(R.mipmap.icon_topbar_overflow, R.id.topbar_right_change_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DoShowing();
                    }
                });
        //------------------------
        mRecyclerView2 = findViewById(R.id.remotepic2);
        mRecyclerView2.setLayoutManager(new GridLayoutManager(instance,4));
        rv2 = new Rv2Adapter();
        mRecyclerView2.setAdapter(rv2);
        //------------------------------------
        mRecyclerView1 = findViewById(R.id.remotepic1);
        mRecyclerView1.setLayoutManager(new LinearLayoutManager(instance, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerViewAdapter = new QDRecyclerViewAdapter();
        mRecyclerView1.setAdapter(mRecyclerViewAdapter);
        mSnapHelper = new PagerSnapHelper();
        mSnapHelper.attachToRecyclerView(mRecyclerView1);
        changeTypeShow();
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
        if(QDPreferenceManager.getInstance(instance).getShowType()) {
            mRecyclerView1.setVisibility(View.GONE);
            mRecyclerView2.setVisibility(View.VISIBLE);
            //rv2.notifyDataSetChanged();
        }else{
            mRecyclerView2.setVisibility(View.GONE);
            mRecyclerView1.setVisibility(View.VISIBLE);
            //mRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    private void doselClick(int idx){
        selectindex = idx;
        Intent at = new Intent();
        at.putExtra("desidx", selectindex);
        setResult(Activity.RESULT_OK, at);
        finish();
    }

    class Rv2Adapter extends RecyclerView.Adapter<Rv2Adapter.VH> {
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mall_classic_view2, parent, false);
            return new VH(v);
        }
        // 创建ViewHolder
        class VH extends RecyclerView.ViewHolder{
            public TextView mtitle;
            public ImageView image;
            public VH(View v) {
                super(v);
                mtitle = v.findViewById(R.id.tvTitle);
                image = v.findViewById(R.id.iv_photo);
            }
        }

        @Override
        public int getItemCount() {
            return showname.size();
        }

        @Override
        public void onBindViewHolder(final VH holder, int position) {
            if(showname.size()==0 || position>=showname.size())return;
            if(holder.mtitle.getText().toString()!=null && holder.mtitle.getText().toString().length()>0)return;
            String astr = showname.get(position);
            holder.mtitle.setText(astr);
            holder.image.setImageBitmap(createBitmapFromByteData(showidx.get(position)));
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    doselClick(showidx.get(pos));
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

    class QDRecyclerViewAdapter extends RecyclerView.Adapter<QDRecyclerViewAdapter.ViewHolder> {
//        private AdapterView.OnItemClickListener mOnItemClickListener;
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            View root = inflater.inflate(R.layout.sel_des_remote_a, viewGroup, false);
            return new ViewHolder(root,QDRecyclerViewAdapter.this);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            viewHolder.mTextView.setText(CfgData.modellist.get(showidx.get(i)).name);
            viewHolder.mImageView.setImageBitmap(createBitmapFromByteData(showidx.get(i)));
        }

        @Override
        public int getItemCount() {
            return showidx.size();
        }

        private void onItemHolderClick(RecyclerView.ViewHolder itemHolder) {
            int position = itemHolder.getAdapterPosition();
            doselClick(showidx.get(position));
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private TextView mTextView;
            private ImageView mImageView;
            private QDRecyclerViewAdapter mAdapter;

            public ViewHolder(View itemView,QDRecyclerViewAdapter adapter) {
                super(itemView);
                itemView.setOnClickListener(this);
                mAdapter = adapter;
                mTextView = (TextView) itemView.findViewById(R.id.rdes);
                mImageView = (ImageView) itemView.findViewById(R.id.apic);
            }

            @Override
            public void onClick(View v) {
                mAdapter.onItemHolderClick(this);
            }
        }
    }
}
