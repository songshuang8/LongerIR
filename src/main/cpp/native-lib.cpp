
#include <jni.h>
#include <string.h>
#include <sys/time.h>
#include <malloc.h>
#include <string>
#include <iostream>
#include "native-lib.h"
#include "AcDecoder.h"
#include "IrDecoder.h"
#include <android/log.h>
#include <stdlib.h>
#include "b64.h"

#define URL_TMPBUFLEN 4096
#define URL_CONTENTLEN 1024*1024

using namespace std;

unsigned char recv_buf[URL_CONTENTLEN];
unsigned int recv_len;

//  file len must short than 16
static char FILE_GS[]	="irapp.db";
static char mesi[32] = {0};
static char mlanguiage[16]= {0};
static int FileDir_Len;
static char FilesDir[255];
static int biniSuc=0;
static AcStatus status;
static unsigned char irseeds=0;
static int iFreq=38000;

char * getFileName(char *aname){
    int mlen = strlen(aname);
    memcpy(FilesDir+FileDir_Len,aname,mlen);
    FilesDir[FileDir_Len+mlen]='\0';
    return FilesDir;
}

FILE * pub_ChkAppFile(AcFileTitle *title,char *aname){
    FILE *fp=NULL;
    char * filename = getFileName(aname);
    //----- ----------------------
    fp = fopen(filename, "rb");
    if (fp == NULL)return fp;
    //-----
    fseek(fp, 0L,SEEK_END);
    unsigned int filesize = ftell(fp);
    if(filesize<sizeof(struct AcFileTitle)) {
        fclose(fp);
        fp = NULL;
        return fp;
    }

    fseek(fp, 0L,SEEK_SET);
    int bytesRead = fread(title, 1,sizeof(struct AcFileTitle), fp);
    if(filesize!=title->size){
        fclose(fp);
        fp = NULL;
        return fp;
    }
    return fp;
}

extern "C"
JNIEXPORT jintArray JNICALL
Java_clurc_net_longerir_MainActivity_irGetIRDataByRaw(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray rawbuf,jint rawlen,jint pressbtn,jint pressval) {
    jbyte *buffer = env->GetByteArrayElements(rawbuf,NULL);
    jintArray ret=NULL;
    if(buffer == NULL){
        return ret;
    }
    //if(biniSuc != 452145)return ret;
    status.pressbtn = pressbtn;
    status.pressval = pressval;
    if(status.pressbtn==0) {
        status.pressval=status.status[0];
    }else if(status.pressbtn!=1)
        status.pressval=0;
    //----------------------------------------------------------
    int *irHighLow = (int *)malloc(sizeof(int)*1024*32);
    int irlen =0;
    //---
    AcDecoder * decoder = new AcDecoder((unsigned char *)buffer,rawlen);
    decoder->setStatus(&status);
    bool bsuc = decoder->getIRData(irHighLow,&irlen);
    if(!bsuc)irlen=0;
    decoder->getStatus(&status);
    iFreq = decoder->getFreq();
    delete decoder;
    //---
    //----------------------------------------------------------
    if(irlen>0){
        ret = env->NewIntArray(irlen);
        env->SetIntArrayRegion(ret,0,irlen,irHighLow);
    }
    free(irHighLow);
    env->ReleaseByteArrayElements(rawbuf,buffer,0);
    return ret;
}

extern "C"
JNIEXPORT void JNICALL
Java_clurc_net_longerir_MainActivity_setMobileId(
        JNIEnv *env,
        jobject ctx,
        jstring astr) {
    int len,m;
    const char *temp = env->GetStringUTFChars(astr, NULL);
    if(NULL == astr)return ;

    len = strlen(temp);
    if(len> (sizeof(mesi)-6))
        len = sizeof(mesi)-6;
    memset(mesi,0,sizeof(mesi));
    sprintf(mesi,"&id=");
    m = 4;
    for(int i=0;i<len;i++){
        if(temp[i]>=0x30 && temp[i]<=0x39){
            mesi[m]=temp[i];
            m++;
        }else if(temp[i]>=0x41 && temp[i]<=0x5a){
            mesi[m]=temp[i];
            m++;
        }else if(temp[i]>=0x61 && temp[i]<=0x7a){
            mesi[m]=temp[i];
            m++;
        }
    }
    env->ReleaseStringUTFChars(astr, temp);
}

extern "C"
JNIEXPORT void JNICALL
Java_clurc_net_longerir_MainActivity_setLangugeId(
        JNIEnv *env,
        jobject ctx,
        jstring alan) {
    int len,m;
    const char *temp = env->GetStringUTFChars(alan, NULL);
    if(NULL == alan)return ;
    len = strlen(temp);
    if(len> (sizeof(mlanguiage)-6))
        len = (sizeof(mlanguiage)-6);
    memset(mlanguiage,0, sizeof(mlanguiage));
    sprintf(mlanguiage,"&ln=%s",temp);
    env->ReleaseStringUTFChars(alan, temp);
}

