package com.chenyee.stephenlau.floatingball.floatingBall;

import static com.chenyee.stephenlau.floatingball.App.getApplication;
import static com.chenyee.stephenlau.floatingball.util.DimensionUtils.dip2px;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.chenyee.stephenlau.floatingball.util.SingleDataManager;

public class FloatingBallDrawer {

  private FloatingBallPaint floatingballPaint;

  public final float edge = dip2px(getApplication(), 4);
  public final float ballRadiusDeltaMaxInAnimation = 7;
  public final int scrollGestureMoveDistance = 18;

  public float ballRadius;
  public float backgroundRadius;

  public int measuredSideLength;

  public float ballCenterY = 0;
  public float ballCenterX = 0;

  public int moveUpDistance;

  private boolean useGrayBackground = true;

  public FloatingBallDrawer(FloatingBallPaint floatingballPaint) {
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


  public void setMoveUpDistance(int moveUpDistance) {
    this.moveUpDistance = moveUpDistance;
  }

  public void setUseGrayBackground(boolean useGrayBackground) {
    this.useGrayBackground = useGrayBackground;
  }

  public void updateFieldBySingleDataManager() {
    useGrayBackground = SingleDataManager.isUseGrayBackground();
    moveUpDistance = SingleDataManager.moveUpDistance();
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



}
