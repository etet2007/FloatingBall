package com.chenyee.stephenlau.floatingball.floatingBall;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Keep;

import com.chenyee.stephenlau.floatingball.repository.BallSettingRepo;

import static com.chenyee.stephenlau.floatingball.App.getApplication;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureListener.STATE_DOWN;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureListener.STATE_LEFT;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureListener.STATE_NONE;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureListener.STATE_RIGHT;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureListener.STATE_UP;
import static com.chenyee.stephenlau.floatingball.util.DimensionUtils.dip2px;

@Keep
public class FloatingBallDrawer {

    //灰色背景长度
    public final float greyBackgroundLength = dip2px(getApplication(), 4);
    public final float ballRadiusDeltaMaxInAnimation = 7;
    public final int scrollGestureMoveDistance = 18;
    public float ballRadius;
    public float grayBackgroundRadius;
    public int measuredSideLength;
    public float ballCenterY = 0;
    public float ballCenterX = 0;
    private RectF ballRect = new RectF();
    private FloatingBallView view;
    private FloatingBallPaint floatingballPaint;
    private boolean useGrayBackground = true;

    public FloatingBallDrawer(FloatingBallView view,
                              FloatingBallPaint floatingballPaint) {
        this.view = view;
        this.floatingballPaint = floatingballPaint;
    }

    public RectF getBallRect() {
        ballRect.set(ballCenterX - ballRadius,
                ballCenterY - ballRadius,
                ballCenterX + ballRadius,
                ballCenterY + ballRadius
        );
        return ballRect;
    }

    public void drawBallWithThisModel(Canvas canvas) {
        canvas.translate(measuredSideLength / 2, measuredSideLength / 2);
        Paint grayBackgroundPaint = floatingballPaint.getBackgroundPaint();

        if (useGrayBackground) {
            //            canvas.drawCircle(0, 0, grayBackgroundRadius, floatingballPaint.getShadowPaint());
            canvas.drawCircle(0, 0, grayBackgroundRadius, grayBackgroundPaint);
        }

        Paint ballEmptyPaint = floatingballPaint.getBallEmptyPaint();
        Paint ballPaint = floatingballPaint.getBallPaint();

        canvas.drawCircle(ballCenterX, ballCenterY, ballRadius, ballEmptyPaint);

        canvas.drawCircle(ballCenterX, ballCenterY, ballRadius, ballPaint);
    }

    public void calculateBackgroundRadiusAndMeasureSideLength(int ballRadius) {
        this.ballRadius = ballRadius;

        grayBackgroundRadius = ballRadius + greyBackgroundLength;

        //r + moveDistance + r在动画变大的值 = r + greyBackgroundLength + gap
        int frameGap = (int) (scrollGestureMoveDistance + ballRadiusDeltaMaxInAnimation - greyBackgroundLength);

        measuredSideLength = (int) (grayBackgroundRadius + frameGap) * 2;
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
        useGrayBackground = BallSettingRepo.isUseGrayBackground();
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
