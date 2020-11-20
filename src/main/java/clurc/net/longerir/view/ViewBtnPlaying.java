package clurc.net.longerir.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

import com.qmuiteam.qmui.util.QMUILangHelper;

import clurc.net.longerir.R;

public class ViewBtnPlaying  extends View {
    private static int stroke = 3;
    private Context context;
    private Paint mShapePaint;

    private int VIEW_W,VIEW_H;
    private RemoteBtnView.ShapeKinds mShapeKinds;
    public int row;
    public int col;
    public String btnName;
    public int idx;

    private boolean ispress=false;
    @Override
    public void draw(Canvas canvas) {
        VIEW_W = getWidth();
        VIEW_H = getHeight();
        super.draw(canvas);
        mShapePaint.setStyle(Paint.Style.FILL);

        DrawMyShape(canvas, true);
        DrawMyShape(canvas, false);

        darwText(strJieduan(btnName),canvas);
    }

    private void DrawMyShape(Canvas canvas,boolean isback){
        if(mShapeKinds == RemoteBtnView.ShapeKinds.CIRCLE){
            int r;
            if(VIEW_W>VIEW_H) {
                r = VIEW_H / 2;
            }else{
                r = VIEW_W / 2;
            }
            if(ispress){
                if (isback) {
//                    mShapePaint.setColor(getResources().getColor(R.color.btnBack));
//                    canvas.drawCircle(VIEW_W / 2, VIEW_H / 2, r, mShapePaint);
                }else {
                    r-=(stroke+2);
                    mShapePaint.setColor(getResources().getColor(R.color.btnPres));
                    canvas.drawCircle(VIEW_W / 2, VIEW_H / 2, r, mShapePaint);
                }
            }else {
                r-=(stroke+2);
                if (isback) {
                    mShapePaint.setColor(getResources().getColor(R.color.btnBack));
                    canvas.drawCircle(VIEW_W / 2 + 6, VIEW_H / 2 + 6, r, mShapePaint);
                }else {
                    mShapePaint.setColor(getResources().getColor(R.color.btnNomral));
                    canvas.drawCircle(VIEW_W / 2, VIEW_H / 2, r, mShapePaint);
                }
            }
        }else{
            Rect r = new Rect(stroke,stroke,VIEW_W-stroke,VIEW_H-stroke);
            canvas.drawRect(r,mShapePaint);
        }
    }

    private void darwText(String showstr,Canvas canvas){
        if (QMUILangHelper.isNullOrEmpty(showstr))
            return;
        if (QMUILangHelper.isNullOrEmpty(showstr))
            return;
        mShapePaint.setColor(Color.WHITE);
        mShapePaint.setStyle(Paint.Style.FILL);
        Paint.FontMetrics fontMetrics=mShapePaint.getFontMetrics();
        float distance=(fontMetrics.bottom - fontMetrics.top)/2 - fontMetrics.bottom;
        float baseline=VIEW_H/2+distance;
        //mShapePaint.setShadowLayer(2f, 0, 1, Color.GRAY);
        canvas.drawText(showstr, VIEW_W/2, baseline, mShapePaint);
        //mShapePaint.setShadowLayer(0, 0, 0, Color.GRAY);//关闭阴影
    }

    public ViewBtnPlaying(Context context,int col,int row,String btnName) {
        super(context);
        this.context = context;
        mShapePaint = new Paint();
        mShapePaint.setAntiAlias(false); //设置画笔为无锯齿
        mShapePaint.setStrokeWidth(stroke);              //线宽
        mShapePaint.setStyle(Paint.Style.FILL);                   //空心效果
        mShapePaint.setTextSize(46);
        mShapePaint.setTextAlign(Paint.Align.CENTER);
        this.col = col;
        this.row = row;
        this.btnName = btnName;
    }

    public RemoteBtnView.ShapeKinds getmShapeKinds() {
        return mShapeKinds;
    }
    public void setmShapeKinds(RemoteBtnView.ShapeKinds mShapeKinds) {
        this.mShapeKinds = mShapeKinds;
    }

    public enum ShapeKinds {
        CIRCLE            (0),
        RECT          (1);
        ShapeKinds(int nativeInt) {
            this.nativeInt = nativeInt;
        }
        final int nativeInt;
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

    public void setIspress(boolean ispress) {
        this.ispress = ispress;
        invalidate();
    }
}
