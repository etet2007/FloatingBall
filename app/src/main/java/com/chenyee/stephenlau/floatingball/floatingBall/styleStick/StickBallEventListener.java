package com.chenyee.stephenlau.floatingball.floatingBall.styleStick;

import android.graphics.PointF;
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
        float translateXAbs = Math.abs(event.getX() - halfMeasuredLength);
        float translateYAbs = Math.abs(event.getY() - halfMeasuredLength);
        float r = floatingBallView.getBallRadius();
        float maxLength = stickBallDrawer.maxLength;

        stickBallDrawer.initState();

        double moveUpDown = (translateYAbs - (r + maxLength * 0.7)) * 0.3 * maxLength / 2000f + r + maxLength * 0.7;
        double moveLeftRight = (translateXAbs - (r + maxLength * 0.7)) * 0.3 * maxLength / 2000f + r + maxLength * 0.7;
        switch (currentGestureState) {
            case STATE_UP:
                if (translateYAbs > r) {
                    if (translateYAbs < r + maxLength * 0.7) {  //r ~ r + m * 0.7保持一致  r + m * 0.7 ~ r + 2000  (translateYAbs - (r + maxLength * 0.7)) 增量
                        stickBallDrawer.getP3().setY(-translateYAbs);
                    } else if (translateYAbs < 2000) {
                        float move = (float) moveUpDown;
                        stickBallDrawer.getP3().setY(-move);
                    }
                }
                break;
            case STATE_DOWN:
                if (translateYAbs > r) {
                    if (translateYAbs < r + maxLength * 0.7) {
                        stickBallDrawer.getP1().setY(translateYAbs);
                    } else if (translateYAbs < 2000) {
                        float move = (float) moveUpDown;
                        stickBallDrawer.getP1().setY(move);
                    }
                }
                break;
            case STATE_LEFT:
                if (translateXAbs > r) {
                    if (translateXAbs < r + maxLength * 0.7) {
                        stickBallDrawer.getP4().setX(-translateXAbs);
                    } else if (translateXAbs < 2000) {
                        float move = (float) moveLeftRight;
                        stickBallDrawer.getP4().setX(-move);
                    }
                }
                break;
            case STATE_RIGHT:
                if (translateXAbs > r) {
                    if (translateXAbs < r + maxLength * 0.7) {
                        stickBallDrawer.getP2().setX(translateXAbs);
                    } else if (translateXAbs < 2000) {
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
