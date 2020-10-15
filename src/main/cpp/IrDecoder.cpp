//
// Created by Administrator on 2017/8/13 0013.
//

#include <malloc.h>
#include <string.h>
#include <string>
#include <math.h>
#include <stdlib.h>
#include "Ant1Freeze.h"
#include "IrDecoder.h"
#include "AcIrMaths.h"

using namespace std;

const string cScaleChar = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

string IntToDigit(unsigned int mNumber,unsigned int mScale, unsigned int mLength = 0) {
	unsigned int I, J;
	string Result;
	I = mNumber;
	while (I >= mScale && mScale > 1) {
		J = (I % mScale);
		I = (I / mScale);
		Result=cScaleChar.at(J)+Result;
		if (mLength > 0)
			if (Result.length() >= mLength)
				break;
	}
	if (I <= mScale)
		if (mLength == 0 || Result.length() < mLength)
			Result=cScaleChar.at(I)+Result;
	if (Result.length() == 0)
		Result="0"+Result;
	if (mLength > 0)
		while (mLength > Result.length())
			Result="0"+Result;
	return Result;
	//itoa(n, c, 16);
}

unsigned int DigitToInt(string mDigit, double mScale) {
	int L, p;
	unsigned int Result = 0;
	L = mDigit.length();
	for(int i=0;i<L;i++) {
		p = cScaleChar.find(mDigit[L - i - 1]);
		Result += (unsigned int)(p * pow(mScale, i));
	}
	return Result;
}

IrDecoder::IrDecoder(unsigned char * buf, int alen, long long *param,
		unsigned int aparacount) {
	Fdata = buf;
	srclen = alen;
	Fparam = param;
	paramcount = aparacount;
	pmain = NULL;
	pframe = NULL;
	Fseeds = 0;
	FWaveDataCount = 0;
	FQOBitStar = 0;
	FWaveData = NULL;
	IFEndCode = false;
	Fisjiou = false;
	FBITCounts = 2;
    FperBits = 1;
    Ffillval=1;
}

IrDecoder::~IrDecoder() {
	if (pframe != NULL && pmain != NULL) {
		for (int i = 0; i < pmain->framecount; i++) {
			free(pframe[i].pc);
		}
		free(pframe);
	}
}

bool IrDecoder::parzegs() {
	unsigned int i, j, ptr;
	bool ret = false;
	ptr = sizeof(struct TAppMain);
	if (srclen < ptr)
		return ret;
	pmain = (TAppMain *) Fdata;

	unsigned int datacount = 0;
	for (i = 0; i < pmain->framecount; i++)
		datacount += pmain->perCodeCount[i];
	datacount *= sizeof(struct TAppCodeStru);
	datacount += (pmain->framecount * sizeof(struct TAppFramesStru));
	datacount += sizeof(struct TAppMain);
	if (datacount != srclen)
		return ret;
	///////////--------------------------
	pframe = (ir_frames *) malloc(sizeof(struct ir_frames) * pmain->framecount);
	for (i = 0; i < pmain->framecount; i++) {
		pframe[i].pf = (TAppFramesStru *) (Fdata + ptr);
		ptr += sizeof(struct TAppFramesStru);

		pframe[i].codecount = pmain->perCodeCount[i];
		datacount = sizeof(void*) * pframe[i].codecount;
		pframe[i].pc = (TAppCodeStru **) malloc(datacount);
		for (j = 0; j < pmain->perCodeCount[i]; j++) {
			pframe[i].pc[j] = (TAppCodeStru *) (Fdata + ptr);
			ptr += sizeof(struct TAppCodeStru);
		}
	}
	///
	FBITCounts = pmain->logicCount;
	if ( FBITCounts< 3)
		FperBits = 1;
	else if (FBITCounts == 4)
		FperBits = 2; // 2 11
	else if (FBITCounts == 3) {
		if (pmain->logic[2].count == 0) {  //��ż��
			FperBits = 1;
			Fisjiou = true;
		} else
			FperBits = 2;
	} else
		FperBits = 4;
	if(Fisjiou)FBITCounts=2;

	switch (FperBits) {
	case 1:
		Ffillval = 1;
		break;
	case 2:
		Ffillval = 3;
		break;
	case 4:
		Ffillval = 0xf;
		break;
	}
	///////////--------------------------
	/*
	 for(i=0;i<pmain->framecount;i++){
	 printf("frame%d\n",pframe[i].pf->NO);
	 for(j=0;j<pframe[i].codecount;j++){
	 printf("	code%d=%s\n",pframe[i].pc[j]->NO,pframe[i].pc[j]->CodeName);
	 }
	 }
	 */
	///////////--------------------------
	ret = true;
	return ret;
}

