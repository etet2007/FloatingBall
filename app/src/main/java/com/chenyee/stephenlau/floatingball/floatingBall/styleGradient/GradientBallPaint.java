package com.chenyee.stephenlau.floatingball.floatingBall.styleGradient;

import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.Log;

import com.chenyee.stephenlau.floatingball.floatingBall.base.BallDrawer;
import com.chenyee.stephenlau.floatingball.floatingBall.base.BallPaint;

import static com.chenyee.stephenlau.floatingball.App.getApplication;
import static com.chenyee.stephenlau.floatingball.util.DimensionUtils.dip2px;

public class GradientBallPaint implements BallPaint {

    private static final String TAG = "GradientBallPaint";
    private Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Matrix matrix;
    private RadialGradient radialGradient;

    public GradientBallPaint() {
        backgroundPaint.setColor(Color.GRAY);
        backgroundPaint.setMaskFilter(new BlurMaskFilter(5, BlurMaskFilter.Blur.SOLID));

        matrix = new Matrix();
    }

    public Paint getBackgroundPaint() {
        return backgroundPaint;
    }

    @Override
    public void setPaintAlpha(int opacity) {
        backgroundPaint.setAlpha(opacity);
    }

    public void refreshPaint() {
        radialGradient = new RadialGradient(
                0, 0,
                BallDrawer.getBallRadius(),
                new int[]{
                        Color.parseColor("#355C7D"),
                        Color.parseColor("#F67280")
                },
                new float[]{0.3f, 1f},
                Shader.TileMode.CLAMP);

        backgroundPaint.setShader(radialGradient);
    }

    public void translate(float dx, float dy) {
        matrix.setTranslate(dx,dy);
        radialGradient.setLocalMatrix(matrix);
    }

}
