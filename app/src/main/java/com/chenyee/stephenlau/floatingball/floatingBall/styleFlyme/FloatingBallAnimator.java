package com.chenyee.stephenlau.floatingball.floatingBall.styleFlyme;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.animation.DecelerateInterpolator;

import com.chenyee.stephenlau.floatingball.floatingBall.FloatingBallView;
import com.chenyee.stephenlau.floatingball.floatingBall.base.BallAnimator;
import com.chenyee.stephenlau.floatingball.floatingBall.base.BallDrawer;

public class FloatingBallAnimator extends BallAnimator {

    private FloatingBallDrawer floatingBallDrawer;

    public FloatingBallAnimator(FloatingBallView view, BallDrawer ballDrawer) {
        super(view, ballDrawer);
        this.floatingBallDrawer = (FloatingBallDrawer) ballDrawer;
    }

    public void setUpTouchAnimator(float ballRadius) {
        Keyframe kf0 = Keyframe.ofFloat(0f, ballRadius);
        Keyframe kf1 = Keyframe.ofFloat(.7f, ballRadius + floatingBallDrawer.ballRadiusDeltaMaxInAnimation - 1);
        Keyframe kf2 = Keyframe.ofFloat(1f, ballRadius + floatingBallDrawer.ballRadiusDeltaMaxInAnimation);
        PropertyValuesHolder onTouch = PropertyValuesHolder.ofKeyframe("ballRadius", kf0, kf1, kf2);
        onTouchAnimator = ObjectAnimator.ofPropertyValuesHolder(view, onTouch);
        onTouchAnimator.setDuration(300);
        onTouchAnimator.addUpdateListener(animation -> view.invalidate());

        Keyframe kf3 = Keyframe.ofFloat(0f, ballRadius + floatingBallDrawer.ballRadiusDeltaMaxInAnimation);
        Keyframe kf4 = Keyframe.ofFloat(0.3f, ballRadius + floatingBallDrawer.ballRadiusDeltaMaxInAnimation);
        Keyframe kf5 = Keyframe.ofFloat(1f, ballRadius);
        PropertyValuesHolder unTouch = PropertyValuesHolder.ofKeyframe("ballRadius", kf3, kf4, kf5);
        unTouchAnimator = ObjectAnimator.ofPropertyValuesHolder(view, unTouch);
        unTouchAnimator.setInterpolator(new DecelerateInterpolator());

        unTouchAnimator.setDuration(400);
        unTouchAnimator.addUpdateListener(animation -> view.invalidate());
    }

    public void startOnTouchAnimator() {
        if (onTouchAnimator != null) {
            onTouchAnimator.start();
        }
    }

    public void startUnTouchAnimator() {
        if (unTouchAnimator != null) {
            unTouchAnimator.start();
        }
    }

    public void moveFloatBallBack() {
        PropertyValuesHolder pvh1 = PropertyValuesHolder.ofFloat("ballCenterX", 0);
        PropertyValuesHolder pvh2 = PropertyValuesHolder.ofFloat("ballCenterY", 0);
        ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(floatingBallDrawer, pvh1, pvh2);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.setDuration(500)
                .start();
    }

}
