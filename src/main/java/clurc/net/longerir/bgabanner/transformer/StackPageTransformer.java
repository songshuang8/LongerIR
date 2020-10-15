package clurc.net.longerir.bgabanner.transformer;

import android.view.View;

import androidx.core.view.ViewCompat;

/**
 * 描述:
 */
public class StackPageTransformer extends BGAPageTransformer {

    @Override
    public void handleInvisiblePage(View view, float position) {
    }

    @Override
    public void handleLeftPage(View view, float position) {
    }

    @Override
    public void handleRightPage(View view, float position) {
        ViewCompat.setTranslationX(view, -view.getWidth() * position);
    }

}