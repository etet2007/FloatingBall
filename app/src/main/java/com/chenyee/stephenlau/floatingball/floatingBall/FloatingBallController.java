package com.chenyee.stephenlau.floatingball.floatingBall;

import android.content.Context;
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
import java.util.ArrayList;

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
//  private FloatingBallView floatingBallView;
  private ArrayList<FloatingBallView> floatingBallViewList = new ArrayList<>();

  private boolean isSoftKeyboardShow = false;
  private boolean isStartedBallView = false;
  private WindowManager windowManager;


  private FloatingBallController() {
  }

  public static FloatingBallController getInstance() {
    return sFloatingBallController;
  }

  /**
   * new floatingBallView and add to WindowManager
   */
  public void startBallView(Context context) {
    if (isStartedBallView) {//已经开启
      return;
    }

    windowManager = (WindowManager) App.getApplication().getApplicationContext()
        .getSystemService(Context.WINDOW_SERVICE);

    int amount = SingleDataManager.amount();
    for (int id = 0; id < amount; id++) {
      addFloatingBallView(context, id);
    }

    updateParameter();

    // init FunctionInterfaceUtils 确保每一次都初始化成功，只有add才能保证每一次都执行成功
    FunctionInterfaceUtils.sFloatingBallService = (FloatingBallService) context;

    isStartedBallView = true;
    SharedPrefsUtils.setBooleanPreference(PREF_IS_ADDED_BALL_IN_SETTING, true);
  }

  public void addFloatingBallView(Context context, int id) {
    if (windowManager == null) {
      throw new NullPointerException();
    }
    FloatingBallView floatingBallView = new FloatingBallView(context, id);
    floatingBallViewList.add(floatingBallView);

    // use code to initialize layout parameters
    LayoutParams params = new LayoutParams();
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
    floatingBallView.setBallViewLayoutParams(params);
    windowManager.addView(floatingBallView, params);

    updateParameter();
  }

  public void removeBallView() {
    SingleDataManager.setIsAddedBallInSetting(false);
    removeAll();
  }

  public void rotateHideBallView() {
    SingleDataManager.setIsBallHideBecauseRotate(true);
    removeAll();
  }

  public void removeLastFloatingBall() {
    if (floatingBallViewList.size() > 1) {//最小数量为1
      FloatingBallView floatingBallView = floatingBallViewList.get(floatingBallViewList.size() - 1);
      floatingBallView.removeBallWithAnimation();
      floatingBallViewList.remove(floatingBallView);
    }
  }

  private void removeAll() {
    isStartedBallView = false;
    for (FloatingBallView floatingBallView : floatingBallViewList) {
        floatingBallView.removeBallWithAnimation();
    }
    floatingBallViewList.clear();
  }


  /**
   * 设置背景图 复制外部路径的图片到目录中去，更新bitmapRead，再进行裁剪
   *
   * @param imagePath 外部图片地址
   */
  public void setBackgroundImage(String imagePath) {
    for (FloatingBallView floatingBallView : floatingBallViewList) {
      BitmapUtils.copyBackgroundImage(imagePath);
      floatingBallView.refreshBitmapRead();
      floatingBallView.createBitmapCropFromBitmapRead();
      floatingBallView.invalidate();
    }
  }

  /**
   * No use
   */
  public void updateSpecificParameter(String key) {
    for (FloatingBallView floatingBallView : floatingBallViewList) {

      switch (key) {
        case PREF_OPACITY:
          floatingBallView.setOpacity(SingleDataManager.opacity());
          break;
        case PREF_OPACITY_MODE:
          floatingBallView.setOpacityMode(SharedPrefsUtils.getIntegerPreference(PREF_OPACITY_MODE, OPACITY_NONE));
          break;
        case PREF_SIZE:
          floatingBallView.changeFloatBallSizeWithRadius(SingleDataManager.size());
          break;
        case PREF_USE_BACKGROUND:
          floatingBallView.setUseBackground(SingleDataManager.isUseBackground());
          break;
        case PREF_USE_GRAY_BACKGROUND:
        case PREF_IS_VIBRATE:
        case PREF_MOVE_UP_DISTANCE:
          floatingBallView.updateModelData();
          break;
      }
      //refresh the layout and draw the view
      floatingBallView.requestLayout();
      floatingBallView.invalidate();

      if (key.equals(PREF_DOUBLE_CLICK_EVENT)) {
        floatingBallView.setDoubleClickEventType(SingleDataManager.doubleClickEvent());
      } else if (key.equals(PREF_LEFT_SLIDE_EVENT)) {
        floatingBallView.setLeftFunctionListener(SingleDataManager.leftSlideEvent());
      } else if (key.equals(PREF_RIGHT_SLIDE_EVENT)) {
        floatingBallView.setRightFunctionListener(SingleDataManager.rightSlideEvent());
      } else if (key.equals(PREF_UP_SLIDE_EVENT)) {
        floatingBallView.setUpFunctionListener(SingleDataManager.upSlideEvent());
      } else if (key.equals(PREF_DOWN_SLIDE_EVENT)) {
        floatingBallView.setDownFunctionListener(SingleDataManager.downSlideEvent());
      } else if (key.equals(PREF_SINGLE_TAP_EVENT)) {
        floatingBallView.setSingleTapFunctionListener(SingleDataManager.singleTapEvent());
      }
    }
  }

  /**
   * Use sharedPreference data to update all parameter
   */
  private void updateParameter() {
    for (FloatingBallView floatingBallView : floatingBallViewList) {

      floatingBallView.updateLayoutParamsWithOrientation();
      /* View */
      floatingBallView.setOpacity(SingleDataManager.opacity());
      floatingBallView.setOpacityMode(SharedPrefsUtils.getIntegerPreference(PREF_OPACITY_MODE, OPACITY_NONE));
      floatingBallView.changeFloatBallSizeWithRadius(SingleDataManager.size());
      floatingBallView.setUseBackground(SingleDataManager.isUseBackground());

      floatingBallView.updateModelData();

      floatingBallView.requestLayout();
      floatingBallView.invalidate();

      /* Function */
      floatingBallView.setDoubleClickEventType(SingleDataManager.doubleClickEvent());
      floatingBallView.setLeftFunctionListener(SingleDataManager.leftSlideEvent());
      floatingBallView.setRightFunctionListener(SingleDataManager.rightSlideEvent());
      floatingBallView.setUpFunctionListener(SingleDataManager.upSlideEvent());
      floatingBallView.setDownFunctionListener(SingleDataManager.downSlideEvent());
      floatingBallView.setSingleTapFunctionListener(SingleDataManager.singleTapEvent());
    }
  }

  /**
   * According to the state of input method, move the floatingBall view.
   */
  public void inputMethodDetect() {

    if (InputMethodDetector.detectIsInputingWithHeight(100)) {
      onKeyboardShow();
    } else {
      onKeyboardDisappear();
    }
  }

  private void onKeyboardShow() {
    if (!isSoftKeyboardShow) {
      for (FloatingBallView floatingBallView : floatingBallViewList) {
        floatingBallView.moveToKeyboardTop();
      }
    }
    isSoftKeyboardShow = true;
  }

  private void onKeyboardDisappear() {
    if (isSoftKeyboardShow) {
      for (FloatingBallView floatingBallView : floatingBallViewList) {
        floatingBallView.moveBackWhenKeyboardDisappear();
      }
    }
    isSoftKeyboardShow = false;
  }


  public void recycleBitmapMemory() {
    for (FloatingBallView floatingBallView : floatingBallViewList) {
      floatingBallView.recycleBitmap();
    }
  }

  /**
   * screenshot时暂时隐藏
   */
  public void setBallViewVisibility(boolean isHide) {
    for (FloatingBallView floatingBallView : floatingBallViewList) {
      floatingBallView.setVisibility(isHide ? View.INVISIBLE : View.VISIBLE);
    }
  }


  public void updateBallViewLayout() {
    for (FloatingBallView floatingBallView : floatingBallViewList) {
      floatingBallView.updateLayoutParamsWithOrientation();
    }
  }


}
