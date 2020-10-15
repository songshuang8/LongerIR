package clurc.net.longerir.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import clurc.net.longerir.R;

public class ViewDragGridReadOnly extends ViewGroup{
    private Context context;
    private int COL_CNT;
    public static float childRatio = 0.8f;
    protected int childSize, paddingW,paddingH, dpi;
    private int view_width;
    private boolean isacview = false;

    public ViewDragGridReadOnly(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        COL_CNT = 4;
        setChildrenDrawingOrderEnabled(true);
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        dpi = metrics.densityDpi;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        for (int i = 0; i < getChildCount(); i++) {
            //这个很重要，没有就不显示
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
    }


    @Override
    public void addView(View child) {
        super.addView(child);
    };

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(isacview){
            for (int i = 0; i < getChildCount(); i++) {
                View v =getChildAt(i);

                v.refreshDrawableState();
                v.layout(getPaddingLeft(),getPaddingTop(),getMeasuredWidth()- getPaddingRight(),getMeasuredHeight()- getPaddingBottom());
            }
        }else {
            childSize = Math.round(((r - l) / COL_CNT) * childRatio);
            view_width = r - l;
            paddingH = (view_width - (childSize * COL_CNT)) / (COL_CNT + 1);
            for (int i = 0; i < getChildCount(); i++) {
                toLayOut(i);
            }
        }
    }

    private void toLayOut(int vidx){
        RemoteBtnView v = getChileByIndex(vidx);
        Point p = getPointFromRowAndCol(v.getCol(),v.getRow());
        v.layout(p.x, p.y, p.x + childSize,p.y + childSize);
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        return i;
    }

    private RemoteBtnView getChileByIndex(int idx){
        return (RemoteBtnView)getChildAt(idx);
    }
    //根据列号，行号获取位置
    protected Point getPointFromRowAndCol(int col,int row) {
        int arowCnt = get_ARowCnt(row);
        paddingW = (view_width - (childSize * arowCnt)) / (arowCnt + 1);
        return new Point(paddingW + (childSize + paddingW) * col,
                paddingH + (childSize + paddingH) * row );
    }

    public void setCOL_CNT(int col_cnt){
        COL_CNT = col_cnt;
    }

    //当前行个数等于总列
    private int get_ARowCnt(int arow){
        int nowrow = 0;
        for (int i = 0; i < getChildCount(); i++) {
            RemoteBtnView v = getChileByIndex(i);
            if(v.getRow()==arow){
                nowrow++;
            }
        }
        return nowrow;
    }

    public void setIsacview(boolean isacview) {
        this.isacview = isacview;
    }
}
