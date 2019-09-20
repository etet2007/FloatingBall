package com.chenyee.stephenlau.floatingball.floatingBall.base;
import android.graphics.Canvas;

import com.chenyee.stephenlau.floatingball.floatingBall.FloatingBallView;

public abstract class BallDrawer {
    protected FloatingBallView view;
    protected int measuredSideLength;

    public int getMeasuredSideLength() {
        return measuredSideLength;
    }

    public abstract void calculateBackgroundRadiusAndMeasureSideLength(float ballRadius);

    public BallDrawer(FloatingBallView view) {
        this.view = view;
    }

    public abstract void setPaintAlpha(int userSetOpacity);

    public void drawBallWithThisModel(Canvas canvas){
        canvas.translate(measuredSideLength / 2, measuredSideLength / 2);
    }
    abstract public void updateFieldBySingleDataManager();

    public abstract void moveBallViewWithCurrentGestureState(int currentGestureState);
}