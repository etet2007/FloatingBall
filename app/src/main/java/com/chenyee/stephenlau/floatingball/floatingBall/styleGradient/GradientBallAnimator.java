package com.chenyee.stephenlau.floatingball.floatingBall.styleGradient;

import android.animation.Animator;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.support.annotation.Keep;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

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
    public void setUpTouchAnimator(float ballRadius) {

    }

    @Override
    public void startOnTouchAnimator() {
        view.animate()
                .scaleY(1.2f).scaleX(1.2f)
                .setDuration(100)
                .start();
    }

    @Override
    public void startUnTouchAnimator() {
        view.animate()
                .setInterpolator(new OvershootInterpolator())
                .scaleY(0.7f).scaleX(0.7f).scaleX(1f).scaleY(1f)
                .setDuration(500)
                .start();
    }
    public void startMoveBackAnimator() {

        PropertyValuesHolder pvh1 = PropertyValuesHolder.ofFloat("dy", gradientBallDrawer.getDy(),0);
        PropertyValuesHolder pvh2 = PropertyValuesHolder.ofFloat("dx", gradientBallDrawer.getDx(),0);
        ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(gradientBallDrawer, pvh1, pvh2);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.addUpdateListener(animation1 -> view.invalidate());
        animation.setDuration(500)
                .start();

    }
}
