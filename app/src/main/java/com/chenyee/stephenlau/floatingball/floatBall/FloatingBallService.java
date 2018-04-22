package com.chenyee.stephenlau.floatingball.floatBall;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodManager;

import com.chenyee.stephenlau.floatingball.R;
import com.chenyee.stephenlau.floatingball.util.SharedPrefsUtils;

import java.lang.reflect.Method;
import java.util.List;

import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.EXTRA_TYPE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_HAS_ADDED_BALL;


/**
 * Accessibility services should only be used to assist users with disabilities in using Android
 * devices and apps.
 * Such a service can optionally随意地 request the capability能力 for querying the content of the
 * active window.
 *
 * Accept the intent from Main activity,
 * Created by wangxiandeng on 2016/11/25.
 */

public class FloatingBallService extends AccessibilityService {
    private static final String TAG = FloatingBallService.class.getSimpleName();

    public static final int TYPE_ADD = 0;
    public static final int TYPE_REMOVE = 1;
    public static final int TYPE_IMAGE = 2;
    public static final int TYPE_CLEAR = 3;

    private FloatingBallManager mFloatingBallManager;

    private boolean hasSoftKeyboardShow = false;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, FloatingBallService.class);
    }
    private SharedPreferences.OnSharedPreferenceChangeListener mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.d(TAG, "onSharedPreferenceChanged: ");
            mFloatingBallManager.updateBallViewParameter(key);
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "onServiceConnected: ");
        if(mFloatingBallManager == null){
            mFloatingBallManager = FloatingBallManager.getInstance();
            addBallViewAndSaveState();

            SharedPrefsUtils.getSharedPreferences().registerOnSharedPreferenceChangeListener(mListener);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPrefsUtils.getSharedPreferences().registerOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt: ");
        if(mFloatingBallManager !=null) mFloatingBallManager.saveFloatingBallState();
        SharedPrefsUtils.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if(mFloatingBallManager !=null) mFloatingBallManager.saveFloatingBallState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged: " + newConfig.keyboard);
        boolean hasAddedBall = SharedPrefsUtils.getBooleanPreference(PREF_HAS_ADDED_BALL, false);
        if (!hasAddedBall) {
            return;
        }
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d(TAG, "onAccessibilityEvent: removeBallView");
            mFloatingBallManager.removeBallView();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d(TAG, "onAccessibilityEvent: addBallView");
            mFloatingBallManager.addBallView(FloatingBallService.this);
        }
    }

    /**
     * todo onAccessibilityEvent 中放太多逻辑会影响性能。有回调的方法解决会更好。
     * @param event
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "onAccessibilityEvent: "+event.getEventType());

        Boolean hasAddBall = SharedPrefsUtils.getBooleanPreference(PREF_HAS_ADDED_BALL, false);
        Log.d(TAG, "onAccessibilityEvent: hasAddBall "+hasAddBall);
        //没有打开悬浮球
        if(!hasAddBall)
            return;

        inputMethodDetect(getApplicationContext());
    }
    /**
     * According to the state of input method, move the floatingBall view.
     * @param context Context
     */
    private void inputMethodDetect(Context context) {
        //得到默认输入法包名
        boolean isInputing = false;
        if(android.os.Build.VERSION.SDK_INT > 20) {//Work
            try{
                InputMethodManager imm = (InputMethodManager) context.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                Class clazz = imm.getClass();
                Method method = clazz.getMethod("getInputMethodWindowVisibleHeight", null);
                method.setAccessible(true);
                int height = (Integer) method.invoke(imm, null);
//                Log.d("LOG", "height == "+height);
                if(height > 100) {
                    isInputing = true;
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
        }else {//应该不work
            String defaultInputName = Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
            defaultInputName = defaultInputName.substring(0, defaultInputName.indexOf("/"));

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
            if (!hasSoftKeyboardShow)
                mFloatingBallManager.moveBallViewUp();
            hasSoftKeyboardShow = true;
        }else {
            if(hasSoftKeyboardShow)
                mFloatingBallManager.moveBallViewDown();
            hasSoftKeyboardShow = false;
        }
    }

    //    Called by the system every time a client explicitly starts the service by calling startService(Intent),
    // providing the arguments it supplied and a unique integer token representing the start request.
    // Do not call this method directly.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        if(intent != null ) {
            //mFloatBallManager的判断是因为生命周期有时候有问题
            if (mFloatingBallManager == null) {
                mFloatingBallManager = FloatingBallManager.getInstance();
            }

            Bundle data = intent.getExtras();
            if (data != null) {
                int type = data.getInt(EXTRA_TYPE);

                if (type == TYPE_ADD) addBallViewAndSaveState();

                if (type == TYPE_REMOVE) removeBallViewAndSaveData();

                //intent中传图片地址，也可以换为sharedPreference吧
                if (type == TYPE_IMAGE) mFloatingBallManager.setBackgroundImage(data.getString("imagePath"));

                if(type == TYPE_CLEAR) mFloatingBallManager.clear();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void addBallViewAndSaveState() {
        mFloatingBallManager.addBallView(FloatingBallService.this);
        mFloatingBallManager.saveFloatingBallState();
    }

    private void removeBallViewAndSaveData() {
        mFloatingBallManager.removeBallView();
        mFloatingBallManager.saveFloatingBallState();
    }

    public void hideBall(){
        if(mFloatingBallManager ==null)
            mFloatingBallManager = FloatingBallManager.getInstance();

        removeBallViewAndSaveData();
        sendNotification();
    }



    private void sendNotification() {
        String contentTitle = getString(R.string.hide_notification_content_title);
        String contentText = getString(R.string.hideNotificationContentText);
        NotificationManager notificationManager  = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if(notificationManager==null)
            return;

        Intent intent = new Intent(this, FloatingBallService.class);
        Bundle data = new Bundle();
        data.putInt(EXTRA_TYPE, FloatingBallService.TYPE_ADD);
        intent.putExtras(data);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);

        if (Build.VERSION.SDK_INT >= 26) {
            String channelID = "1";
            String channelName = "channel_name";
            NotificationChannel mChannel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_LOW);

            notificationManager.createNotificationChannel(mChannel);

            Notification notification = new Notification.Builder(this, channelID)
                    .setSmallIcon(R.mipmap.ic_launcher_app)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_app))
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setAutoCancel(true)
                    .setContentIntent(pintent)
                    .build();
            notificationManager.notify(1, notification);

        } else {
            Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher_app)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_app))
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setAutoCancel(true)
                    .setContentIntent(pintent)
                    .build();
            notificationManager.notify(1, notification);
        }
    }
}
