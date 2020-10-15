package clurc.net.longerir.activity;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import clurc.net.longerir.R;
import clurc.net.longerir.Utils.GlideImageUtils;
import clurc.net.longerir.Utils.ResCcb;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.MallGoodsRec;

public class MallCart extends BaseActivity {
    private static String goodsdes[] = {
        "CLR79825-L",
                "CLR79826-TV",
                "CLR79826-DVB",
                "CLR79827-TV",

                "CLR79827-DVB",
                "CLR79828-SAT",
                "CLR79828-DVD",
                "CLR79829-E4",
    };
    private static String goodsimage[] = {
            "http://www.cldic.com/pic/big/34_0.jpg",
            "http://www.cldic.com/pic/big/35_0.jpg",
            "http://www.cldic.com/pic/big/36_0.jpg",
            "http://www.cldic.com/pic/big/37_0.jpg",
            "http://www.cldic.com/pic/big/38_0.jpg",
            "http://www.cldic.com/pic/big/39_0.jpg",
            "http://www.cldic.com/pic/big/40_0.jpg",
            "http://www.cldic.com/pic/big/41_0.jpg"};

    private RecyclerView rvShop;
    private MallGoodsRec items;
    private CheckBox cbAll;
    private TextView jiesuan;
    GoodsAdapter goodsAp;

    @Override
    public void getViewId() {
        layid = R.layout.mall_order;
        title = "My cart";
    }

    @Override
    public void DoInit() {
        rvShop = findViewById(R.id.rvShop);
        cbAll = findViewById(R.id.spc_cb_all);
        jiesuan = findViewById(R.id.jiesuan);
        rvShop.setLayoutManager(new LinearLayoutManager(instance,LinearLayoutManager.VERTICAL, false));
        rvShop.setHasFixedSize(true); //禁止recyclerview滑动，避免和ScrollView冲突；
        rvShop.setNestedScrollingEnabled(false); //禁止recyclerview滑动，避免和ScrollView冲突；
        goodsAp = new GoodsAdapter(R.layout.mall_goods_item);
        rvShop.setAdapter(goodsAp);

        items = new MallGoodsRec();
        items.carList = new ArrayList<>();
        //添加商品
        for (int i = 0; i < goodsdes.length; i++) {
            MallGoodsRec.CarListBean goods = new MallGoodsRec.CarListBean();
            goods.title = goodsdes[i];
            goods.icon = goodsimage[i];
            items.carList.add(goods);
        }
        goodsAp.setNewData(items.carList);
        //
        cbAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean is = cbAll.isChecked();
                for (int j = 0; j < items.carList.size(); j++) {
                        items.carList.get(j).isSelectGoods = is;
                }
                goodsAp.notifyDataSetChanged();
            }
        });
        jiesuan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(instance, "Hello", Toast.LENGTH_SHORT).show();
            }
        });
    }

    class GoodsAdapter extends BaseQuickAdapter<MallGoodsRec.CarListBean,BaseViewHolder>{
        public GoodsAdapter(int layoutResId) {
            super(layoutResId);
        }

        @Override
        protected void convert(final BaseViewHolder helper, MallGoodsRec.CarListBean item) {
            helper.setText(R.id.spc_tv_shop_name_msg,item.title)
                    .setOnClickListener(R.id.rl, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //mContext.startActivity(new Intent(mContext, GoodsInfoActivity.class));
                        }
                    });
            GlideImageUtils.display(mContext, item.icon,(ImageView)helper.getView(R.id.spc_iv_page));
            final CheckBox cbGoods = helper.getView(R.id.spc_cb_goods);
            cbGoods.setChecked(item.isSelectGoods);
            cbGoods.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    items.carList.get(helper.getAdapterPosition()).isSelectGoods = cbGoods.isChecked();
                    //改变这个商品的选中状态后，遍历看是否全部选中，若全部选中则改变全选的选中状态
                    List<Boolean> goodslist = new ArrayList<>();
                    for (int i = 0; i < items.carList.size(); i++) {
                        goodslist.add(items.carList.get(i).isSelectGoods);
                    }
                    if (goodslist.contains(false)){
                        cbAll.setChecked(false);
                    }else{
                        cbAll.setChecked(true);
                    }
                    goodsAp.notifyDataSetChanged();
                    notifyDataSetChanged();
                }
            });

            Button btnJian = helper.getView(R.id.spc_btn_comm_count_jian);
            Button btnJia = helper.getView(R.id.spc_btn_comm_count_jia);
            TextView count = helper.getView(R.id.spc_et_comm_count);
            count.setText(item.count+"");
            btnJia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    items.carList.get(helper.getAdapterPosition()).count =  items.carList.get(helper.getAdapterPosition()).count + 1;
                    notifyDataSetChanged();
                }
            });
            btnJian.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (items.carList.get(helper.getAdapterPosition()).count > 1){
                        items.carList.get(helper.getAdapterPosition()).count =  items.carList.get(helper.getAdapterPosition()).count - 1;}
                    notifyDataSetChanged();
                }
            });
        }
    }
}
