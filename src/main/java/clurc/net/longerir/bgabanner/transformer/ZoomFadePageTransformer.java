package clurc.net.longerir.bgabanner.transformer;

import android.view.View;

import androidx.core.view.ViewCompat;

/**
 * 描述:
 */
public class ZoomFadePageTransformer extends BGAPageTransformer {

    @Override
    public void handleInvisiblePage(View view, float position) {
    }

    @Override
    public void handleLeftPage(View view, float position) {
        ViewCompat.setTranslationX(view, -view.getWidth() * position);

        ViewCompat.setPivotX(view, view.getWidth() * 0.5f);
        ViewCompat.setPivotY(view, view.getHeight() * 0.5f);
        ViewCompat.setScaleX(view, 1 + position);
        ViewCompat.setScaleY(view, 1 + position);

        ViewCompat.setAlpha(view, 1 + position);
    }

    @Override
    public void handleRightPage(View view, float position) {
        ViewCompat.setTranslationX(view, -view.getWidth() * position);

        ViewCompat.setPivotX(view, view.getWidth() * 0.5f);
        ViewCompat.setPivotY(view, view.getHeight() * 0.5f);
        ViewCompat.setScaleX(view, 1 - position);
        ViewCompat.setScaleY(view, 1 - position);
        ViewCompat.setAlpha(view, 1 - position);
    }

}