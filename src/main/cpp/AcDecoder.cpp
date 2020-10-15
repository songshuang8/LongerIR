//
//  AcDecoder.cpp
//  AcirTest
//
//  Created by songshuang on 17/8/4.
//  Copyright © 2017年 songshuang. All rights reserved.
//
#include <string.h>
#include <malloc.h>
#include "AcIrMaths.h"
#include "AcDecoder.h"

int getBtnIdx(int v) {
    int ret = v - 1;
    if (v < 3)
        ret = v;
    return ret;
}

int getBtnDataLen(int btnidx) {
    int ret = 4;
    switch (btnidx) {
        case 2:
            ret = 8;
            break;
        case 3:
            ret = 7;
            break;
        case 5:
            ret = 3;
            break;
    }
    return ret;
}

int getBtnStatusLen(int btnidx){
    int ret = 4;
    switch (btnidx) {
        case 0:
            ret = 2;
            break;
        case 1:
            ret = 15;
            break;
        case 2:
            ret = 5;
            break;
        case 3:
            ret = 4;
            break;
        case 4:
            ret = 2;
            break;
        case 5:
            ret = 7;
            break;
    }
    return ret;
}

int ChkFunctionA(int chkKind, int w, int val, int currval) {
    int ret = currval;
	int i;
    switch (chkKind) {
        case 0:
            ret = ret + val;
            break;
        case 1:
            ret = ret & val;
            break;
        case 2:
            ret = ret | val;
            break;
        case 3:
            ret = ret ^ val;
            break;
        case 4:
            for (i = 0; i < w; i++) {
                if (MYBIT_GET(val, i))
                    ret++;
            }
            break;
        case 5:
            int m = 0;
            for (i = 0; i < w; i++) {
                if (MYBIT_GET(val, i))
                    m++;
                if ((m % 2) == 1)
                    ret++;
            }
            break;
    }
    return ret;
}

int ChkFunctionB(int chkKind, int w, int con, int currval) {
    int ret = currval;
    switch (chkKind) {
        case 0:
            ret = ret + con;
            break;
        case 1:
            ret = ret - con;
            break;
        case 2:
            ret = con - ret;
            break;
        case 3:
            ret = ret & con;
            break;
        case 4:
            ret = ret | con;
            break;
        case 5:
            ret = ret ^ con;
            break;
        case 6:
            ret = con;
            break;
        case 7:
            int b = ret;
            ret = ((b >> 4) & 0xf) + (b >> 4);
            break;
    }
    return ret;
}

AcDecoder::AcDecoder(unsigned char * buf,int alen){
    Fdata = buf;
    srclen = alen;
    Ferr = 0;
    for(int i=0;i<BTN_COUNT;i++){
        parts[i].func = NULL;
        parts[i].funclen = 0;
        tables[i].data = NULL;
        tables[i].groupCount=0;
    }
    baseCount = 0;
    inits.funclen = 0;
    inits.func = NULL;
    Fgsdata.func = NULL;
    Fgsdata.funclen = 0;
    Fchkdata.groupCount=0;
    Fchkdata.data=NULL;
}

AcDecoder::~AcDecoder(){
    for(int i=0;i<BTN_COUNT;i++){
        if(tables[i].data!=NULL){
            free(tables[i].data);
        }
    }
    if(Fchkdata.data!=NULL)
        free(Fchkdata.data);
}

void AcDecoder::setStatus(AcStatus * asta){
    memcpy(&status, asta,sizeof(AcStatus));
}

void AcDecoder::getStatus(AcStatus * asta){
    memcpy(asta,&status,sizeof(AcStatus));
}

int AcDecoder::getFreq(){
    return Freq;
}

bool AcDecoder::getIRData(int *irdata,int * irlen){
    if(!DoParaseData()) // 分析提取各数据
        return false;
    if(!DoChangeBase()) // 修改基础数据
        return false;
    if (((Fflag >> 4) & 1) == 1)
        if (!DoChkCalculate())// 校验计算
            return false;
    DoMakeIrData(irdata,irlen);
    return (*irlen>0)?true:false;
}

