package com.chenyee.stephenlau.floatingball.floatingBall;

import android.accessibilityservice.AccessibilityService;
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
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.chenyee.stephenlau.floatingball.R;
import com.chenyee.stephenlau.floatingball.util.SharedPrefsUtils;

import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.EXTRA_TYPE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_HAS_ROTATE_HIDE_BALL;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_IS_ADDED_BALL;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_IS_ROTATE_HIDE;


/**
 * Accessibility services should only be used to assist users with disabilities in using Android devices and apps. Such
 * a service can optionally随意地 request the capability能力 for querying the content of the active window.
 */

public class FloatingBallService extends AccessibilityService {

  private static final String TAG = FloatingBallService.class.getSimpleName();

  public static final int TYPE_ADD = 0;
  public static final int TYPE_REMOVE = 1;
  public static final int TYPE_IMAGE = 2;
  public static final int TYPE_CLEAR = 3;
  public static final int TYPE_HIDE = 4;

  private FloatingBallController mFloatingBallController;

  private void getFloatingBallController() {
    if (mFloatingBallController == null) {
      mFloatingBallController = FloatingBallController.getInstance();
    }
  }

  public static Intent getStartIntent(Context context) {
    return new Intent(context, FloatingBallService.class);
  }

  private SharedPreferences.OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      Log.d(TAG, "onSharedPreferenceChanged: ");
      mFloatingBallController.updateSpecificParameter(key);
    }
  };

  /**
   * 初始化
   */
  @Override
  protected void onServiceConnected() {
    super.onServiceConnected();
    Log.d(TAG, "onServiceConnected: ");
    getFloatingBallController();

    mFloatingBallController.addBallView(FloatingBallService.this);

    SharedPrefsUtils.getSharedPreferences().registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    SharedPrefsUtils.getSharedPreferences().registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
  }

  @Override
  public void onInterrupt() {

  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    SharedPrefsUtils.getSharedPreferences()
        .unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    boolean isAddedBall = SharedPrefsUtils.getBooleanPreference(PREF_IS_ADDED_BALL, false);
    if (!isAddedBall) {
      return;
    }

    boolean hasRotateHideBall = SharedPrefsUtils.getBooleanPreference(PREF_HAS_ROTATE_HIDE_BALL, false);


    if (SharedPrefsUtils.getBooleanPreference(PREF_IS_ROTATE_HIDE, true)) {
      if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        mFloatingBallController.rotateHideBallView();
      } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT  && hasRotateHideBall ) {
        mFloatingBallController.addBallView(FloatingBallService.this);
        SharedPrefsUtils.setBooleanPreference(PREF_HAS_ROTATE_HIDE_BALL, false);
      }
    }
  }

  @Override
  public void onAccessibilityEvent(AccessibilityEvent event) {
    Boolean hasAddBall = SharedPrefsUtils.getBooleanPreference(PREF_IS_ADDED_BALL, false);

    if (!hasAddBall || mFloatingBallController == null) {
      //do nothing
      return;
    }
    //触发输入法检测
    mFloatingBallController.inputMethodDetect();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG, "onStartCommand");

    if (intent != null) {
      //mFloatBallManager的判断是因为生命周期有时候有问题
      getFloatingBallController();

      Bundle data = intent.getExtras();
      if (data != null) {
        int type = data.getInt(EXTRA_TYPE);

        if (type == TYPE_ADD) {
          mFloatingBallController.addBallView(FloatingBallService.this);
        }

        if (type == TYPE_REMOVE) {
          mFloatingBallController.removeBallView();
        }

        if (type == TYPE_HIDE) {
          mFloatingBallController.setBallViewVisibility(data.getBoolean("isHide"));
        }

        //intent中传图片地址，也可以换为sharedPreference吧
        if (type == TYPE_IMAGE) {
          mFloatingBallController.setBackgroundImage(data.getString("imagePath"));
        }

        if (type == TYPE_CLEAR) {
          mFloatingBallController.recycleBitmapMemory();
        }
      }
    }
    return super.onStartCommand(intent, flags, startId);
  }


  public void hideBall() {
    getFloatingBallController();
    mFloatingBallController.removeBallView();

    sendHideBallNotification();
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
    data.putInt(EXTRA_TYPE, FloatingBallService.TYPE_ADD);
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
