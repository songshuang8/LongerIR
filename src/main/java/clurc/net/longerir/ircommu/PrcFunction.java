package clurc.net.longerir.ircommu;

import clurc.net.longerir.data.CfgData;

//下载所需的函数
public class PrcFunction {
    //给下载数据前加３２个字节  只用于专业空调
    public byte[] getEepData(String acbase) {
        byte[] puerdata = android.util.Base64.decode(acbase,android.util.Base64.DEFAULT);
        int len = puerdata.length + 32;
        while (((len + 4) % 6) != 0) len++;
        byte[] data = new byte[len + 4];
        for (int i = 0; i < 32; i++) {
            data[i] = 0;
        }
        for (int i = 0; i < puerdata.length; i++) {
            data[i + 32] = puerdata[i];
        }
        int chk = 0;
        for (int i = 0; i < len; i++) {
            chk += (data[i] & 0xff);
        }
        data[len] = (byte) (chk & 0xff);
        data[len + 1] = (byte) ((chk >> 8) & 0xff);
        chk = ~chk;
        len += 2;
        data[len] = (byte) (chk & 0xff);
        data[len + 1] = (byte) ((chk >> 8) & 0xff);
        return data;
    }
}
