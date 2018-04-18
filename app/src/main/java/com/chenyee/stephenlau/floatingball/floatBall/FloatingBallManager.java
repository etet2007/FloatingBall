package com.chenyee.stephenlau.floatingball.floatBall;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;


import com.chenyee.stephenlau.floatingball.util.FunctionUtil;
import com.chenyee.stephenlau.floatingball.util.SharedPrefsUtils;
import com.chenyee.stephenlau.floatingball.util.StaticStringUtil;

import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.*;

/**
 * 管理FloatingBall的类。
 * 单例，因为只需要一个FloatingBallView
 */
public class FloatingBallManager {
    private static final String TAG =FloatingBallManager.class.getSimpleName();

    private static FloatingBallManager sFloatingBallManager =new FloatingBallManager();
    private FloatingBallManager(){}
    public static FloatingBallManager getInstance() {
        return sFloatingBallManager;
    }

    // FloatingBallView
    private FloatingBallView mFloatingBallView;
    private FunctionUtil mFunctionUtil;

//    private SharedPreferences defaultSharedPreferences;
    private boolean isOpenedBall;

    public boolean isOpenedBall() {
        return isOpenedBall;
    }
    public void setOpenedBall(boolean openedBall) {
        isOpenedBall = openedBall;
    }

    // 创建BallView
    public void addBallView(Context context) {
        Log.d(TAG, "FloatingBallManager addBallView: ");
        if (mFloatingBallView == null) {
            mFloatingBallView = new FloatingBallView(context);

            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Point size = new Point();
            if (windowManager == null) return;

            windowManager.getDefaultDisplay().getSize(size);
            int screenWidth = size.x;
            int screenHeight = size.y;

            LayoutParams params = new LayoutParams();
            //位置
            params.x=SharedPrefsUtils.getIntegerPreference(PREF_PARAM_X,screenWidth / 2);
            params.y= SharedPrefsUtils.getIntegerPreference(PREF_PARAM_Y,screenHeight / 2);
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
                    |LayoutParams.FLAG_NOT_FOCUSABLE
                    |LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    |LayoutParams.FLAG_LAYOUT_INSET_DECOR;

            //把引用传进去
            mFloatingBallView.setLayoutParams(params);
            //使用windowManager把ballView加进去
            windowManager.addView(mFloatingBallView, params);

            mFunctionUtil=new FunctionUtil((FloatingBallService) context);
            updateBallViewParameter();

            isOpenedBall = true;
        }
    }

    public void removeBallView() {
        if (mFloatingBallView == null) return;
        //动画
        mFloatingBallView.performRemoveAnimator();
        mFloatingBallView = null;
        isOpenedBall = false;
    }

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

    /**
     * 保存打开状态
     */
    public void saveFloatingBallState(){
        SharedPrefsUtils.setBooleanPreference(PREF_HAS_ADDED_BALL, isOpenedBall);
    }

    /**
     *  根据SharedPreferences中的数据更新BallView的参数。
     */
    public void updateBallViewParameter() {
        if (mFloatingBallView != null) {
            //Opacity
            mFloatingBallView.setOpacity(SharedPrefsUtils.getIntegerPreference(PREF_OPACITY,125));
            //Size
            mFloatingBallView.changeFloatBallSizeWithRadius(SharedPrefsUtils.getIntegerPreference(PREF_SIZE,25));

            mFloatingBallView.createBitmapCropFromBitmapRead();

            //Use gray background
            mFloatingBallView.setUseGrayBackground(SharedPrefsUtils.getBooleanPreference(PREF_USE_GRAY_BACKGROUND, true));
            //Use background
            mFloatingBallView.setUseBackground(SharedPrefsUtils.getBooleanPreference(PREF_USE_BACKGROUND,false));

            //Double click event
            int double_click_event= SharedPrefsUtils.getIntegerPreference(PREF_DOUBLE_CLICK_EVENT,NONE);
            boolean useDoubleClick=true;
            if(double_click_event==NONE) useDoubleClick=false;
            mFloatingBallView.setDoubleClickEventType(useDoubleClick,getListener(double_click_event));

            //LeftSlideEvent
            int leftSlideEvent =SharedPrefsUtils.getIntegerPreference(PREF_LEFT_SLIDE_EVENT,RECENT_APPS);
            mFloatingBallView.setLeftFunctionListener(getListener(leftSlideEvent));
            //RightSlideEvent
            int rightSlideEvent =SharedPrefsUtils.getIntegerPreference(PREF_RIGHT_SLIDE_EVENT,RECENT_APPS);
            mFloatingBallView.setRightFunctionListener(getListener(rightSlideEvent));
            //UpSlideEvent
            int upSlideEvent =SharedPrefsUtils.getIntegerPreference(PREF_UP_SLIDE_EVENT,HOME);
            mFloatingBallView.setUpFunctionListener(getListener(upSlideEvent));
            //DownSlideEvent
            int downSlideEvent =SharedPrefsUtils.getIntegerPreference(PREF_DOWN_SLIDE_EVENT,NOTIFICATION);
            mFloatingBallView.setDownFunctionListener(getListener(downSlideEvent));

            mFloatingBallView.setMoveUpDistance(SharedPrefsUtils.getIntegerPreference(StaticStringUtil.PREF_MOVE_UP_DISTANCE, 200));

            mFloatingBallView.requestLayout();
            mFloatingBallView.invalidate();
        }
    }

    private FunctionListener getListener(int key) {
        FunctionListener functionListener = mFunctionUtil.nullFunctionListener;
        if(key==RECENT_APPS){
            functionListener = mFunctionUtil.recentAppsFunctionListener;
        }else if(key==LAST_APPS){
            functionListener = mFunctionUtil.lastAppFunctionListener;
        } else if (key == HIDE) {
            functionListener =  mFunctionUtil.hideFunctionListener;
        } else if (key == NONE) {
            functionListener =  mFunctionUtil.nullFunctionListener;
        } else if(key==HOME){
            functionListener =  mFunctionUtil.homeFunctionListener;
        }else if(key ==LOCK_SCREEN){
            functionListener =  mFunctionUtil.deviceLockFunctionListener;
        } else if (key == ROOT_LOCK_SCREEN) {
            functionListener =  mFunctionUtil.rootLockFunctionListener;
        } else if (key == NOTIFICATION) {
            functionListener =  mFunctionUtil.notificationFunctionListener;
        }
        return functionListener;
    }

    public void moveBallViewUp() {
        if(mFloatingBallView !=null) mFloatingBallView.performMoveUpAnimator();
    }

    public void moveBallViewDown() {
        if(mFloatingBallView !=null) mFloatingBallView.performMoveDownAnimator();
    }

}

