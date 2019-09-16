package com.chenyee.stephenlau.floatingball.floatingBall.styleStick;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.support.annotation.Keep;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.chenyee.stephenlau.floatingball.floatingBall.FloatingBallView;
import com.chenyee.stephenlau.floatingball.floatingBall.base.BallAnimator;
import com.chenyee.stephenlau.floatingball.floatingBall.base.BallDrawer;
public class StickBallAnimator extends BallAnimator {

    private AnimatorSet set;
    private float oldBallRadius;

    public StickBallAnimator(FloatingBallView floatingBallView, BallDrawer ballDrawer) {
        super(floatingBallView,ballDrawer);
    }

    @Override
    public void setUpTouchAnimator(float ballRadius) {
        oldBallRadius = view.getBallRadius();
    }

    @Override
    public void startUnTouchAnimator() {
        StickBallDrawer stickBallDrawer = (StickBallDrawer) ballDrawer;

        float ballRadius = view.getBallRadius();
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(stickBallDrawer, "p1Y",stickBallDrawer.getP1().y,ballRadius);
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(stickBallDrawer, "p3Y",stickBallDrawer.getP3().y,-ballRadius);
        ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(stickBallDrawer, "p2X",stickBallDrawer.getP2().x,ballRadius);
        ObjectAnimator objectAnimator4 = ObjectAnimator.ofFloat(stickBallDrawer, "p4X",stickBallDrawer.getP4().x,-ballRadius);
        objectAnimator1.addUpdateListener(animation -> view.invalidate());

        set = new AnimatorSet();
        set.setInterpolator(new OvershootInterpolator());
        set.setDuration(500);
        set.playTogether(objectAnimator1, objectAnimator2, objectAnimator3, objectAnimator4);

        set.start();

        view.animate()
                .scaleY(1).scaleX(1)
                .setDuration(200)
                .start();

    }

    @Override
    public void startOnTouchAnimator() {
        view.animate()
                .scaleY(1.2f).scaleX(1.2f)
                .setDuration(100)
                .start();
    }

}
