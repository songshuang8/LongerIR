package clurc.net.longerir.base;

public class MobileEncode {
    private static int con_ir_high = 10;
    private static int con_ir_0 = 160;
    private static int con_ir_1 = 240;
    private static int con_ir_start = 120;
    private static int con_ir_end = 80;

    public static int[] longer_ir_encoder(byte[] data8){
        int[] ret = new int[data8.length*20-1];
        int n=0;
        for (int i = 0; i < data8.length; i++) {
            ret[n++] = con_ir_high;
            ret[n++] = con_ir_start;
            for (int j = 7; j >=0; j--) {
                ret[n++] = con_ir_high;
                int aval = data8[i]&0xff;
                if(((aval>>j)&1)==1){
                    ret[n++] = con_ir_1;
                }else{
                    ret[n++] = con_ir_0;
                }
            }
            ret[n++] = con_ir_high;
            if(i!=(data8.length-1))
                ret[n++] = con_ir_end;
        }
        return ret;
    }
}
