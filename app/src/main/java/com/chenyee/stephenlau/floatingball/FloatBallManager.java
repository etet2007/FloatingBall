package com.wangxiandeng.floatball;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

/**
 * 改成了单例
 * Created by wangxiandeng on 2016/11/25.
 */

public class FloatBallManager {
    private static FloatBallManager mFloatBallManager=new FloatBallManager();

    private FloatBallManager(){}

    public static FloatBallManager getInstance() {
        return mFloatBallManager;
    }
    //View
    private MyFloatBallView mBallView;
    //WindowManager
    private WindowManager mWindowManager;
    private SharedPreferences defaultSharedPreferences;
    private boolean isOpenBall;
    private boolean useBackground;
    public void addBallView(Context context) {
        if (mBallView == null) {
            mBallView = new MyFloatBallView(context);

            WindowManager windowManager = getWindowManager(context);

            Point size = new Point();
            windowManager.getDefaultDisplay().getSize(size);
            int screenWidth = size.x;
            int screenHeight = size.y;

            defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            //初始化布局参数
            LayoutParams params = new LayoutParams();
            params.x=defaultSharedPreferences.getInt("paramsX",screenWidth / 2);
            params.y=defaultSharedPreferences.getInt("paramsY",screenHeight / 2);
            params.width = LayoutParams.WRAP_CONTENT;
            params.height = LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.START | Gravity.TOP;
            params.type = LayoutParams.TYPE_PHONE;
            params.format = PixelFormat.RGBA_8888;
            params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | LayoutParams.FLAG_NOT_FOCUSABLE|LayoutParams.FLAG_LAYOUT_IN_SCREEN|LayoutParams.FLAG_LAYOUT_INSET_DECOR; //FLAG_LAYOUT_IN_SCREEN
            mBallView.setLayoutParams(params);

            windowManager.addView(mBallView, params);

            mBallView.setOpacity(defaultSharedPreferences.getInt("opacity",125));

            mBallView.changeFloatBallSizeWithRadius(defaultSharedPreferences.getInt("size",25));

            mBallView.makeBitmapRead();

            mBallView.createBitmapCropFromBitmapRead();
            //Call this when something has changed which has invalidated the layout of this view.
            mBallView.requestLayout();

            useBackground = defaultSharedPreferences.getBoolean("useBackground", false);
            setUseBackground(useBackground);
            //外部目录 好处是用户可见。
//            String path = Environment.getExternalStorageDirectory().toString();

            mBallView.refreshAddAnimator();


            isOpenBall=true;
        }
    }

    public void removeBallView(final Context context) {
        if (mBallView != null) {
            mBallView.refreshRemoveAnimator();
            isOpenBall=false;

            saveFloatBallData();
            mBallView = null;
        }
    }

    private WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }
    public  void setOpacity(int opacity) {
        if (mBallView != null) {
            mBallView.setOpacity(opacity);
            mBallView.invalidate();
        }
    }
    public  void setSize(int size) {
        if (mBallView != null) {
            mBallView.changeFloatBallSizeWithRadius(size);

            mBallView.createBitmapCropFromBitmapRead();
            mBallView.requestLayout();

            mBallView.invalidate();
        }

    }
    public  void setBackgroundPic(Context context,String imagePath){
        if (mBallView != null) {

            mBallView.setBitmapRead(imagePath);
            mBallView.makeBitmapRead();
            mBallView.createBitmapCropFromBitmapRead();
            mBallView.invalidate();
        }
    }
    public void saveFloatBallData(){
        if(defaultSharedPreferences==null || mBallView==null){
            return;
        }

        SharedPreferences.Editor editor = defaultSharedPreferences.edit();

        editor.putBoolean("isOpenBall",isOpenBall);

        LayoutParams params = mBallView.getLayoutParams();
        editor.putInt("paramsX",params.x);
        editor.putInt("paramsY",params.y);

        editor.putInt("opacity",mBallView.getOpacity());
        editor.putInt("size", (int) mBallView.getBallRadius());
        editor.putBoolean("useBackground", useBackground);
        editor.apply();
    }

    public void setUseBackground(boolean useBackground) {
        if (mBallView != null) {
            this.useBackground=useBackground;
            mBallView.useBackground = useBackground;
            mBallView.invalidate();

        }
    }
}

