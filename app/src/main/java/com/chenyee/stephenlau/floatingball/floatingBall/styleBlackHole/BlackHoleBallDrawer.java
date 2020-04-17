package com.chenyee.stephenlau.floatingball.floatingBall.styleBlackHole;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.support.annotation.Keep;

import com.chenyee.stephenlau.floatingball.floatingBall.FloatingBallView;
import com.chenyee.stephenlau.floatingball.floatingBall.base.BallDrawer;
import com.chenyee.stephenlau.floatingball.floatingBall.styleGradient.GradientBallPaint;

import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.GestureProcessor.STATE_DOWN;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.GestureProcessor.STATE_LEFT;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.GestureProcessor.STATE_NONE;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.GestureProcessor.STATE_RIGHT;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.GestureProcessor.STATE_UP;

@Keep
public class BlackHoleBallDrawer extends BallDrawer {

    private static final String TAG = "BlackHoleBallDrawer";

    private Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RadialGradient radialGradient;

    public BlackHoleBallDrawer(FloatingBallView view) {
        super(view);
    }

    @Override
    public void setPaintAlpha(int userSetOpacity) {
        backgroundPaint.setAlpha(userSetOpacity);

    }

    @Override
    public void calculateBackgroundRadiusAndMeasureSideLength(float ballRadius) {
        measuredSideLength = (int) (ballRadius * 1.2 + 5) * 2 ;

        if (ballRadius > 0) {
            radialGradient = new RadialGradient(
                    0, 0,
                    ballRadius,
                    new int[]{
                            Color.parseColor("#971000"),
                            Color.parseColor("#F56700")
                    },
                    new float[]{0f, 1f},
                    Shader.TileMode.CLAMP);

            backgroundPaint.setShader(radialGradient);
        }
    }

    @Override
    public void drawBallWithThisModel(Canvas canvas) {
        super.drawBallWithThisModel(canvas);

    }

    @Override
    public void updateFieldBySingleDataManager() {

    }

    @Override
    public void moveBallViewWithCurrentGestureState(int currentGestureState) {


        view.invalidate();
    }

}
