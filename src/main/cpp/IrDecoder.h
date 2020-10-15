//
// Created by Administrator on 2017/8/13 0013.
//

#include <android/log.h>

#ifndef MYAPPLICATION3_IRDECODER_H
#define MYAPPLICATION3_IRDECODER_H

#define CON_IRINVAALID 0xffffff

#define LOG_TAG "SSJNI_LOG"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

struct TAppLogic{
    int alogic[6];
    int count;
};


struct TAppMain{
	char name[16];//����
	int Freg;   //Ƶ��
	//////-------------------
	TAppLogic logic[16];
	unsigned char logicCount;
	unsigned char DataBit;
	short BaseTime;  //�������ʱ�� us
	//
	unsigned char perCodeCount[8];
	unsigned char framecount;
	//
	bool PreLow;   //��λ��ǰ      boolean or bool ����???
	bool MustIncData;  ///ÿһλ����Ҫ���ӵ�ƽ��
};

struct TAppFramesStru{
   int NO;   //���
   short sameAsNo;
   short FanmaAsNo;
//
   int Length;
   int DCLength;       //0 is no dc,big than 0 is dc;   us
   //UselogicTotal:boolean;
   unsigned char frameKind;
   bool Repeated;    //�Ƿ��ظ���
   //
   //logic:array[0..1] of TAppLogic;
};

struct TAppCodeStru{
	unsigned char NO;         //���
	bool Logic;
    short sameAsNo;
    short FanmaAsNo;

    char CodeName[12];
    unsigned char BitCount;
    unsigned char codekind;
    bool ParamsChge;  //true ���Ըı䣬false�����ɸı�Ĳ���
    unsigned char wavecount;
   //
    long long Params;
    int Length;      //�볤�� us
   //
    TAppLogic wave[2];
   //
    char ParamsFunc[64];  //У��λ���㹫ʽ
   //
    bool hadPart;
    unsigned char bPartFr;
    unsigned char bPartTo;
};

struct ir_frames{
	TAppFramesStru *pf;
	unsigned int codecount;
	TAppCodeStru **pc;
};

class IrDecoder {
public:
    IrDecoder(unsigned char * buf,int alen, long long *param,unsigned int aparacount);
    ~IrDecoder();
    void SetSeeds(unsigned char mseeds);
    int getIRData(int *irdata);
    char *getPID();
	int getZb();
protected:
    bool parzegs();
    bool setParams();
    unsigned int RenderCodeData(TAppFramesStru *APFrame,
    		TAppCodeStru *APCode,TAppCodeStru *APSameCode,
			unsigned int CurrLen,bool FanMaBool);
    TAppCodeStru *LookMyTreeNodeFromNo(int index);
    ir_frames *LookMyFrameTreeNodeFromNo(int index);
    unsigned int GetCheckCode(char * chkstr);
    int AddTreeBitData(unsigned char bitcount,
        		long long params,
        	    bool hadPart,
        	    unsigned char bPartFr,
        	    unsigned char bPartTo,bool FanMaBool);
    int AddTreeWaveData(TAppLogic *waveArr,
            		int currlen,int dclen);
    int AddTreeLogicData(TAppLogic *logicArr);
    void AddData(int data);
private:
    unsigned int srclen;
    unsigned char *Fdata;
    unsigned int paramcount;
    long long *Fparam;
    unsigned char Fseeds;
    //
    TAppMain *pmain;
    ir_frames *pframe;
    ///
    unsigned int FWaveDataCount;
    int *FWaveData;

    bool IFEndCode;
    unsigned char FQOBitStar; //�ж���żλ��
    int FBITCounts;
    bool Fisjiou;
    int FperBits;
    int Ffillval;
};

#endif //MYAPPLICATION3_IRDECODER_H
