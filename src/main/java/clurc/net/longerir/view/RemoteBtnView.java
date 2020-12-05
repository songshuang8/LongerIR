package clurc.net.longerir.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import com.qmuiteam.qmui.util.QMUILangHelper;

import java.util.List;

import clurc.net.longerir.data.BtnInfo;
import clurc.net.longerir.data.CfgData;

public class RemoteBtnView  extends AppCompatImageView {
    private Context context;
    private int mNormalColor;
    private int mSelectColor;
    private Paint mShapePaint;
    private String btnName,customName;
    private int stroke = 3;
    private int row;
    private int col;
    private int keyidx;
    private int sno;
    private ShapeKinds mShapeKinds;
    private boolean mSelected = false;
    private boolean mlearning = false;
    private boolean madjust = false;
    private boolean mshift = false;

    public int freq;
    public int gsno = -1;
    public String param16;
    public String wave = null;
    public int[] params;
    public int srcidx = -1;

    public RemoteBtnView(Context context, int mNormal, int mxtColor,boolean fontBiger) {
        super(context);
        this.context = context;
        mNormalColor = mNormal;
        mSelectColor = mxtColor;
        mShapePaint = new Paint();
        mShapePaint.setAntiAlias(false); //设置画笔为无锯齿
        mShapePaint.setColor(mNormalColor); //设置画笔颜色
        mShapePaint.setStrokeWidth(stroke);              //线宽
        mShapePaint.setStyle(Paint.Style.FILL);                   //空心效果
        if(fontBiger)
            mShapePaint.setTextSize(46);
        else
            mShapePaint.setTextSize(23);
        mShapePaint.setTextAlign(Paint.Align.CENTER);
        mSelected = false;
    }

    public RemoteBtnView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mShapePaint = new Paint();
        mShapePaint.setAntiAlias(true);
        mShapePaint.setColor(mNormalColor);
        mSelected = false;
    }
//    setShadowLayer只有文字绘制才支持硬件加速，其他都不支持
//    因此，要为此控件单独关闭硬件加速：setLayerType(LAYER_TYPE_SOFTWARE, null);
    @Override
    public void draw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        super.draw(canvas);
        canvas.drawColor(Color.WHITE);
        //-------------
        if(mSelected && madjust==false){
            mShapePaint.setColor(mSelectColor);
            mShapePaint.setStyle(Paint.Style.FILL);
            DrawMyShape(canvas);

            mShapePaint.setColor(Color.WHITE);
            darwText(strJieduan(btnName),canvas,w /2,h/2);
            if(madjust){
                drawAdjustSrc(canvas,w,h);
            }
        }else{
            if(mlearning || madjust) {
                if(gsno<0 && wave==null) {
                    mShapePaint.setColor(mNormalColor);
                }else{
                    if(mshift)
                        mShapePaint.setColor(mSelectColor);
                    else
                        mShapePaint.setColor(Color.BLACK);
                }
            }else{
                mShapePaint.setColor(mNormalColor);
            }
            mShapePaint.setStyle(Paint.Style.STROKE);
            DrawMyShape(canvas);
            darwText(strJieduan(btnName),canvas,w /2,h/2);
            if(madjust){
                drawAdjustSrc(canvas,w,h);
            }
        }
        if(mlearning){
            if(gsno>=0 && params!=null && params.length>0) {
                String s = "";
                for (int i = 0; i < params.length; i++) {
                    s += Integer.toHexString(params[i]) + " ";
                }
                darwText(s.trim(), canvas, w / 2, h / 2 + getTxtHeight() + 4);
            }else if(wave!=null && wave.length()>0){
                darwText("WAVE", canvas, w / 2, h / 2 + getTxtHeight() + 4);
            }
        }
    }

    private void drawAdjustSrc(Canvas canvas,int w,int h){
        String s = "";
        if(QMUILangHelper.isNullOrEmpty(customName)) {
            if(gsno>=0 && params!=null && params.length>0) {
                for (int i = 0; i < params.length; i++) {
                    s += Integer.toHexString(params[i]) + " ";
                }
            }
        }else {
            s = customName;
        }
        darwText(s.trim(),canvas,w /2,h/2 + getTxtHeight()+4);
    }

    private void DrawMyShape(Canvas canvas){
        int w = getWidth();
        int h = getHeight();

        if(mShapeKinds == ShapeKinds.CIRCLE){
            int r;
            if(w>h) {
                r = h / 2;
            }else{
                r = w / 2;
            }
            r-=(stroke+2);
            canvas.drawCircle(w/2, h/2, r, mShapePaint);
        }else{
            Rect r = new Rect(stroke,stroke,w-stroke,h-stroke);
            canvas.drawRect(r,mShapePaint);
        }
    }

    private void darwText(String showstr,Canvas canvas,int x, int y){
        if (QMUILangHelper.isNullOrEmpty(showstr))
            return;
        if (QMUILangHelper.isNullOrEmpty(showstr))
            return;
        mShapePaint.setStyle(Paint.Style.FILL);
        Paint.FontMetrics fontMetrics=mShapePaint.getFontMetrics();
        float distance=(fontMetrics.bottom - fontMetrics.top)/2 - fontMetrics.bottom;
        float baseline=y+distance;
        //mShapePaint.setShadowLayer(2f, 0, 1, Color.GRAY);
        canvas.drawText(showstr, x, baseline, mShapePaint);
        //mShapePaint.setShadowLayer(0, 0, 0, Color.GRAY);//关闭阴影
    }

    private int getTxtHeight(){
        Paint.FontMetrics fontMetrics=mShapePaint.getFontMetrics();
        float distance=fontMetrics.bottom - fontMetrics.top;
        return (int)distance;
    }

    //字符串截取
    private String strJieduan(String src){
        if(src==null){
            return "";
        }
        String des = src;
        Rect bounds = new Rect();
        int p = src.length();
        while(true){
            mShapePaint.getTextBounds(src, 0, p, bounds);
            if(bounds.width()<getWidth())break;
            if(p==0)break;
            p--;
            if(p<0)break;
        }
        des = src.substring(0,p);
        if(p!=src.length())
            des +="...";
        return des;
    }

    public void setBtnName(String btnName) {
        this.btnName = btnName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public int getCol() {
        return col;
    }

    public void setRownCol(int col,int row) {
        this.col = col;
        this.row = row;
    }

    public int getRow() {
        return row;
    }

    public void setmSelected(boolean mSelected) {
        this.mSelected = mSelected;
    }

    public boolean getmSelected(){
        return mSelected;
    }

    public String getBtnName() {
        return btnName;
    }

    public enum ShapeKinds {
        CIRCLE            (0),
        RECT          (1);
        ShapeKinds(int nativeInt) {
            this.nativeInt = nativeInt;
        }
        final int nativeInt;
    }

    public ShapeKinds getmShapeKinds() {
        return mShapeKinds;
    }
    public void setmShapeKinds(ShapeKinds mShapeKinds) {
        this.mShapeKinds = mShapeKinds;
    }

    public int getKeyidx() {
        return keyidx;
    }

    public int getSno() {
        return sno;
    }

    public void setKeyidx(int keyidx) {
        this.keyidx = keyidx;
    }

    public void setSno(int sno) {
        this.sno = sno;
    }

    public void setMlearning(boolean mlearning) {
        this.mlearning = mlearning;
    }
    public boolean getMLearning(){
        return this.mlearning;
    }

    public void setMadjust(boolean madjust) {
        this.madjust = madjust;
    }

    public String getCustomName() {
        return customName;
    }

    public void setMshift(boolean mshift) {
        this.mshift = mshift;
    }
}
