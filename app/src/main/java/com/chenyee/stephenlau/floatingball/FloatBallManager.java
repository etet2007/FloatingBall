package com.chenyee.stephenlau.floatingball;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;


import static com.chenyee.stephenlau.floatingball.SharedPreferencesUtil.*;


/**
 * 单例FloatBallManager
 */

public class FloatBallManager {
    private static FloatBallManager mFloatBallManager=new FloatBallManager();
    private FloatBallManager(){}
    public static FloatBallManager getInstance() {
        return mFloatBallManager;
    }

    //View
    private BallView mBallView;
    //WindowManager
    private WindowManager mWindowManager;

    private SharedPreferences defaultSharedPreferences;
    private boolean isOpenBall;
    private boolean useBackground;

    //创建BallView
    public void addBallView(Context context) {
        if (mBallView == null) {
            mBallView = new BallView(context);

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                params.type = LayoutParams.TYPE_APPLICATION_OVERLAY; //Android 8.0
            }else {
                params.type = LayoutParams.TYPE_SYSTEM_ALERT;
            }
            params.format = PixelFormat.RGBA_8888;
            params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | LayoutParams.FLAG_NOT_FOCUSABLE|LayoutParams.FLAG_LAYOUT_IN_SCREEN|LayoutParams.FLAG_LAYOUT_INSET_DECOR; //FLAG_LAYOUT_IN_SCREEN


            //把引用传进去
            mBallView.setLayoutParams(params);

            windowManager.addView(mBallView, params);

            updateData();

//            mBallView.setOpacity(defaultSharedPreferences.getInt("opacity",125));
//            mBallView.changeFloatBallSizeWithRadius(defaultSharedPreferences.getInt("size",25));

//            mBallView.makeBitmapRead();

//            mBallView.createBitmapCropFromBitmapRead();

            //Call this when something has changed which has invalidated the layout of this view.
//            mBallView.requestLayout();

//            useBackground = defaultSharedPreferences.getBoolean("useBackground", false);
//            setUseBackground(useBackground);

            mBallView.refreshAddAnimator();

            isOpenBall=true;
            saveFloatBallData();
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


    public void setOpacity(int opacity) {
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
        editor.putBoolean(KEY_HAS_Added_Ball,isOpenBall);

        LayoutParams params = mBallView.getLayoutParams();
        editor.putInt(KEY_PARAM_X,params.x);
        editor.putInt(KEY_PARAM_Y,params.y);

        editor.apply();
    }

    public void setUseBackground(boolean useBackground) {
        if (mBallView != null) {
            this.useBackground=useBackground;
            mBallView.useBackground = useBackground;
            mBallView.invalidate();
        }
    }
// 根据SharedPreferences中的数据更新BallView的显示参数
    public void updateData() {
        if (mBallView != null) {
            //Opacity
            mBallView.setOpacity(defaultSharedPreferences.getInt(SharedPreferencesUtil.KEY_OPACITY,125));
            //Size
            mBallView.changeFloatBallSizeWithRadius(defaultSharedPreferences.getInt(KEY_SIZE,25));

            mBallView.createBitmapCropFromBitmapRead();

            //Use background
            mBallView.useBackground =defaultSharedPreferences.getBoolean(KEY_USE_BACKGROUND ,false);

            mBallView.requestLayout();

            mBallView.invalidate();
        }
    }
}

