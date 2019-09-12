package com.chenyee.stephenlau.floatingball.util;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import com.chenyee.stephenlau.floatingball.R;
import com.chenyee.stephenlau.floatingball.floatingBall.FunctionListener;
import com.chenyee.stephenlau.floatingball.floatingBall.service.FloatingBallService;
import com.chenyee.stephenlau.floatingball.ui.activity.ScreenCaptureImageActivity;

import static com.chenyee.stephenlau.floatingball.util.RootUtil.rootCommand;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.*;


/**
 * Created by stephenlau on 18-3-13.
 */

public class FunctionInterfaceUtils {
    public static final String TAG = FunctionInterfaceUtils.class.getSimpleName();

    public FunctionInterfaceUtils(FloatingBallService floatingBallService) {
        this.floatingBallService = floatingBallService;
    }

    private FloatingBallService floatingBallService;


    private class NullFunctionListener implements FunctionListener {
        @Override
        public void onFunction() {
        }
    }

    private class BackFunctionListener implements FunctionListener {
        @Override
        public void onFunction() {
            AccessibilityUtils.doBack(floatingBallService);
        }
    }

    private class KillFrontProcessListener implements FunctionListener {
        @Override
        public void onFunction() {
            if (!floatingBallService.getTargetPackageName().contains("launcher")) {
                ActivityUtils.killBackgroundProcesses(floatingBallService, floatingBallService.getTargetPackageName());
            }
        }
    }

    private class SwitchAppListener implements FunctionListener {
        @Override
        public void onFunction() {
            if (!UsageStatsUtils.hasPermission(floatingBallService)) { //若用户未开启权限，则引导用户开启“Apps with usage access”权限
                floatingBallService.getApplicationContext().startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            } else {
                if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                    try {
                        UsageStatsUtils.sortUsageStatsByLastTimeUsed(floatingBallService);
                        UsageStatsUtils.switchToRightApp(floatingBallService);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class RecentAppsFunctionListener implements FunctionListener {
        @Override
        public void onFunction() {
            floatingBallService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
        }
    }

    private class LastAppFunctionListener implements FunctionListener {
        @Override
        public void onFunction() {
            final boolean isOk = floatingBallService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (isOk) {
                        floatingBallService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
                    }
                }
            }.start();
        }
    }

    private class HomeFunctionListener implements FunctionListener {
        @Override
        public void onFunction() {
            AccessibilityUtils.doHome(floatingBallService);
        }
    }

    private class HideFunctionListener implements FunctionListener {
        @Override
        public void onFunction() {
            Toast.makeText(floatingBallService, floatingBallService.getString(R.string.hide), Toast.LENGTH_LONG).show();
            floatingBallService.hideBallForLongTime();
        }
    }

    private class NotificationFunctionListener implements FunctionListener {
        @Override
        public void onFunction() {
            floatingBallService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
        }
    }

    private class DeviceLockFunctionListener implements FunctionListener {
        @Override
        public void onFunction() {
            LockScreenUtils.lockScreen(floatingBallService);
        }
    }

    private class RootLockFunctionListener implements FunctionListener {
        @Override
        public void onFunction() {
            String apkRoot = "input keyevent 26";
            rootCommand(apkRoot);
        }
    }

    private class ScreenshotFunctionListener implements FunctionListener {
        @Override
        public void onFunction() {
            floatingBallService.hideBallTemporarily();

            Intent intent = new Intent(floatingBallService, ScreenCaptureImageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            floatingBallService.startActivity(intent);
        }
    }
    private class SystemScreenshotFunctionListener implements FunctionListener {
        @Override
        public void onFunction() {
            floatingBallService.hideBallTemporarily();

            floatingBallService.postRunnable(() -> {
                floatingBallService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT);
                floatingBallService.showBall();
            });

        }
    }

    private class SystemLockScreen implements FunctionListener {
        @RequiresApi(api = VERSION_CODES.P)
        @Override
        public void onFunction() {
            floatingBallService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN);
        }
    }

    public FunctionListener getListener(int key) {
        FunctionListener functionListener = new NullFunctionListener();
        if (key == RECENT_APPS) {
            functionListener = new RecentAppsFunctionListener();
        } else if (key == LAST_APPS) {
            functionListener = new LastAppFunctionListener();
        } else if (key == HIDE) {
            functionListener = new HideFunctionListener();
        } else if (key == NONE) {
            functionListener = new NullFunctionListener();
        } else if (key == HOME) {
            functionListener = new HomeFunctionListener();
        } else if (key == LOCK_SCREEN) {
            functionListener = new DeviceLockFunctionListener();
        } else if (key == ROOT_LOCK_SCREEN) {
            functionListener = new RootLockFunctionListener();
        } else if (key == NOTIFICATION) {
            functionListener = new NotificationFunctionListener();
        } else if (key == SCREEN_SHOT) {
            functionListener = new ScreenshotFunctionListener();
        } else if (key == BACK) {
            functionListener = new BackFunctionListener();
        } else if (key == KILL_PROCESS) {
            functionListener = new KillFrontProcessListener();
        } else if (key == SYSTEM_LOCK_SCREEN) {
            functionListener = new SystemLockScreen();
        } else if (key == SYSTEM_SCREEN_SHOT) {
            functionListener = new SystemScreenshotFunctionListener();
        }
        return functionListener;
    }
}
