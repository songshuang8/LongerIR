package clurc.net.longerir.data;

import java.util.List;

public class MallGoodsRec {
    public List<CarListBean> carList;
    public static class CarListBean {
        public int count = 1;
        public String title;
        public String icon;
        public boolean isSelectGoods;
    }
}