bool AcDecoder::doBaseData(unsigned char *src,int alen){
	int i;
    int ptr = 0;
    int len = 0;
    for (i = 0; i < Fgsidx; i++) {
        if ((ptr + 1) > alen)
            return false;
        len = src[ptr] & 0xff;
        ptr += (len + 2);
    }
    if ((ptr + 1) > alen)
        return false;
    // base data
    baseCount = (src[ptr] & 0xff) - 1;
    if (baseCount < 0)
        return false;
    if ((alen - ptr) < (baseCount + 3))
        return false;
    ptr++;
    Fbase = src+ptr;
    ptr += baseCount;
    int iniCount = src[ptr] & 0xff;
    ptr++;
    int initbtn = (src[ptr] & 7);
    int iniGroup = ((src[ptr] >> 3) & 3);
    int bInitKinds = (src[ptr] >> 7) & 1;
    ptr++;
    //////////
    for (i = (Fgsidx + 1); i < Fgscount; i++) {
        if ((ptr + 1) > alen)
            return false;
        len = src[ptr] & 0xff;
        ptr += len + 2;
    }
    ;
    if ((ptr + 1) > alen)
        return false;
    // btn data
    for (i = 0; i < BTN_COUNT; i++) {
        parts[i].funclen = 0;
        if ((ptr + 1) > alen)
            return false;
        len = src[ptr] & 0xff;
        ptr++;
        
        if (len == 0)continue;

        if ((ptr + len + 1) > alen)
            return false;
        //
        if (bInitKinds == 0) {
            if (i == initbtn && iniCount > 0) {
                inits.func = src+ptr;
                inits.funclen=(iniCount + 1);
                ptr += inits.funclen;
                len -= (iniCount + 1);
            }
        }
        ////
        parts[i].funclen =(len + 1);
        if(parts[i].funclen>0)
            parts[i].func = src+ptr;
        ptr += (len + 1);
    }
    //// init data kind=1
    if (bInitKinds == 0) {
        return true;
    }
    //
    for (i = 0; i < iniGroup; i++) {
        if ((ptr + 1) > alen)
            return false;
        len = src[ptr] & 0xff;
        ptr += (len + 1 + 1);
    }
    //
    len = src[ptr] & 0xff;
    if (len > 0) {
        inits.funclen = (len + 1);
        ptr++;
        if ((ptr + len + 1) > alen)
            return false;
        if(inits.funclen>0)
            inits.func = src+ptr;
    }
    return true;
}

bool AcDecoder::getIfHadTableData(){
	int i;
    int w = 4;
    int alen = inits.funclen / w;
    if ((alen * w) != inits.funclen) {
        Ferr = 10;
        return false;
    }
    for (i = 0; i < alen; i++) {
        if (((inits.func[i * w + 1] >> 7) & 1) == 1) {
            if (((inits.func[i * w + 2] >> 7) & 1) == 0)
                return true;
        }
    }
    ///
    for (i = 0; i < BTN_COUNT; i++) {
        w = getBtnDataLen(i);
        alen = parts[i].funclen / w;
        if ((alen * w) != parts[i].funclen) {
            Ferr = 10;
            return false;
        }
        for (int j = 0; j < alen; j++) {
            if (w == 4 || w == 3) {
                if (((parts[i].func[j * w + 1] >> 7) & 1) == 1) {
                    
                    if (((parts[i].func[j * w + 2] >> 7) & 1) == 0)
                        return true;
                }
            } else {
                for (int k = 0; k < (w - 3); k++) {
                    if (((parts[i].func[j * w] >> (7 - k)) & 1) == 1) {
                        
                        if (((parts[i].func[j * w + 3 + k] >> 7) & 1) == 0)
                            return true;
                    } //
                }
            }
        }
    }
    return false;
}

