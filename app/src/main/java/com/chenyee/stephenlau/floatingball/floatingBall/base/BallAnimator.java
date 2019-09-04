package com.chenyee.stephenlau.floatingball.floatingBall.base;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.support.annotation.Keep;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.chenyee.stephenlau.floatingball.floatingBall.FloatingBallView;

public abstract class BallAnimator {

    protected FloatingBallView view;
    private BallDrawer ballDrawer;
    private ObjectAnimator reduceOpacityAnimator;
    private ObjectAnimator breathingOpacityAnimator;

    protected ObjectAnimator onTouchAnimator;
    protected ObjectAnimator unTouchAnimator;


    public BallAnimator(FloatingBallView view, BallDrawer ballDrawer) {
        this.view = view;
        this.ballDrawer = ballDrawer;
    }

    abstract public void setUpTouchAnimator(int ballRadius);
    abstract public void startUnTouchAnimator();
    abstract public void startOnTouchAnimator();

        public void setUpReduceAnimator(int opacity) {
        Keyframe kf1 = Keyframe.ofInt(0f, opacity);
        Keyframe kf2 = Keyframe.ofInt(0.5f, opacity);
        Keyframe kf3 = Keyframe.ofInt(1f, (int) (opacity * 0.6));
        PropertyValuesHolder pVH = PropertyValuesHolder.ofKeyframe("paintAlpha", kf1, kf2, kf3);
        reduceOpacityAnimator = ObjectAnimator.ofPropertyValuesHolder(ballDrawer, pVH);
        reduceOpacityAnimator.setDuration(3000);
        reduceOpacityAnimator.addUpdateListener(animation -> view.invalidate());
    }

    public void startReduceOpacityAnimator() {
        if (reduceOpacityAnimator != null) {
            reduceOpacityAnimator.start();
        }
    }

    @Keep
    public void setUpBreathingAnimator(int opacity) {
        Keyframe kf1 = Keyframe.ofInt(0f, (int) (opacity * 0.4));
        Keyframe kf2 = Keyframe.ofInt(0.35f, opacity);
        Keyframe kf3 = Keyframe.ofInt(0.5f, opacity);
        Keyframe kf4 = Keyframe.ofInt(0.65f, opacity);
        Keyframe kf5 = Keyframe.ofInt(1f, (int) (opacity * 0.4));
        PropertyValuesHolder pVH = PropertyValuesHolder.ofKeyframe("paintAlpha", kf1, kf2, kf3, kf4, kf5);
        breathingOpacityAnimator = ObjectAnimator.ofPropertyValuesHolder(ballDrawer, pVH);
        breathingOpacityAnimator.setRepeatCount(ValueAnimator.INFINITE);
        breathingOpacityAnimator.setRepeatMode(ValueAnimator.RESTART);
        breathingOpacityAnimator.setDuration(4000);
        breathingOpacityAnimator.addUpdateListener(animation -> view.invalidate());
    }

    public void startBreathingAnimator() {
        if (breathingOpacityAnimator != null) {
            breathingOpacityAnimator.start();
        }
    }

    public void cancelBreathingAnimator() {
        if (breathingOpacityAnimator != null) {
            breathingOpacityAnimator.cancel();
        }
    }

    public void startParamsYAnimationTo(int paramsY) {
        ObjectAnimator animation = ObjectAnimator
                .ofInt(view, "layoutParamsY", view.getLayoutParamsY(), paramsY);
        animation.setDuration(300);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.addUpdateListener(animation1 -> view.updateLayoutParamsWithOrientation());
        animation.start();
    }
}
