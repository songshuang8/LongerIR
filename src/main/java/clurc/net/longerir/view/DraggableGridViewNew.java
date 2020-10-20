package clurc.net.longerir.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.qmuiteam.qmui.widget.QMUITopBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import clurc.net.longerir.data.BtnInfo;
import clurc.net.longerir.data.CfgData;

//dragged 为view类的序号

public class DraggableGridViewNew  extends ViewGroup implements View.OnTouchListener, View.OnClickListener, View.OnLongClickListener{
    private static int COL_CNT = 4;
    public static float childRatio = 0.8f;
    protected int childSize, padding, dpi, scroll = 0;
    protected float lastDelta = 0;
    protected Handler handler = new Handler();
    protected int dragged = -1, lastX = -1, lastY = -1, lastTarget = -1;
    protected boolean enabled = true, touching = false;
    public static int animT = 150;
    protected OnRearrangeListener onRearrangeListener;
    protected OnClickListener secondaryOnClickListener;
    private AdapterView.OnItemClickListener onItemClickListener;

    private List<BtnInfo> viewInfoList=null;
    private int targetview=-1;

    private boolean changed;
    private int touchmoved=-1;
    private boolean studying = false;
    private int selected = 0;

    public void setViewInfoList(List<BtnInfo> viewList){
        this.viewInfoList=viewList;
        changed = false;
    }

    public DraggableGridViewNew(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnTouchListener(this);
        super.setOnClickListener(this);
        setOnLongClickListener(this);

        handler.removeCallbacks(updateTask);
        handler.postAtTime(updateTask, SystemClock.uptimeMillis() + 500);
        setChildrenDrawingOrderEnabled(true);
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        dpi = metrics.densityDpi;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        secondaryOnClickListener = l;
    }

    @Override
    public void addView(View child) {
        super.addView(child);
    };

    @Override
    public void removeViewAt(int index) {
        super.removeViewAt(index);
    };

    private int flash_cnt = 0;
    private boolean flash_visi = false;
    protected Runnable updateTask = new Runnable() {
        public void run() {
            if (dragged != -1) {
                if (lastY < padding * 3 && scroll > 0)
                    scroll -= 20;
                else if (lastY > getBottom() - getTop() - (padding * 3)&& scroll < getMaxScroll())
                    scroll += 20;
            } else if (lastDelta != 0 && !touching) {
                scroll += lastDelta;
                lastDelta *= .9;
                if (Math.abs(lastDelta) < .25)
                    lastDelta = 0;
            }
            clampScroll();
            if(studying && flash_cnt++>20 && selected>=0){
                flash_visi  = !flash_visi;
                View v = getChildAt(selected);
                if(flash_visi){
                    v.setVisibility(View.VISIBLE);
                }else{
                    v.setVisibility(View.GONE);
                }
                flash_cnt = 0;
            }
            refreshchild(getLeft(), getTop(), getRight(), getBottom());
            handler.postDelayed(this, 50);
        }
    };

    private void refreshchild(int l, int t, int r, int b){
        childSize = Math.round(((r - l) / COL_CNT) * childRatio);
        padding = ((r - l) - (childSize * COL_CNT)) / (COL_CNT + 1);
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
        View v = getChildAt(vidx);
        int vfindx = (int)v.getTag();
        BtnInfo viewInfo=viewInfoList.get(vfindx);
        Point xy = getCoorFromIndex(viewInfo.col,viewInfo.row);
        v.layout(xy.x, xy.y, xy.x + childSize,xy.y + childSize);
    }

    private int getOldTargetInfo(int x,int y){
        int p=-1;
        for (int i = 0; i < viewInfoList.size(); i++) {
            BtnInfo viewInfo=viewInfoList.get(i);
            Point xy = getCoorFromIndex(viewInfo.col,viewInfo.row);
            Rect r= new Rect(xy.x, xy.y, xy.x + childSize,xy.y + childSize);
            if(r.contains(x,y)){
                p = i;
                break;
            }
        }
        if(p>0){
            int k=-1;
            for (int i = 0; i < getChildCount(); i++) {
                if(((int)(getChildAt(i).getTag()))==p){
                    k = i;
                    break;
                }
            }
            p = k;
        }
        return p;
    }

