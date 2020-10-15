package clurc.net.longerir.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import clurc.net.longerir.R;

public class SegmentedRadioGroup extends RadioGroup {

    private int curridx = 0;

    public SegmentedRadioGroup(Context context) {
        super(context);
    }

    public SegmentedRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // ① 定义点击回调接口
    public interface OnItemChangedListener {
        void onItemChanged(int position);
    }
    private OnItemChangedListener onitemChangedListener;

    public void setOnItemChgeListener(OnItemChangedListener listener) {
        this.onitemChangedListener = listener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                changeButtonsImages();
            }
        });
        changeButtonsImages();
    }

    public void changeButtonsImages() {
        int count = super.getChildCount();
        if (count > 1) {
            int m = -1;
            for (int i = 0; i < count; i++) {
                RadioButton abtn = (RadioButton) super.getChildAt(i);
                if (abtn.isChecked()) {
                    abtn.setBackgroundColor(0xFF2BBDF3);
                    abtn.setTextColor(Color.WHITE);
                    m = i;
                } else {
                    abtn.setBackgroundResource(R.drawable.bianjie);//.setBackgroundColor(Color.WHITE);
                    abtn.setTextColor(Color.BLACK);
                }
            }
            if (m != curridx) {
                curridx = m;
                onitemChangedListener.onItemChanged(curridx);
            }
//			super.getChildAt(0).setBackgroundResource(R.drawable.segment_radio_left);
//			for(int i=1; i < count-1; i++){
//				super.getChildAt(i).setBackgroundResource(R.drawable.segment_radio_middle);
//			}
//			super.getChildAt(count-1).setBackgroundColor(0xFF2BBDF3);// .setBackgroundResource(R.drawable.segment_radio_right);
        }
    }

    public void setPosition(int pos) {
        for (int i = 0; i < super.getChildCount(); i++) {
            RadioButton abtn = (RadioButton) super.getChildAt(i);
            if(i==pos)
                abtn.setChecked(true);
            else
                abtn.setChecked(false);
            if (abtn.isChecked()) {
                abtn.setBackgroundColor(0xFF2BBDF3);
                abtn.setTextColor(Color.WHITE);
            } else {
                abtn.setBackgroundResource(R.drawable.bianjie);//.setBackgroundColor(Color.WHITE);
                abtn.setTextColor(Color.BLACK);
            }
        }
    }
}
