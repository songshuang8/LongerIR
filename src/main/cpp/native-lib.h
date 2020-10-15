//
//  AcConst.h
//  AcirTest
//
//  Created by songshuang on 17/8/4.
//  Copyright © 2017 songshuang. All rights reserved.
//

#ifndef NATIVE_LIB_h
#define NATIVE_LIB_h

#define BTN_COUNT 6  // key count
#define ex_con_freqx  2.7368

#define MYBIT_GET(var, bit)    ((var >> bit) & 0x01)
#define MYBIT_SET(var, bit)    (var |= (1 << bit))
#define MYBIT_CLEAR(var, bit)  (var &= ~(1 << bit))

#define ByteToShort(var,pos) (var[pos] + (var[pos+1]<<8))


#define BTN_POW 0
#define BTN_TEMP 1
#define BTN_MODE 2
#define BTN_FAN 3
#define BTN_SWING 4
#define BTN_VANE 5

struct AcFileTitle{
    int ver;
    int count;
    long long size;
};

struct AcFileInfo{
    char brand[32];
    char pid[6];
    unsigned short len;
};

struct IrItem{
    unsigned short len;
    unsigned short pid;
};

struct AcStatus{
    unsigned char status[BTN_COUNT];
    unsigned char pressbtn;
    unsigned char pressval;
};

//----------------remote info
struct TRemoteFileTitle { ;
    unsigned  int VER;
    unsigned  int SETS;
    unsigned  int PP;
    unsigned  int XH;
    unsigned  int size;
};

struct TAppPPSTRU {
    unsigned  int Ptr;
    char Name[16];
};

struct TAppXHSTRU {
    unsigned  short mpp;
    unsigned  short code;
    unsigned char name[31];
    unsigned char dev;
};

struct TKeyDbItemStru {
    unsigned char keycount;
    unsigned char maxparam;
    unsigned short pid;
};

#endif /* AcConst_h */
