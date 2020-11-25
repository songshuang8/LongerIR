package clurc.net.longerir.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import clurc.net.longerir.R;
import clurc.net.longerir.data.BtnModelData;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.modeldata.DataModelInfo;
import clurc.net.longerir.data.webHttpClientCom;
import clurc.net.longerir.manager.QDPreferenceManager;
import clurc.net.longerir.manager.UiUtils;

public class ViewPlayAdjust extends ViewGroup implements View.OnTouchListener , View.OnClickListener, View.OnLongClickListener{
    public static float childRatio = 0.8f;
    private Context context;
    private float clickY;
    private int maxRow;
    private boolean pressed = false;
    protected Handler handler = new Handler();

    private int COL_CNT;
    private int childSize, paddingH = 0;
    private int clickscroll,scroll = 0;
    private int VIEW_WID = 0;

    public int clickIndex = -1;
    private boolean filechanged = false;
    private int dragged = -1;
    private int exchanged = -1;
    private int movecol,moverow;
    private boolean isinrect;
    private int lastmovedy;

    public ViewPlayAdjust(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        maxRow = 0;
        setOnTouchListener(this);
        setOnClickListener(this);
        setOnLongClickListener(this);
        COL_CNT = 4;
        setChildrenDrawingOrderEnabled(true);

        handler.removeCallbacks(updateTask);
        handler.postAtTime(updateTask, SystemClock.uptimeMillis() + 500);
    }

