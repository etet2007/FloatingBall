package com.chenyee.stephenlau.floatingball.floatingBall.base;

import android.view.MotionEvent;

import com.chenyee.stephenlau.floatingball.floatingBall.FloatingBallView;
import com.chenyee.stephenlau.floatingball.floatingBall.gesture.OnGestureEventListener;

import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.OPACITY_REDUCE;

abstract public class BaseBallEventListener implements OnGestureEventListener {

    protected FloatingBallView floatingBallView;

    public BaseBallEventListener(FloatingBallView floatingBallView) {
        this.floatingBallView = floatingBallView;
    }

    @Override
    public void onActionDown() {
        floatingBallView.ballAnimator.startOnTouchAnimator();

        if (floatingBallView.opacityMode == OPACITY_REDUCE) {
            floatingBallView.ballDrawer.setPaintAlpha(floatingBallView.userSetOpacity);
        }
    }

    @Override
    public void onActionUp() {
        floatingBallView.ballAnimator.startUnTouchAnimator();
        if (floatingBallView.opacityMode == OPACITY_REDUCE) {
            floatingBallView.ballAnimator.startReduceOpacityAnimator();
        }
    }

    @Override
    public void onMove(int x, int y) {
        floatingBallView.setLayoutPositionParamsAndSave(x, y);
    }

    @Override
    abstract public void onScrollEnd();

    @Override
    public void onSingeTap() {
        floatingBallView.singleTapFunctionListener.onFunction();
    }

    @Override
    public void onDoubleTap() {
        floatingBallView.doubleTapFunctionListener.onFunction();
    }

    @Override
    public void upGesture() {
        floatingBallView.upFunctionListener.onFunction();
    }

    @Override
    public void downGesture() {
        floatingBallView.downFunctionListener.onFunction();
    }

    @Override
    public void leftGesture() {
        floatingBallView.leftFunctionListener.onFunction();
    }

    @Override
    public void rightGesture() {
        floatingBallView.rightFunctionListener.onFunction();
    }

    @Override
    abstract public void onScrollStateChange(int currentGestureState);

    @Override
    public void onLongPressEnd() {
        if (floatingBallView.isKeyboardShow) {

            int ballBottomYPlusGap = floatingBallView.getLayoutParamsY() + floatingBallView.getMeasureLength() + floatingBallView.moveUpDistance;
            if (ballBottomYPlusGap >= floatingBallView.keyboardTopY) { //球在键盘下方
                floatingBallView.lastLayoutParamsY = floatingBallView.getLayoutParamsY();
                floatingBallView.ballAnimator.startParamsYAnimationTo(floatingBallView.keyboardTopY - floatingBallView.getMeasureLength() - floatingBallView.moveUpDistance);
            } else {
                floatingBallView.isBallMoveUp = false;
            }
        }
    }

    public void onTouching(MotionEvent event) {

    }
}