//set params
bool IrDecoder::setParams() {
	if (pframe == NULL || pmain == NULL)
		return false;
	//
	unsigned int i, j, n = 0;
	for (i = 0; i < pmain->framecount; i++) {
		for (j = 0; j < pframe[i].codecount; j++) {
			if (pframe[i].pc[j]->Logic && pframe[i].pc[j]->ParamsChge
					&& pframe[i].pc[j]->sameAsNo < 0
					&& pframe[i].pc[j]->FanmaAsNo < 0) {
				if (pframe[i].pc[j]->codekind != 6) {
					if (pframe[i].pc[j]->codekind == 4) {
						pframe[i].pc[j]->Params = Fseeds;
					} else if (paramcount > n) {
						pframe[i].pc[j]->Params = Fparam[n];
						n++;
					} else
						break;
				}
			}
		}
	}
	return true;
}

static string ValidFuncCtrChar = " ()+-*aAnNoOrR";

bool getInValidFuncCtrChar(char c) {
	bool ret = false;
	for (int i = 0; i < 14; i++) {
		if (ValidFuncCtrChar[i] == c) {
			ret = true;
			break;
		}
	}
	return ret;
}

unsigned int IrDecoder::GetCheckCode(char * chkstr) {
	string chkstring = chkstr;
	string valstr;
	int po, pl, pr,bits;
	unsigned int nodeidx;
	unsigned int tmplong;
	//
	while (true) {
		po = chkstring.find_first_of(":");
		if (po < 1)
			break;
		pl = 0;
		for (int i = (po - 1); i >= 0; i--) {
			if (ValidFuncCtrChar.find(chkstring[i]) != string::npos) {
				pl = i + 1;
				break;
			}
		}
		if (pl < 0)
			pl = 0;
		if (pl > po)
			break;
		valstr = chkstring.substr(pl, (po-pl));
		if (valstr.length() == 0)
			break;
		nodeidx = atoi(valstr.c_str());
		//---------------------
		pr = chkstring.length();
		for (int i = (po + 1); i < pr; i++) {
			if (ValidFuncCtrChar.find(chkstring[i]) != string::npos) {
				pr = i - 1;
				break;
			}
		}
		bits = -1;
		if (po <= pr) {
			valstr = chkstring.substr(po+1, (pr-po));
			bits = atoi(valstr.c_str());
		}
		//------------------
		TAppCodeStru *code = LookMyTreeNodeFromNo(nodeidx);
		tmplong = 0;
		if (code != NULL) {
			tmplong = (unsigned int)code->Params;
			if (bits >= 0) {
				string tmpstr = IntToDigit(tmplong,FBITCounts,code->BitCount);
				bits = (code->BitCount - bits - 1);
				if(bits>=0 && bits<((int)tmpstr.length())){
					tmplong = DigitToInt(tmpstr.substr(bits,1),FBITCounts);
				}
			}
		}
		chkstring.erase(pl,(pr-pl+1));

		chkstring.insert(pl,IntToDigit(tmplong,10));
	}
	//
	int ret=0;
	TCALC *CALC = new TCALC();
	try{
		CALC->Compile((char *)chkstring.c_str());
		CALC->Evaluate();
		ret = CALC->GetResult();
		delete CALC;
	}catch(TError error){
		delete CALC;
	}
	//
	return ret;
}

void IrDecoder::AddData(int data) {
	FWaveData[FWaveDataCount] = data;
	FWaveDataCount++;
}

int IrDecoder::AddTreeWaveData(TAppLogic *waveArr, int currlen, int dclen) {
	int ret, i;
	int tmpint;
	ret = 0;
	for (i = 0; i < waveArr->count; i++) {
		if (waveArr->alogic[i] == INT_MAX) {
			tmpint = dclen - currlen - ret;
			if (tmpint < 0)
				tmpint = 0;
		} else if (waveArr->alogic[i] == -INT_MAX) {
			tmpint = dclen - currlen - ret;
			if (tmpint < 0)
				tmpint = 0;
			tmpint = -tmpint;
		} else
			tmpint = waveArr->alogic[i];

		ret += abs(tmpint);
		AddData(tmpint);
	}
	return ret;
}

