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
    @Override
    public void onMove(int x, int y) {

    }

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

        switch (currentGestureState) {
            case STATE_UP:
                if (translateYAbs > r) {
                    if (translateYAbs < r + maxLength * 0.7) {
                        stickBallDrawer.getP3().setY(-translateYAbs);
                    } else if (translateYAbs < r + 2000) {
                        float move = (float) ((translateYAbs - (r + maxLength * 0.7)) * maxLength / 2000f + r + maxLength * 0.7);
                        stickBallDrawer.getP3().setY(-move);
                    }
                }
                break;
            case STATE_DOWN:
                if (translateYAbs > r) {
                    if (translateYAbs < r + maxLength * 0.7) {
                        stickBallDrawer.getP1().setY(translateYAbs);
                    } else if (translateYAbs < r + 2000) {
                        float move = (float) ((translateYAbs - (r + maxLength * 0.7)) * maxLength / 2000f + r + maxLength * 0.7);
                        stickBallDrawer.getP1().setY(move);
                    }
                }
                break;
            case STATE_LEFT:
                if (translateXAbs > r) {
                    if (translateXAbs < r + maxLength * 0.7) {
                        stickBallDrawer.getP4().setX(-translateXAbs);
                    } else if (translateXAbs < r + 2000) {
                        float move = (float) ((translateXAbs - (r + maxLength * 0.7)) * maxLength / 2000f + r + maxLength * 0.7);
                        stickBallDrawer.getP4().setX(-move);
                    }
                }
                break;
            case STATE_RIGHT:
                if (translateXAbs > r) {
                    if (translateXAbs < r + maxLength * 0.7) {
                        stickBallDrawer.getP2().setX(translateXAbs);
                    } else if (translateXAbs < r + 2000) {
                        float move = (float) ((translateXAbs - (r + maxLength * 0.7)) * maxLength / 2000f + r + maxLength * 0.7);
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