bool AcDecoder::DoParaseData(){
	int i;
    int addr[5];
    if (srclen < 10)
        return false;
    for (int i = 0; i < 5; i++) {
        addr[i] = ByteToShort(Fdata, i * 2);
        addr[i]-=0x30;
    }
    // ini data
    int ptr = addr[0];
    if ((ptr + 2) > srclen)
        return false;
    int mw = ByteToShort(Fdata, ptr) - 0x30;
    ptr = mw;
    if ((ptr + 1) > srclen)
        return false;
    Fflag = (char) (Fdata[ptr] & 0xff);
    Flowpre = !MYBIT_GET(Fflag, 5);
    ptr++;
    //
    if(status.status[5] > 0)
    if(!MYBIT_GET(Fflag,2))
        status.status[5] = 0;
    // look gsidx
    Fgsidx = 0;
    Fgscount = (Fflag & 3) + 1;
    if (Fgscount > 0) {
        if (MYBIT_GET(Fflag, 4)) // 有无校验
            ptr += 2;
        ptr += 4;
        
        if ((ptr + 2) > srclen)
            return false;
        
        mw = ByteToShort(Fdata, ptr);
        if (status.pressbtn == 0) {
            if (status.pressval == 0)
                Fgsidx = (mw & 3);
            else
                Fgsidx = ((mw >> 2) & 3);
        } else
            Fgsidx = ((mw >> (status.pressbtn * 2 + 2)) & 3);
    }
    // relashion
    ptr = addr[3];
    if ((ptr + 2) > srclen)
        return false;
    mw = ByteToShort(Fdata, ptr) - 0x30;
    ptr = mw;
    if ((ptr + 1) > srclen)
        return false;
    if (!doBaseData(Fdata+ptr,srclen - ptr))
        return false;
    // table FData-----------------------------------------
    ptr = addr[2];
    mw = ByteToShort(Fdata, ptr) - 0x30;
    ptr = mw;
    if ((ptr + 6) > srclen)
        return false;
    int ptrsub = ptr + 6;
    bool bHadTables = getIfHadTableData();
    if (Ferr != 0)
        return false;
    if (bHadTables) {
        for (int i = 0; i < BTN_COUNT; i++) {
            int len = (Fdata[ptr] & 0xf);
            if (len > 0) {
                int k = ((Fdata[ptr] >> 4) & 0xf) + 1;
                tables[i].groupCount = k;
                tables[i].data = (AcTableData *)malloc(sizeof(AcTableData)*k);
                for (int j = 0; j < k; j++) {
                    if ((ptrsub + len) > srclen)
                        return false;
                    tables[i].data[j].buf = (Fdata+ptrsub);
                    tables[i].data[j].len = len;
                    ptrsub += len;
                }
            } else {
                tables[i].groupCount = 0;
            }
            ptr++;
        }
    }
    ///////////// gs data ------------------------------
    ptr = addr[1] + Fgsidx * 2;
    mw = ByteToShort(Fdata, ptr) - 0x30;
    ptr = mw;
    if ((ptr + 1) > srclen)
        return false;
    mw = Fdata[ptr] & 0xff;
    ptr++;
    if ((ptr + mw) > srclen)
        return false;
    Fgsdata.funclen = mw;
    if (mw > 0) {
        Fgsdata.func = (Fdata+ptr);
    }
    // crc
    if (((Fflag >> 4) & 1) == 1) { // 有校验
        ptr = addr[4];
        mw = ByteToShort(Fdata, ptr) - 0x30;
        ptr = mw;
        if ((ptr + 1) > srclen)
            return false;
        mw = Fdata[ptr] & 0xff; // crc count
        Fchkdata.groupCount =(mw+1);
        Fchkdata.data = (AcTableData *)malloc(sizeof(AcTableData)*(mw+1));
        ptr++;
        for (i = 0; i <= mw; i++) {
            if ((ptr + 1) > srclen)
                return false;
            int j = Fdata[ptr] & 0xff;
            ptr++;
            if ((ptr + j + 1) > srclen)
                return false;
            Fchkdata.data[i].len = j+1;
            Fchkdata.data[i].buf = (Fdata+ptr);
            ptr += (j + 1);
        }
        ////////////
        for (i = 0; i < Fchkdata.groupCount; i++) {
            mw = Fchkdata.data[i].len - 3;
            int len = mw / 5;
            if ((5 * len) != mw)
                return false;
        }
        ///////
        if (ptr != srclen)
            return false;
    }
    return true;
}

