package com.chenyee.stephenlau.floatingball.util;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.widget.Toast;

import com.chenyee.stephenlau.floatingball.ui.activity.ScreenCaptureImageActivity;
import com.chenyee.stephenlau.floatingball.floatingBall.FunctionListener;
import com.chenyee.stephenlau.floatingball.floatingBall.FloatingBallService;

import static com.chenyee.stephenlau.floatingball.util.RootUtil.rootCommand;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.*;

/**
 * Created by stephenlau on 18-3-13.
 */

public class FunctionUtils {
    public static final String TAG = FunctionUtils.class.getSimpleName();

    public static FloatingBallService sFloatingBallService;

    //静态内部类 只需要一个
    private static FunctionListener nullFunctionListener = new FunctionListener() {
        @Override
        public void onClick() {
        }
    };

    private static FunctionListener recentAppsFunctionListener=new FunctionListener() {
        @Override
        public void onClick() {
            sFloatingBallService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
        }
    };

    private static FunctionListener lastAppFunctionListener=new FunctionListener() {
        @Override
        public void onClick() {
            final boolean isOk= sFloatingBallService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(isOk)
                        sFloatingBallService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
                }
            }.start();
        }
    };

    private static FunctionListener homeFunctionListener=new FunctionListener() {
        @Override
        public void onClick() {
            AccessibilityUtils.doHome(sFloatingBallService);
        }
    };

    private static FunctionListener hideFunctionListener=new FunctionListener() {
        @Override
        public void onClick() {
            Toast.makeText(sFloatingBallService, "setVisibility", Toast.LENGTH_LONG).show();
            sFloatingBallService.hideBall();
        }
    };

    private static FunctionListener notificationFunctionListener=new FunctionListener() {
        @Override
        public void onClick() {
            sFloatingBallService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
        }
    };

    private static FunctionListener deviceLockFunctionListener=new FunctionListener() {
        @Override
        public void onClick() {
            LockScreenUtils.lockScreen(sFloatingBallService);
        }
    };
    private static FunctionListener rootLockFunctionListener=new FunctionListener() {
        @Override
        public void onClick() {
            String apkRoot="input keyevent 26";
            rootCommand(apkRoot);
//            Shell.SU.run(apkRoot);
        }
    };

    private static FunctionListener screenshotFunctionListener=new FunctionListener() {
        @Override
        public void onClick() {
            sFloatingBallService.startActivity(new Intent(sFloatingBallService.getApplicationContext(),ScreenCaptureImageActivity.class));
        }
    };

    public static FunctionListener getListener(int key) {
        FunctionListener functionListener = FunctionUtils.nullFunctionListener;
        if (key == RECENT_APPS) {
            functionListener = FunctionUtils.recentAppsFunctionListener;
        } else if (key == LAST_APPS) {
            functionListener = FunctionUtils.lastAppFunctionListener;
        } else if (key == HIDE) {
            functionListener = FunctionUtils.hideFunctionListener;
        } else if (key == NONE) {
            functionListener = FunctionUtils.nullFunctionListener;
        } else if (key == HOME) {
            functionListener = FunctionUtils.homeFunctionListener;
        } else if (key == LOCK_SCREEN) {
            functionListener = FunctionUtils.deviceLockFunctionListener;
        } else if (key == ROOT_LOCK_SCREEN) {
            functionListener = FunctionUtils.rootLockFunctionListener;
        } else if (key == NOTIFICATION) {
            functionListener = FunctionUtils.notificationFunctionListener;
        }else if(key==SCREEN_SHOT){
            functionListener = FunctionUtils.screenshotFunctionListener;
        }

        return functionListener;
    }
}
