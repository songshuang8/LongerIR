package clurc.net.longerir.data;

public class BM_ModelData {
    private int id;
    private String xh;
    private String dev;
    private String pp;
    private int downcnt;
    private int goodcnt;
    private int badcnt;
    private String Provider;
    private int type;
    private int isAc;

    public BM_ModelData(int id,String xh,String dev,int downcnt,int goodcnt,int badcnt,String provider,int type,int isAc) {
        this.id=id;
        this.xh=xh;
        this.dev =dev;
        this.downcnt = downcnt;
        this.goodcnt = goodcnt;
        this.badcnt = badcnt;
        this.Provider = provider;
        this.type = type;
        this.isAc = isAc;
    }
    public BM_ModelData(int id,String pp,String xh,String dev,int downcnt,int goodcnt,int badcnt,int type,int isAc) {
        this.id=id;
        this.pp = pp;
        this.xh=xh;
        this.dev =dev;
        this.downcnt = downcnt;
        this.goodcnt = goodcnt;
        this.badcnt = badcnt;
        this.Provider = "";
        this.type = type;
        this.isAc = isAc;
    }

    public int getBadcnt() {
        return badcnt;
    }

    public int getDowncnt() {
        return downcnt;
    }

    public int getGoodcnt() {
        return goodcnt;
    }

    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public int getIsAc() {
        return isAc;
    }

    public String getDev() {
        return dev;
    }

    public String getProvider() {
        return Provider;
    }

    public String getXh() {
        if(xh==null)
            return "";
        else
            return xh;
    }
    public String getPp(){
        return pp;
    }

    public void setPp(String app){
        this.pp = app;
    }

    public void setXh(String xh) {
        this.xh = xh;
    }

    public void setDev(String dev) {
        this.dev = dev;
    }

    public void setProvider(String apro){
        this.Provider = apro;
    }
}