void AcDecoder::SetBaseBitValue(int bytex, int bitx){
    Fbase[bytex] |= (1 << bitx);
}

void AcDecoder::sub_setvaleLittle(int bytex, int bitx, int bitw, int val){
	int i;
    int bwid = bitw / 8 + 1;
    int bto = bytex + bwid;
    if (bto >= baseCount)
        bto = baseCount - 1;
    bwid = bto - bytex + 1;
    unsigned char *bitArr = (unsigned char *)malloc(bwid * 8);
    //
    int n = 0;
    for (i = bytex; i <= bto; i++)
        for (int j = 0; j < 8; j++) {
            bitArr[n] = ((Fbase[i] >> j) & 1);
            n++;
        }
    //
    n = 0;
    for (i = bitx; i <= (bitx + bitw - 1); i++) {
        bitArr[i] = ((val >> n) & 1);
        n++;
    }
    //
    n = 0;
    for (i = bytex; i <= bto; i++) {
        Fbase[i] = 0;
        for (int j = 0; j < 8; j++) {
            if (bitArr[n] == 1)
                SetBaseBitValue(i, j);
            n++;
        }
    }
    free(bitArr);
}

void AcDecoder::sub_setvaleBigger(int bytex, int bitx, int bitw, int val){
	int i;
    int bwid = bitw / 8 + 1;
    int bto = bytex + bwid;
    if (bto >= baseCount)
        bto = baseCount - 1;
    bwid = bto - bytex + 1;
    unsigned char * bitArr = (unsigned char *)malloc(bwid * 8);
    //
    int n = 0;
    for (i = bytex; i <= bto; i++)
        for (int j = 0; j < 8; j++) {
            bitArr[n] = ((Fbase[i] >> (7 - j)) & 1);
            n++;
        }
    //
    n = bitw - 1;
    for (i = bitx; i <= (bitx + bitw - 1); i++) {
        bitArr[i] = ((val >> n) & 1);
        n--;
    }
    //
    n = 0;
    for (i = bytex; i <= bto; i++) {
        Fbase[i] = 0;
        for (int j = 0; j < 8; j++) {
            if (bitArr[n] == 1)
                SetBaseBitValue(i, 7 - j);
            n++;
        }
    }
    free(bitArr);
}

void AcDecoder::sub_setvale(int bytex, int bitx, int bitw, int val){
    if (Flowpre)
        sub_setvaleLittle(bytex, bitx, bitw, val);
    else
        sub_setvaleBigger(bytex, bitx, bitw, val);
}

void AcDecoder::sub_dochge(int bp, int bbit, int val){
	int mw,mbits;
    if (bp >= baseCount)return; //字节号
    mw = ((bbit >> 3) & 7) + 1;  // 宽
    mbits = bbit & 7;   //位号
    sub_setvale(bp,mbits,mw,val);
}

