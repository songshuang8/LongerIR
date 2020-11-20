package clurc.net.longerir.data;

import java.util.List;

public class RemoteInfo {
    public int id;
    public String descname; //用不到
    public String pp,xh,dev;
    public int pageidx;
    public boolean islearned;
    public boolean codecannot; //数据不能看哦
    public int isAc;
    public boolean fav;
    public RemoteInfo(){
        islearned = false;
        codecannot = false;
        acdata = null;
        id = -1;
        rid = -1;
        fav = false;
    }
    public int rid;// server id
    public String acdata;
    public List<BtnInfo> btns;
}
