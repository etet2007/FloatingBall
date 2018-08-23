package com.chenyee.stephenlau.floatingball.floatingBall;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;


import android.view.inputmethod.InputMethodManager;
import com.chenyee.stephenlau.floatingball.App;
import com.chenyee.stephenlau.floatingball.util.FunctionInterfaceUtils;
import com.chenyee.stephenlau.floatingball.util.SharedPrefsUtils;
import java.lang.reflect.Method;
import java.util.List;

import static com.chenyee.stephenlau.floatingball.App.gScreenHeight;
import static com.chenyee.stephenlau.floatingball.App.gScreenWidth;
import static com.chenyee.stephenlau.floatingball.App.getApplication;
import static com.chenyee.stephenlau.floatingball.util.FunctionInterfaceUtils.getListener;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.*;

/**
 * 管理FloatingBall的类。 单例，因为只需要一个FloatingBallView
 */
public class FloatingBallController {

  private static final String TAG = FloatingBallController.class.getSimpleName();
  //Single
  private static FloatingBallController sFloatingBallController = new FloatingBallController();

  /**
   * View
   */
  private FloatingBallView mFloatingBallView;

  private boolean isSoftKeyboardShow = false;
  private boolean isAddedBall = false;


  private FloatingBallController() {
  }

  public static FloatingBallController getInstance() {
    return sFloatingBallController;
  }


  /**
   * create floatingBallView and add to WindowManager
   */
  public void addBallView(Context context) {
    if (isAddedBall) {
      return;
    }
    if (mFloatingBallView == null) {
      mFloatingBallView = new FloatingBallView(context);
    }
    WindowManager windowManager = (WindowManager) App.getApplication().getApplicationContext()
        .getSystemService(Context.WINDOW_SERVICE);
    if (windowManager == null) {
      return;
    }

    // use code to initialize layout parameters
    LayoutParams params = new LayoutParams();
    params.x = SharedPrefsUtils.getIntegerPreference(PREF_PARAM_X, gScreenWidth / 2);
    params.y = SharedPrefsUtils.getIntegerPreference(PREF_PARAM_Y, gScreenHeight / 2);
    params.width = LayoutParams.WRAP_CONTENT;
    params.height = LayoutParams.WRAP_CONTENT;
    params.gravity = Gravity.START | Gravity.TOP;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      params.type = LayoutParams.TYPE_APPLICATION_OVERLAY; //适配Android 8.0
    } else {
      params.type = LayoutParams.TYPE_SYSTEM_ALERT;
    }
    params.format = PixelFormat.RGBA_8888;
    params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
        | LayoutParams.FLAG_NOT_FOCUSABLE
        | LayoutParams.FLAG_LAYOUT_IN_SCREEN
        | LayoutParams.FLAG_LAYOUT_INSET_DECOR;

    mFloatingBallView.setLayoutParams(params);
    windowManager.addView(mFloatingBallView, params);

    updateParameter();


    // init FunctionInterfaceUtils 确保每一次都初始化成功，只有add才能保证每一次都执行成功
    FunctionInterfaceUtils.sFloatingBallService = (FloatingBallService) context;