int IrDecoder::AddTreeLogicData(TAppLogic *logicArr) {
	int ret, i;
	int tmpint;
	ret = 0;
	for (i = 0; i < logicArr->count; i++) {
		tmpint = logicArr->alogic[i] * pmain->BaseTime;
		ret += abs(tmpint);
		AddData(tmpint);
	}
	return ret;
}

int IrDecoder::AddTreeBitData(unsigned char bitcount, long long params,
		bool hadPart, unsigned char bPartFr, unsigned char bPartTo,
		bool turned) {
	int ret = 0;
	int m_1 = 0;
	int m_2 = (bitcount - 1);
	int logicx = 0;
	int n;
	// �Ƿ��ǲ�����
	if (hadPart) {
		if (bPartFr > m_2)
			return ret;
		if (bPartTo < m_1)
			return ret;

		m_1 = bPartFr;
		m_2 = bPartTo;
		if (!pmain->PreLow) {
			m_2 = (bitcount - bPartFr - 1);
			m_1 = (bitcount - bPartTo - 1);
		}
	}
	////////////////
	for (int i = m_1; i <= m_2; i++) {
		if (pmain->PreLow)
			n = i;
		else
			n = bitcount - i - 1;
		n *= FperBits;
		logicx = ((params >> n) & Ffillval);
		if (turned)
			logicx = ((~logicx) & Ffillval);

		if (Fisjiou && logicx == 1) {
			if (((FQOBitStar + i + 1) % 2) & 1)
				logicx = 3;
		}
		ret += AddTreeLogicData(&pmain->logic[logicx]);
	}
	return ret;
}

unsigned int IrDecoder::RenderCodeData(TAppFramesStru *APFrame,
		TAppCodeStru *APCode, TAppCodeStru *APSameCode, unsigned int CurrLen,
		bool FanMaBool) {
	unsigned int ret = 0;
	TAppCodeStru *APTrueCode;
	if (APSameCode == NULL) {
		APTrueCode = APCode;
	} else {
		APTrueCode = APSameCode;
	}
	if (APTrueCode->Logic) {                 //��λ
		//У�����
		if (strlen(APTrueCode->ParamsFunc) > 0)    //У����
			APTrueCode->Params = GetCheckCode(APTrueCode->ParamsFunc);
		//
		APCode->Length = AddTreeBitData(APTrueCode->BitCount,
				APTrueCode->Params, APCode->hadPart, APCode->bPartFr,
				APCode->bPartTo, FanMaBool);
		ret += APCode->Length;
	} else {        //������
		int waveidx = 0;
		if (APTrueCode->wavecount > 1) {        // turn wave
			waveidx = Fseeds % APTrueCode->wavecount;
		}
		APCode->Length = AddTreeWaveData(&APTrueCode->wave[waveidx],
				(CurrLen + ret), APFrame->DCLength);
		ret += APCode->Length;
	}
	return ret;
}

TAppCodeStru *IrDecoder::LookMyTreeNodeFromNo(int index) {
	TAppCodeStru * ret = NULL;
	unsigned int i, j = 0;
	for (i = 0; i < pmain->framecount; i++) {
		for (j = 0; j < pframe[i].codecount; j++) {
			if (pframe[i].pc[j]->NO == index) {
				ret = pframe[i].pc[j];
				break;
			}
		}
	}
	return ret;
}

ir_frames *IrDecoder::LookMyFrameTreeNodeFromNo(int index) {
	ir_frames * ret = NULL;
	unsigned int i = 0;
	for (i = 0; i < pmain->framecount; i++) {
		if (pframe[i].pf->NO == index) {
			ret = &pframe[i];
			break;
		}
	}
	return ret;
}