    public boolean onTouch(View view, MotionEvent event) {
        ViewBtnPlaying abtn;
        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                clickY =  event.getY();
                clickIndex = getViewIndexByPoint((int)event.getX(),(int)clickY);
                if(clickIndex>=0) {
                    abtn = getChileByIndex(clickIndex);
                    abtn.setIspress(true);
                }
                clickscroll = scroll;
                pressed = true;
                exchanged =-1;
                isinrect = false;
                lastmovedy = (int)clickY;
                mustrefresh = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (dragged != -1) {
                    abtn = getChileByIndex(dragged);
                    int x = (int) event.getX();
                    lastmovedy = (int) event.getY();
                    // 陷坑作用
                    moverow = Math.round((lastmovedy-paddingH+scroll)/(childSize + paddingH));
                    movecol = Math.round((x-paddingH)/(childSize + paddingH));
                    Point p = new Point(paddingH + (childSize + paddingH) * movecol,
                            paddingH + (childSize + paddingH) * moverow - scroll);
                    p.x += (childSize/2);
                    p.y += (childSize/2);
                    int exoffset = (int)(childSize*0.3f);
                    Rect r= new Rect(p.x-exoffset, p.y-exoffset, p.x + exoffset,p.y  + exoffset);
                    isinrect = r.contains(x,lastmovedy);
                    if(isinrect){
                        int aexchange = getTargetViewByButSelf(movecol,moverow,dragged);
                        if(aexchange>=0){
                            if(aexchange!=exchanged) {
                                startMoveOld(exchanged);
                                exchanged = aexchange;
                                //Point xy = getPointFromRowAndCol(abtn.col, abtn.row);
                                startMoveNew(exchanged,abtn.col,abtn.row);
                                //getChileByIndex(exchanged).layout(xy.x, xy.y, xy.x + childSize, xy.y + childSize);
                            }
                            Point xy = getPointFromRowAndCol(movecol, moverow);
                            abtn.layout(xy.x, xy.y, xy.x + childSize, xy.y + childSize);
                        }else{
                            startMoveOld(exchanged);
                            Point xy = getPointFromRowAndCol(movecol, moverow);
                            abtn.layout(xy.x, xy.y, xy.x + childSize, xy.y + childSize);
                            exchanged = -1;
                        }
                    }else{
                        startMoveOld(exchanged);
                        exchanged = -1;
                        abtn.layout(x- childSize/2, lastmovedy - childSize / 2,
                                x + childSize / 2,lastmovedy + childSize / 2);
                    }
                }else{
                    scroll = clickscroll + (int)(clickY - event.getY());
                }
                break;
            case MotionEvent.ACTION_UP:
                if(clickIndex>=0) {
                    getChileByIndex(clickIndex).setIspress(false);
                }
                if(dragged>=0){
                    abtn = getChileByIndex(dragged);
                    if(isinrect){
                        if(exchanged>=0){
                            ViewBtnPlaying v = getChileByIndex(exchanged);
                            v.col = abtn.col;
                            v.row = abtn.row;
                        }
                        abtn.col = movecol;
                        abtn.row = moverow;
                        //reFreshChild(abtn);
                        doFileChange();
                    }
                    abtn.clearAnimation();
                    dragged = -1;
                }
                exchanged =-1;
                pressed = false;
                ClearAllAnition();
                mustrefresh = true;
                break;
        }
        if (dragged != -1)
            return true;
        return false;
    }

     protected void clampScroll() {
        int stretch = 3;
        int overreach = getHeight() / 2;
        int max = getMaxScroll();
        max = Math.max(max, 0);
        if (scroll < -overreach) {
            scroll = -overreach;
        } else if (scroll > max + overreach) {
            scroll = max + overreach;
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

    private int currscr=-1;
    private boolean mustrefresh = true;
    protected Runnable updateTask = new Runnable() {
        public void run() {
            if (pressed) {
                if(dragged>=0){
                    if (lastmovedy < paddingH * 3 && scroll > 0)
                        scroll -= 20;
                    else if (lastmovedy > getBottom() - getTop() - (paddingH * 3)&& scroll < getMaxScroll())
                        scroll += 20;
                    clampScroll();
                }
            } else{
                clampScroll();
            }
            if(currscr!=scroll || mustrefresh) {
                refreshAllchild(getLeft(), getTop(), getRight(), getBottom());
                currscr = scroll;
                mustrefresh = false;
            }
            handler.postDelayed(this, 50);
        }
    };

    protected Point getPointFromRowAndCol(int col,int row) {
        return new Point(paddingH + (childSize + paddingH) * col,
                    paddingH + (childSize + paddingH) * row - scroll);
    }

    private void refreshAllchild(int l, int t, int r, int b){
        childSize = Math.round(((r - l) / COL_CNT) * childRatio);
        VIEW_WID = r - l;
        paddingH = (VIEW_WID - (childSize * COL_CNT)) / (COL_CNT + 1);
        for (int i = 0; i < getChildCount(); i++) {
            if(dragged==i)continue;
            if(exchanged==i)continue;
            reFreshView(i);
        }
    }

    @Override
    public void onClick(View view) {
        if (clickIndex>=0) {
            String[] items = new String[2];
            items[0] = context.getString(R.string.str_btnname_change);
            items[1] = context.getString(R.string.str_delete);
            new QMUIDialog.MenuDialogBuilder(context)
                    .addItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            switch (which){
                                case 0:
                                    QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(context);
                                    builder.setTitle(context.getString(R.string.str_rename))
                                            .setPlaceholder(context.getString(R.string.str_inputnew))
                                            .setInputType(InputType.TYPE_CLASS_TEXT)
                                            .setDefaultText(getChileByIndex(clickIndex).btnName)
                                            .addAction(UiUtils.getString(R.string.str_cancel), new QMUIDialogAction.ActionListener() {
                                                @Override
                                                public void onClick(QMUIDialog dialog, int index) {
                                                    dialog.dismiss();
                                                }
                                            })
                                            .addAction(UiUtils.getString(R.string.str_Ok), new QMUIDialogAction.ActionListener() {
                                                @Override
                                                public void onClick(QMUIDialog dialog, int index) {
                                                    dialog.dismiss();
                                                    String text = builder.getEditText().getText().toString();
                                                    getChileByIndex(clickIndex).btnName = text;
                                                    getChileByIndex(clickIndex).invalidate();
                                                    doFileChange();
                                                }
                                            })
                                            .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
                                    break;
                                case 1:
                                    new QMUIDialog.MessageDialogBuilder(context)
                                            .setTitle(context.getString(R.string.str_info))
                                            .setMessage(context.getString(R.string.str_suredelete))
                                            .addAction(context.getString(R.string.str_cancel), new QMUIDialogAction.ActionListener() {
                                                @Override
                                                public void onClick(QMUIDialog dialog, int index) {
                                                    dialog.dismiss();
                                                }
                                            })
                                            .addAction(0, context.getString(R.string.str_Ok), QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener() {
                                                @Override
                                                public void onClick(QMUIDialog dialog, int index) {
                                                    dialog.dismiss();
                                                    removeViewAt(clickIndex);
                                                    doFileChange();
                                                }
                                            })
                                            .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
                                    break;
                            }
                        }
                    })
                    .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (clickIndex>=0) {
            dragged = clickIndex;
            animateMoveing(dragged);
            return true;
        }
        return false;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        refreshAllchild(l,t,r,b);
    }

    private void reFreshView(int vidx){
        if(vidx<0)return;
        reFreshChild(getChileByIndex(vidx));
    }

    private void reFreshChild(ViewBtnPlaying v){
        Point p = getPointFromRowAndCol(v.col,v.row);
        v.layout(p.x, p.y, p.x + childSize,p.y + childSize);
    }

    private ViewBtnPlaying getChileByIndex(int idx){
        return (ViewBtnPlaying)getChildAt(idx);
    }

    public void setCOL_CNT(int COL_CNT) {
        this.COL_CNT = COL_CNT;
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

    public boolean isFilechanged() {
        return filechanged;
    }

    public void setFilechanged(boolean filechanged) {
        this.filechanged = filechanged;
    }

    protected void animateMoveing(int idx) {
        ViewBtnPlaying v = getChileByIndex(idx);
        Point pt = getPointFromRowAndCol(v.col,v.row);
        AnimationSet animSet = new AnimationSet(true);
        ScaleAnimation scale = new ScaleAnimation(1f, 1.9f, 1f, 1.9f, Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        scale.setDuration(300);
        AlphaAnimation alpha = new AlphaAnimation(1, .2f);
        alpha.setDuration(300);

        animSet.addAnimation(scale);
        animSet.addAnimation(alpha);
        animSet.setFillEnabled(true);
        animSet.setFillAfter(true);
        v.clearAnimation();
        v.startAnimation(animSet);
    }

    private int getTargetViewByButSelf(int col,int row,int self){
        for (int i = 0; i < getChildCount(); i++) {
            if(i==self)continue;
            ViewBtnPlaying v = getChileByIndex(i);
            if(v.col==col && v.row == row) {
                return i;
            }
        }
        return -1;
    }

    //动画不能清除？
    private void startMoveOld(int idx){
        if(idx<0)return;
        ViewBtnPlaying v = getChileByIndex(idx);
        v.setHadAni();
        Point p = getPointFromRowAndCol(v.col, v.row);
        v.animate()
                .x(p.x)
                .y(p.y)
                .setDuration(300)
                .start();
    }

    private void startMoveNew(int idx,int col,int row){
        if(idx<0)return;
        ViewBtnPlaying v = getChileByIndex(idx);
        v.setHadAni();
        Point p = getPointFromRowAndCol(col, row);
        v.animate()
                .x(p.x)
                .y(p.y)
                .setDuration(300)
//                .setInterpolator(new AccelerateInterpolator())
//                .withLayer() //硬件加速
                .start();
    }

    private void ClearAnition(int idx){
        ViewBtnPlaying v = getChileByIndex(idx);
        ViewBtnPlaying vnew = new ViewBtnPlaying(getContext(),v.col,v.row,v.btnName);
        vnew.idx = v.idx;
        vnew.setmShapeKinds(v.getmShapeKinds());
        removeViewAt(idx);
        addView(vnew);
    }

    private boolean ClearAAnition(){
        for (int i = 0; i < getChildCount(); i++) {
            ViewBtnPlaying v = getChileByIndex(i);
            if(v.isHadAni()){
                ClearAnition(i);
                return true;
            }
        }
        return false;
    }

    private void ClearAllAnition(){
        while(ClearAAnition()){}
    }

    private OnBtnAjust OnAdjustEvent;
    public interface OnBtnAjust{
        void onAdjust();
    }

    public void setOnAdjustEvent(OnBtnAjust onAdjustEvent) {
        OnAdjustEvent = onAdjustEvent;
    }

    private void doFileChange(){
        filechanged = true;
        if(OnAdjustEvent!=null)
            OnAdjustEvent.onAdjust();
        mustrefresh = true;
    }
}
