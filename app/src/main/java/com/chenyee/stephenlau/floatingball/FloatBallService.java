package com.chenyee.stephenlau.floatingball;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodManager;

import static com.chenyee.stephenlau.floatingball.BallView.TAG;


/**
 * Accessibility services should only be used to assist users with disabilities in using Android devices and apps.
 *  Such a service can optionally随意地 request the capability能力 for querying the content of the active window.
 *
 * Created by wangxiandeng on 2016/11/25.
 */

public class FloatBallService extends AccessibilityService {

    public static final int TYPE_ADD = 0;
    public static final int TYPE_DEL = 1;
    public static final int TYPE_OPACITY =2;
    public static final int TYPE_SIZE =3;
    public static final int TYPE_IMAGE =4;
    public static final int TYPE_SAVE =5;
    public static final int TYPE_USEBACKGROUND =6;
    public static final int TYPE_UPDATE_DATA =7;

    private FloatBallManager mFloatBallManager;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        if(mFloatBallManager==null)
            mFloatBallManager = FloatBallManager.getInstance();



    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//        Log.d("lqt", "onAccessibilityEvent "+event);




        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            if(event.getEventType()==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
//                if (event.getPackageName().toString().contains("om.sohu.inputmethod.sogou")) {

//                }
            }
        }
    }

    private class IMMResult extends ResultReceiver {
        public int result = -1;
        public IMMResult() {
            super(null);
        }

        @Override
        public void onReceiveResult(int r, Bundle data) {
            result = r;
        }

        // poll result value for up to 500 milliseconds
        public int getResult() {
            try {
                int sleep = 0;
                while (result == -1 && sleep < 500) {
                    Thread.sleep(100);
                    sleep += 100;
                }
            } catch (InterruptedException e) {
                Log.e("IMMResult", e.getMessage());
            }
            return result;
        }
    }

    @Override
    public void onInterrupt() {
        Log.d("lqt", "onInterrupt");
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
//                if(type==TYPE_OPACITY){
//                    mFloatBallManager.setOpacity(data.getInt("opacity"));
//                }
//                if (type == TYPE_SIZE) {
//                    mFloatBallManager.setSize(data.getInt("size"));
//                }
                if (type == TYPE_IMAGE) {
                    mFloatBallManager.setBackgroundPic(this,data.getString("imagePath"));
                }
                if(type == TYPE_SAVE){
                    mFloatBallManager.saveFloatBallData();
                }
                if(type==TYPE_USEBACKGROUND){
                    mFloatBallManager.setUseBackground(data.getBoolean("useBackground"));
                }

                if(type==TYPE_UPDATE_DATA){
                    mFloatBallManager.updateData();
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }
/*
public enum CropType {
        CIRCLE(1), RECTANGLE(2);

        private int mValue;

        CropType(int value) {
            this.mValue = value;
        }

        public int value() {
            return mValue;
        }

        public static CropType valueOf(int value) {
            switch (value) {
                case CropImageBorderView.CIRCLE:
                    return CIRCLE;
                case CropImageBorderView.RECTANGLE:
                    return RECTANGLE;
                default:
                    return null;
            }
        }
    }
 */

}
