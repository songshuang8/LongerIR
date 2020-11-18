package clurc.net.longerir.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import clurc.net.longerir.data.BtnInfo;
import clurc.net.longerir.data.CfgData;

public class ViewDragGrid extends ViewGroup implements View.OnTouchListener, View.OnClickListener, View.OnLongClickListener{
    private int COL_CNT;
    public static float childRatio = 0.8f;
    protected int childSize, paddingW,paddingH, dpi, scroll = 0;
    protected float lastDelta = 0;
    protected Handler handler = new Handler();
    protected int dragged = -1, lastX = -1, lastY = -1, lastTarget = -1;
    protected boolean enabled = true, touching = false;
    public static int animT = 150;
    protected OnClickListener selectChangedListen;
    private OnClickListener onItemClickListener;
    private int view_width;

    private RemoteBtnView targetview=null;
    private int targetindex = -1;

    private boolean changed;
    private int touchmoved=-1;
    private int selected = -1;
    private boolean aline = false; //每行是否自动分配

    public ViewDragGrid(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnTouchListener(this);
        super.setOnClickListener(this);
        setOnLongClickListener(this);

        COL_CNT = 4;
        handler.removeCallbacks(updateTask);
        handler.postAtTime(updateTask, SystemClock.uptimeMillis() + 500);
        setChildrenDrawingOrderEnabled(true);
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        dpi = metrics.densityDpi;
    }

    @Override
    public void addView(View child) {
        super.addView(child);
    };

    @Override
    public void removeView(View chid) {
        RemoteBtnView vr = (RemoteBtnView)chid;
        int adelcol = vr.getCol();
        int adelrow = vr.getRow();
        super.removeView(chid);
        ReArrangeRow(adelrow,adelcol);
        selected = -1;
    };

    protected Runnable updateTask = new Runnable() {
        public void run() {
            if (dragged != -1) {
                if (lastY < paddingH * 3 && scroll > 0)
                    scroll -= 20;
                else if (lastY > getBottom() - getTop() - (paddingH * 3)&& scroll < getMaxScroll())
                    scroll += 20;
            } else if (lastDelta != 0 && !touching) {
                scroll += lastDelta;
                lastDelta *= .9;
                if (Math.abs(lastDelta) < .25)
                    lastDelta = 0;
            }
            clampScroll();
            refreshchild(getLeft(), getTop(), getRight(), getBottom());
            handler.postDelayed(this, 50);
        }
    };

