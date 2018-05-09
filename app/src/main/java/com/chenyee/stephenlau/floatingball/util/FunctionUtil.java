package com.chenyee.stephenlau.floatingball.util;

import android.accessibilityservice.AccessibilityService;
import android.widget.Toast;

import com.chenyee.stephenlau.floatingball.floatBall.FunctionListener;
import com.chenyee.stephenlau.floatingball.floatBall.FloatingBallService;

import java.util.List;


import static com.chenyee.stephenlau.floatingball.util.RootUtil.rootCommand;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.HIDE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.HOME;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.LAST_APPS;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.LOCK_SCREEN;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.NONE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.NOTIFICATION;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.RECENT_APPS;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.ROOT_LOCK_SCREEN;

/**
 * Created by stephenlau on 18-3-13.
 */

public class FunctionUtil {
    public static FloatingBallService sFloatingBallService;

    public static FunctionListener nullFunctionListener=new FunctionListener() {
        @Override
        public void onClick() {
        }
    };

    public static FunctionListener recentAppsFunctionListener=new FunctionListener() {
        @Override
        public void onClick() {
            sFloatingBallService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
        }
    };

    public static FunctionListener lastAppFunctionListener=new FunctionListener() {
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

    public static FunctionListener homeFunctionListener=new FunctionListener() {
        @Override
        public void onClick() {
            AccessibilityUtil.doHome(sFloatingBallService);
        }
    };

    public static FunctionListener hideFunctionListener=new FunctionListener() {
        @Override
        public void onClick() {
            Toast.makeText(sFloatingBallService, "hide", Toast.LENGTH_LONG).show();
            sFloatingBallService.hideBall();
        }
    };

    public static FunctionListener notificationFunctionListener=new FunctionListener() {
        @Override
        public void onClick() {
            sFloatingBallService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
        }
    };

    public static FunctionListener deviceLockFunctionListener=new FunctionListener() {
        @Override
        public void onClick() {
            LockScreenUtil.lockScreen(sFloatingBallService);
        }
    };
    public static FunctionListener rootLockFunctionListener=new FunctionListener() {
        @Override
        public void onClick() {
            String apkRoot="input keyevent 26";
            rootCommand(apkRoot);
//            Shell.SU.run(apkRoot);
        }
    };

    public static FunctionListener getListener(int key) {
        FunctionListener functionListener = FunctionUtil.nullFunctionListener;
        if (key == RECENT_APPS) {
            functionListener = FunctionUtil.recentAppsFunctionListener;
        } else if (key == LAST_APPS) {
            functionListener = FunctionUtil.lastAppFunctionListener;
        } else if (key == HIDE) {
            functionListener = FunctionUtil.hideFunctionListener;
        } else if (key == NONE) {
            functionListener = FunctionUtil.nullFunctionListener;
        } else if (key == HOME) {
            functionListener = FunctionUtil.homeFunctionListener;
        } else if (key == LOCK_SCREEN) {
            functionListener = FunctionUtil.deviceLockFunctionListener;
        } else if (key == ROOT_LOCK_SCREEN) {
            functionListener = FunctionUtil.rootLockFunctionListener;
        } else if (key == NOTIFICATION) {
            functionListener = FunctionUtil.notificationFunctionListener;
        }
        return functionListener;
    }
}