extern "C"
JNIEXPORT jint JNICALL
Java_clurc_net_longerir_MainActivity_irFileInit(
        JNIEnv *env,
        jobject ctx,
        jobject context_object,
        jstring filename) {
    //检查COntext--------------------------------------------
//    if(!ChkContext(env,ctx)){
//        LOGI("chk error!");
//        return  -1;
//    }
    LOGI("apk is good!");
    //----- -----获取文件路径--------------------------------
    const char *localstr = env->GetStringUTFChars(filename, NULL);
    if(NULL == localstr){
        return -2;
    }

    FileDir_Len = strlen(localstr);
    memcpy(FilesDir,localstr,FileDir_Len);
    //LOGI("file dir=%s",FilesDir);
    env->ReleaseStringUTFChars(filename, localstr);
    //
//    if(strstr(FilesDir,"sunglesoft.com")==NULL){
//        return -3;
//    }
    AcFileTitle title;
    FILE *fp = pub_ChkAppFile(&title,FILE_GS);
    if(fp==NULL)
        return -4;
    fclose(fp);
    fp = NULL;
    //LOGI("ac count=%d",mlen);
    memset(&status,0, sizeof(AcStatus));
    memset(mesi,0,sizeof(mesi));
    memset(mlanguiage,0, sizeof(mlanguiage));
    biniSuc = 452145;
    return title.ver;
}

extern "C"
JNIEXPORT jintArray JNICALL
Java_clurc_net_longerir_MainActivity_irGsGetData(
        JNIEnv *env,
        jobject /* this */,
        jlongArray parambuf,jint pramlen,jint pid) {
    AcFileTitle title;
    IrItem gsinfo;
    jintArray ret=NULL;
    int gsindex;
    int dataptr;
    if(biniSuc != 452145)return ret;
    FILE *fp = pub_ChkAppFile(&title,FILE_GS);
    if(fp==NULL)
        return ret;
    // look   gsidx
    gsindex = -1;
    dataptr = 0;
    for(int i=0;i<title.count;i++){
        fread(&gsinfo,1,sizeof(struct IrItem), fp);
        if(gsinfo.pid==pid){
            gsindex = i;
            break;
        }
        dataptr += gsinfo.len;
    }
    if(gsindex<0 || gsinfo.len<=0){
        fclose(fp);
        fp = NULL;
        return ret;
    }
    //--------------------
    jlong *param = env->GetLongArrayElements(parambuf,NULL);
    if(param == NULL){
        fclose(fp);
        fp = NULL;
        return ret;
    }
    //
    int alen = sizeof(AcFileTitle) + sizeof(struct IrItem)*title.count;
    alen+= dataptr;
    fseek(fp, 0L,SEEK_END);
    unsigned int filesize = ftell(fp);
    if((alen+gsinfo.len)>filesize){
        LOGI("gs data is more than file%d,%d,%d",filesize,alen,gsinfo.len);
        fclose(fp);
        fp = NULL;
        return ret;
    }
    fseek(fp, alen,SEEK_SET);
    alen  = gsinfo.len;
    unsigned char *buffer = (unsigned char *)malloc(alen);
    int bytesRead = fread(buffer, 1,alen, fp);
    fclose(fp);
    fp = NULL;
    if (bytesRead != alen) {
        free(buffer);
        return ret;
    }
    //------------------------------
    int *irHighLow = (int *)malloc(sizeof(int)*1024*2*8);
    IrDecoder * decoder = new IrDecoder((unsigned char *)buffer,alen,(long long *)param,pramlen);
    decoder->SetSeeds(irseeds++);
    int irlen = decoder->getIRData(irHighLow);
    env->ReleaseLongArrayElements(parambuf,param,0);
    //
    if(irlen>0){
        iFreq = decoder->getZb();
        ret = env->NewIntArray(irlen);
        env->SetIntArrayRegion(ret,0,irlen,irHighLow);
    }
    delete decoder;
    return ret;
}

