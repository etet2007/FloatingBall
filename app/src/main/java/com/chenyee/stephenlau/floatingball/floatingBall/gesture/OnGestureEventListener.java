package com.chenyee.stephenlau.floatingball.floatingBall.gesture;

public interface OnGestureEventListener {
  void onActionDown();
  void onActionUp();
  void onScrollEnd();
  void onLongPressEnd();
  void onFunctionWithCurrentGestureState(int currentGestureState);
}
