package clurc.net.longerir.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import clurc.net.longerir.R;
import clurc.net.longerir.Utils.GlideRoundTransform;
import clurc.net.longerir.manager.UiUtils;

public class GlideImageUtils {
    private static final String TAG = "MainActivity";
    /**
     * 加载普通图片
     * @param context
     * @param url
     * @param imageView
     */
    public static void display(Context context, String url, ImageView imageView) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.mipmap.loading)
                .error(R.drawable.ic_default_image);
        Glide.with(context)
                .load(url)
                .apply(options)
//                .listener(mRequestListener)
                .into(imageView);
    }

//    static  RequestListener mRequestListener = new RequestListener() {
//        @Override
//        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
//            Log.d(TAG, "onException: " + e.toString()+"  model:"+model+" isFirstResource: "+isFirstResource);
//            return false;
//        }
//
//        @Override
//        public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
//            Log.e(TAG,  "model:"+model+" isFirstResource: "+isFirstResource);
//            return false;
//        }
//    };

    /**
     * 加载普通图片 , 有过渡动画
     * @param context
     * @param url
     * @param imageView
     */
    public static void displayTransition(Context context, String url, ImageView imageView) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.mipmap.loading)
                .error(R.drawable.ic_default_image);
        Glide.with(context)
                .load(url)
                .apply(options)
                .transition(new DrawableTransitionOptions().crossFade())
                .into(imageView);
    }

    public static void Display(Context context, String url, ImageView imageView,int failImgRes,boolean isCenterCrop) {
        RequestOptions options = new RequestOptions()
                .error(failImgRes);
        if (isCenterCrop){
            options.centerCrop();
        }
        Glide.with(context)
                .load(url)
                .apply(options)
                .transition(new DrawableTransitionOptions().crossFade())
                .into(imageView);
    }

    public static void DisplayHomeGridViewItem(Context context, String url, ImageView imageView) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .error(R.drawable.ic_default_image)
                .transform(new GlideRoundTransform(context, 0, context.getResources().getColor(R.color.qmui_config_color_white))) //color_e0e0e0
                ;
        Glide.with(context).load(url)
                .apply(options)
                .transition(new DrawableTransitionOptions().crossFade())
                .into(imageView);
    }

    /**
     * 加载普通图片
     *
     * @param context
     * @param url
     * @param imageView
     */
    public static void DisplayNoPlaceholder(Context context, String url, ImageView imageView) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.mipmap.loading)
                .error(R.color.colorTransparent)
                ;

        Glide.with(context)
                .load(url)
                .apply(options)
                .transition(new DrawableTransitionOptions().crossFade())
                .into(imageView);
    }

    public static void displayTransitionNoPlace(Context context, String url, ImageView imageView) {
        RequestOptions options = new RequestOptions()
                .error(R.color.qmui_config_color_25_white)
                ;

        Glide.with(context)
                .load(url)
//                .placeholder(R.color.colorWhite)
//                .error(R.color.colorWhite)
//                .centerCrop()
                .apply(options)
                .transition(new DrawableTransitionOptions().crossFade())
                .into(imageView);
    }

    public static void DisplayNoPlaceholderErrorf0f0f0(Context context, String url, ImageView imageView) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.color.color_f0f0f0)
                .error(R.color.color_f0f0f0)
//                .priority(Priority.HIGH)
//                .fitCenter()
                //                .diskCacheStrategy(DiskCacheStrategy.NONE)
                ;

        Glide.with(context)
                .load(url)
//                .placeholder(R.color.color_f0f0f0)
//                .error(R.color.color_f0f0f0)
//                .centerCrop()
//                .crossFade()
                .apply(options)
                .transition(new DrawableTransitionOptions().crossFade())
                .into(imageView);
    }

    /**
     * 加载图片，并切成圆形
     *
     * @param context
     * @param url
     * @param imageView
     */
    public static void DisplayCircle(final Context context, String url, final ImageView imageView) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_default_image)
                .error(R.drawable.ic_default_image)
                ;
        Glide.with(context)
                .asBitmap()
                .load(url)
                .apply(options)
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        imageView.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }

    /**
     * 加载圆角图片
     *
     * @param context
     * @param url
     * @param imageView
     */
    public static void DisplayRoundCorner(final Context context, String url, final ImageView imageView,
                                          final int dpCorner) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.mipmap.loading)
                .error(R.mipmap.zanwutupian)
                ;
        Glide.with(context)
                .asBitmap()
                .load(url)
                .apply(options)
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        circularBitmapDrawable.setCornerRadius(UiUtils.dp2px(dpCorner));
                        circularBitmapDrawable.setAntiAlias(true);
                        imageView.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }

    public static void displayGif(Context context, @DrawableRes int resId, final ImageView imageView) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.mipmap.loading)
                .error(R.drawable.ic_default_image)
                ;
        Glide.with(context)
                .asGif()
                .apply(options)
                .load(resId)
                .into(new SimpleTarget<GifDrawable>() {
                    @Override
                    public void onResourceReady(@NonNull GifDrawable resource, @Nullable Transition<? super GifDrawable> transition) {
                        if (resource instanceof GifDrawable) {
                            GifDrawable gifDrawable = (GifDrawable) resource;
                            gifDrawable.setLoopCount(GifDrawable.LOOP_FOREVER);
                            imageView.setImageDrawable(resource);
                            gifDrawable.start();
                        }

                    }

                });
    }
}