    isAddedBall = true;
    SharedPrefsUtils.setBooleanPreference(PREF_IS_ADDED_BALL, true);
  }


  public void removeBallView() {
    isAddedBall = false;
    SharedPrefsUtils.setBooleanPreference(PREF_IS_ADDED_BALL, false);

    if (mFloatingBallView == null) {
      return;
    }
    mFloatingBallView.performRemoveAnimator();
    mFloatingBallView = null;
  }

  /**
   * 设置背景图 复制外部路径的图片到目录中去，更新bitmapRead，再进行裁剪
   *
   * @param imagePath 外部图片地址
   */
  public void setBackgroundImage(String imagePath) {
    if (mFloatingBallView != null) {

      mFloatingBallView.copyBackgroundImage(imagePath);
      mFloatingBallView.refreshBitmapRead();
      mFloatingBallView.createBitmapCropFromBitmapRead();
      mFloatingBallView.invalidate();
    }
  }

  /**
   * No use
   * @param key
   */
  public void updateSpecificParameter(String key) {
    if (mFloatingBallView == null) {
      return;
    }
    switch (key) {
      case PREF_OPACITY:
        mFloatingBallView.setOpacity(SharedPrefsUtils.getIntegerPreference(PREF_OPACITY, 125));
        break;
      case PREF_OPACITY_MODE:
        mFloatingBallView.setOpacityMode(SharedPrefsUtils.getIntegerPreference(PREF_OPACITY_MODE, OPACITY_NONE));
        break;
      case PREF_SIZE:
        mFloatingBallView.changeFloatBallSizeWithRadius(SharedPrefsUtils.getIntegerPreference(PREF_SIZE, 25));
        break;
      case PREF_USE_BACKGROUND:
        mFloatingBallView.setUseBackground(SharedPrefsUtils.getBooleanPreference(PREF_USE_BACKGROUND, false));
        break;
      case PREF_USE_GRAY_BACKGROUND:
        mFloatingBallView.setUseGrayBackground(
            SharedPrefsUtils.getBooleanPreference(PREF_USE_GRAY_BACKGROUND, true));
        break;
      case PREF_IS_VIBRATE:
        mFloatingBallView
            .setIsVibrate(SharedPrefsUtils.getBooleanPreference(PREF_IS_VIBRATE, true));
        break;
    }
    //refresh the layout and draw the view
    mFloatingBallView.requestLayout();
    mFloatingBallView.invalidate();

    if (key.equals(PREF_DOUBLE_CLICK_EVENT)) {
      mFloatingBallView.setDoubleClickEventType(SharedPrefsUtils.getIntegerPreference(PREF_DOUBLE_CLICK_EVENT, NONE));
    }
    if (key.equals(PREF_LEFT_SLIDE_EVENT)) {
      //LeftSlideEvent
      int leftSlideEvent = SharedPrefsUtils
          .getIntegerPreference(PREF_LEFT_SLIDE_EVENT, RECENT_APPS);
      mFloatingBallView.setLeftFunctionListener(getListener(leftSlideEvent));
    }
    if (key.equals(PREF_RIGHT_SLIDE_EVENT)) {
      //RightSlideEvent
      int rightSlideEvent = SharedPrefsUtils
          .getIntegerPreference(PREF_RIGHT_SLIDE_EVENT, RECENT_APPS);
      mFloatingBallView.setRightFunctionListener(getListener(rightSlideEvent));
    }
    if (key.equals(PREF_UP_SLIDE_EVENT)) {
      //UpSlideEvent
      int upSlideEvent = SharedPrefsUtils.getIntegerPreference(PREF_UP_SLIDE_EVENT, HOME);
      mFloatingBallView.setUpFunctionListener(getListener(upSlideEvent));
    }
    if (key.equals(PREF_DOWN_SLIDE_EVENT)) {
      //DownSlideEvent
      int downSlideEvent = SharedPrefsUtils
          .getIntegerPreference(PREF_DOWN_SLIDE_EVENT, NOTIFICATION);
      mFloatingBallView.setDownFunctionListener(getListener(downSlideEvent));
    }

  }

  /**
   * Use sharedPreference data to update all parameter
   */
  private void updateParameter() {
    if (mFloatingBallView == null) {
      return;
    }
    /* View */
    mFloatingBallView.setOpacity(SharedPrefsUtils.getIntegerPreference(PREF_OPACITY, 125));
    mFloatingBallView
        .changeFloatBallSizeWithRadius(SharedPrefsUtils.getIntegerPreference(PREF_SIZE, 25));
    mFloatingBallView
        .setUseBackground(SharedPrefsUtils.getBooleanPreference(PREF_USE_BACKGROUND, false));
    mFloatingBallView.setUseGrayBackground(
        SharedPrefsUtils.getBooleanPreference(PREF_USE_GRAY_BACKGROUND, true));

    mFloatingBallView.requestLayout();
    mFloatingBallView.invalidate();

    /* Function */
    mFloatingBallView.setDoubleClickEventType(SharedPrefsUtils.getIntegerPreference(PREF_DOUBLE_CLICK_EVENT, NONE));
    int leftSlideEvent = SharedPrefsUtils
        .getIntegerPreference(PREF_LEFT_SLIDE_EVENT, RECENT_APPS);
    mFloatingBallView.setLeftFunctionListener(getListener(leftSlideEvent));
    int rightSlideEvent = SharedPrefsUtils
        .getIntegerPreference(PREF_RIGHT_SLIDE_EVENT, RECENT_APPS);
    mFloatingBallView.setRightFunctionListener(getListener(rightSlideEvent));
    int upSlideEvent = SharedPrefsUtils.getIntegerPreference(PREF_UP_SLIDE_EVENT, HOME);
    mFloatingBallView.setUpFunctionListener(getListener(upSlideEvent));
    int downSlideEvent = SharedPrefsUtils
        .getIntegerPreference(PREF_DOWN_SLIDE_EVENT, NOTIFICATION);
    mFloatingBallView.setDownFunctionListener(getListener(downSlideEvent));

    mFloatingBallView.setmSingleTapFunctionListener(getListener(BACK));
  }

  /**
   * According to the state of input method, move the floatingBall view.
   */
  public void inputMethodDetect() {
    if (mFloatingBallView == null) {
      return;
    }

    boolean isInputing = false;
    int inputMethodWindowHeight = 0;

    if (android.os.Build.VERSION.SDK_INT > 20) {//Work
      try {
        InputMethodManager imm = (InputMethodManager) App.getApplication().getApplicationContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE);
        Class clazz = imm.getClass();
        Method method = clazz.getMethod("getInputMethodWindowVisibleHeight", null);
        method.setAccessible(true);
        inputMethodWindowHeight = (Integer) method.invoke(imm, null);
        Log.d(TAG, "inputMethodWindowHeight = " + inputMethodWindowHeight);
        if (inputMethodWindowHeight > 100) {
          isInputing = true;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {//应该不work
      String defaultInputName = Settings.Secure
          .getString(getApplication().getApplicationContext().getContentResolver(),
              Settings.Secure.DEFAULT_INPUT_METHOD);
      defaultInputName = defaultInputName.substring(0, defaultInputName.indexOf("/"));

      ActivityManager activityManager = (ActivityManager) getApplication().getApplicationContext()
          .getSystemService(Context.ACTIVITY_SERVICE);
      List<RunningAppProcessInfo> appProcesses = activityManager
          .getRunningAppProcesses();

      for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
        if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
          if (appProcess.processName.equals(defaultInputName)) {
            isInputing = true;
            break;
          }
        }
      }
    }
    //过滤掉100以下的了
    if (isInputing) {// 键盘正在显示
      if (!isSoftKeyboardShow) {// 键盘第一次显示
//        mFloatingBallManager.moveBallViewUp();
        Log.d(TAG, "notifyBallViewKeyboardShow call");
        mFloatingBallView.inputMethodWindowHeight = inputMethodWindowHeight;
        mFloatingBallView.moveToKeyboardTop();

      }
      isSoftKeyboardShow = true;
    } else {// 键盘不再显示
      if (isSoftKeyboardShow) {// 键盘第一次消失
        mFloatingBallView.moveBackWhenKeyboardDisappear();
      }
      isSoftKeyboardShow = false;
    }
  }

  public void moveBallViewUp() {
    if (mFloatingBallView != null) {
      mFloatingBallView.performMoveUpAnimator();
    }
  }

  public void moveBallViewDown() {
    if (mFloatingBallView != null) {
      mFloatingBallView.performMoveDownAnimator();
    }
  }


  public void clear() {
    if (mFloatingBallView != null) {
      mFloatingBallView.recycleBitmap();
    }
  }

  public void setVisibility(boolean isHide) {
    if (mFloatingBallView != null) {
      mFloatingBallView.setVisibility(isHide ? View.INVISIBLE : View.VISIBLE);
    }
  }
}
