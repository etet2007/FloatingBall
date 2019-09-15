package com.chenyee.stephenlau.floatingball.floatingBall.styleFlyme;

import android.view.MotionEvent;

import com.chenyee.stephenlau.floatingball.floatingBall.FloatingBallView;
import com.chenyee.stephenlau.floatingball.floatingBall.base.BaseBallEventListener;

public class FloatingBallEventListener extends BaseBallEventListener {
    public FloatingBallEventListener(FloatingBallView floatingBallView) {
        super(floatingBallView);
    }

    @Override
    public void onScrollEnd() {
        //球移动动画
        if (floatingBallView.ballAnimator instanceof FloatingBallAnimator) {
            ((FloatingBallAnimator) floatingBallView.ballAnimator).moveFloatBallBack();
        }
    }

    @Override
    public void onScrollStateChange(int currentGestureState) {
        floatingBallView.ballDrawer.moveBallViewWithCurrentGestureState(currentGestureState);
    }

}
