package com.chenyee.stephenlau.floatingball.floatingBall.service;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.chenyee.stephenlau.floatingball.R;
import com.chenyee.stephenlau.floatingball.floatingBall.FloatingBallController;
import com.chenyee.stephenlau.floatingball.repository.BallSettingRepo;

import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.EXTRAS_COMMAND;

/**
 * Accessibility services should only be used to assist users with disabilities in using Android devices and apps. Such
 * a service can optionally随意地 request the capability能力 for querying the content of the active window.
 */

public class FloatingBallService extends AccessibilityService {
    public static final int TYPE_SWITCH_ON = 0;
    public static final int TYPE_REMOVE_ALL = 1;
    public static final int TYPE_IMAGE_PATH = 2;
    public static final int TYPE_CLEAR = 3;
    public static final int TYPE_HIDE_TEMPORARILY = 4;
    public static final int TYPE_ADD = 5;
    public static final int TYPE_REMOVE_LAST = 6;

    private static final String TAG = FloatingBallService.class.getSimpleName();

    private String targetPackageName;
    private FloatingBallController floatingBallController = FloatingBallController.getInstance();

    private SharedPreferences.OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = (sharedPreferences, key) -> {
        Log.d(TAG, "onSharedPreferenceChanged: ");
        floatingBallController.updateSpecificParameter(key);
    };

    public String getTargetPackageName() {
        return targetPackageName;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");

        BallSettingRepo.registerOnDataChangeListener(mOnSharedPreferenceChangeListener);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "onServiceConnected: ");
        
        floatingBallController.startBallView(FloatingBallService.this);
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BallSettingRepo.unregisterOnDataChangeListener(mOnSharedPreferenceChangeListener);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        //没有在设置中打开
        boolean isAddedBall = BallSettingRepo.isAddedBallInSetting();
        if (!isAddedBall) {
            return;
        }

        if (BallSettingRepo.isRotateHideSetting()) { //LANDSCAPE 隐藏
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                floatingBallController.hideWhenRotate();
            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT &&
                    floatingBallController.isHideBecauseRotate()) {
                floatingBallController.startWhenRotateBack(FloatingBallService.this);
            }
        } else { //LANDSCAPE 不隐藏，更新位置
            floatingBallController.updateBallViewLayout();
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //没有打开
        boolean isAddedBall = BallSettingRepo.isAddedBallInSetting();
        if (!isAddedBall) {
            return;
        }

        int type = event.getEventType();

        if (type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            targetPackageName = event.getPackageName() == null ? "" : event.getPackageName().toString();
        }

        //触发输入法检测
        floatingBallController.inputMethodDetect(FloatingBallService.this);
    }

    /**
     * 接受其他组件的信息
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (intent != null) {

            Bundle data = intent.getExtras();
            if (data != null) {
                int type = data.getInt(EXTRAS_COMMAND);

                if (type == TYPE_SWITCH_ON) {
                    floatingBallController.startBallView(FloatingBallService.this);
                }
                if (type == TYPE_REMOVE_ALL) {
                    floatingBallController.removeBallView();
                }
                if (type == TYPE_HIDE_TEMPORARILY) {
                    floatingBallController.setBallViewIsHide(data.getBoolean("isHide"));
                }
                //intent中传图片地址，也可以换为sharedPreference吧
                if (type == TYPE_IMAGE_PATH) {
                    floatingBallController.setBackgroundImage(data.getString("imagePath"));
                }
                if (type == TYPE_CLEAR) {
                    floatingBallController.recycleBitmapMemory();
                }

                //动态变化的需要通过intent ShardPref无法区分是增还是减。
                if (type == TYPE_ADD) {
                    floatingBallController.addFloatingBallView(FloatingBallService.this, BallSettingRepo.amount() - 1);
                }
                if (type == TYPE_REMOVE_LAST) {
                    floatingBallController.removeLastFloatingBall();
                }

            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 暂时隐藏，通过Visibility
     */
    public void hideBallTemporarily() {
        floatingBallController.setBallViewIsHide(true);
    }

    public void showBall() {
        floatingBallController.setBallViewIsHide(false);
    }

    /**
     * 长时间隐藏，通过remove
     */
    public void hideBallForLongTime() {
        floatingBallController.removeBallView();
        sendHideBallNotification();
    }

    public void postRunnable(Runnable r) {
        floatingBallController.postRunnable(r);
    }

    /**
     * send notification when hiding
     */
    private void sendHideBallNotification() {
        String contentTitle = getString(R.string.hide_notification_content_title);
        String contentText = getString(R.string.hideNotificationContentText);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            return;
        }

        Intent intent = new Intent(this, FloatingBallService.class);
        Bundle data = new Bundle();
        data.putInt(EXTRAS_COMMAND, FloatingBallService.TYPE_SWITCH_ON);
        intent.putExtras(data);
        PendingIntent addBallPendingIntent = PendingIntent.getService(this, 0, intent, 0);

        if (Build.VERSION.SDK_INT >= 26) {
            String channelID = "1";
            String channelName = "channel_name";
            NotificationChannel mChannel = new NotificationChannel(channelID, channelName,
                    NotificationManager.IMPORTANCE_LOW);

            notificationManager.createNotificationChannel(mChannel);

            Notification notification = new Notification.Builder(this, channelID)
                    .setSmallIcon(R.mipmap.ic_launcher_app)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_app))
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setAutoCancel(true)
                    .setContentIntent(addBallPendingIntent)
                    .build();
            notificationManager.notify(1, notification);

        } else {
            Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher_app)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_app))
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setAutoCancel(true)
                    .setContentIntent(addBallPendingIntent)
                    .build();
            notificationManager.notify(1, notification);
        }
    }
}