    private void refreshchild(int l, int t, int r, int b){
        childSize = Math.round(((r - l) / COL_CNT) * childRatio);
        view_width = r - l;
        paddingH = (view_width - (childSize * COL_CNT)) / (COL_CNT + 1);
        for (int i = 0; i < getChildCount(); i++) {
            if (i != dragged) {
                toLayOut(i);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        refreshchild(l,t,r,b);
    }

    private void toLayOut(int vidx){
        if(touchmoved==vidx)return;
        RemoteBtnView v = getChileByIndex(vidx);
        Point p = getPointFromRowAndCol(v.getCol(),v.getRow());
        v.layout(p.x, p.y, p.x + childSize,p.y + childSize);
    }
    // 根据位置寻找该位置的btn
    private RemoteBtnView getTargetView(int pixx,int pixy){
        RemoteBtnView p=null;
        targetindex = -1;
        for (int i = 0; i < getChildCount(); i++) {
            RemoteBtnView v = getChileByIndex(i);
            Point xy = getPointFromRowAndCol(v.getCol(),v.getRow());
            Rect r= new Rect(xy.x, xy.y, xy.x + childSize,xy.y + childSize);
            if(r.contains(pixx,pixy)){
                p = v;
                targetindex = i;
                break;
            }
        }
        return p;
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (dragged == -1)
            return i;
        else if (i == childCount - 1)
            return dragged;
        else if (i >= dragged)
            return i + 1;
        return i;
    }

    protected int getFromCoorX(int posX) {
        posX -= paddingW;
        for (int i = 0; posX > 0; i++) {
            if (posX < childSize)
                return i;
            posX -= (childSize + paddingW);
        }
        return -1;
    }

    protected int getFromCoorY(int posY) {
        posY -= paddingH;
        for (int i = 0; posY > 0; i++) {
            if (posY < childSize)
                return i;
            posY -= (childSize + paddingH);
        }
        return -1;
    }

    //找坑位号
    protected int getTargetFromCoor(int x, int y) {
        int row = getFromCoorY(y + scroll);
        if(row== -1)return -1;

        int col = getFromCoorX(x);
        if (col == -1)return -1;

        return row * COL_CNT + col;
    }

    @Override
    public void onClick(View view) {
        if (enabled) {
            int alst = getViewIndexByPoint(lastX,lastY);
            if(alst!=-1){
                if (onItemClickListener != null) {  //点击发码
                    onItemClickListener.onClick(getChildAt(alst));
                    alst = -1;
                    return;
                }
                if(alst!=selected) {
                    selected = alst;
                    setSelectedChildView(selected);
                    if (selectChangedListen != null)
                        selectChangedListen.onClick(getChildAt(alst));
                }
            }
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (!enabled)
            return false;
        if (onItemClickListener != null)   //点击发码
            return false;
        dragged = getViewIndexByPoint(lastX,lastY);
        if (dragged>=0) {
            animateDragged(dragged);
            return true;
        }
        return false;
    }
    public boolean onTouch(View view, MotionEvent event) {
        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                enabled = true;
                lastX = (int) event.getX();
                lastY = (int) event.getY();
                touching = true;
                break;
            case MotionEvent.ACTION_MOVE:
                int delta = lastY - (int) event.getY();
                if (dragged != -1) {
                    RemoteBtnView vcap = getChileByIndex(dragged);
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    int l = x - (3 * childSize / 4);
                    int t = y - (3 * childSize / 4);
                    lastTarget = getTargetFromCoor(x, y);
                    targetview=getTargetView(x, y);
                    vcap.layout(l, t, l + (childSize * 3 / 2),t + (childSize * 3 / 2));

                    if(targetindex==dragged) {
                        targetview = null;
                        targetindex = -1;
                    }
                    if(lastTarget!=-1 && targetview!=null){
                        if(touchmoved!=targetindex)
                        {
                            Point xy = getPointFromRowAndCol(vcap.getCol(), vcap.getRow());
                            targetview.layout(xy.x, xy.y, xy.x + childSize, xy.y + childSize);
                            touchmoved = targetindex;
                        }
                    }else
                        touchmoved = -1;
                } else {
                    scroll += delta;
                    if (Math.abs(delta) > 8) {
                        //clampScroll();
                        enabled = false;
                        //onLayout(true, getLeft(), getTop(), getRight(), getBottom());
                    }
                    //invalidate();
                }
                lastX = (int) event.getX();
                lastY = (int) event.getY();
                lastDelta = delta;
                break;
            case MotionEvent.ACTION_UP:
                if (dragged != -1) {
                    RemoteBtnView vsrc = getChileByIndex(dragged);
                    if (lastTarget != -1){
                        if(targetview==null){
                            int adelrow = vsrc.getRow();
                            int adelcol = vsrc.getCol();
                            if(!((lastTarget % 4)==adelcol && (lastTarget / 4)==adelrow)){
                                toAppendPosition(vsrc, lastTarget % 4, lastTarget / 4);
                                ReArrangeRow(adelrow, adelcol);
                            }
                            toLayOut(dragged);
                        }else{
                            int tempcol  = vsrc.getCol();
                            int temprow  = vsrc.getRow();
                            vsrc.setRownCol(targetview.getCol(), targetview.getRow());
                            targetview.setRownCol(tempcol, temprow);
                            toLayOut(dragged);
                            toLayOut(targetindex);
                        }
                        changed = true;
                    }else
                        toLayOut(dragged);
                    vsrc.clearAnimation();
                    if (vsrc instanceof ImageView)
                        vsrc.setAlpha(255);
                    lastTarget = -1;
                    dragged = -1;
                    targetview=null;
                    targetindex = -1;
                    touchmoved = -1;
                }
                touching = false;
                break;
        }
        if (dragged != -1)
            return true;
        return false;
    }

    public void scrollToTop() {
        scroll = 0;
    }

    public void scrollToBottom() {
        scroll = Integer.MAX_VALUE;
        clampScroll();
    }

    protected void clampScroll() {
        int stretch = 3, overreach = getHeight() / 2;
        int max = getMaxScroll();
        max = Math.max(max, 0);
        if (scroll < -overreach) {
            scroll = -overreach;
            lastDelta = 0;
        } else if (scroll > max + overreach) {
            scroll = max + overreach;
            lastDelta = 0;
        } else if (scroll < 0) {
            if (scroll >= -stretch)
                scroll = 0;
            else if (!touching)
                scroll -= scroll / stretch;
        } else if (scroll > max) {
            if (scroll <= max + stretch)
                scroll = max;
            else if (!touching)
                scroll += (max - scroll) / stretch;
        }
    }

    protected int getMaxScroll() {
        int maxNum=0;
        for (int i = 0; i < getChildCount(); i++) {
            RemoteBtnView v = getChileByIndex(i);
            if(v.getRow()>maxNum){
                maxNum=v.getRow();
            }
        }
        int rowCount=maxNum + 2;
        return rowCount * childSize + (rowCount + 1) * paddingH - getHeight();
    }

    public int getViewIndexByPoint(int x,int y) {
        int count=getChildCount();
        for (int i = 0; i < count; i++) {
            View v = getChildAt(i);
            Rect r = new Rect(v.getLeft(),v.getTop(),v.getRight(),v.getBottom());
            if(r.contains(x,y)){
                return i;
            }
        }
        return -1;
    }

    protected void animateDragged(int pos) {
        RemoteBtnView v = getChileByIndex(pos);
        Point pt = getPointFromRowAndCol(v.getCol(),v.getRow());
        int x = pt.x + childSize / 2;
        int y = pt.y+ childSize / 2;

        int l = x - (3 * childSize / 4), t = y - (3 * childSize / 4);
        v.layout(l, t, l + (childSize * 3 / 2), t + (childSize * 3 / 2));
        AnimationSet animSet = new AnimationSet(true);
        ScaleAnimation scale = new ScaleAnimation(.667f, 1, .667f, 1,childSize * 3 / 4, childSize * 3 / 4);
        scale.setDuration(animT);
        AlphaAnimation alpha = new AlphaAnimation(1, .5f);
        alpha.setDuration(animT);

        animSet.addAnimation(scale);
        animSet.addAnimation(alpha);
        animSet.setFillEnabled(true);
        animSet.setFillAfter(true);

        v.clearAnimation();
        v.startAnimation(animSet);
    }

    public void setOnItemClickListener(OnClickListener  l) {
        this.onItemClickListener = l;
    }

    public boolean getChanged(){
        return changed;
    }

    public void setSelectedChildView(int idx){
        int count=getChildCount();
        for (int i = 0; i < count; i++) {
            RemoteBtnView v = (RemoteBtnView)getChildAt(i);
            if(i==idx){
                v.setmSelected(true);
                v.invalidate();
            }else{
                if(v.getmSelected()){
                    v.setmSelected(false);
                    v.invalidate();
                }
            }
        }
    }

    private RemoteBtnView getChileByIndex(int idx){
        return (RemoteBtnView)getChildAt(idx);
    }
    //根据列号，行号获取位置
    protected Point getPointFromRowAndCol(int col,int row) {
        if(aline){
            return new Point(paddingH + (childSize + paddingH) * col,
                    paddingH + (childSize + paddingH) * row - scroll);
        }else {
            int arowCnt = 0;
            int rowindex = 0;
            for (int i = 0; i < getChildCount(); i++) {
                RemoteBtnView v = getChileByIndex(i);
                if(v.getRow()==row){
                    arowCnt++;
                    if(v.getCol()<col)rowindex++;
                }
            }
            paddingW = (view_width - (childSize * arowCnt)) / (arowCnt + 1);
            return new Point(paddingW + (childSize + paddingW) * rowindex,
                    paddingH + (childSize + paddingH) * row - scroll);
        }
    }

    public void setSelectChangedListen(OnClickListener selectChangedListen) {
        this.selectChangedListen = selectChangedListen;
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

    private  void toAppendPosition(RemoteBtnView src,int willcol,int willrow){
        int arowCnt = get_ARowCnt(willrow);
        if(arowCnt==COL_CNT){
            src.setRownCol(willcol,willrow);
            return;
        }
        for (int i = 0; i < getChildCount(); i++) {
            RemoteBtnView v = getChileByIndex(i);
            if(v.getRow()==willrow){
                if(v.getCol()>=willcol)
                    v.setRownCol(v.getCol()+1,willrow);
            }
        }
        src.setRownCol(willcol,willrow);
    }

    //当一行删除变化后整理
    private void ReArrangeRow(int arow,int delcol){
        int arowCnt = get_ARowCnt(arow);
        for (int i = 0; i < getChildCount(); i++) {
            RemoteBtnView v = getChileByIndex(i);
            if(v.getRow()==arow){
                if(v.getCol()>delcol){
                    v.setRownCol(v.getCol()-1,arow);
                }
            }
        }
    }

    public void setAline(boolean b) {
        this.aline = b;
    }
}
