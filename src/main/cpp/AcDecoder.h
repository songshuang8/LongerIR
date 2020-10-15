//
//  AcDecoder.hpp
//  AcirTest
//
//  Created by songshuang on 17/8/4.
//  Copyright © 2017年 songshuang. All rights reserved.
//

#ifndef AcDecoder_hpp
#define AcDecoder_hpp

#include <stdio.h>
#include "native-lib.h"

struct AcByteArr{
    unsigned char *func;
    int funclen;
};

struct AcTableData{
    unsigned char *buf;
    int len;
};

struct AcBtnTables{
    AcTableData *data;
    int groupCount;
};

struct AcIRHL{
    int x;
    int y;
};

class AcDecoder{
public:
    AcDecoder(unsigned char * buf,int alen);
    ~AcDecoder();
    bool getIRData(int *irdata,int * irlen);
    void setStatus(AcStatus * asta);
    void getStatus(AcStatus * asta);
    int getFreq();
protected:
    bool DoParaseData();
    bool DoChangeBase();
    bool DoChkCalculate();
    void DoMakeIrData(int *irdata,int * irlen);
    
private:
    int srclen;
    unsigned char *Fdata;
    
    bool Flowpre;
    char Fflag;
    int Fgsidx;// 格式序号//
    int Fgscount;
    int Freq;// 载波 一般位38000
    int Ferr;
    
    AcStatus status;
    
    int baseCount;
    unsigned char *Fbase;
    
    AcByteArr parts[BTN_COUNT];
    AcByteArr inits;
    AcBtnTables tables[BTN_COUNT];
    AcByteArr Fgsdata;
    AcBtnTables Fchkdata;
    
    bool doBaseData(unsigned char *src,int alen);
    
    int sub_getTableValue(int btnx, int groupx);
    void sub_setStatusByTable(int btnx,int groupx,int val);
    
    void SetBaseBitValue(int bytex, int bitx);
    void sub_setvaleLittle(int bytex, int bitx, int bitw, int val);
    void sub_setvaleBigger(int bytex, int bitx, int bitw, int val);
    
    void sub_setvale(int bytex, int bitx, int bitw, int val);
    
    void sub_dochge(int bp, int bbit, int val);
    bool sub_Cp_ChangeParts4(unsigned char *b);
    bool sub_Cp_ChangeParts3(unsigned char *b);
    bool sub_JumbBtn(int btnPtr, int gidx);
    
    bool getIfHadTableData();
};

#endif /* AcDecoder_hpp */