extern "C"
JNIEXPORT void JNICALL
Java_clurc_net_longerir_MainActivity_irSetStatus(
        JNIEnv *env,
        jobject /* this */,jbyteArray val) {
    jbyte *buffer = env->GetByteArrayElements(val,NULL);
    if(buffer == NULL)return;
    for(int i=0;i<BTN_COUNT;i++)
        status.status[i]=buffer[i];
    env->ReleaseByteArrayElements(val,buffer,0);
    return;
}
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_clurc_net_longerir_MainActivity_irGetStatus(
        JNIEnv *env,
        jobject /* this */) {
    jbyteArray ret;
    unsigned char *buf = (unsigned char *)malloc(BTN_COUNT);
    for(int i=0;i<BTN_COUNT;i++)
        buf[i]=status.status[i];
    ret = env->NewByteArray(BTN_COUNT);
    env->SetByteArrayRegion(ret,0,BTN_COUNT,(jbyte *)buf);
    free(buf);
    return ret;
}

extern "C"
JNIEXPORT jint JNICALL
Java_clurc_net_longerir_MainActivity_irGetCurrentFreq(
        JNIEnv *env,
        jobject /* this */) {
    return iFreq;
}
//---------------------------------------------key db
unsigned char * sub_CreateCodeGoupdBuffer(FILE *fp,TKeyDbItemStru *aitem,long *bufflen){
    unsigned char *buffer;
    int perCount;
    int ptr;
    perCount = 3+aitem->maxparam*4;
    *bufflen  =perCount*aitem->keycount;
    (*bufflen)++;
    buffer = (unsigned char *)malloc(*bufflen);
    buffer[0] = aitem->keycount;
    //-------------------------------
    int bytesRead = 0;
    ptr = 1;
    for(int i=0;i<aitem->keycount;i++){
        bytesRead += fread(buffer+ptr, 1,perCount, fp);
        unsigned int param0 = 0;
        memcpy(&param0,buffer+ptr+3,4);
        param0 = (unsigned int )(param0 - aitem->keycount*3 - aitem->maxparam);
        memcpy(buffer+ptr+3,&param0,4);
        ptr +=perCount;
    }
    if (bytesRead != (*bufflen - 1)) {
        free(buffer);
        buffer = NULL;
        *bufflen = 0;
    }
    return  buffer;
}

unsigned char * sub_CreateAbtnBuffer(FILE *fp,TKeyDbItemStru *aitem,int keyidx,long *bufflen){
    unsigned char *buffer=NULL;
    int perCount;
    int bytesRead;
    unsigned int param0;
    perCount = 3+aitem->maxparam*4;
    buffer = (unsigned char *)malloc(perCount);
    //-------------------------------
    *bufflen = 0;
    for(int i=0;i<aitem->keycount;i++){
        bytesRead = fread(buffer, 1,perCount, fp);
        if(bytesRead!=perCount)
            break;
        if(buffer[2]==keyidx) {
            param0 = 0;
            memcpy(&param0, buffer + 3, 4);
            param0 = (unsigned int) (param0 - aitem->keycount * 3 - aitem->maxparam);
            memcpy(buffer + 3, &param0, 4);
            *bufflen = perCount;
            break;
        }
    }
    if (*bufflen == 0) {
        free(buffer);
        buffer = NULL;
    }
    return  buffer;
}
//--------------------------------------------------------------------------------------------
static unsigned char con_url_mimi[] = {2,45,124,78,95,36,241,58};


long getCurrentTime()
{
    struct timeval tv;
    gettimeofday(&tv,NULL);
    return tv.tv_sec * 1000 + tv.tv_usec / 1000;
}

int endcodingur(const char *urlparam,unsigned char *des,int dlen){
    int srclen,deslen,ptr,i;
    unsigned char * tempbuf;
    char * url;

    srclen = strlen(urlparam);
    url = (char *)malloc(srclen+64);
    memset(url,0,srclen+64);
    sprintf(url,"%s\r\ntime=%lld",urlparam,time(NULL));
    srclen = strlen(url);
    LOGI("new url=%s",url);

    tempbuf = (unsigned char *)malloc(srclen+64);

    if(srclen==0)return 0;
    deslen = srclen+1+4+1;  //he + srclen  + he
    if((deslen % 32)!=0){
        deslen /=32;
        deslen++;
        deslen*=32;
    }
    tempbuf[0] = 55;
    ptr = 0;
    for(i=0;i<srclen;i++){
        tempbuf[0] +=url[i];
        tempbuf[i+5] = url[i] - con_url_mimi[ptr];
        ptr++;
        if(ptr>=sizeof(con_url_mimi))
            ptr = 0;
    }
    tempbuf[0] = ~tempbuf[0]+5;
    for(i=1;i<5;i++)
        tempbuf[i] = 0;
    memcpy(tempbuf+1,&srclen,4);

    srand((int)time(NULL));
    for(i=(srclen+5);i<deslen;i++)
        tempbuf[i] = (rand() % 255);
    tempbuf[deslen-1] = 33;
    for(i=0;i<(deslen-1);i++)
        tempbuf[deslen-1] += tempbuf[i];
    tempbuf[deslen-1] = ~ tempbuf[deslen-1];
    memset(des,0,dlen);
    ptr = b64_encode(tempbuf,deslen,des,dlen);
    free(tempbuf);
    free(url);
    return ptr;
}

