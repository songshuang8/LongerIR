package clurc.net.longerir.ircommu;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.max;

public class MadeBleRushData {
    private int[] wave;
    private int freq;
    public static List<Point> wavetype=new ArrayList<Point>();
    public static List<Byte> irbit=new ArrayList<Byte>();
    public MadeBleRushData(int freq,int[] wave){ // first is freq
        this.freq = freq;
        this.wave = wave;
        irbit.clear();
        wavetype.clear();
    }

    public byte[] getData(){
        int turnx = 0;
        float frqlen = 1;
        if(wave.length<3)return null;
        int hlcnt = (wave.length) / 2;
        if(((wave.length) % 2)!=0)hlcnt++;
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
        // dd  freqH freqL wavetype_len bit_len10  x  chk
        ret.add((byte)0xdd);
        if(freq==0) {
            frqlen = wavetype.get(0).x;
            int c = Math.round(frqlen * 3 - 4 + 0.5f);
            ret.add(( byte)c);
            ret.add(( byte)c);
        }else {
            frqlen = 1000000/freq;
            int c = Math.round(frqlen*3-4+0.5f);
            int m = c /5;
            if(m<=1)m=1;
            ret.add((byte)(m));
            ret.add((byte)(c-m));
        }
        ret.add((byte)wavetype.size());
        ret.add((byte)((hlcnt >> 8)&0xff)); //High bit
        ret.add((byte)(hlcnt &0xff));
        ret.add((byte)0xff);
        byte crc =0;
        for (int i = 0; i < 7; i++) {
            crc += (ret.get(i)&0xff);
        }
        ret.add(crc);
        //--------------   dx  freq_count10, all length321  x  chk
        for (int i = 0; i < wavetype.size(); i++) {
            int m;
            int p = ret.size();
            ret.add((byte)(0xd0+turnx++));
            if(turnx>2)turnx=0;
            if(freq>0) {
                m = wavetype.get(i).x;
                m = Math.round( (float)(m /frqlen)+0.5f);  //载波个数
                ret.add((byte)((m >> 8)&0xff));
                ret.add((byte)(m&0xff));

                m = abs(wavetype.get(i).y) + wavetype.get(i).x;
            }else {
                ret.add((byte)0);
                ret.add((byte)0x01);
                m = abs(wavetype.get(i).y) + wavetype.get(i).x - Math.round(frqlen+0.5f);
            }
            ret.add((byte)((m >> 24)&0xff));
            ret.add((byte)((m >> 16)&0xff));
            ret.add((byte)((m >> 8)&0xff));
            ret.add((byte)(m&0xff));

            crc =0;
            for (int j = p; j < ret.size(); j++) {
                crc += (ret.get(j)&0xff);
            }
            ret.add(crc);
        }
        //--
        List<Byte> mbit=new ArrayList<Byte>();
        byte abit = 0;
        for (int i = 0; i < irbit.size(); i++) {
            if((i % 2)==0){
                abit = (byte) (irbit.get(i)&0xf);
            }else{
                abit += (byte) ((irbit.get(i)&0xf)<<4);
                mbit.add(abit);
            }
        }
        if((irbit.size() % 2)!=0)
            mbit.add((byte)(16+abit));  //?
        while((mbit.size()%6)!=0)mbit.add((byte)0x00);   //必须 6 的倍数
        int bitframe = mbit.size() / 6;
        for (int i = 0; i < bitframe; i++) {
            int p = ret.size();
            ret.add((byte)(0xd0+turnx++));
            if(turnx>2)turnx=0;
            for (int j = 0; j < 6; j++) {
                ret.add(mbit.get(i*6+j));
            }
            crc =0;
            for (int j = p; j < ret.size(); j++) {
                crc += (ret.get(j)&0xff);
            }
            ret.add(crc);
        }
        //
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
