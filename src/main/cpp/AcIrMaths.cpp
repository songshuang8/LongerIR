//
//  AcIrMaths.cpp
//  AcirTest
//
//  Created by songshuang on 17/8/5.
//  Copyright © 2017年 songshuang. All rights reserved.
//

#include <stdlib.h>
#include "AcIrMaths.h"

void getHebingData(int * src,int srccount,int * des,int *deslen) {
	int i,m,n,tmpInt;
	bool ZhengBool;
	n = -1;
	for (i = 0; i < srccount; i++) {
		if(src[i]>0){
			n=i;
			break;
		}
    }
	if (n<0 || srccount == 0){
		*deslen = 0;
		return;
	}
	tmpInt = src[n++];
	m = 0;
	ZhengBool = true;
	for (int i = n; i < srccount; i++) {
        if (ZhengBool) {
            if (src[i] > 0) {
                tmpInt += src[i];
            } else {
                des[m++]=tmpInt;
                ZhengBool = false;
                tmpInt = abs(src[i]);
            }
        } else {
            if (src[i] < 0) {
                tmpInt +=abs(src[i]);
            } else {
                des[m++]=tmpInt;
                ZhengBool = true;
                tmpInt = src[i];
            }
        }
	}
	if(ZhengBool)
		des[m++]=tmpInt;
    *deslen = m;
    return;
}