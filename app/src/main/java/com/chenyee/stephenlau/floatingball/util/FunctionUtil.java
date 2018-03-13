package com.chenyee.stephenlau.floatingball.util;

import android.accessibilityservice.AccessibilityService;
import android.widget.Toast;

import com.chenyee.stephenlau.floatingball.FunctionListener;
import com.chenyee.stephenlau.floatingball.services.FloatingBallService;

/**
 * Created by stephenlau on 18-3-13.
 */

public class FunctionUtil {
    private FloatingBallService mFloatingBallService;
    public FunctionUtil(FloatingBallService floatingBallService) {
        mFloatingBallService = floatingBallService;
    }

    public FunctionListener nullFunctionListener=new FunctionListener() {
        @Override
        public void onClick() {
        }
    };
    public FunctionListener recentAppsFunctionListener=new FunctionListener() {
        @Override
        public void onClick() {
            mFloatingBallService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
        }
    };

    public FunctionListener homeFunctionListener=new FunctionListener() {
        @Override
        public void onClick() {
            AccessibilityUtil.doHome(mFloatingBallService);
        }
    };

    public FunctionListener hideFunctionListener=new FunctionListener() {
        @Override
        public void onClick() {
            Toast.makeText(mFloatingBallService, "hide", Toast.LENGTH_LONG).show();
            mFloatingBallService.hideBall();
        }
    };

    public FunctionListener notificationFunctionListener=new FunctionListener() {
        @Override
        public void onClick() {
            mFloatingBallService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
        }
    };


}
