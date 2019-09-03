package com.chenyee.stephenlau.floatingball.floatingBall;

import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.support.annotation.Keep;

@Keep
public class FloatingBallPaint {

    private Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ballPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ballEmptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public FloatingBallPaint() {
        backgroundPaint.setColor(Color.GRAY);
        backgroundPaint.setAlpha(80);
        backgroundPaint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.SOLID));

        ballPaint.setFilterBitmap(true);
        ballPaint.setColor(Color.WHITE);
        ballPaint.setAlpha(150);

        ballEmptyPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        backgroundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

//        RadialGradient radialGradient = new RadialGradient(
//                0, 0,
//                FloatingBallDrawer.ballRadius + FloatingBallDrawer.greyBackgroundLength,
//                Color.parseColor("#E91E63"),
//                Color.parseColor("#2196F3"),
//                Shader.TileMode.CLAMP);
//
//        backgroundPaint.setShader(radialGradient);
    }

    public void setPaintAlpha(int opacity) {
        backgroundPaint.setAlpha(opacity);
        ballPaint.setAlpha(opacity);
    }

    public int getOpacity() {
        return backgroundPaint.getAlpha();
    }

    public Paint getGrayBackgroundPaint() {
        return backgroundPaint;
    }

    public Paint getBallPaint() {
        return ballPaint;
    }

    public Paint getBallEmptyPaint() {
        return ballEmptyPaint;
    }

}
