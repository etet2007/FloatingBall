package com.chenyee.stephenlau.floatingball.floatingBall.gesture;

import android.content.Context;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.chenyee.stephenlau.floatingball.App;
import com.chenyee.stephenlau.floatingball.floatingBall.FloatingBallView;
import com.chenyee.stephenlau.floatingball.repository.BallSettingRepo;

/**
 * 只暴露了FloatingBall需要的接口：OnGestureEventListener
 */
public class GestureProcessor implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    private static final String TAG = "GestureProcessor";

    public static final int STATE_UP = 1;
    public static final int STATE_DOWN = 2;
    public static final int STATE_LEFT = 3;
    public static final int STATE_RIGHT = 4;
    public static final int STATE_NONE = 5;

    private int lastGestureState = STATE_NONE;
    private int currentGestureState;
    private boolean isLongPress = false;
    private boolean isFirstLongPressEvent = false;
    private boolean isScrolling = false;

    private boolean isUseDoubleClick = false;

    private float lastTouchEventPositionX;
    private float lastTouchEventPositionY;

    private FloatingBallView floatingBallView;

    private OnGestureEventListener onGestureEventListener;
    //GestureDetector处理双击事件
    private GestureDetector gestureDetector;

    private boolean isVibrate = true;
    private Vibrator vibrator;

    private int scrollGestureChangeCount;

    public GestureProcessor(FloatingBallView floatingBallView) {
        this.floatingBallView = floatingBallView;

        vibrator = (Vibrator) App.getApplication().getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void setIsVibrate(boolean isVibrate) {
        this.isVibrate = isVibrate;
    }

    public void updateFieldBySingleDataManager() {
        isVibrate = BallSettingRepo.isVibrate();
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    //单击抬起 没使用DoubleClick时使用
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (!isUseDoubleClick) {
            onGestureEventListener.onSingeTap();
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
            onGestureEventListener.onScrollStateChange(currentGestureState);
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
        onGestureEventListener.onSingeTap();
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        onGestureEventListener.onDoubleTap();
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    public void onTouchEvent(MotionEvent event) {
        //GestureDetector处理双击事件
        gestureDetector.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onGestureEventListener.onActionDown();

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
                    int x = (int) (event.getRawX() - lastTouchEventPositionX);
                    int y = (int) (event.getRawY() - lastTouchEventPositionY);

//                    Log.d(TAG, "onTouchEvent: lastTouchEventPosition" + lastTouchEventPositionX + "Y " + lastTouchEventPositionY);
//                    Log.d(TAG, "onTouchEvent: x " + x + "y " + y);
                    onGestureEventListener.onMove(x, y);
                }

                onGestureEventListener.onTouching(event);

                break;
            case MotionEvent.ACTION_UP:
                onGestureEventListener.onActionUp();

                //滑动操作
                if (isScrolling) {
                    onGestureEventListener.onScrollEnd();
                    if (scrollGestureChangeCount == 1) {
                        onFunctionWithCurrentGestureState(currentGestureState);
                    }
                    scrollGestureChangeCount = 0;

                    currentGestureState = STATE_NONE;
                    lastGestureState = STATE_NONE;
                    isScrolling = false;
                }

                if (isLongPress) {
                    // 长按结束回调
                    onGestureEventListener.onLongPressEnd();
                }
                isLongPress = false;
                isFirstLongPressEvent = false;
                break;
        }
    }

    private void onFunctionWithCurrentGestureState(int currentGestureState) {
        switch (currentGestureState) {
            case GestureProcessor.STATE_UP:
                onGestureEventListener.upGesture();
                break;
            case GestureProcessor.STATE_DOWN:
                onGestureEventListener.downGesture();
                break;
            case GestureProcessor.STATE_LEFT:
                onGestureEventListener.leftGesture();
                break;
            case GestureProcessor.STATE_RIGHT:
                onGestureEventListener.rightGesture();
                break;
            case GestureProcessor.STATE_NONE:
                break;
        }
    }

    public void setUseDoubleClick(boolean useDoubleClick) {
        isUseDoubleClick = useDoubleClick;

        if (isUseDoubleClick) {
            gestureDetector.setOnDoubleTapListener(this);
        } else {
            gestureDetector.setOnDoubleTapListener(null);
        }
    }

    public void setOnGestureEventListener(OnGestureEventListener onGestureEventListener) {
        this.onGestureEventListener = onGestureEventListener;
        if (onGestureEventListener != null) {
            gestureDetector = new GestureDetector(floatingBallView.getContext(), this);
        }
    }
}
