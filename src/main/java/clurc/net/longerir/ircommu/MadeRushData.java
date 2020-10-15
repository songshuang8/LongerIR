package clurc.net.longerir.ircommu;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

public class MadeRushData {
    private int freq;
    private int[] wave;
    public static List<Point> wavetype=new ArrayList<Point>();
    public static List<Byte> irbit=new ArrayList<Byte>();
    public MadeRushData(int freq,int[] wave){
        this.freq = freq;
        this.wave = wave;
        irbit.clear();
        wavetype.clear();
    }

    public byte[] getData(){  //必须 8 的倍数
        float frqlen=2;
        if(wave.length<3)return null;
        int hlcnt = wave.length / 2;
        if((wave.length % 2)!=0)hlcnt++;
        List<Byte> ret=new ArrayList<Byte>();
        for (int i = 0; i < hlcnt; i++) {
            byte m;
            if((2*i+1)>=wave.length){
                m = AppendType(wave[2*i],wave[1]);
            }else{
                m= AppendType(wave[2*i],wave[2*i+1]);
            }
            irbit.add(m);
        }
        //
        ret.add((byte)0xf1);
        ret.add((byte)0xf2);
        ret.add((byte)12);
        ret.add((byte)wavetype.size());
        if(freq==0)
            ret.add((byte)0);
        else {
            frqlen = 1000000/freq;
            int c = (int)Math.round(frqlen*2-6+0.5f);
            if(c<=1)c=2;
            ret.add((byte)(c));
        }
        ret.add((byte)((hlcnt >> 8)&0xff));
        ret.add((byte)(hlcnt &0xff));
        byte crc =(byte) 0xf1;
        for (int i = 0; i < 7; i++) {
            crc += (ret.get(i)&0xff);
        }
        ret.add(crc);
        //--------------
        for (int i = 0; i < wavetype.size(); i++) {
            int m = wavetype.get(i).x;
            if(freq>0)
                m = Math.round(m/frqlen+0.5f);
            else
                m = m*2;
            ret.add((byte)((m >> 8)&0xff));
            ret.add((byte)(m&0xff));

            m = abs(wavetype.get(i).y);
            m = m*2;
            ret.add((byte)((m >> 24)&0xff));
            ret.add((byte)((m >> 16)&0xff));
            ret.add((byte)((m >> 8)&0xff));
            ret.add((byte)(m&0xff));

            ret.add((byte)0);
            ret.add((byte)0);
        }
        //--
        byte abit = 0;
        for (int i = 0; i < irbit.size(); i++) {
            if((i % 2)==0){
                abit = (byte) (irbit.get(i)&0xf);
            }else{
                abit += (byte) ((irbit.get(i)&0xf)<<4);
                ret.add(abit);
            }
        }
        if((irbit.size() % 2)!=0)
            ret.add((byte)(16+abit));
        while((ret.size()%8)!=0)ret.add((byte)0xff);
        byte[] mm = new byte[ret.size()];
        for (int i = 0; i < ret.size(); i++) {
            mm[i] = ret.get(i);
        }
        return mm;
    }

    private byte AppendType(int x,int y){
        for (int i = 0; i < wavetype.size(); i++) {
            if(SameCompare(x,y,wavetype.get(i))){
                return (byte) i;
            }
        }
        Point p = new Point(x,y);
        wavetype.add(p);
        return (byte) (wavetype.size()-1);
    }

    private boolean SameCompare(int x,int y,Point p){
        if(abs(x-p.x)>80)return false;
        if(abs(y-p.y)>120)return false;
        return true;
    }
}
