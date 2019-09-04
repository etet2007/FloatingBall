package com.chenyee.stephenlau.floatingball.floatingBall.styleFlyme;

import com.chenyee.stephenlau.floatingball.floatingBall.FloatingBallView;
import com.chenyee.stephenlau.floatingball.floatingBall.base.BallEventListener;

public class FloatingBallEventListener extends BallEventListener {
    public FloatingBallEventListener(FloatingBallView floatingBallView) {
        super(floatingBallView);
    }
    @Override
    public void onScrollStateChange(int currentGestureState) {
        floatingBallView.ballDrawer.moveBallViewWithCurrentGestureState(currentGestureState);
    }

    @Override
    public void onScrollEnd() {
        //球移动动画
        if (floatingBallView.ballAnimator instanceof FloatingBallAnimator) {
            ((FloatingBallAnimator) floatingBallView.ballAnimator).moveFloatBallBack();
        }
    }

}
