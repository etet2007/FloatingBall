package com.chenyee.stephenlau.floatingball.floatingBall.styleGradient;

import android.view.MotionEvent;

import com.chenyee.stephenlau.floatingball.floatingBall.FloatingBallView;
import com.chenyee.stephenlau.floatingball.floatingBall.base.BaseBallEventListener;


public class GradientBallEventListener extends BaseBallEventListener {
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