int AcDecoder::sub_getTableValue(int btnx, int groupx){
    int ret = 0;
    if(btnx > 5){
        Ferr = 1;
        return ret;
    }
    if(tables[btnx].groupCount==0){
        Ferr = 1;
        return ret;
    }
    
    if (groupx >= tables[btnx].groupCount) {
        Ferr = 2;// 这里有几组需要修正
        return ret;
    }
    
    int idx = status.status[btnx];
    
    if (btnx == 1 && tables[btnx].data[groupx].len == 2) {
        if(tables[btnx].data[groupx].buf[0]<tables[btnx].data[groupx].buf[1])
            ret = tables[btnx].data[groupx].buf[0] + idx;
        else
            ret = tables[btnx].data[groupx].buf[0] - idx;
        return ret;
    }
    
    if (btnx == 5 && MYBIT_GET(Fflag, 2) == false) {
        if (idx > 0) {
            status.status[btnx]=0;
            idx = 0;
        }
    }
    
    if (idx >= tables[btnx].data[groupx].len) {
        Ferr = 3;
        ret = tables[btnx].data[groupx].buf[0];
        return ret;
    }
    return tables[btnx].data[groupx].buf[idx];
}

void AcDecoder::sub_setStatusByTable(int btnx, int groupx,int val){
    if(btnx > 5){
        Ferr = 1;
        return;
    }
    if(tables[btnx].groupCount==0){
        Ferr = 1;
        return;
    }

    if (groupx >= tables[btnx].groupCount) {
        Ferr = 2;// 这里有几组需要修正
        return;
    }

    if (btnx == 1 && tables[btnx].data[groupx].len == 2) {
        if(tables[btnx].data[groupx].buf[1] > tables[btnx].data[groupx].buf[0]) {
            int m = 0;
            for (int i = tables[btnx].data[groupx].buf[0];
                 i <= tables[btnx].data[groupx].buf[1]; i++) {
                if (i == val) {
                    if (m < getBtnStatusLen(btnx))
                        status.status[btnx] = m;
                    break;
                }
                m++;
            }
        }else{
            int m = 0;
            for (int i = tables[btnx].data[groupx].buf[0];
                 i >= tables[btnx].data[groupx].buf[1]; i--) {
                if (i == val) {
                    if (m < getBtnStatusLen(btnx))
                        status.status[btnx] = m;
                    break;
                }
                m++;
            }
        }
        return;
    }
    for(int i=0;i<tables[btnx].data[groupx].len;i++) {
        if (tables[btnx].data[groupx].buf[i] == val) {
            if (i < getBtnStatusLen(btnx))
                status.status[btnx] = i;
            break;
        }
    }
}

bool AcDecoder::sub_Cp_ChangeParts4(unsigned char *b){
    unsigned char bFlag1 = 0;
    int bT = 0;
    if(b==NULL){
        Ferr=1000;
        return false;
    }
    if (status.pressval == 0) {
        bFlag1 = MYBIT_GET(b[1], 7);
        bT = b[2];
    } else {
        bFlag1 = MYBIT_GET(b[1], 6);
        bT = b[3];
    }
    if (bFlag1) {
        int m = getBtnIdx((bT >> 4) & 7);
        int n = bT & 0xf;
        if (MYBIT_GET(bT, 7)) {
            if (sub_JumbBtn(m, n) == false)
                return false;
        } else {
            bT = sub_getTableValue(m, n);
            sub_dochge(b[0], b[1], bT);
        }
    } else {
        sub_dochge(b[0], b[1], bT);
    }
    return true;
}

bool AcDecoder::sub_Cp_ChangeParts3(unsigned char *b){
    if (MYBIT_GET(b[1], 7)) {
        int m = getBtnIdx((b[2] >> 4) & 7);
        int n = b[2] & 0xf;
        if (MYBIT_GET(b[2], 7)) {
            if (sub_JumbBtn(m, n) == false)
                return false;
        } else {
            int bx = sub_getTableValue(m, n);
            sub_dochge(b[0], b[1], bx);
        }
    } else
        sub_dochge(b[0], b[1], b[2]);
    return true;
}

