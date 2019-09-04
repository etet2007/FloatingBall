package com.chenyee.stephenlau.floatingball.floatingBall.styleGradient;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.animation.DecelerateInterpolator;

import com.chenyee.stephenlau.floatingball.floatingBall.FloatingBallView;
import com.chenyee.stephenlau.floatingball.floatingBall.base.BallAnimator;
import com.chenyee.stephenlau.floatingball.floatingBall.base.BallDrawer;

public class GradientBallAnimator extends BallAnimator {

    GradientBallDrawer gradientBallDrawer;

    public GradientBallAnimator(FloatingBallView view, BallDrawer ballDrawer) {
        super(view, ballDrawer);
        gradientBallDrawer = (GradientBallDrawer) ballDrawer;
    }

    @Override
    public void setUpTouchAnimator(int ballRadius) {

    }

    @Override
    public void startUnTouchAnimator() {

    }

    @Override
    public void startOnTouchAnimator() {

    }
    public void moveFloatBallBack() {

    }
}
