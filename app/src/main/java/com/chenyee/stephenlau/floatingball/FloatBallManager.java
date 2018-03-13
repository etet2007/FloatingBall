package com.chenyee.stephenlau.floatingball;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;


import com.chenyee.stephenlau.floatingball.services.FloatingBallService;
import com.chenyee.stephenlau.floatingball.util.AccessibilityUtil;
import com.chenyee.stephenlau.floatingball.util.FunctionUtil;
import com.chenyee.stephenlau.floatingball.util.StaticStringUtil;
import com.chenyee.stephenlau.floatingball.views.FloatingBallView;

import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.*;


/**
 * 管理FloatingBall的类。
 * 单例，因为只需要一个FloatingBallView
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
    private FunctionUtil mFunctionUtil;

    // WindowManager
//    private WindowManager mWindowManager;

    private SharedPreferences defaultSharedPreferences;
    private boolean isOpenedBall;

    // 创建BallView
    public void addBallView(Context context) {
        if (mFloatingBallView == null) {
            mFloatingBallView = new FloatingBallView(context);

            FloatingBallService floatingBallService = (FloatingBallService) context;

            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

            Point size = new Point();
            windowManager.getDefaultDisplay().getSize(size);
            int screenWidth = size.x;
            int screenHeight = size.y;

            //Use ShardPreferences to init layout parameters.
            defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            LayoutParams params = new LayoutParams();
            //位置
            params.x=defaultSharedPreferences.getInt(PREF_PARAM_X,screenWidth / 2);
            params.y=defaultSharedPreferences.getInt(PREF_PARAM_Y,screenHeight / 2);
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
                    |LayoutParams.FLAG_LAYOUT_INSET_DECOR; //FLAG_LAYOUT_IN_SCREEN

            //把引用传进去
            mFloatingBallView.setLayoutParams(params);
            //使用windowManager把ballView加进去
            windowManager.addView(mFloatingBallView, params);

            mFunctionUtil=new FunctionUtil(floatingBallService);
            mFloatingBallView.setUpFunctionListener(mFunctionUtil.homeFunctionListener);
            mFloatingBallView.setLeftFunctionListener(mFunctionUtil.recentAppsFunctionListener);
            mFloatingBallView.setRightFunctionListener(mFunctionUtil.recentAppsFunctionListener);
            mFloatingBallView.setDownFunctionListener(mFunctionUtil.notificationFunctionListener);


            updateBallViewParameter();

            isOpenedBall =true;
            saveFloatBallData();
        }
    }

    public void removeBallView() {
        if (mFloatingBallView == null)
            return;
        //改变状态
        isOpenedBall =false;
        //动画
        mFloatingBallView.performRemoveAnimator();

        saveFloatBallData();
        mFloatingBallView = null;
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
     * 保存打开状态，位置。
     */
    public void saveFloatBallData(){
        if(defaultSharedPreferences==null || mFloatingBallView ==null) return;

        SharedPreferences.Editor editor = defaultSharedPreferences.edit();
        editor.putBoolean(PREF_HAS_ADDED_BALL, isOpenedBall);

        LayoutParams params = mFloatingBallView.getLayoutParams();
        editor.putInt(PREF_PARAM_X,params.x);
        editor.putInt(PREF_PARAM_Y,params.y);

        editor.apply();
    }

    /**
     *  根据SharedPreferences中的数据更新BallView的参数。
     */
    public void updateBallViewParameter() {
        if (mFloatingBallView != null) {
            //Opacity
            mFloatingBallView.setOpacity(defaultSharedPreferences.getInt(StaticStringUtil.PREF_OPACITY,125));
            //Size
            mFloatingBallView.changeFloatBallSizeWithRadius(defaultSharedPreferences.getInt(PREF_SIZE,25));

            mFloatingBallView.createBitmapCropFromBitmapRead();

            //Use gray background
            mFloatingBallView.setUseGrayBackground(defaultSharedPreferences.getBoolean(PREF_USE_GRAY_BACKGROUND, true));
            //Use background
            mFloatingBallView.setUseBackground(defaultSharedPreferences.getBoolean(PREF_USE_BACKGROUND,false));

            //Double click event
            mFloatingBallView.setDoubleClickEventType(defaultSharedPreferences.getInt(PREF_DOUBLE_CLICK_EVENT,0));

            int rightSlideEvent =defaultSharedPreferences.getInt(PREF_RIGHT_SLIDE_EVENT,0);
            if(rightSlideEvent==RECENT_APPS)
                mFloatingBallView.setRightFunctionListener(mFunctionUtil.recentAppsFunctionListener);
            else if (rightSlideEvent == HIDE)
                mFloatingBallView.setRightFunctionListener(mFunctionUtil.hideFunctionListener);


            mFloatingBallView.setMoveUpDistance(defaultSharedPreferences.getInt(StaticStringUtil.PREF_MOVE_UP_DISTANCE, 200));

            mFloatingBallView.requestLayout();
            mFloatingBallView.invalidate();
        }
    }


    public void moveBallViewUp() {
        if(mFloatingBallView !=null) mFloatingBallView.performMoveUpAnimator();
    }

    public void moveBallViewDown() {
        if(mFloatingBallView !=null) mFloatingBallView.performMoveDownAnimator();
    }


//    private FunctionListener nullFunctionListener=new FunctionListener() {
//        @Override
//        public void onClick() {
//        }
//    };
//    private FunctionListener recentAppsFunctionListener=new FunctionListener() {
//        @Override
//        public void onClick() {
//            mFloatingBallService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
//        }
//    };
//
//    private FunctionListener homeFunctionListener=new FunctionListener() {
//        @Override
//        public void onClick() {
//            AccessibilityUtil.doHome(mFloatingBallService);
//        }
//    };
//
//    private FunctionListener hideFunctionListener=new FunctionListener() {
//        @Override
//        public void onClick() {
//            Toast.makeText(mFloatingBallService, "hide", Toast.LENGTH_LONG).show();
//            mFloatingBallService.hideBall();
//        }
//    };
//
//    private FunctionListener notificationFunctionListener=new FunctionListener() {
//        @Override
//        public void onClick() {
//            mFloatingBallService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
//        }
//    };
}

