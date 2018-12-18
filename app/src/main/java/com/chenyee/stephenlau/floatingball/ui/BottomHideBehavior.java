package com.chenyee.stephenlau.floatingball.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import static com.chenyee.stephenlau.floatingball.util.DimensionUtils.dip2px;

public class BottomHideBehavior extends CoordinatorLayout.Behavior<View> {
    private ObjectAnimator outAnimator, inAnimator;

    public BottomHideBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL;    // 垂直滑动
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        if (dy > 0) {// 上滑隐藏
            // 创建outAnimator
            if (outAnimator == null) {
                outAnimator = ObjectAnimator.ofFloat(child, "translationY", 0, child.getHeight() + dip2px(coordinatorLayout.getContext(), 16));
                outAnimator.setDuration(200);
            }
            // 启动条件
            if (!outAnimator.isRunning() && child.getTranslationY() <= 0) {
                outAnimator.start();
            }
        } else if (dy < 0) {// 下滑显示
            if (inAnimator == null) {
                inAnimator = ObjectAnimator.ofFloat(child, "translationY", child.getTranslationY(), 0);
                inAnimator.setDuration(200);
            }
            if (!inAnimator.isRunning() && child.getTranslationY() >= child.getHeight()) {
                inAnimator.start();
            }
        }
    }

}
