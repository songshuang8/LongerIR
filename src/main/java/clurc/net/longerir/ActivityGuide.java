package clurc.net.longerir;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import clurc.net.longerir.bgabanner.BGABanner;
import clurc.net.longerir.bgabanner.BGALocalImageSize;

public class ActivityGuide extends Activity {
    private static final String TAG = ActivityGuide.class.getSimpleName();
    //private BGABanner mBackgroundBanner;
    private BGABanner mForegroundBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        //mBackgroundBanner = findViewById(R.id.banner_guide_background);
        mForegroundBanner = findViewById(R.id.banner_guide_foreground);
        /**
         * 设置进入按钮和跳过按钮控件资源 id 及其点击事件
         * 如果进入按钮和跳过按钮有一个不存在的话就传 0
         * 在 BGABanner 里已经帮开发者处理了防止重复点击事件
         * 在 BGABanner 里已经帮开发者处理了「跳过按钮」和「进入按钮」的显示与隐藏
         */
        mForegroundBanner.setEnterSkipViewIdAndDelegate(R.id.btn_guide_enter, R.id.tv_guide_skip, new BGABanner.GuideDelegate() {
            @Override
            public void onClickEnterOrSkip() {
                startActivity(new Intent(ActivityGuide.this, MainActivity.class));
                finish();
            }
        });
        // Bitmap 的宽高在 maxWidth maxHeight 和 minWidth minHeight 之间
        BGALocalImageSize localImageSize = new BGALocalImageSize(720, 1280, 320, 640);
        // 设置数据源
//       mBackgroundBanner.setData(localImageSize, ImageView.ScaleType.CENTER_CROP,
//                R.mipmap.start1,
//                R.mipmap.start2,
//                R.mipmap.start3,
//                R.mipmap.start4);

        mForegroundBanner.setData(localImageSize, ImageView.ScaleType.FIT_XY,
                R.mipmap.start1,
                R.mipmap.start2,
                R.mipmap.start3,
                R.mipmap.start4);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 如果开发者的引导页主题是透明的，需要在界面可见时给背景 Banner 设置一个白色背景，避免滑动过程中两个 Banner 都设置透明度后能看到 Launcher
        //mBackgroundBanner.setBackgroundResource(android.R.color.white);
    }
}
