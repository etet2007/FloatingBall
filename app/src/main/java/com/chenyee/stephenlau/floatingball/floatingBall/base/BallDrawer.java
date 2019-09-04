package com.chenyee.stephenlau.floatingball.floatingBall.base;

import android.graphics.Canvas;

import com.chenyee.stephenlau.floatingball.repository.BallSettingRepo;

public abstract class BallDrawer {
    protected int measuredSideLength;

    public int getMeasuredSideLength() {
        return measuredSideLength;
    }

    protected static float ballRadius;

    public static float getBallRadius() {
        return ballRadius;
    }

    public void calculateBackgroundRadiusAndMeasureSideLength(int ballRadius) {
        BallDrawer.ballRadius = ballRadius;
    }

    protected BallPaint ballPaint;

    public BallDrawer(BallPaint ballPaint) {
        this.ballPaint = ballPaint;
    }

    public void setPaintAlpha(int userSetOpacity) {
        ballPaint.setPaintAlpha(userSetOpacity);
    }

    public void drawBallWithThisModel(Canvas canvas){
        canvas.translate(measuredSideLength / 2, measuredSideLength / 2);
    }
    abstract public void updateFieldBySingleDataManager();

    public abstract void moveBallViewWithCurrentGestureState(int currentGestureState);
}