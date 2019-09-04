package com.chenyee.stephenlau.floatingball.floatingBall.styleGradient;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.chenyee.stephenlau.floatingball.floatingBall.FloatingBallView;
import com.chenyee.stephenlau.floatingball.floatingBall.base.BallDrawer;

import static com.chenyee.stephenlau.floatingball.App.getApplication;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureProcessor.STATE_DOWN;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureProcessor.STATE_LEFT;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureProcessor.STATE_NONE;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureProcessor.STATE_RIGHT;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureProcessor.STATE_UP;
import static com.chenyee.stephenlau.floatingball.util.DimensionUtils.dip2px;

public class GradientBallDrawer extends BallDrawer {

    public static final float padding = dip2px(getApplication(), 2);
    private GradientBallPaint gradientBallPaint;
    private FloatingBallView view;

    public GradientBallDrawer(FloatingBallView view) {
        super(new GradientBallPaint());
        gradientBallPaint = (GradientBallPaint) ballPaint;
        this.view = view;
    }

    @Override
    public void calculateBackgroundRadiusAndMeasureSideLength(int ballRadius) {
        super.calculateBackgroundRadiusAndMeasureSideLength(ballRadius);
        measuredSideLength = (int) (ballRadius + padding) * 2;

        gradientBallPaint.refreshPaint();
    }

    @Override
    public void drawBallWithThisModel(Canvas canvas) {
        super.drawBallWithThisModel(canvas);

        Paint grayBackgroundPaint = gradientBallPaint.getBackgroundPaint();
        canvas.drawCircle(0, 0, ballRadius, grayBackgroundPaint);
    }

    @Override
    public void updateFieldBySingleDataManager() {

    }

    @Override
    public void moveBallViewWithCurrentGestureState(int currentGestureState) {
        switch (currentGestureState) {
            case STATE_UP:
                gradientBallPaint.traslate(0, -ballRadius * 0.8f);
                break;
            case STATE_DOWN:
                gradientBallPaint.traslate(0, ballRadius * 0.8f);
                break;
            case STATE_LEFT:
                gradientBallPaint.traslate(-ballRadius * 0.8f,0);
                break;
            case STATE_RIGHT:
                gradientBallPaint.traslate(ballRadius * 0.8f,0);
                break;
            case STATE_NONE:
                gradientBallPaint.traslate(0,0);
                break;
        }
        view.invalidate();
    }
}
