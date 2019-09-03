package com.chenyee.stephenlau.floatingball.floatingBall;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.Keep;

import com.chenyee.stephenlau.floatingball.App;
import com.chenyee.stephenlau.floatingball.R;
import com.chenyee.stephenlau.floatingball.repository.BallSettingRepo;

import static com.chenyee.stephenlau.floatingball.App.getApplication;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureProcessor.STATE_DOWN;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureProcessor.STATE_LEFT;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureProcessor.STATE_NONE;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureProcessor.STATE_RIGHT;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureProcessor.STATE_UP;
import static com.chenyee.stephenlau.floatingball.util.DimensionUtils.dip2px;

@Keep
public class FloatingBallDrawer {

    //灰色背景长度
    public static final float greyBackgroundLength = dip2px(getApplication(), 4);
    public static float ballRadius;
    public final float ballRadiusDeltaMaxInAnimation = dip2px(getApplication(), 2.5f);
    private final int scrollGestureMoveDistance = dip2px(getApplication(), 6.6f);//18
    public int measuredSideLength;
    private float grayBackgroundRadius;
    private float ballCenterY = 0;
    private float ballCenterX = 0;
    private RectF ballRect = new RectF();
    private FloatingBallView view;
    private FloatingBallPaint floatingballPaint;
    private boolean useGrayBackground = true;

    private BackgroundImageHelper backgroundImageHelper;

    public FloatingBallDrawer(FloatingBallView view,
                              FloatingBallPaint floatingballPaint) {
        this.view = view;
        this.floatingballPaint = floatingballPaint;
        backgroundImageHelper = new BackgroundImageHelper();
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
        Paint grayBackgroundPaint = floatingballPaint.getGrayBackgroundPaint();

        if (useGrayBackground) {
            //            canvas.drawCircle(0, 0, grayBackgroundRadius, floatingballPaint.getShadowPaint());
            canvas.drawCircle(0, 0, grayBackgroundRadius, grayBackgroundPaint);
        }

        Paint ballEmptyPaint = floatingballPaint.getBallEmptyPaint();
        Paint ballPaint = floatingballPaint.getBallPaint();

        canvas.drawCircle(ballCenterX, ballCenterY, ballRadius, ballEmptyPaint);

        canvas.drawCircle(ballCenterX, ballCenterY, ballRadius, ballPaint);

        if (BackgroundImageHelper.isUseBackgroundImage()) {
            canvas.drawBitmap(BackgroundImageHelper.bitmapScaledCrop, null, getBallRect(), ballPaint);
        }
    }

    public void calculateBackgroundRadiusAndMeasureSideLength(int ballRadius) {
        this.ballRadius = ballRadius;

        grayBackgroundRadius = ballRadius + greyBackgroundLength;

        //r + moveDistance + r在动画变大的值 = r + greyBackgroundLength + gap
        int frameGap = (int) (scrollGestureMoveDistance + ballRadiusDeltaMaxInAnimation - greyBackgroundLength);

        measuredSideLength = (int) (grayBackgroundRadius + frameGap) * 2;

        if (BackgroundImageHelper.isUseBackgroundImage()) {
            BackgroundImageHelper.createBitmapCropFromBitmapRead();
        }
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

    public static class BackgroundImageHelper {
        //background image
        private static Bitmap bitmapRead;
        private static Bitmap bitmapScaledCrop;
        //标志位
        private static boolean useBackgroundImage = false;

        public static boolean isUseBackgroundImage() {
            return useBackgroundImage;
        }

        public static void setUseBackgroundImage(boolean useBackgroundImage) {
            if (useBackgroundImage) {
                setBitmapRead();
                createBitmapCropFromBitmapRead();
            } else {
                recycleBitmap();
            }

            BackgroundImageHelper.useBackgroundImage = useBackgroundImage;
        }

        public static void recycleBitmap() {
            if (bitmapRead != null && !bitmapRead.isRecycled()) {
                bitmapRead.recycle();
            }
            if (!useBackgroundImage && bitmapScaledCrop != null && !bitmapScaledCrop.isRecycled()) {
                bitmapScaledCrop.recycle();
            }
        }

        /**
         * 更新bitmapRead的值
         */
        public static void setBitmapRead() {
            //path为app内部目录
            String path = App.getApplication().getFilesDir().toString();
            bitmapRead = BitmapFactory.decodeFile(path + "/ballBackground.png");

            //读取不成功就取默认图片
            if (bitmapRead == null) {
                bitmapRead = BitmapFactory.decodeResource(App.getApplication().getResources(), R.drawable.joe_big);
            }
        }

        /**
         * 每次大小改变时需要重新计算
         */
        public static void createBitmapCropFromBitmapRead() {
            if (FloatingBallDrawer.ballRadius <= 0) {
                return;
            }
            //bitmapRead可能已被回收
            if (bitmapRead == null || bitmapRead.isRecycled()) {
                setBitmapRead();
            }

            //边长
            int edge = (int) FloatingBallDrawer.ballRadius * 2;

            //缩放到edge的大小
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmapRead, edge, edge, true);

            //裁剪后的输出bitmapScaledCrop
            bitmapScaledCrop = Bitmap.createBitmap(edge, edge, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmapScaledCrop);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            //x y r
            canvas.drawCircle(FloatingBallDrawer.ballRadius, FloatingBallDrawer.ballRadius, FloatingBallDrawer.ballRadius, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

            canvas.drawBitmap(scaledBitmap, 0, 0, paint);

            scaledBitmap.recycle();
        }
    }

}