size_t PostDispose(char *buffer, size_t size, size_t nmemb, void *userdata)
{
    int bytelen = size * nmemb;
    //LOGI("recved length = %d/%d",bytelen,recv_len);
    if(bytelen>URL_CONTENTLEN)
        return bytelen;
    if((recv_len+bytelen)>URL_CONTENTLEN)
        recv_len = 0;

    memcpy(recv_buf+recv_len,buffer,bytelen);
    recv_len +=bytelen;
    return bytelen;
}

bool myhttp_get(const char *param) {
    bool ret = false;
    int postlen;
    char *tbuf1 = (char *) malloc(URL_TMPBUFLEN);
    char *parammimi = (char *) malloc(URL_TMPBUFLEN);

    memset(tbuf1, 0, URL_TMPBUFLEN);
    postlen = endcodingur(param, (unsigned char *) tbuf1, URL_TMPBUFLEN);
    //LOGI("url=%s,%s",tbuf1,tbuf2);
    memset(parammimi, 0, URL_TMPBUFLEN);
    //sprintf(parammimi, "http://192.168.2.5/remotedata3?ver=3");
    //sprintf(parammimi, "http://10.0.2.2/remotedata3?ver=3");
    sprintf(parammimi, "https://sunglesoft.com/remotedata3?ver=2");
    if(strlen(mesi)>0) {
        strcat(parammimi,mesi);
    }
    if(strlen(mlanguiage)>0) {
        strcat(parammimi,mlanguiage);
    }
///---------------------------------------------
/*
    recv_len = 0;
    HttpPostModule *post = new HttpPostModule();
    post->SetTimeOut(6);
    post->SetHttpHead("Content-Type:application/json;charset=UTF-8");
    post->SetWriteFunction(PostDispose);
    post->SetURL(parammimi);
    post->SetGzip();
    post->AddPostString(tbuf1);
    int nRet = post->SendPostRequest();
    free(post);
    if (nRet == 0){
        LOGI("post success!");
        ret = true;
    }else
        LOGE("post error code:%d", nRet);
        */
///----------------------------------------------------
    free(tbuf1);
    free(parammimi);
    return ret;
}

extern "C"
JNIEXPORT jintArray JNICALL
Java_clurc_net_longerir_MainActivity_acGetIRData(
        JNIEnv *env,
        jobject /* this */,
        jstring p0,jint powval) {
    jintArray ret=NULL;
    bool bSuc;

    if(biniSuc != 452145)return ret;

    status.pressbtn = 0;
    status.pressval = powval;
    status.status[0]=status.pressval;
    //
    const char *param0 = env->GetStringUTFChars(p0, NULL);
    bSuc = myhttp_get(param0);
    env->ReleaseStringUTFChars(p0, param0);

    if(!bSuc || recv_len<32)
        return  ret;
    if(recv_len<8)
        return ret;
    //----------------------------------------------------------
    int *irHighLow = (int *)malloc(sizeof(int)*1024*2*8);
    int irlen =0;
    //---
    AcDecoder * decoder = new AcDecoder(recv_buf,recv_len);
    decoder->setStatus(&status);
    bool bsuc = decoder->getIRData(irHighLow,&irlen);
    if(!bsuc)irlen=0;
    delete decoder;
    //----------------------------------------------------------
    if(irlen>0){
        ret = env->NewIntArray(irlen);
        env->SetIntArrayRegion(ret,0,irlen,irHighLow);
    }
    free(irHighLow);

    return ret;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_clurc_net_longerir_MainActivity_cHttpGet(
        JNIEnv *env,
        jobject thiz,
        jstring p0) {
    jbyteArray ret=NULL;
    bool bSuc;
    if(biniSuc != 452145)return ret;

    const char *param0 = env->GetStringUTFChars(p0, NULL);
    bSuc = myhttp_get(param0);
    env->ReleaseStringUTFChars(p0, param0);

    if(bSuc && recv_len>0) {
        ret = env->NewByteArray(recv_len);
        env->SetByteArrayRegion(ret, 0, recv_len, (jbyte *) recv_buf);
    }
    return ret;
}

extern "C"
JNIEXPORT void JNICALL
Java_clurc_net_longerir_MainActivity_Init(
        JNIEnv *env,
        jobject obj) {
}

extern "C"
JNIEXPORT void JNICALL
Java_clurc_net_longerir_MainActivity_Cleanup(
        JNIEnv *env,
        jobject obj) {
}
