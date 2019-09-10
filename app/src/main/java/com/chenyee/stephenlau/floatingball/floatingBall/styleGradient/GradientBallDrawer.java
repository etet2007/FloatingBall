package com.chenyee.stephenlau.floatingball.floatingBall.styleGradient;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Keep;
import android.util.Log;

import com.chenyee.stephenlau.floatingball.floatingBall.FloatingBallView;
import com.chenyee.stephenlau.floatingball.floatingBall.base.BallDrawer;

import static com.chenyee.stephenlau.floatingball.App.getApplication;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureProcessor.STATE_DOWN;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureProcessor.STATE_LEFT;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureProcessor.STATE_NONE;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureProcessor.STATE_RIGHT;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureProcessor.STATE_UP;
import static com.chenyee.stephenlau.floatingball.util.DimensionUtils.dip2px;
@Keep
public class GradientBallDrawer extends BallDrawer {

    public static final float padding = dip2px(getApplication(), 2);
    private static final String TAG = "GradientBallDrawer";
    private GradientBallPaint gradientBallPaint;
    private FloatingBallView view;


    public float getDx() {
        return dx;
    }

    public float getDy() {
        return dy;
    }

    private float dx;
    private float dy;

    public GradientBallDrawer(FloatingBallView view) {
        super(new GradientBallPaint());
        gradientBallPaint = (GradientBallPaint) ballPaint;
        this.view = view;
    }

    @Override
    public void calculateBackgroundRadiusAndMeasureSideLength(float ballRadius) {
        super.calculateBackgroundRadiusAndMeasureSideLength(ballRadius);
        measuredSideLength = (int) (ballRadius * 2 * 1.25);

        gradientBallPaint.refreshPaint(ballRadius);
    }

    @Override
    public void drawBallWithThisModel(Canvas canvas) {
        super.drawBallWithThisModel(canvas);
        Log.d(TAG, "lqt drawBallWithThisModel: ballRadius " + ballRadius);
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
                dx = 0;
                dy = -ballRadius * 0.8f;
                break;
            case STATE_DOWN:
                dx = 0;
                dy = ballRadius * 0.8f;
                break;
            case STATE_LEFT:
                dx = -ballRadius * 0.8f;
                dy = 0;
                break;
            case STATE_RIGHT:
                dx = ballRadius * 0.8f;
                dy = 0;
                break;
            case STATE_NONE:
                dx = 0;
                dy = 0;
                break;
        }
        gradientBallPaint.translate(dx, dy);

        view.invalidate();
    }

    public void setDx(float dx) {
        this.dx = dx;
        gradientBallPaint.translate(dx,dy);
    }

    public void setDy(float dy) {
        this.dy = dy;
        gradientBallPaint.translate(dx,dy);
    }

}
