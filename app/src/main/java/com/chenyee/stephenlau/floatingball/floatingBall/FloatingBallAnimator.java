package com.chenyee.stephenlau.floatingball.floatingBall;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.support.annotation.Keep;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

public class FloatingBallAnimator {

  private FloatingBallView view;
  private FloatingBallDrawer floatingBallDrawer;
  private FloatingBallPaint floatingBallPaint;

  private ObjectAnimator reduceOpacityAnimator;
  private ObjectAnimator breathingOpacityAnimator;

  private ObjectAnimator onTouchAnimator;
  private ObjectAnimator unTouchAnimator;

  /**
   * 持有drawer 控制drawer参数
   * @param view
   * @param floatingBallDrawer
   */
  public FloatingBallAnimator(FloatingBallView view, FloatingBallDrawer floatingBallDrawer) {
    this.view = view;
    this.floatingBallDrawer = floatingBallDrawer;
    floatingBallPaint = view.getFloatingBallPaint();
  }

  public void setUpReduceAnimator(int opacity) {
    Keyframe kf1 = Keyframe.ofInt(0f, opacity);
    Keyframe kf2 = Keyframe.ofInt(0.5f, opacity);
    Keyframe kf3 = Keyframe.ofInt(1f, (int) (opacity * 0.6));
    PropertyValuesHolder pVH = PropertyValuesHolder.ofKeyframe("paintAlpha", kf1, kf2, kf3);
    reduceOpacityAnimator = ObjectAnimator.ofPropertyValuesHolder(floatingBallPaint, pVH);
    reduceOpacityAnimator.setDuration(3000);
    reduceOpacityAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        view.invalidate();
      }
    });
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
    breathingOpacityAnimator = ObjectAnimator.ofPropertyValuesHolder(floatingBallPaint, pVH);
    breathingOpacityAnimator.setRepeatCount(ValueAnimator.INFINITE);
    breathingOpacityAnimator.setRepeatMode(ValueAnimator.RESTART);
    breathingOpacityAnimator.setDuration(4000);
    breathingOpacityAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        view.invalidate();
      }
    });
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

  public void performAddAnimator() {
    view.setScaleX(0);
    view.setScaleY(0);

    view.animate()
        .scaleY(1).scaleX(1)
        .setDuration(200)
        .start();
  }

  public void performRemoveAnimatorWithEndAction(Runnable runnable) {
    view.animate()
        .scaleY(0).scaleX(0)
        .setDuration(200)
        .withEndAction(runnable)
        .start();
  }

  public void setUpTouchAnimator(int ballRadius) {
    Keyframe kf0 = Keyframe.ofFloat(0f, ballRadius);
    Keyframe kf1 = Keyframe.ofFloat(.7f, ballRadius + floatingBallDrawer.ballRadiusDeltaMaxInAnimation - 1);
    Keyframe kf2 = Keyframe.ofFloat(1f, ballRadius + floatingBallDrawer.ballRadiusDeltaMaxInAnimation);
    PropertyValuesHolder onTouch = PropertyValuesHolder.ofKeyframe("ballRadius", kf0, kf1, kf2);
    onTouchAnimator = ObjectAnimator.ofPropertyValuesHolder(floatingBallDrawer, onTouch);
    onTouchAnimator.setDuration(300);
    onTouchAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        view.invalidate();
      }
    });

    Keyframe kf3 = Keyframe.ofFloat(0f, ballRadius + floatingBallDrawer.ballRadiusDeltaMaxInAnimation);
    Keyframe kf4 = Keyframe.ofFloat(0.3f, ballRadius + floatingBallDrawer.ballRadiusDeltaMaxInAnimation);
    Keyframe kf5 = Keyframe.ofFloat(1f, ballRadius);
    PropertyValuesHolder unTouch = PropertyValuesHolder.ofKeyframe("ballRadius", kf3, kf4, kf5);
    unTouchAnimator = ObjectAnimator.ofPropertyValuesHolder(floatingBallDrawer, unTouch);
    unTouchAnimator.setInterpolator(new DecelerateInterpolator());

    unTouchAnimator.setDuration(400);
    unTouchAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        view.invalidate();
      }
    });
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

  public void startParamsYAnimationTo(int paramsY) {
    ObjectAnimator animation = ObjectAnimator
        .ofInt(view, "layoutParamsY", view.getLayoutParamsY(), paramsY);
    animation.setDuration(300);
    animation.setInterpolator(new AccelerateDecelerateInterpolator());
    animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        view.updateLayoutParamsWithOrientation();
      }
    });
    animation.start();
  }

}
