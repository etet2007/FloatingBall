package com.chenyee.stephenlau.floatingball.floatingBall.styleGradient;

import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import com.chenyee.stephenlau.floatingball.floatingBall.base.BallPaint;

public class GradientBallPaint implements BallPaint {

    private static final String TAG = "GradientBallPaint";
    private Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Matrix matrix;
    private RadialGradient radialGradient;

    public GradientBallPaint() {
        backgroundPaint.setColor(Color.GRAY);
        backgroundPaint.setMaskFilter(new BlurMaskFilter(5, BlurMaskFilter.Blur.SOLID));
//        backgroundPaint.setShadowLayer(5,0,0,Color.BLACK);

        matrix = new Matrix();
    }

    public Paint getBackgroundPaint() {
        return backgroundPaint;
    }

    @Override
    public void setPaintAlpha(int opacity) {
        backgroundPaint.setAlpha(opacity);
    }

    public void refreshPaint(float ballRadius) {
        if (ballRadius > 0) {
            radialGradient = new RadialGradient(
                    0, 0,
                    ballRadius,
                    new int[]{
                            Color.parseColor("#355C7D"),
                            Color.parseColor("#F67280")
                    },
                    new float[]{0f, 1f},
                    Shader.TileMode.CLAMP);
//            radialGradient = new RadialGradient(
//                    0, 0,
//                    ballRadius,
//                    new int[]{
//                            Color.parseColor("#00FFA0"),
//                            Color.parseColor("#40007E")
//                    },
//                    new float[]{0f, 1f},
//                    Shader.TileMode.CLAMP);

            backgroundPaint.setShader(radialGradient);
        }
    }

    public void translate(float dx, float dy) {
        matrix.setTranslate(dx,dy);
        radialGradient.setLocalMatrix(matrix);
    }

}
