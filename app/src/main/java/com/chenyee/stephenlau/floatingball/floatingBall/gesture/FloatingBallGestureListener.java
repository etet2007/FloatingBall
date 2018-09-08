package com.chenyee.stephenlau.floatingball.floatingBall.gesture;

import static com.chenyee.stephenlau.floatingball.App.gScreenHeight;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.OPACITY_REDUCE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_PARAM_X;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_PARAM_Y;

import android.content.Context;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import com.chenyee.stephenlau.floatingball.App;
import com.chenyee.stephenlau.floatingball.floatingBall.FloatingBallView;
import com.chenyee.stephenlau.floatingball.util.SharedPrefsUtils;
import com.chenyee.stephenlau.floatingball.util.SingleDataManager;


public class FloatingBallGestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

  private FloatingBallView floatingBallView;

  public static final int STATE_UP = 1;
  public static final int STATE_DOWN = 2;
  public static final int STATE_LEFT = 3;
  public static final int STATE_RIGHT = 4;
  public static final int STATE_NONE = 5;


  private int currentGestureState;
  public int lastGestureState = STATE_NONE;

  private boolean isLongPress = false;
  private boolean isFirstLongPressEvent = false;
  private boolean isScrolling = false;

  private float lastTouchEventPositionX;
  private float lastTouchEventPositionY;

  private OnGestureEventListener onGestureEventListener;

  private boolean isVibrate = true;

  private Vibrator vibrator;
  private int scrollGestureChangeCount;

  public void setOnGestureEventListener(
      OnGestureEventListener onGestureEventListener) {
    this.onGestureEventListener = onGestureEventListener;
  }

  public void setIsVibrate(boolean isVibrate) {
    this.isVibrate = isVibrate;
  }

  public FloatingBallGestureListener(FloatingBallView floatingBallView) {
    this.floatingBallView = floatingBallView;
    vibrator = (Vibrator) App.getApplication().getSystemService(Context.VIBRATOR_SERVICE);

  }

  public void updateFieldBySingleDataManager() {
    isVibrate = SingleDataManager.isVibrate();
  }

  //GestureDetector.OnGestureListener

  @Override
  public boolean onDown(MotionEvent e) {
    return false;
  }

  @Override
  public void onShowPress(MotionEvent e) {

  }

  //单击抬起
  @Override
  public boolean onSingleTapUp(MotionEvent e) {
    if (!floatingBallView.isUseDoubleClick) {
      if (floatingBallView.singleTapFunctionListener != null) {
        floatingBallView.singleTapFunctionListener.onFunction();
      }

    }
    return false;
  }

  @Override
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    isScrolling = true;

    float firstScrollX = e1.getX();
    float firstScrollY = e1.getY();

    float lastScrollX = e2.getX();
    float lastScrollY = e2.getY();

    float deltaX = lastScrollX - firstScrollX;
    float deltaY = lastScrollY - firstScrollY;

    double angle = Math.atan2(deltaY, deltaX);
    //判断currentGestureSTATE
    if (angle > -Math.PI / 4 && angle < Math.PI / 4) {
      currentGestureState = STATE_RIGHT;
    } else if (angle > Math.PI / 4 && angle < Math.PI * 3 / 4) {
      currentGestureState = STATE_DOWN;
    } else if (angle > -Math.PI * 3 / 4 && angle < -Math.PI / 4) {
      currentGestureState = STATE_UP;
    } else {
      currentGestureState = STATE_LEFT;
    }
    if (currentGestureState != lastGestureState) {
      scrollGestureChangeCount++;
      floatingBallView.getFloatingBallDrawer().moveBallViewWithCurrentGestureState(currentGestureState);
      lastGestureState = currentGestureState;
    }
    return false;
  }

  @Override
  public void onLongPress(MotionEvent e) {
    if (isVibrate) {
      long[] pattern = {0, 70};
      vibrator.vibrate(pattern, -1);
    }
    isLongPress = true;
  }

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    return false;
  }

  //GestureDetector.OnDoubleTapListener

  @Override
  public boolean onSingleTapConfirmed(MotionEvent e) {
    if (floatingBallView.singleTapFunctionListener != null) {
      floatingBallView.singleTapFunctionListener.onFunction();
    }
    return false;
  }

  @Override
  public boolean onDoubleTap(MotionEvent e) {
    if (floatingBallView.doubleTapFunctionListener != null) {
      floatingBallView.doubleTapFunctionListener.onFunction();
    }
    return false;
  }

  @Override
  public boolean onDoubleTapEvent(MotionEvent e) {
    return false;
  }


  public void processEvent(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        if (onGestureEventListener != null) {
          onGestureEventListener.onActionDown();
        }

      case MotionEvent.ACTION_MOVE:
        // 长按移动模式
        if (isLongPress) {
          //getX()、getY()返回的则是触摸点相对于View的位置。
          //getRawX()、getRawY()返回的是触摸点相对于屏幕的位置
          if (!isFirstLongPressEvent) {
            isFirstLongPressEvent = true;
            lastTouchEventPositionX = event.getX();
            lastTouchEventPositionY = event.getY();
          }
          LayoutParams ballViewLayoutParams = new LayoutParams();
          ballViewLayoutParams.x = (int) (event.getRawX() - lastTouchEventPositionX);
          ballViewLayoutParams.y = (int) (event.getRawY() - lastTouchEventPositionY);

          SharedPrefsUtils.setIntegerPreference(PREF_PARAM_X, ballViewLayoutParams.x);
          SharedPrefsUtils.setIntegerPreference(PREF_PARAM_Y, ballViewLayoutParams.y);
          floatingBallView.updateViewLayout(ballViewLayoutParams);
        }
        break;
      case MotionEvent.ACTION_UP:
        if (onGestureEventListener != null) {
          onGestureEventListener.onActionUp();
        }

        //滑动操作
        if (isScrolling) {
          if (scrollGestureChangeCount == 1) {
            if (onGestureEventListener != null) {
              onGestureEventListener.onFunctionWithCurrentGestureState(currentGestureState);
            }
          }
          scrollGestureChangeCount = 0;

          if (onGestureEventListener != null) {
            onGestureEventListener.onScrollEnd();
          }
          currentGestureState = STATE_NONE;
          lastGestureState = STATE_NONE;
          isScrolling = false;
        }

        if (isLongPress) {
          // 长按结束回调
          if (onGestureEventListener != null) {
            onGestureEventListener.onLongPressEnd();
          }

        }
        isLongPress = false;
        isFirstLongPressEvent = false;
        break;
    }
  }
}