bool AcDecoder::sub_JumbBtn(int btnPtr, int gidx){
    int w = getBtnDataLen(btnPtr);
    int alen = parts[btnPtr].funclen / w;
    if (gidx >= alen)
        return false;
    if (w == 4) {
        return sub_Cp_ChangeParts4(parts[btnPtr].func+gidx * w);
    } else if (w == 3) {
        return sub_Cp_ChangeParts3(parts[btnPtr].func+gidx * w);
    } else {
        int b = parts[btnPtr].func[gidx * w];
        int bbit = status.status[btnPtr];
        if (MYBIT_GET(b, (7 - bbit))) {
            b = parts[btnPtr].func[gidx * w + 3 + bbit];
            int m = getBtnIdx((b >> 4) & 7);
            int n = b & 0xf;
            if (MYBIT_GET(b, 7)) {
                if (!sub_JumbBtn(m, n))
                    return false;
            } else {
                b = sub_getTableValue(m, n);
                sub_dochge(parts[btnPtr].func[gidx * w + 1], parts[btnPtr].func[gidx * w + 2], b);
            }
        } else {
            int theval = parts[btnPtr].func[gidx * w + 3 + bbit];
            sub_dochge(parts[btnPtr].func[gidx * w + 1],parts[btnPtr].func[gidx * w + 2],theval);
            //修改状态     look goup btn
            alen = getBtnStatusLen(btnPtr);
            bbit = parts[btnPtr].func[gidx * w];
            for(int i=0;i<alen;i++) {
                if (MYBIT_GET(bbit, (7 - i))) {
                    b = parts[btnPtr].func[gidx * w + 3 + i];
                    int m = getBtnIdx((b >> 4) & 7);
                    int n = b & 0xf;
                    if (!MYBIT_GET(b, 7)) {
                        sub_setStatusByTable(m, n, theval);
                        return true;
                    }
                }
            }
        }
    }
    return true;
}

bool AcDecoder::DoChangeBase(){
	int i;
    int w = 4;
    int len = inits.funclen / w;
    for (i = 0; i < len; i++) {
        if (!sub_Cp_ChangeParts4(inits.func+i * w))
            return false;
    }
    ///
    int n = status.pressbtn;
    w = getBtnDataLen(n);
    len = parts[n].funclen / w;
    for (i = 0; i < len; i++) {
        if (!sub_JumbBtn(n, i))
            return false;
    }
    return true;
}


