package com.chenyee.stephenlau.floatingball.floatingBall;

import android.view.View;

public class ViewAnimator {

    public static void performAddAnimator(View view) {
        view.setScaleX(0);
        view.setScaleY(0);

        view.animate()
                .scaleY(1).scaleX(1)
                .setDuration(200)
                .start();
    }

    public static void performRemoveAnimatorWithEndAction(View view,Runnable runnable) {
        view.animate()
                .scaleY(0).scaleX(0)
                .setDuration(200)
                .withEndAction(runnable)
                .start();
    }

}
