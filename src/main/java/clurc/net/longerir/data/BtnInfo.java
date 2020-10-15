package clurc.net.longerir.data;

public class BtnInfo {
    public int id;
    public String btnname;
    public String imgpath;
    public int row,col;
    public int shapekinds;
    public int gsno;
    public String param16; //16进制字符串
    public int keyidx;  //空调作为载波数
    public String wave;
    public int desidx;
    //---
    public int[] params;  //空调的学习数据作为sta
    public boolean flag;
    public BtnInfo(){
        flag = false;
        id=-1;
    }
}
