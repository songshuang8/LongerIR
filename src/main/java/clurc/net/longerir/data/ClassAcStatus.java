package clurc.net.longerir.data;

import java.io.Serializable;

public class ClassAcStatus  implements Serializable {
    private static final long serialVersionUID = 3L;
    public static int BTN_COUNT = 6;
    private int[] sta = new int[BTN_COUNT];
    private static int[] staMax = {1,14,4,3,1,5};
    private int pressbtn;
    private int pressval=0; //按键的值  只有 开关，温度有效，当温度时，1表示温度-  0表示温度+。只有温度键时必须调用SetPressBtnVal
    public ClassAcStatus(){
        for (int i = 0; i < sta.length; i++) {
            sta[i]=0;
        }
        pressbtn=0;
        pressval = 0;
    }

    public void setBtnStaus(int btnx,int btnval){
        sta[btnx]=btnval;
    }
    public int getBtnStaus(int btnx){
        return sta[btnx];
    }

    public void setBtnInc(int btnx,boolean bloop){
        sta[btnx]++;
        if(sta[btnx]>staMax[btnx]) {
            if(bloop)
                sta[btnx] = 0;
            else
                sta[btnx] = staMax[btnx];
        }
        pressbtn = btnx;
    }
    public void setBtnDec(int btnx){
        if(sta[btnx]>0)
            sta[btnx]--;
        pressbtn = btnx;
    }

    public void setPressBtn(int btnx){
        pressbtn = btnx;
    }
    public int getPressBtn(){
        return pressbtn;
    }
    public int getPressVal(){
        return pressval;
    }
    public void setPressval(int val){
        pressval = val;
    }
}
