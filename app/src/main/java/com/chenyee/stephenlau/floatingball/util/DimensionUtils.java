package com.chenyee.stephenlau.floatingball.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.chenyee.stephenlau.floatingball.App;

public class DimensionUtils {
    public static int gScreenWidth;
    public static int gScreenHeight;
    public static int gScreenDensity;

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static void setupScreenSize() {
        WindowManager windowManager = (WindowManager) App.getApplication().getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            final DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);

            gScreenDensity = metrics.densityDpi;
            gScreenWidth = metrics.widthPixels;
            gScreenHeight = metrics.heightPixels;
        }
    }
}
