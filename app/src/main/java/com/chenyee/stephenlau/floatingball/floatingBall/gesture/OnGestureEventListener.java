package com.chenyee.stephenlau.floatingball.floatingBall.gesture;

import android.view.MotionEvent;

public interface OnGestureEventListener {
  void onActionDown();
  void onActionUp();

  void onMove(int x,int y);

  void onScrollEnd();
  void onLongPressEnd();

  void onSingeTap();
  void onDoubleTap();

  void upGesture();
  void downGesture();
  void leftGesture();
  void rightGesture();

  void onScrollStateChange(int currentGestureState);

  void onTouching(MotionEvent event);
}
