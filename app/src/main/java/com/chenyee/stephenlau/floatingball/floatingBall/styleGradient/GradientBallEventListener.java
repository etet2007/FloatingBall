package com.chenyee.stephenlau.floatingball.floatingBall.styleGradient;

import com.chenyee.stephenlau.floatingball.floatingBall.FloatingBallView;
import com.chenyee.stephenlau.floatingball.floatingBall.base.BallEventListener;


public class GradientBallEventListener extends BallEventListener {
    public GradientBallEventListener(FloatingBallView floatingBallView) {
        super(floatingBallView);
    }

    @Override
    public void onScrollEnd() {
        GradientBallAnimator gradientBallAnimator = (GradientBallAnimator) floatingBallView.ballAnimator;
        gradientBallAnimator.startMoveBackAnimator();
    }

    @Override
    public void onScrollStateChange(int currentGestureState) {
        floatingBallView.ballDrawer.moveBallViewWithCurrentGestureState(currentGestureState);
    }
}