bool AcDecoder::DoChkCalculate(){
	int k;
    int btnpos = 0;
    if (status.pressbtn == 0) {
        if (status.pressval == 0)
            btnpos = 0;
        else
            btnpos = 1;
    } else
        btnpos = status.pressbtn + 1;
    
    for (int i = 0; i < Fchkdata.groupCount; i++) {
        if (!MYBIT_GET(Fchkdata.data[i].buf[0], btnpos))
            continue;
        /////////////////////////////
        int val = 0;
        int m = Fchkdata.data[i].len - 3;
        int alen = m / 5;
        int bFunc8 = 4;
        for (int j = 0; j < alen; j++) {
            if (MYBIT_GET(Fchkdata.data[i].buf[j * 5 + 3 + 0], 6))
                bFunc8 = 8;
            else
                bFunc8 = 4;
            if (MYBIT_GET(Fchkdata.data[i].buf[j * 5 + 3 + 1], 6)) {
                int bfrom = Fchkdata.data[i].buf[j * 5 + 3 + 0] & 0x3f;
                int bto = Fchkdata.data[i].buf[j * 5 + 3 + 1] & 0x3f;
                bool bBit4 = MYBIT_GET(Fchkdata.data[i].buf[j * 5 + 3 + 0], 7);
                if (bBit4) {
                    if (bfrom < baseCount) { // 高低位？
                        if (Flowpre)
                            val=ChkFunctionA(Fchkdata.data[i].buf[j * 5 + 3 + 2] & 0xf,
                                             bFunc8,
                                             (Fbase[bfrom] >> 4) & 0xf,
                                             val);
                        else
                            val=ChkFunctionA(Fchkdata.data[i].buf[j * 5 + 3 + 2] & 0xf,
                                             bFunc8,
                                             Fbase[bfrom] & 0xf,
                                             val);
                    }
                    bfrom++;
                }
                ///////////
                int n = bto;
                if (!MYBIT_GET(Fchkdata.data[i].buf[j * 5 + 3 + 1], 7))
                    n = bto - 1;
                for (k = bfrom; k <= n; k++) {
                    if (k >= baseCount)
                        continue;
                    if (bFunc8 == 8)
                        val=ChkFunctionA(Fchkdata.data[i].buf[j * 5 + 3 + 2] & 0xf,
                                         bFunc8,
                                         Fbase[k],
                                         val);
                    else {
                        val=ChkFunctionA(Fchkdata.data[i].buf[j * 5 + 3 + 2] & 0xf,
                                         bFunc8,
                                         Fbase[k] & 0xf,
                                         val);
                        val=ChkFunctionA(Fchkdata.data[i].buf[j * 5 + 3 + 2] & 0xf,
                                         bFunc8,
                                         (Fbase[k] >> 4) & 0xf,
                                         val);
                    }
                    ;
                }
                ////////////////////////////////
                if (!MYBIT_GET(Fchkdata.data[i].buf[j * 5 + 3 + 1], 7)) {
                    if (Flowpre)
                        val=ChkFunctionA(Fchkdata.data[i].buf[j * 5 + 3 + 2] & 0xf,
                                         bFunc8,
                                         Fbase[bto] & 0xf,
                                         val);
                    else
                        val=ChkFunctionA(Fchkdata.data[i].buf[j * 5 + 3 + 2] & 0xf,
                                         bFunc8,
                                         (Fbase[bto] >> 4) & 0xf,
                                         val);
                }
                ///////////////////// 外部算法//////////////////////////////////////////////////////////////////
                val=ChkFunctionB((Fchkdata.data[i].buf[j * 5 + 3 + 2] >> 4) & 0xf,
                                 bFunc8,
                                 Fchkdata.data[i].buf[j * 5 + 3 + 4],
                                 val);
            } else
                val=ChkFunctionB(Fchkdata.data[i].buf[j * 5 + 3 + 3] & 0xf,
                                 bFunc8,
                                 Fchkdata.data[i].buf[j * 5 + 3 + 4],
                                 val);
        }
        ///
        int bfrom = Fchkdata.data[i].buf[1];
        if (bfrom >= baseCount)
            return false;
        int bto = Fchkdata.data[i].buf[2] & 7;// 位
        k = ((Fchkdata.data[i].buf[2] >> 3) & 7) + 1; // 位宽
        sub_setvale(bfrom, bto, k, val);
    }
    return true;
}

