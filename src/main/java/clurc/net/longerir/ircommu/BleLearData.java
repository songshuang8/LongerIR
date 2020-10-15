package clurc.net.longerir.ircommu;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

import static android.os.SystemClock.uptimeMillis;

public class BleLearData {
    private long lasttick;
    public static List<Point> wavetype=new ArrayList<Point>();
    public static List<Byte> irbit=new ArrayList<Byte>();
    private int ircount;
    private int wavelen;
    private int zbcnt;
    private int framecount;
    private int currframe;
    private int currwave;
    private float xishu;
    private byte currframex;

    public BleLearData(float x){
        xishu = x;
        clear();
    }

    public void clear(){
        ircount = 0;
        wavelen = 0;
        currframe = 0;
        currwave = 0;
        wavetype.clear();
        irbit.clear();
        currframex = -1;
        lasttick = uptimeMillis();
    }

    public String getWaveArr(){
        String ret = "";
        for (int i = 0; i < ircount; i++) {
            int x = irbit.get(i);
            if(x>=wavetype.size())continue;
            Point p = wavetype.get(x);
            ret += Integer.valueOf(p.x)+",-"+Integer.valueOf(p.y)+",";
        }
        return ret;
    }

    public int getFreq(){
        int req = 0;
        if(zbcnt>0){
            req = (int)( 1000000/(wavetype.get(0).x/(zbcnt*xishu)));
        }
        return req;
    }

    public int getWordValue(byte[] buf,int pos){
        int ret = buf[pos]&0xff;
        ret <<=8;
        ret +=(int)(buf[pos+1]&0xff);
        return ret;
    }

    public int getDWordValue(byte[] buf,int pos){
        int ret1 = getWordValue(buf,pos);
        int ret2 = getWordValue(buf,pos+2);
        ret1 <<=16;
        ret2 +=ret1;
        return ret2;
    }

    public boolean AppendData(byte[] buf){
        if(ircount>0){
            if((uptimeMillis()-lasttick)>800){
                clear();
            }
        }
        if(currframex==buf[0])return false; //过滤重复
        currframex=buf[0];
        lasttick = uptimeMillis();
        if(ircount==0){
            if((buf[0]&0xff)!=0x00 || (buf[1]&0xff)!=0xaa || (buf[2]&0xff)!=0x66){
                return false;
            }
            clear();
            ircount = getWordValue(buf,3);
            wavelen = buf[5]&0xff;
            zbcnt = getWordValue(buf,6);

            int len = ircount / 2;
            if ((ircount % 2)!=0)len++;
            framecount = len / 7;
            if ((len % 7)!=0)framecount++;
        }else{
            if(currwave<wavelen) {
                Point p = new Point();
                p.x = getWordValue(buf,1);
                p.y = getDWordValue(buf,3);
                p.x *= xishu;
                p.y *= xishu;
                wavetype.add(p);
                currwave++;
            }else if(currframe<framecount){
                for (int i = 1; i < buf.length; i++) {
                    byte bit = (byte)(buf[i]&0xf);
                    irbit.add(bit);
                    bit = (byte)((buf[i]>>4)&0xf);
                    irbit.add(bit);
                }
                currframe++;
                if(currframe>=framecount)
                    return true;
                else
                    return false;
            }
        }
        return false;
    }
}
