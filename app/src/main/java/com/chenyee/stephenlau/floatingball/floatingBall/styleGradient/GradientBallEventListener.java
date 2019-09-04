package com.chenyee.stephenlau.floatingball.floatingBall.styleGradient;

import com.chenyee.stephenlau.floatingball.floatingBall.FloatingBallView;
import com.chenyee.stephenlau.floatingball.floatingBall.base.BallEventListener;

import static com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureProcessor.STATE_NONE;

public class GradientBallEventListener extends BallEventListener {
    public GradientBallEventListener(FloatingBallView floatingBallView) {
        super(floatingBallView);
    }

    @Override
    public void onScrollEnd() {
        floatingBallView.ballDrawer.moveBallViewWithCurrentGestureState(STATE_NONE);
    }

    @Override
    public void onScrollStateChange(int currentGestureState) {
        floatingBallView.ballDrawer.moveBallViewWithCurrentGestureState(currentGestureState);
    }
}