void AcDecoder::DoMakeIrData(int *irdata,int * irlen){
	int i;
    *irlen=0;
    int bitsKind = Fgsdata.func[0];
    Freq = Fgsdata.func[1];
    if (Freq != 0)
        Freq = (int) ((Freq * 1000) / ex_con_freqx);
    int StruCount = (Fgsdata.func[2] & 0x3f) + 1;
    int logicCount = ((Fgsdata.func[2] >> 6) & 3) + 1;
    if ((StruCount + logicCount * 4 + 3) > Fgsdata.funclen)
        return;
    AcIRHL *logic = (AcIRHL *)malloc(sizeof(AcIRHL)*logicCount);
    int ptr = 3;
    for (i = 0; i < logicCount; i++) {
        logic[i].x = Fgsdata.func[ptr] + ((Fgsdata.func[ptr + 1] >> 1) << 8);
        if ((Fgsdata.func[ptr + 1] & 1) == 0)
            logic[i].x = -logic[i].x;
        
        ptr += 2;
        logic[i].y = Fgsdata.func[ptr] + ((Fgsdata.func[ptr + 1] >> 1) << 8);
        if ((Fgsdata.func[ptr + 1] & 1) == 0)
            logic[i].y = -logic[i].y;
        logic[i].y -= 40;
        ptr += 2;
    }
    unsigned char *strus = Fgsdata.func+ptr;
    ptr += StruCount;
    ///
    int len = Fgsdata.funclen - (StruCount + logicCount * 4 + 3);
    int waveCount = len / 5;
    if ((waveCount * 5) != len)
        return;
    AcIRHL *wave =(AcIRHL *)malloc(sizeof(AcIRHL)*waveCount);
    for (i = 0; i < waveCount; i++) {
        wave[i].x = Fgsdata.func[ptr] + ((Fgsdata.func[ptr + 1] >> 1) << 8);
        if ((Fgsdata.func[ptr + 1] & 1) == 0)
            wave[i].x = -wave[i].x;
        ptr += 2;
        
        wave[i].y = Fgsdata.func[ptr] + (Fgsdata.func[ptr + 1] << 8) + ((Fgsdata.func[ptr + 2] >> 1) << 16);
        if ((Fgsdata.func[ptr + 2] & 1) == 0)
            wave[i].y = -wave[i].y;
        if (wave[i].y < 0)
            wave[i].y -=60;
        ptr += 3;
    }
    ///////////// 第5位为重复点 + 重复数 d6 d7 = bit kinds
    i = 0;
    int rptArrLen  = 0;
    unsigned char * rptStrus = (unsigned char *)malloc(StruCount*50);
    while (i < StruCount) {
        if (((strus[i] >> 5) & 1) == 1) {
            int rpt = (strus[i] & 0xf);
            if (rpt > 0) {
                int r1 = i + 1;
                int r2 = -1;
                for (int j = r1; j < StruCount; j++) {
                    if (((strus[j] >> 5) & 1) == 1) {
                        r2 = j - 1;
                        break;
                    }
                }
                if (r1 >= 0 && r2 > r1) {
                    for (int k = 1; k <= rpt; k++)
                        for (int j = r1; j <= r2; j++){
                           rptStrus[rptArrLen++]=strus[j];
                        }
                    i = r2 + 2;
                } else {
                    rptStrus[rptArrLen++]=strus[i];
                    i++;
                }
            } else { // 这里的第5位 有限制超过32？
                rptStrus[rptArrLen++]=strus[i];
                i++;
            }
        } else {
            rptStrus[rptArrLen++]=strus[i];
            i++;
        }
    }
    ///--------------------------------------------------------
    int *irhl  = (int *)malloc(sizeof(int)*rptArrLen*2*8);
    len = 0;
    for (i = 0; i < rptArrLen; i++) {
        int sb = (rptStrus[i] >> 6) & 3;
        ptr = rptStrus[i] & 0x3f;
        if (sb == 0) {
            irhl[len++]=wave[ptr].x;
            irhl[len++]=wave[ptr].y;
        } else {
            if (ptr >= baseCount)
                break;
            int bits = 7;
            if (sb == 2)
                bits = bitsKind & 7;
            else if (sb == 3)
                bits = (bitsKind >> 3) & 7;
            /////////////////////////////////////////////
            if (Flowpre) {
                for (int j = 0; j <= bits; j++) {
                    if (MYBIT_GET(Fbase[ptr], j)) {
                        irhl[len++]=logic[1].x;
                        irhl[len++]=logic[1].y;
                    } else {
                        irhl[len++]=logic[0].x;
                        irhl[len++]=logic[0].y;
                    }
                }
            } else {
                sb = 7 - bits;
                for (int j = 7; j >= sb; j--) {
                    if (MYBIT_GET(Fbase[ptr], j)) {
                        irhl[len++]=logic[1].x;
                        irhl[len++]=logic[1].y;
                    } else {
                        irhl[len++]=logic[0].x;
                        irhl[len++]=logic[0].y;
                    }
                }
            }
        }
    }
    free(logic);
    free(wave);
    free(rptStrus);
    getHebingData(irhl,len,irdata,irlen);
    free(irhl);
    return;
}

