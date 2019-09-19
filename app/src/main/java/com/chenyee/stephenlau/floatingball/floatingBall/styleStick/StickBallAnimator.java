package com.chenyee.stephenlau.floatingball.floatingBall.styleStick;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;
import com.chenyee.stephenlau.floatingball.floatingBall.FloatingBallView;
import com.chenyee.stephenlau.floatingball.floatingBall.base.BallAnimator;
import com.chenyee.stephenlau.floatingball.floatingBall.base.BallDrawer;

public class StickBallAnimator extends BallAnimator {

    public StickBallAnimator(FloatingBallView floatingBallView, BallDrawer ballDrawer) {
        super(floatingBallView,ballDrawer);
    }

    @Override
    public void setUpTouchAnimator(float ballRadius) {

    }

    @Override
    public void startUnTouchAnimator() {
        StickBallDrawer stickBallDrawer = (StickBallDrawer) ballDrawer;

        float ballRadius = view.getBallRadius();

        PropertyValuesHolder holder1 = PropertyValuesHolder.ofFloat("p1Y",stickBallDrawer.getP1().y,ballRadius);
        PropertyValuesHolder holder2 = PropertyValuesHolder.ofFloat("p3Y",stickBallDrawer.getP3().y,-ballRadius);
        PropertyValuesHolder holder3 = PropertyValuesHolder.ofFloat("p2X",stickBallDrawer.getP2().x,ballRadius);
        PropertyValuesHolder holder4 = PropertyValuesHolder.ofFloat("p4X",stickBallDrawer.getP4().x,-ballRadius);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(stickBallDrawer, holder1, holder2, holder3, holder4);
        animator.setDuration(500)
                .setInterpolator(new AnticipateOvershootInterpolator());
        animator.addUpdateListener(animation -> view.invalidate());
        animator.start();

        view.animate()
                .setInterpolator(new OvershootInterpolator())
                .scaleY(0.7f).scaleX(0.7f).scaleX(1f).scaleY(1f)
                .setDuration(500)
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
