package com.chenyee.stephenlau.floatingball.floatingBall;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;


import com.chenyee.stephenlau.floatingball.App;
import com.chenyee.stephenlau.floatingball.util.BitmapUtils;
import com.chenyee.stephenlau.floatingball.util.FunctionInterfaceUtils;
import com.chenyee.stephenlau.floatingball.util.InputMethodDetector;
import com.chenyee.stephenlau.floatingball.util.SharedPrefsUtils;
import com.chenyee.stephenlau.floatingball.util.SingleDataManager;

import static com.chenyee.stephenlau.floatingball.App.gScreenHeight;
import static com.chenyee.stephenlau.floatingball.App.gScreenWidth;
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
    Configuration configuration = App.getApplication().getResources().getConfiguration(); //获取设置的配置信息
    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      params.x = SharedPrefsUtils.getIntegerPreference(PREF_PARAM_X_LANDSCAPE, gScreenWidth / 2);
      params.y = SharedPrefsUtils.getIntegerPreference(PREF_PARAM_Y_LANDSCAPE, gScreenHeight / 2);
    } else if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
      params.x = SharedPrefsUtils.getIntegerPreference(PREF_PARAM_X_PORTRAIT, gScreenWidth / 2);
      params.y = SharedPrefsUtils.getIntegerPreference(PREF_PARAM_Y_PORTRAIT, gScreenHeight / 2);
    }

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

    mFloatingBallView.setBallViewLayoutParams(params);
    windowManager.addView(mFloatingBallView, params);

    updateParameter();

    // init FunctionInterfaceUtils 确保每一次都初始化成功，只有add才能保证每一次都执行成功
    FunctionInterfaceUtils.sFloatingBallService = (FloatingBallService) context;

    isAddedBall = true;
    SharedPrefsUtils.setBooleanPreference(PREF_IS_ADDED_BALL_IN_SETTING, true);
  }


  public void removeBallView() {
    SingleDataManager.setIsAddedBallInSetting(false);
    remove();
  }

  public void rotateHideBallView() {
    SingleDataManager.setIsBallHideBecauseRotate(true);
    remove();
  }

  private void remove() {
    isAddedBall = false;
    if (mFloatingBallView == null) {
      return;
    }
    mFloatingBallView.removeBallWithAnimation();
    mFloatingBallView = null;
  }


  /**
   * 设置背景图 复制外部路径的图片到目录中去，更新bitmapRead，再进行裁剪
   *
   * @param imagePath 外部图片地址
   */
  public void setBackgroundImage(String imagePath) {
    if (mFloatingBallView != null) {

      BitmapUtils.copyBackgroundImage(imagePath);
      mFloatingBallView.refreshBitmapRead();
      mFloatingBallView.createBitmapCropFromBitmapRead();
      mFloatingBallView.invalidate();
    }
  }

  /**
   * No use
   */
  public void updateSpecificParameter(String key) {
    if (mFloatingBallView == null) {
      return;
    }
    switch (key) {
      case PREF_OPACITY:
        mFloatingBallView.setOpacity(SingleDataManager.opacity());
        break;
      case PREF_OPACITY_MODE:
        mFloatingBallView.setOpacityMode(SharedPrefsUtils.getIntegerPreference(PREF_OPACITY_MODE, OPACITY_NONE));
        break;
      case PREF_SIZE:
        mFloatingBallView.changeFloatBallSizeWithRadius(SingleDataManager.size());
        break;
      case PREF_USE_BACKGROUND:
        mFloatingBallView.setUseBackground(SingleDataManager.isUseBackground());
        break;
      case PREF_USE_GRAY_BACKGROUND:
      case PREF_IS_VIBRATE:
      case PREF_MOVE_UP_DISTANCE:
        mFloatingBallView.updateModelData();
        break;
    }
    //refresh the layout and draw the view
    mFloatingBallView.requestLayout();
    mFloatingBallView.invalidate();

    if (key.equals(PREF_DOUBLE_CLICK_EVENT)) {
      mFloatingBallView.setDoubleClickEventType(SingleDataManager.doubleClickEvent());
    } else if (key.equals(PREF_LEFT_SLIDE_EVENT)) {
      mFloatingBallView.setLeftFunctionListener(SingleDataManager.leftSlideEvent());
    } else if (key.equals(PREF_RIGHT_SLIDE_EVENT)) {
      mFloatingBallView.setRightFunctionListener(SingleDataManager.rightSlideEvent());
    } else if (key.equals(PREF_UP_SLIDE_EVENT)) {
      mFloatingBallView.setUpFunctionListener(SingleDataManager.upSlideEvent());
    } else if (key.equals(PREF_DOWN_SLIDE_EVENT)) {
      mFloatingBallView.setDownFunctionListener(SingleDataManager.downSlideEvent());
    } else if (key.equals(PREF_SINGLE_TAP_EVENT)) {
      mFloatingBallView.setSingleTapFunctionListener(SingleDataManager.singleTapEvent());
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
    mFloatingBallView.setOpacity(SingleDataManager.opacity());
    mFloatingBallView.changeFloatBallSizeWithRadius(SingleDataManager.size());
    mFloatingBallView.setUseBackground(SingleDataManager.isUseBackground());
    mFloatingBallView.updateModelData();

    mFloatingBallView.requestLayout();
    mFloatingBallView.invalidate();

    /* Function */
    mFloatingBallView.setDoubleClickEventType(SingleDataManager.doubleClickEvent());
    mFloatingBallView.setLeftFunctionListener(SingleDataManager.leftSlideEvent());
    mFloatingBallView.setRightFunctionListener(SingleDataManager.rightSlideEvent());
    mFloatingBallView.setUpFunctionListener(SingleDataManager.upSlideEvent());
    mFloatingBallView.setDownFunctionListener(SingleDataManager.downSlideEvent());
    mFloatingBallView.setSingleTapFunctionListener(SingleDataManager.singleTapEvent());
  }

  /**
   * According to the state of input method, move the floatingBall view.
   */
  public void inputMethodDetect() {
    if (mFloatingBallView == null) {
      return;
    }

    if (InputMethodDetector.detectIsInputingWithHeight(100)) {
      onKeyboardShow();
    } else {
      onKeyboardDisappear();
    }
  }

  private void onKeyboardShow() {
    if (!isSoftKeyboardShow) {

      mFloatingBallView.moveToKeyboardTop();
    }
    isSoftKeyboardShow = true;
  }

  private void onKeyboardDisappear() {
    if (isSoftKeyboardShow) {
      mFloatingBallView.moveBackWhenKeyboardDisappear();
    }
    isSoftKeyboardShow = false;
  }


  public void recycleBitmapMemory() {
    if (mFloatingBallView != null) {
      mFloatingBallView.recycleBitmap();
    }
  }

  /**
   * screenshot时暂时隐藏
   */
  public void setBallViewVisibility(boolean isHide) {
    if (mFloatingBallView != null) {
      mFloatingBallView.setVisibility(isHide ? View.INVISIBLE : View.VISIBLE);
    }
  }


  public void updateBallViewLayout(int orientation) {
    if (mFloatingBallView != null) {
      if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        int x = SharedPrefsUtils.getIntegerPreference(PREF_PARAM_X_LANDSCAPE, gScreenWidth / 2);
        int y = SharedPrefsUtils.getIntegerPreference(PREF_PARAM_Y_LANDSCAPE, gScreenHeight / 2);

        mFloatingBallView.updateViewLayoutWithValue(x,y);

      } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        int x = SharedPrefsUtils.getIntegerPreference(PREF_PARAM_X_PORTRAIT, gScreenWidth / 2);
        int y = SharedPrefsUtils.getIntegerPreference(PREF_PARAM_Y_PORTRAIT, gScreenHeight / 2);

        mFloatingBallView.updateViewLayoutWithValue(x,y);
      }
    }
    }
  }
