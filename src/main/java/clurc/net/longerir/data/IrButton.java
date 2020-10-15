package clurc.net.longerir.data;

public class IrButton {
  final public static int LONGER_NEC=51;
  final public static int LONGER_RC5=45;
  private int snumber;
  private int page;
  private int protocol;
  private int[] params;
  private int[] wave;
  private int freq;

  public IrButton(int page, int snumber, int protocol, int[] params){
    this.page =page;
    this.snumber = snumber;
    this.protocol = protocol;
    this.params = params;
    wave = null;
  }

  public IrButton(int page, int snumber,int[] wave,int freq){
    this.page =page;
    this.snumber = snumber;
    this.protocol = -1;
    this.params = null;
    this.wave = wave;
    this.freq = freq;
  }

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public int getProtocol() {
    return protocol;
  }
  public void setProtocol(int protocol) {
    this.protocol = protocol;
  }

  public int getSnumber() {
    return snumber;
  }

  public void setSnumber(int snumber) {
    this.snumber = snumber;
  }

  public int[] getParams() {
    return params;
  }
  public void setParams(int[] params) {
    this.params = params;
  }

  public int[] getWave() {
    return wave;
  }

  public void setWave(int[] wave) {
    this.wave = wave;
  }

  public int getFreq() {
    return freq;
  }

  public void setFreq(int freq) {
    this.freq = freq;
  }
}
