package com.chenyee.stephenlau.floatingball.floatingBall;

import static com.chenyee.stephenlau.floatingball.App.getApplication;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureListener.STATE_DOWN;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureListener.STATE_LEFT;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureListener.STATE_NONE;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureListener.STATE_RIGHT;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureListener.STATE_UP;
import static com.chenyee.stephenlau.floatingball.util.DimensionUtils.dip2px;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Keep;
import com.chenyee.stephenlau.floatingball.util.SingleDataManager;

@Keep
public class FloatingBallDrawer {

  private FloatingBallView view;
  private FloatingBallPaint floatingballPaint;

  public final float edge = dip2px(getApplication(), 4);
  public final float ballRadiusDeltaMaxInAnimation = 7;
  public final int scrollGestureMoveDistance = 18;

  public float ballRadius;
  public float backgroundRadius;

  public int measuredSideLength;

  public float ballCenterY = 0;
  public float ballCenterX = 0;


  private boolean useGrayBackground = true;

  public FloatingBallDrawer(FloatingBallView floatingBallView,
      FloatingBallPaint floatingballPaint) {
    this.view = floatingBallView;
    this.floatingballPaint = floatingballPaint;
  }

  public float getBallRadius() {
    return ballRadius;
  }

  public void setBallRadius(float ballRadius) {
    this.ballRadius = ballRadius;
  }

  public float getBallCenterY() {
    return ballCenterY;
  }

  public void setBallCenterY(float ballCenterY) {
    this.ballCenterY = ballCenterY;
  }

  public float getBallCenterX() {
    return ballCenterX;
  }

  public void setBallCenterX(float ballCenterX) {
    this.ballCenterX = ballCenterX;
  }


  public void setUseGrayBackground(boolean useGrayBackground) {
    this.useGrayBackground = useGrayBackground;
  }

  public void updateFieldBySingleDataManager() {
    useGrayBackground = SingleDataManager.isUseGrayBackground();
  }

  public void calculateBackgroundRadiusAndMeasureSideLength(int ballRadius) {
    this.ballRadius = ballRadius;

    backgroundRadius = ballRadius + edge;

    //View宽高 r + moveDistance + r在动画变大的值 = r + edge + gap
    int frameGap = (int) (scrollGestureMoveDistance + ballRadiusDeltaMaxInAnimation - edge);

    measuredSideLength = (int) (backgroundRadius + frameGap) * 2;
  }

  public void drawBallWithThisModel(Canvas canvas) {
    canvas.translate(measuredSideLength / 2, measuredSideLength / 2);
    Paint backgroundPaint = floatingballPaint.getBackgroundPaint();

    if (useGrayBackground) {
      canvas.drawCircle(0, 0, backgroundRadius, backgroundPaint);
    }

    Paint ballEmptyPaint = floatingballPaint.getBallEmptyPaint();
    Paint ballPaint = floatingballPaint.getBallPaint();

    canvas.drawCircle(ballCenterX, ballCenterY, ballRadius, ballEmptyPaint);

    canvas.drawCircle(ballCenterX, ballCenterY, ballRadius, ballPaint);
  }

  public void moveBallViewWithCurrentGestureState(int currentGestureState) {
    switch (currentGestureState) {
      case STATE_UP:
        setBallCenterX(0);
        setBallCenterY(-scrollGestureMoveDistance);
        break;
      case STATE_DOWN:
        setBallCenterX(0);
        setBallCenterY(scrollGestureMoveDistance);

        break;
      case STATE_LEFT:
        setBallCenterX(-scrollGestureMoveDistance);
        setBallCenterY(0);
        break;
      case STATE_RIGHT:
        setBallCenterX(scrollGestureMoveDistance);
        setBallCenterY(0);
        break;
      case STATE_NONE:
        setBallCenterX(0);
        setBallCenterY(0);
        break;
    }
    view.invalidate();
  }

}
