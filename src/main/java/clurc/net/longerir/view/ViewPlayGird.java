package clurc.net.longerir.view;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import clurc.net.longerir.R;

public class ViewPlayGird extends ViewGroup implements View.OnTouchListener{
    public static float childRatio = 0.8f;
    private float lastY = -1, clickY = -1;
    private int maxRow;
    private boolean pressed = false;
    protected Handler handler = new Handler();
    protected float lastMoved = 0,maxMoveY;
    private int animate = -1;

    private int COL_CNT;
    private int childSize, paddingW,paddingH = 0;
    private int scroll = 0;
    private int VIEW_WID = 0;
    private boolean fixdcolrow=false;

    private int clickIndex = -1;
    public ViewPlayGird(Context context, AttributeSet attrs) {
        super(context, attrs);
        maxRow = 0;
        setOnTouchListener(this);
        COL_CNT = 4;
        setChildrenDrawingOrderEnabled(true);

        handler.removeCallbacks(updateTask);
        handler.postAtTime(updateTask, SystemClock.uptimeMillis() + 500);
    }

    public boolean onTouch(View view, MotionEvent event) {
        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                clickY =  event.getY();
                lastY = clickY;
                clickIndex = getViewIndexByPoint((int)event.getX(),(int)lastY);
                if(clickIndex>=0) {
                    ViewBtnPlaying abtn = getChileByIndex(clickIndex);
                    abtn.setIspress(true);
                }
                pressed = true;
                maxMoveY = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                lastMoved = lastY - (int) event.getY();
                scroll += lastMoved;
                lastY = (int) event.getY();

                float movedY = Math.abs(clickY - lastY);
                if(movedY>maxMoveY)
                    maxMoveY = movedY;
                break;
            case MotionEvent.ACTION_UP:
                if(clickIndex>=0) {
                    ViewBtnPlaying abtn = getChileByIndex(clickIndex);
                    if(maxMoveY<10) {
                        if (OnPlayEvent != null) {
                            if (OnPlayEvent.onPlay(clickIndex)) {
                                abtn.setIspress(false);
                                animateRushing(clickIndex);
                            }
                        }
                    }
                    abtn.setIspress(false);
                }
                clickIndex  = -1;
                pressed = false;
                break;
        }
        return true;
    }

    protected void animateRushing(int idx) {
        StopClearAnimate();
        ViewBtnPlaying v = getChileByIndex(idx);
        Point pt = getPointFromRowAndCol(v.col,v.row);
//        int x = pt.x + childSize / 2;
//        int y = pt.y+ childSize / 2;
//
//        int l = x - (3 * childSize / 4), t = y - (3 * childSize / 4);
//        v.layout(l, t, l + (childSize * 3 / 2), t + (childSize * 3 / 2));
        AnimationSet animSet = new AnimationSet(true);
        ScaleAnimation scale = new ScaleAnimation(1f, 1.9f, 1f, 1.9f,Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        scale.setDuration(300);
        AlphaAnimation alpha = new AlphaAnimation(1, .2f);
        alpha.setDuration(300);

        animSet.addAnimation(scale);
        animSet.addAnimation(alpha);
        animSet.setFillEnabled(true);
        animSet.setFillAfter(true);

        animSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                StopClearAnimate();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        v.clearAnimation();
        v.startAnimation(animSet);
        animate = idx;
    }

    protected void StopClearAnimate(){
        if(animate>=0){
            getChileByIndex(animate).clearAnimation();
            animate = -1;
        }
    }

    protected void clampScroll() {
        int stretch = 3;
        int overreach = getHeight() / 2;
        int max = getMaxScroll();
        max = Math.max(max, 0);
        if (scroll < -overreach) {
            scroll = -overreach;
            lastMoved = 0;
        } else if (scroll > max + overreach) {
            scroll = max + overreach;
            lastMoved = 0;
        } else if (scroll < 0) {
            if (scroll >= -stretch)
                scroll = 0;
            else if (!pressed)
                scroll -= scroll / stretch;
        } else if (scroll > max) {
            if (scroll <= max + stretch)
                scroll = max;
            else if (!pressed)
                scroll += (max - scroll) / stretch;
        }
    }

    protected Runnable updateTask = new Runnable() {
        public void run() {
            int oldscroll = scroll;
            if (clickIndex != -1) {
                if (lastY < paddingH * 3 && scroll > 0)
                    scroll -= 20;
                else if (lastY > getBottom() - getTop() - (paddingH * 3)&& scroll < getMaxScroll())
                    scroll += 20;
            } else if (lastMoved != 0 && !pressed) {
                scroll += lastMoved;
                lastMoved *= .9;
                if (Math.abs(lastMoved) < .25)
                    lastMoved = 0;
            }
            clampScroll();
            //if(oldscroll!=scroll)
                refreshchild(getLeft(), getTop(), getRight(), getBottom());
            handler.postDelayed(this, 50);
        }
    };

    protected Point getPointFromRowAndCol(int col,int row) {
        if(fixdcolrow){
            return new Point(paddingH + (childSize + paddingH) * col,
                    paddingH + (childSize + paddingH) * row - scroll);
        }else {
            int arowCnt = 0;
            int rowindex = 0;
            for (int i = 0; i < getChildCount(); i++) {
                ViewBtnPlaying v = getChileByIndex(i);
                if(v.row==row){
                    arowCnt++;
                    if(v.col<col)rowindex++;
                }
            }
            paddingW = (VIEW_WID - (childSize * arowCnt)) / (arowCnt + 1);
            return new Point(paddingW + (childSize + paddingW) * rowindex,
                    paddingH + (childSize + paddingH) * row - scroll);
        }
    }

    private void refreshchild(int l, int t, int r, int b){
        childSize = Math.round(((r - l) / COL_CNT) * childRatio);
        VIEW_WID = r - l;
        paddingH = (VIEW_WID - (childSize * COL_CNT)) / (COL_CNT + 1);
        for (int i = 0; i < getChildCount(); i++) {
            if(animate!=i)
                toLayOut(i);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        refreshchild(l,t,r,b);
    }

    private void toLayOut(int vidx){
        ViewBtnPlaying v = getChileByIndex(vidx);
        Point p = getPointFromRowAndCol(v.col,v.row);
        v.layout(p.x, p.y, p.x + childSize,p.y + childSize);
    }

    private ViewBtnPlaying getChileByIndex(int idx){
        return (ViewBtnPlaying)getChildAt(idx);
    }

    public void setFixdcolrow(boolean fixdcolrow) {
        this.fixdcolrow = fixdcolrow;
    }

    public void setCOL_CNT(int COL_CNT) {
        this.COL_CNT = COL_CNT;
    }

    private OnBtnPlay OnPlayEvent;
    public interface OnBtnPlay{
        boolean onPlay(int idx);
    }

    public void setOnPlayEvent(OnBtnPlay onPlayEvent) {
        OnPlayEvent = onPlayEvent;
    }

    public int getViewIndexByPoint(int x,int y) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View v = getChildAt(i);
            Rect r = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
            if (r.contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    protected int getMaxScroll() {
        int rowCount = maxRow + 2;
        return rowCount * childSize + (rowCount + 1) * paddingH - getHeight();
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        if(((ViewBtnPlaying)child).row>maxRow)
            maxRow = ((ViewBtnPlaying)child).row;
    };
}