    protected Point getCoorFromIndex(int col,int row) {
       return new Point(padding + (childSize + padding) * col,
                padding + (childSize + padding) * row - scroll);
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

    public int getIndexFromCoor(int x, int y) {
        int col = getColOrRowFromCoor(x), row = getColOrRowFromCoor(y + scroll);
        if (col == -1 || row == -1)
            return -1;
        return row * COL_CNT + col;
    }

    protected int getColOrRowFromCoor(int coor) {
        coor -= padding;
        for (int i = 0; coor > 0; i++) {
            if (coor < childSize)
                return i;
            coor -= (childSize + padding);
        }
        return -1;
    }

    protected int getTargetFromCoor(int x, int y) {
        if (getColOrRowFromCoor(y + scroll) == -1)
            return -1;
        int pos = getIndexFromCoor(x, y);
        if (pos == -1)
            return -1;
        return pos;
    }

    @Override
    public void onClick(View view) {
        if (enabled) {
            int alst = getViewIndexByPoint(lastX,lastY);
            if(alst!=-1){
                if (secondaryOnClickListener != null)
                    secondaryOnClickListener.onClick(getChildAt(alst));
                if(studying && alst!=selected) {
                    if (selected >= 0)
                        getChildAt(selected).setVisibility(View.VISIBLE);
                    flash_cnt = 30;
                    flash_visi = true;
                    selected = alst;
                }
            }
            if (onItemClickListener != null && alst != -1)
                onItemClickListener.onItemClick(null, getChildAt(alst),alst,alst / COL_CNT);
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (!enabled)
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
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    int l = x - (3 * childSize / 4);
                    int t = y - (3 * childSize / 4);
                    getChildAt(dragged).layout(l, t, l + (childSize * 3 / 2),t + (childSize * 3 / 2));
                    lastTarget = getTargetFromCoor(x, y);
                    targetview=getOldTargetInfo(x, y);
                    if(targetview==dragged)
                        targetview=-1;
                    if(lastTarget!=-1 && targetview!=-1){
                        if(touchmoved!=targetview)
                        {
                            int vfindx = (int) getChildAt(dragged).getTag();
                            BtnInfo viewInfo = viewInfoList.get(vfindx);
                            Point xy = getCoorFromIndex(viewInfo.col, viewInfo.row);
                            getChildAt(targetview).layout(xy.x, xy.y, xy.x + childSize, xy.y + childSize);
                            touchmoved = targetview;
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
                    View v = getChildAt(dragged);
                    int src = (int)v.getTag();
                    if (lastTarget != -1){
                        if(targetview==-1){
                            BtnInfo viewInfo=viewInfoList.get(src);
                            viewInfo.col=lastTarget%4;
                            viewInfo.row=lastTarget/4;
                            toLayOut(dragged);
                        }else{
                            int des = (int)getChildAt(targetview).getTag();
                            int temp  = viewInfoList.get(src).col;
                            viewInfoList.get(src).col = viewInfoList.get(des).col;
                            viewInfoList.get(des).col = temp;

                            temp  = viewInfoList.get(src).row;
                            viewInfoList.get(src).row = viewInfoList.get(des).row;
                            viewInfoList.get(des).row = temp;

                            toLayOut(dragged);
                            toLayOut(targetview);
                        }
                        changed = true;
                    }else
                        toLayOut(dragged);
                    v.clearAnimation();
                    if (v instanceof ImageView)
                        ((ImageView) v).setAlpha(255);
                    lastTarget = -1;
                    dragged = -1;
                    targetview=-1;
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
        for(int i=0;i<viewInfoList.size();i++){
            BtnInfo viewInfo=viewInfoList.get(i);
            if(viewInfo.row>maxNum){
                maxNum=viewInfo.row;
            }
        }
        int rowCount=maxNum + 2;
        return rowCount * childSize + (rowCount + 1) * padding - getHeight();
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
        View v = getChildAt(pos);
        BtnInfo viewInfo=viewInfoList.get((int)v.getTag());
        int x = getCoorFromIndex(viewInfo.col,viewInfo.row).x + childSize / 2;
        int y = getCoorFromIndex(viewInfo.col,viewInfo.row).y+ childSize / 2;

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

    public void setOnRearrangeListener(OnRearrangeListener l) {
        this.onRearrangeListener = l;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener l) {
    }

    public boolean getChanged(){
        return changed;
    }

    public void SetStudying(boolean b){
        studying = b;
    }

    public void setCurrIrData(byte[] decode){
        if(selected<0)return;
        View v = getChildAt(selected);
        int vfindx = (int)v.getTag();
        BtnInfo viewInfo=viewInfoList.get(vfindx);
        if(viewInfo==null)return;
        viewInfo.gsno = CfgData.getDWordValue(decode,0);
        int paramlen = (decode.length-4)/4;
        viewInfo.param16 = "";
        viewInfo.params = new int[paramlen];
        for (int i = 0; i < paramlen; i++) {
            viewInfo.params[i] = CfgData.getDWordValue(decode,i*4+4);
            viewInfo.param16 += Integer.toHexString(viewInfo.params[i])+",";
        }
        getChildAt(selected).setVisibility(View.VISIBLE);
        selected++;
        if(selected>=getChildCount())selected = 0;
    }
}