int IrDecoder::getIRData(int *irdata) {
	unsigned int i, j = 0;
	int tmpInt;
	unsigned int mFr, mTo;
	bool IsFanSameCode;

	ir_frames *apframes;
	TAppCodeStru *APCode;
	TAppCodeStru *APSameCode;
	//-------------------------
	bool ret = parzegs();
	if (!ret)
		return -1;
	//set params
	ret = setParams();
	if (!ret)
		return -2;
	//
	FWaveData =  (int *)malloc(sizeof(int)*512*2*8);// irdata;
	mFr = CON_IRINVAALID;
	mTo = CON_IRINVAALID;
	for (i = 0; i < pmain->framecount; i++) {
		FQOBitStar = 0;   //��żλ�жϳ�ʼ
		tmpInt = 0;  //�곤�� ��ʼ

		if (mFr == CON_IRINVAALID)
			if (pframe[i].pf->Repeated)
				mFr = FWaveDataCount;

		if (mFr != CON_IRINVAALID)
			if (mTo == CON_IRINVAALID)
				if (!pframe[i].pf->Repeated) {
					mTo = (FWaveDataCount + 1);
				}

		if (pframe[i].pf->sameAsNo < 0 && pframe[i].pf->FanmaAsNo < 0) { //************************* //����ͬ�����
			for (j = 0; j < pframe[i].codecount; j++) {
				IFEndCode = (j == (pframe[i].codecount - 1)) ? true : false;
				APCode = pframe[i].pc[j];
				if (APCode->sameAsNo < 0 && APCode->FanmaAsNo < 0) { //����ͬ����
					tmpInt += RenderCodeData(pframe[i].pf, APCode, NULL, tmpInt,
							false);
					if (APCode->Logic)
						FQOBitStar += APCode->BitCount;
				} else {      ///��ͬ�� ��  ����
					if (APCode->sameAsNo >= 0) {
						APSameCode = LookMyTreeNodeFromNo(APCode->sameAsNo);
						IsFanSameCode = false;
					} else {
						APSameCode = LookMyTreeNodeFromNo(APCode->FanmaAsNo);
						IsFanSameCode = true;
					}
					tmpInt += RenderCodeData(pframe[i].pf, APCode, APSameCode,
							tmpInt, IsFanSameCode);
					if (APSameCode->Logic)
						FQOBitStar += APSameCode->BitCount;
				}
			} //end of code
			pframe[i].pf->Length = tmpInt;
		} else {   //************************* ��ͬ�����
			bool IsFanSameFrame;
			if (pframe[i].pf->sameAsNo >= 0) {
				apframes = LookMyFrameTreeNodeFromNo(pframe[i].pf->sameAsNo);
				IsFanSameFrame = false;
			} else {
				apframes = LookMyFrameTreeNodeFromNo(pframe[i].pf->sameAsNo);
				IsFanSameFrame = true;
			}
			for (j = 0; j < apframes->codecount; j++) {
				IFEndCode = (j == (apframes->codecount - 1)) ? true : false;
				APCode = apframes->pc[j];
				if (APCode->sameAsNo < 0 && APCode->FanmaAsNo < 0) { //����ͬ����
					tmpInt += RenderCodeData(apframes->pf, APCode, NULL, tmpInt,
							(false ^ IsFanSameFrame));
					if (APCode->Logic)
						FQOBitStar += APCode->BitCount;
				} else {      ///��ͬ�� ��  ����
					if (APCode->sameAsNo >= 0) {
						APSameCode = LookMyTreeNodeFromNo(APCode->sameAsNo);
						IsFanSameCode = false;
					} else {
						APSameCode = LookMyTreeNodeFromNo(APCode->FanmaAsNo);
						IsFanSameCode = true;
					}
					tmpInt += RenderCodeData(apframes->pf, APCode, APSameCode,
							tmpInt, IsFanSameCode ^ IsFanSameFrame);
					if (APSameCode->Logic)
						FQOBitStar += APSameCode->BitCount;
				}
			} //end of code
		}
		pframe[i].pf->Length = tmpInt;
	}
	///-------------
	if (mFr != CON_IRINVAALID)
		if (mTo == CON_IRINVAALID) {
			mTo = FWaveDataCount;
		}
	//
	getHebingData(FWaveData,FWaveDataCount,irdata,&tmpInt);
	free(FWaveData);
	return tmpInt;
}

void IrDecoder::SetSeeds(unsigned char mseeds) {
	Fseeds = mseeds;
}

int IrDecoder::getZb(){
	if (pmain == NULL)
		return 0;
	else
		return pmain->Freg;
}

char *IrDecoder::getPID() {
	if (pmain == NULL)
		return NULL;
	else
		return pmain->name;
}

