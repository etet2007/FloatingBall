package com.chenyee.stephenlau.floatingball;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.Method;
import java.util.List;


/**
 * Accessibility services should only be used to assist users with disabilities in using Android devices and apps.
 *  Such a service can optionally随意地 request the capability能力 for querying the content of the active window.
 *
 * Created by wangxiandeng on 2016/11/25.
 */

public class FloatBallService extends AccessibilityService {
    public static final String TAG = "AccessibilityService";

    public static final int TYPE_ADD = 0;
    public static final int TYPE_DEL = 1;
    public static final int TYPE_OPACITY =2;
    public static final int TYPE_SIZE =3;
    public static final int TYPE_IMAGE =4;
    public static final int TYPE_SAVE =5;
    public static final int TYPE_USE_BACKGROUND =6;
    public static final int TYPE_UPDATE_DATA =7;

    private FloatBallManager mFloatBallManager;

    private boolean hasSoftKeyboardShow=false;
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "onServiceConnected");

        if(mFloatBallManager==null)
            mFloatBallManager = FloatBallManager.getInstance();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//        Log.d(TAG, "onAccessibilityEvent "+event);

        inputMethodSate(getApplicationContext());
    }

    /**
     * 软键盘状态判断
     * @param context
     */
    public void inputMethodSate(Context context) {
        //得到默认输入法包名
        String defaultInputName = Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        defaultInputName = defaultInputName.substring(0, defaultInputName.indexOf("/"));
        boolean isInputing = false;
        if(android.os.Build.VERSION.SDK_INT > 20) {//这代码太牛了
            try{
                InputMethodManager imm = (InputMethodManager) context.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                Class clazz = imm.getClass();
                Method method = clazz.getMethod("getInputMethodWindowVisibleHeight", null);
                method.setAccessible(true);
                int height = (Integer) method.invoke(imm, null);
                Log.d("LOG", "height == "+height);
                if(height > 100) {
                    isInputing = true;
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
        }else {//应该不work
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();

            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                    if(appProcess.processName.equals(defaultInputName)) {
                        isInputing = true;
                        break;
                    }
                }
            }
        }

        if(isInputing) {
            Log.d(TAG, "软键盘显示中");
            if (!hasSoftKeyboardShow)
                mFloatBallManager.moveBallViewUp();
            hasSoftKeyboardShow=true;
        }else {
            Log.d(TAG, "软键盘隐藏中");
            if(hasSoftKeyboardShow)
                mFloatBallManager.moveBallViewDown();
            hasSoftKeyboardShow=false;
        }
    }


    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFloatBallManager.saveFloatBallData();
    }

    //    Called by the system every time a client explicitly starts the service by calling startService(Intent),
// providing the arguments it supplied and a unique integer token representing the start request.
// Do not call this method directly.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onAccessibilityEvent onStartCommand");

        if(intent != null ) {
            //mFloatBallManager的判断是因为生命周期有时候有问题
            if(mFloatBallManager==null)
                mFloatBallManager = FloatBallManager.getInstance();

            Bundle data = intent.getExtras();
            if (data != null) {
                int type = data.getInt("type");
                if (type == TYPE_ADD) {
                    mFloatBallManager.addBallView(this);
                }
                if(type== TYPE_DEL){
                    mFloatBallManager.removeBallView(this);//内部有mFloatBallManager.saveFloatBallData();
                }

                if (type == TYPE_IMAGE) {
                    mFloatBallManager.setBackgroundPic(this,data.getString("imagePath"));
                }
                if(type == TYPE_SAVE){
                    mFloatBallManager.saveFloatBallData();
                }
                if(type== TYPE_USE_BACKGROUND){
                    mFloatBallManager.setUseBackground(data.getBoolean("useBackground"));
                }

                if(type==TYPE_UPDATE_DATA){
                    mFloatBallManager.updateBallViewData();
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }


}
