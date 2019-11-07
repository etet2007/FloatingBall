package com.chenyee.stephenlau.floatingball.floatingBall.styleStick;

import android.util.Log;
import android.view.MotionEvent;

import com.chenyee.stephenlau.floatingball.floatingBall.FloatingBallView;
import com.chenyee.stephenlau.floatingball.floatingBall.base.BaseBallEventListener;

import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.GestureProcessor.STATE_DOWN;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.GestureProcessor.STATE_LEFT;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.GestureProcessor.STATE_NONE;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.GestureProcessor.STATE_RIGHT;
import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.GestureProcessor.STATE_UP;

public class StickBallEventListener extends BaseBallEventListener {

    public static final String TAG = "StickBallEventListener";
    private int currentGestureState;

    public StickBallEventListener(FloatingBallView floatingBallView) {
        super(floatingBallView);
    }

    @Override
    public void onScrollEnd() {
        StickBallDrawer stickBallDrawer = (StickBallDrawer) floatingBallView.ballDrawer;
        stickBallDrawer.initState();
        floatingBallView.invalidate();
    }

    @Override
    public void onScrollStateChange(int currentGestureState) {
        Log.d(TAG, "onScrollStateChange: ");
        this.currentGestureState = currentGestureState;
    }

    @Override
    public void onTouching(MotionEvent event) {
        Log.d(TAG, "onTouching: " + event.getX() + " " + event.getY());

        StickBallDrawer stickBallDrawer = (StickBallDrawer) floatingBallView.ballDrawer;
        float halfMeasuredLength = stickBallDrawer.getMeasuredSideLength() / 2;

        float distanceToCenterOfBallXAbs = Math.abs(event.getX() - halfMeasuredLength);//距离圆心
        float distanceToCenterOfBallYAbs = Math.abs(event.getY() - halfMeasuredLength);

        float r = floatingBallView.getBallRadius();
        float maxIncreaseLength = stickBallDrawer.maxIncreaseLength;
        double threshold = r + maxIncreaseLength * 0.7;

        //一个有趣的实现
        //        double moveUpDown = (maxIncreaseLength) * Math.sin((distanceToCenterOfBallYAbs - r)  / (2000 - r) * Math.PI / 2) + r;
        double moveUpDown = (distanceToCenterOfBallYAbs - threshold) * 0.3 * maxIncreaseLength / (2000f - threshold) + threshold;
        double moveLeftRight = (distanceToCenterOfBallXAbs - threshold) * 0.3 * maxIncreaseLength / (2000f - threshold) + threshold;

        stickBallDrawer.initState();
        switch (currentGestureState) {
            case STATE_UP:
                if (distanceToCenterOfBallYAbs > r) {
                    //r ~ r + m * 0.7的范围中保持一致
                    if (distanceToCenterOfBallYAbs < threshold) {
                        stickBallDrawer.getP3().setY(-distanceToCenterOfBallYAbs);
                    } else if (distanceToCenterOfBallYAbs < 2000) { // 2000以上不动
                        //r + m * 0.7 ~ r + 2000的范围  (distanceToCenterOfBallYAbs - (r + maxIncreaseLength * 0.7)) 增量
                        float move = (float) moveUpDown;
                        stickBallDrawer.getP3().setY(-move);
                    }
                }
                break;
            case STATE_DOWN:
                if (distanceToCenterOfBallYAbs > r) {
                    if (distanceToCenterOfBallYAbs < threshold) {
                        stickBallDrawer.getP1().setY(distanceToCenterOfBallYAbs);
                    } else if (distanceToCenterOfBallYAbs < 2000) {
                        float move = (float) moveUpDown;
                        stickBallDrawer.getP1().setY(move);
                    }
                }
                break;
            case STATE_LEFT:
                if (distanceToCenterOfBallXAbs > r) {
                    if (distanceToCenterOfBallXAbs < threshold) {
                        stickBallDrawer.getP4().setX(-distanceToCenterOfBallXAbs);
                    } else if (distanceToCenterOfBallXAbs < 2000) {
                        float move = (float) moveLeftRight;
                        stickBallDrawer.getP4().setX(-move);
                    }
                }
                break;
            case STATE_RIGHT:
                if (distanceToCenterOfBallXAbs > r) {
                    if (distanceToCenterOfBallXAbs < threshold) {
                        stickBallDrawer.getP2().setX(distanceToCenterOfBallXAbs);
                    } else if (distanceToCenterOfBallXAbs < 2000) {
                        float move = (float) moveLeftRight;
                        stickBallDrawer.getP2().setX(move);
                    }
                }
                break;
            case STATE_NONE:
                break;
        }
        floatingBallView.invalidate();
    }

}
