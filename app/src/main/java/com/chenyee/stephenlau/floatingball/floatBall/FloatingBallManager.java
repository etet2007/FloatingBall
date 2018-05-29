package com.chenyee.stephenlau.floatingball.floatBall;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;


import com.chenyee.stephenlau.floatingball.util.FunctionUtil;
import com.chenyee.stephenlau.floatingball.util.SharedPrefsUtils;

import static com.chenyee.stephenlau.floatingball.util.FunctionUtil.getListener;
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
            //获取size
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

            FunctionUtil.sFloatingBallService=(FloatingBallService) context;
            updateAllBallViewParameter();

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
     * 设置背景图
     * 复制外部路径的图片到目录中去，更新bitmapRead，再进行裁剪
     * @param imagePath 外部图片地址
     */
    public void setBackgroundImage(String imagePath){
        if (mFloatingBallView != null) {

            mFloatingBallView.copyBackgroundImage(imagePath);
            mFloatingBallView.refreshBitmapRead();
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
    public void updateBallViewParameter(String key) {
        if (mFloatingBallView != null) {
            /* View */
            switch (key) {
                case PREF_OPACITY:
                    //Opacity
                    mFloatingBallView.setOpacity(SharedPrefsUtils.getIntegerPreference(PREF_OPACITY, 125));
                    break;
                case PREF_OPACITY_MODE:
                    
                    break;
                case PREF_SIZE:
                    //Size
                    mFloatingBallView.changeFloatBallSizeWithRadius(SharedPrefsUtils.getIntegerPreference(PREF_SIZE, 25));
                    break;
                case PREF_USE_BACKGROUND:
                    //Use background
                    mFloatingBallView.setUseBackground(SharedPrefsUtils.getBooleanPreference(PREF_USE_BACKGROUND, false));
                    break;
                case PREF_USE_GRAY_BACKGROUND:
                    //Use gray background
                    mFloatingBallView.setUseGrayBackground(SharedPrefsUtils.getBooleanPreference(PREF_USE_GRAY_BACKGROUND, true));
                    break;
                case PREF_IS_VIBRATE:
                    mFloatingBallView.setIsVibrate(SharedPrefsUtils.getBooleanPreference(PREF_IS_VIBRATE, true));
                    break;
            }
            //Refresh view
            mFloatingBallView.requestLayout();
            mFloatingBallView.invalidate();

            //功能性
            if (key.equals(PREF_DOUBLE_CLICK_EVENT)) {
                /* Function */
                //Double click event
                int double_click_event = SharedPrefsUtils.getIntegerPreference(PREF_DOUBLE_CLICK_EVENT, NONE);
                boolean useDoubleClick = true;
                if (double_click_event == NONE) useDoubleClick = false;
                mFloatingBallView.setDoubleClickEventType(useDoubleClick, getListener(double_click_event));
            }
            if (key.equals(PREF_LEFT_SLIDE_EVENT)) {
                //LeftSlideEvent
                int leftSlideEvent = SharedPrefsUtils.getIntegerPreference(PREF_LEFT_SLIDE_EVENT, RECENT_APPS);
                mFloatingBallView.setLeftFunctionListener(getListener(leftSlideEvent));
            }
            if (key.equals(PREF_RIGHT_SLIDE_EVENT)) {
                //RightSlideEvent
                int rightSlideEvent = SharedPrefsUtils.getIntegerPreference(PREF_RIGHT_SLIDE_EVENT, RECENT_APPS);
                mFloatingBallView.setRightFunctionListener(getListener(rightSlideEvent));
            }
            if (key.equals(PREF_UP_SLIDE_EVENT)) {
                //UpSlideEvent
                int upSlideEvent = SharedPrefsUtils.getIntegerPreference(PREF_UP_SLIDE_EVENT, HOME);
                mFloatingBallView.setUpFunctionListener(getListener(upSlideEvent));
            }
            if(key.equals(PREF_DOWN_SLIDE_EVENT)){
                //DownSlideEvent
                int downSlideEvent = SharedPrefsUtils.getIntegerPreference(PREF_DOWN_SLIDE_EVENT,NOTIFICATION);
                mFloatingBallView.setDownFunctionListener(getListener(downSlideEvent));
            }
            if(key.equals(PREF_MOVE_UP_DISTANCE)) {
                //要考虑多线程么
                mFloatingBallView.setMoveUpDistance(SharedPrefsUtils.getIntegerPreference(PREF_MOVE_UP_DISTANCE, 200));
            }
        }
    }
    private void updateAllBallViewParameter() {
        if (mFloatingBallView != null) {
            /* View */
            //Opacity
            mFloatingBallView.setOpacity(SharedPrefsUtils.getIntegerPreference(PREF_OPACITY, 125));
            //Size
            mFloatingBallView.changeFloatBallSizeWithRadius(SharedPrefsUtils.getIntegerPreference(PREF_SIZE, 25));
            //Use background
            mFloatingBallView.setUseBackground(SharedPrefsUtils.getBooleanPreference(PREF_USE_BACKGROUND, false));
            //Use gray background
            mFloatingBallView.setUseGrayBackground(SharedPrefsUtils.getBooleanPreference(PREF_USE_GRAY_BACKGROUND, true));
            //Refresh view
            mFloatingBallView.requestLayout();
            mFloatingBallView.invalidate();

            /* Function */
            //Double click event
            int double_click_event = SharedPrefsUtils.getIntegerPreference(PREF_DOUBLE_CLICK_EVENT, NONE);
            boolean useDoubleClick = true;
            if (double_click_event == NONE) useDoubleClick = false;
            mFloatingBallView.setDoubleClickEventType(useDoubleClick, getListener(double_click_event));
            //LeftSlideEvent
            int leftSlideEvent = SharedPrefsUtils.getIntegerPreference(PREF_LEFT_SLIDE_EVENT, RECENT_APPS);
            mFloatingBallView.setLeftFunctionListener(getListener(leftSlideEvent));
            //RightSlideEvent
            int rightSlideEvent = SharedPrefsUtils.getIntegerPreference(PREF_RIGHT_SLIDE_EVENT, RECENT_APPS);
            mFloatingBallView.setRightFunctionListener(getListener(rightSlideEvent));
            //UpSlideEvent
            int upSlideEvent = SharedPrefsUtils.getIntegerPreference(PREF_UP_SLIDE_EVENT, HOME);
            mFloatingBallView.setUpFunctionListener(getListener(upSlideEvent));
            //DownSlideEvent
            int downSlideEvent = SharedPrefsUtils.getIntegerPreference(PREF_DOWN_SLIDE_EVENT, NOTIFICATION);
            mFloatingBallView.setDownFunctionListener(getListener(downSlideEvent));
            //要考虑多线程么
            mFloatingBallView.setMoveUpDistance(SharedPrefsUtils.getIntegerPreference(PREF_MOVE_UP_DISTANCE, 200));
        }
    }

    public void moveBallViewUp() {
        if(mFloatingBallView !=null) mFloatingBallView.performMoveUpAnimator();
    }

    public void moveBallViewDown() {
        if(mFloatingBallView !=null) mFloatingBallView.performMoveDownAnimator();
    }

    public void clear() {
        if(mFloatingBallView !=null) mFloatingBallView.recycleBitmap();
    }
}

