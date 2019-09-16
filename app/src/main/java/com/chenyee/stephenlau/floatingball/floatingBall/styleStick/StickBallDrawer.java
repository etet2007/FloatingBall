package com.chenyee.stephenlau.floatingball.floatingBall.styleStick;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.support.annotation.Keep;

import com.chenyee.stephenlau.floatingball.floatingBall.FloatingBallView;
import com.chenyee.stephenlau.floatingball.floatingBall.base.BallDrawer;

@Keep
public class StickBallDrawer extends BallDrawer {

    public float maxLength;
    private Path mPath;
    private Paint mFillCirclePaint;
    private VPoint p2, p4;
    private HPoint p1, p3;
    private float blackMagic = 0.551915024494f;
    private float c;

    public StickBallDrawer(FloatingBallView view) {
        super(view);

        mFillCirclePaint = new Paint();
        mFillCirclePaint.setColor(0xFFF67280);
        mFillCirclePaint.setStyle(Paint.Style.FILL);
        mFillCirclePaint.setStrokeWidth(1);
        mFillCirclePaint.setAntiAlias(true);
        mFillCirclePaint.setShadowLayer(5,0,0,Color.BLACK);
//        mFillCirclePaint.setMaskFilter(new BlurMaskFilter(5, BlurMaskFilter.Blur.SOLID));

        mPath = new Path();

        p2 = new VPoint();
        p4 = new VPoint();

        p1 = new HPoint();
        p3 = new HPoint();
    }

    public void setP2X(float value) {
        this.p2.setX(value);
    }

    public void setP4X(float value) {
        this.p4.setX(value);
    }

    public void setP1Y(float value) {
        this.p1.setY(value);
    }

    public void setP3Y(float value) {
        this.p3.setY(value);
    }

    public VPoint getP2() {
        return p2;
    }

    public VPoint getP4() {
        return p4;
    }

    public HPoint getP1() {
        return p1;
    }

    public HPoint getP3() {
        return p3;
    }

    public void initState() {
        p3.x = p1.x = 0;
        p1.setY(view.getBallRadius());//0 r
        p3.setY(-view.getBallRadius());//0 -r
        p3.left.x = p1.left.x = -c;
        p3.right.x = p1.right.x = c;

        p2.setX(view.getBallRadius()); //r 0
        p4.setX(-view.getBallRadius());//-r 0
        p2.y = p4.y = 0;
        p2.top.y = p4.top.y = -c;
        p2.bottom.y = p4.bottom.y = c;
    }

    @Override
    public void setPaintAlpha(int userSetOpacity) {
        mFillCirclePaint.setAlpha(userSetOpacity);
    }

    @Override
    public void calculateBackgroundRadiusAndMeasureSideLength(float ballRadius) {
        maxLength = (float) (ballRadius * 0.6);
        measuredSideLength = (int) ((ballRadius + maxLength) * 2 * 1.2);
        c = ballRadius * blackMagic;
        initState();

        if (ballRadius > 0) {
            Shader shader = new LinearGradient(0, -ballRadius, 0, ballRadius, Color.parseColor("#d9afd9"),
                    Color.parseColor("#97d9e1"), Shader.TileMode.CLAMP);
            mFillCirclePaint.setShader(shader);
        }
    }

    @Override
    public void drawBallWithThisModel(Canvas canvas) {
        super.drawBallWithThisModel(canvas);

        mPath.reset();
        mPath.moveTo(p1.x, p1.y);
        //四条曲线 起始默认位置 两个控制点 一个结束点，共四个点确定一条直线
        mPath.cubicTo(p1.right.x, p1.right.y, p2.bottom.x, p2.bottom.y, p2.x, p2.y);
        mPath.cubicTo(p2.top.x, p2.top.y, p3.right.x, p3.right.y, p3.x, p3.y);
        mPath.cubicTo(p3.left.x, p3.left.y, p4.top.x, p4.top.y, p4.x, p4.y);
        mPath.cubicTo(p4.bottom.x, p4.bottom.y, p1.left.x, p1.left.y, p1.x, p1.y);

        canvas.drawPath(mPath, mFillCirclePaint);
    }

    @Override
    public void updateFieldBySingleDataManager() {

    }

    @Override
    public void moveBallViewWithCurrentGestureState(int currentGestureState) {

    }

    class VPoint {
        public float x;
        public float y;
        public PointF top = new PointF();
        public PointF bottom = new PointF();

        public void setX(float x) {
            this.x = x;
            top.x = x;
            bottom.x = x;
        }

        public void adjustY(float offset) {
            top.y -= offset;
            bottom.y += offset;
        }

        public void adjustAllX(float offset) {
            this.x += offset;
            top.x += offset;
            bottom.x += offset;
        }
    }

    class HPoint {
        public float x;
        public float y;
        public PointF left = new PointF();
        public PointF right = new PointF();

        public void setY(float y) {
            this.y = y;
            left.y = y;
            right.y = y;
        }

        public void adjustAllX(float offset) {
            this.x += offset;
            left.x += offset;
            right.x += offset;
        }
    }
}
