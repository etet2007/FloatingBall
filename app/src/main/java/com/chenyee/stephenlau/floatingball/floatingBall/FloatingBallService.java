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
import com.chenyee.stephenlau.floatingball.util.SingleDataManager;

import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.EXTRA_TYPE;


/**
 * Accessibility services should only be used to assist users with disabilities in using Android devices and apps. Such
 * a service can optionally随意地 request the capability能力 for querying the content of the active window.
 */

public class FloatingBallService extends AccessibilityService {

  private static final String TAG = FloatingBallService.class.getSimpleName();

  public static final int TYPE_START = 0;
  public static final int TYPE_REMOVE_ALL = 1;
  public static final int TYPE_IMAGE = 2;
  public static final int TYPE_CLEAR = 3;
  public static final int TYPE_HIDE = 4;
  public static final int TYPE_ADD = 5;
  public static final int TYPE_REMOVE_LAST = 6;

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

    mFloatingBallController.startBallView(FloatingBallService.this);

    SingleDataManager.registerOnDataChangeListener(mOnSharedPreferenceChangeListener);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    SingleDataManager.registerOnDataChangeListener(mOnSharedPreferenceChangeListener);
  }

  @Override
  public void onInterrupt() {

  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    SingleDataManager.unregisterOnDataChangeListener(mOnSharedPreferenceChangeListener);

  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    boolean isAddedBall = SingleDataManager.isAddedBallInSetting();
    if (!isAddedBall) {
      return;
    }

    boolean isBallHideBecauseRotate = SingleDataManager.isBallHideBecauseRotate();

    if (SingleDataManager.isRotateHideSetting()) {//LANDSCAPE 隐藏

      if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

        mFloatingBallController.rotateHideBallView();

      } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT && isBallHideBecauseRotate) {

        mFloatingBallController.startBallView(FloatingBallService.this);
        SingleDataManager.setIsBallHideBecauseRotate(false);
      }

    } else { //LANDSCAPE 不隐藏

        mFloatingBallController.updateBallViewLayout();

      }

  }

  @Override
  public void onAccessibilityEvent(AccessibilityEvent event) {
    Boolean hasAddBall = SingleDataManager.isAddedBallInSetting();
    Boolean isAvoidKeyboard = SingleDataManager.isAvoidKeyboard();
    if (hasAddBall && isAvoidKeyboard && mFloatingBallController != null) {
      //触发输入法检测
      mFloatingBallController.inputMethodDetect();
    }
  }

  /**
   * 转化外界的指令。
   * @param intent
   * @param flags
   * @param startId
   * @return
   */
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG, "onStartCommand");

    if (intent != null) {
      //mFloatBallManager的判断是因为生命周期有时候有问题
      getFloatingBallController();

      Bundle data = intent.getExtras();
      if (data != null) {
        int type = data.getInt(EXTRA_TYPE);

        if (type == TYPE_START) {
          mFloatingBallController.startBallView(FloatingBallService.this);
        }

        if (type == TYPE_REMOVE_ALL) {
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

        if (type == TYPE_ADD) {
          mFloatingBallController.addFloatingBallView(FloatingBallService.this,SingleDataManager.amount()-1);
        }
        if (type == TYPE_REMOVE_LAST) {
          mFloatingBallController.removeLastFloatingBall();
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
    data.putInt(EXTRA_TYPE, FloatingBallService.TYPE_START);
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
