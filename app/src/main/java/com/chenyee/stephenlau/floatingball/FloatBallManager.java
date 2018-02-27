package com.chenyee.stephenlau.floatingball;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;


import com.chenyee.stephenlau.floatingball.activities.MainActivity;
import com.chenyee.stephenlau.floatingball.util.SharedPreferencesUtil;
import com.chenyee.stephenlau.floatingball.views.FloatingBallView;

import static com.chenyee.stephenlau.floatingball.util.SharedPreferencesUtil.*;


/**
 * 管理FloatBall的类。
 * 单例
 */
public class FloatBallManager {
    private static final String TAG =FloatBallManager.class.getSimpleName();

    //单例
    private static FloatBallManager mFloatBallManager=new FloatBallManager();
    private FloatBallManager(){}
    public static FloatBallManager getInstance() {
        return mFloatBallManager;
    }

    // FloatingBallView
    private FloatingBallView mFloatingBallView;
    // WindowManager
    private WindowManager mWindowManager;

    private SharedPreferences defaultSharedPreferences;
    private boolean isOpenedBall;

    private int moveUpDistance=130;

    // 创建BallView
    public void addBallView(Context context) {
        if (mFloatingBallView == null) {
            mFloatingBallView = new FloatingBallView(context);

            WindowManager windowManager = getWindowManager(context);

            Point size = new Point();
            windowManager.getDefaultDisplay().getSize(size);
            int screenWidth = size.x;
            int screenHeight = size.y;

            //Use ShardPreferences to init layout parameters.
            defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            LayoutParams params = new LayoutParams();
            params.x=defaultSharedPreferences.getInt("paramsX",screenWidth / 2);
            params.y=defaultSharedPreferences.getInt("paramsY",screenHeight / 2);
            params.width = LayoutParams.WRAP_CONTENT;
            params.height = LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.START | Gravity.TOP;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                params.type = LayoutParams.TYPE_APPLICATION_OVERLAY; //适配Android 8.0
            }else {
                params.type = LayoutParams.TYPE_SYSTEM_ALERT;
            }
            params.format = PixelFormat.RGBA_8888;
            params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | LayoutParams.FLAG_NOT_FOCUSABLE|LayoutParams.FLAG_LAYOUT_IN_SCREEN|LayoutParams.FLAG_LAYOUT_INSET_DECOR; //FLAG_LAYOUT_IN_SCREEN

            //把引用传进去
            mFloatingBallView.setLayoutParams(params);
            //使用windowManager把ballView加进去
            windowManager.addView(mFloatingBallView, params);

            updateBallViewParameter();
//            mBallView.performAddAnimator();

            isOpenedBall =true;
            saveFloatBallData();
        }
    }

    public void removeBallView() {
        if (mFloatingBallView == null)
            return;

        isOpenedBall =false;

        mFloatingBallView.performRemoveAnimator();

        saveFloatBallData();
        mFloatingBallView = null;
    }

    private WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }


//    public void setOpacity(int opacity) {
//        if (mBallView != null) {
//            mBallView.setOpacity(opacity);
//            mBallView.invalidate();
//        }
//    }

//    public  void setSize(int size) {
//        if (mBallView != null) {
//            mBallView.changeFloatBallSizeWithRadius(size);
//
//            mBallView.createBitmapCropFromBitmapRead();
//            mBallView.requestLayout();
//
//            mBallView.invalidate();
//        }
//    }

    /**
     *
     * @param imagePath 外部图片地址
     */
    public void setBackgroundPic(String imagePath){
        if (mFloatingBallView != null) {

            mFloatingBallView.copyBackgroundImage(imagePath);
            mFloatingBallView.getBitmapRead();
            mFloatingBallView.createBitmapCropFromBitmapRead();
            mFloatingBallView.invalidate();
        }
    }

    public void saveFloatBallData(){
        if(defaultSharedPreferences==null || mFloatingBallView ==null){
            return;
        }

        SharedPreferences.Editor editor = defaultSharedPreferences.edit();
        editor.putBoolean(PREF_HAS_ADDED_BALL, isOpenedBall);

        LayoutParams params = mFloatingBallView.getLayoutParams();
        editor.putInt(PREF_PARAM_X,params.x);
        editor.putInt(PREF_PARAM_Y,params.y);

        editor.apply();
    }

    public void setUseBackground(boolean useBackground) {
        if (mFloatingBallView != null) {
            mFloatingBallView.useBackground = useBackground;
            mFloatingBallView.invalidate();
        }
    }
    // 根据SharedPreferences中的数据更新BallView的显示参数
    public void updateBallViewParameter() {
        if (mFloatingBallView != null) {
            //Opacity
            mFloatingBallView.setOpacity(defaultSharedPreferences.getInt(SharedPreferencesUtil.PREF_OPACITY,125));
            //Size
            mFloatingBallView.changeFloatBallSizeWithRadius(defaultSharedPreferences.getInt(PREF_SIZE,25));

            mFloatingBallView.createBitmapCropFromBitmapRead();

            //Use gray background
            mFloatingBallView.useGrayBackground = defaultSharedPreferences.getBoolean(PREF_USE_GRAY_BACKGROUND, true);
            //Use background
            mFloatingBallView.useBackground =defaultSharedPreferences.getBoolean(PREF_USE_BACKGROUND,false);

            //Double click event
            int doubleClickEvent =defaultSharedPreferences.getInt(PREF_DOUBLE_CLICK_EVENT,0);
            mFloatingBallView.doubleClickEvent=doubleClickEvent;
            if(doubleClickEvent!=NONE)
                mFloatingBallView.setUseDoubleTapOrNot(true);
            else
                mFloatingBallView.setUseDoubleTapOrNot(false);

            moveUpDistance = defaultSharedPreferences.getInt(SharedPreferencesUtil.PREF_MOVE_UP_DISTANCE, 130);

            mFloatingBallView.requestLayout();
            mFloatingBallView.invalidate();
        }
    }


    public void moveBallViewUp() {
        if(mFloatingBallView !=null){
            mFloatingBallView.performUpAnimator(moveUpDistance);
        }
    }

    public void moveBallViewDown() {
        if(mFloatingBallView !=null){
            mFloatingBallView.performDownAnimator(moveUpDistance);
        }
    }
}

