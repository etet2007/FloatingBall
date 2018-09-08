package com.chenyee.stephenlau.floatingball.floatingBall.gesture;

public interface OnGestureEventListener {

  public void onActionDown();
  public void onActionUp();
  public void onScrollEnd();
  public void onLongPressEnd();
  public void onFunctionWithCurrentGestureState(int currentGestureState);
}
