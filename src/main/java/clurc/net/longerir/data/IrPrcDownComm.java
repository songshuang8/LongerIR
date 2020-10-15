package clurc.net.longerir.data;

public class IrPrcDownComm {
    private byte[] data;
    private int framelen;
    private int lastcount;
    private int currframe;
    public IrPrcDownComm(byte[] adata){
        data = adata;
        framelen = (int) (data.length / 6);
        lastcount = data.length % 6;
        if (lastcount == 0) {
            lastcount = 6;
        } else
            framelen++; //不是6的倍数，加一帧
        framelen++;//第一帧不发有效数据，所以加1
        currframe = 0;
    }

    public byte[] getCurrData(){
        int[] sendbuffer = new int[8];
        if (currframe == 0) {
            sendbuffer[0] = 0xaa;
            sendbuffer[1] = 0;
            sendbuffer[2] = 0;
            sendbuffer[3] = (int) ((framelen - 1) & 0xff); //low 8
            sendbuffer[4] = (int) (((framelen - 1) >> 8) & 0xff); //high 8
            sendbuffer[5] = lastcount;
            sendbuffer[6] = 0;
        } else {
            sendbuffer[0] = 0xa0 + ((currframe - 1) % 8);
            int sublen = 6;
            if ((currframe + 1) == framelen) {
                sublen = lastcount;
            }
            for (int i = 0; i < sublen; i++) {
                sendbuffer[i + 1] = (data[(currframe - 1) * 6 + i] & 0xff);
            }
        }
        sendbuffer[7] = 0;
        for (int i = 0; i < 7; i++) {
            sendbuffer[7] += sendbuffer[i];
        }
        sendbuffer[7] &= 0xff;
        byte[] sb = CfgData.intArrToByte(sendbuffer);
        return sb;
    }

    public void DoFrameInc(){
        currframe++;
    }

    public int getCurrframe() {
        return currframe;
    }

    public int getFramelen() {
        return framelen;
    }

    public boolean getHaveOk(){
        return (currframe>=framelen)?true:false;
    }

    public int getPercent(){
        if(currframe>=framelen)
            return 100;
        else
        return (currframe*100 / framelen);
    }
}
