package com.chenyee.stephenlau.floatingball.util;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.Settings;
import android.widget.Toast;

import com.chenyee.stephenlau.floatingball.R;
import com.chenyee.stephenlau.floatingball.ui.activity.ScreenCaptureImageActivity;
import com.chenyee.stephenlau.floatingball.floatingBall.FunctionListener;
import com.chenyee.stephenlau.floatingball.floatingBall.service.FloatingBallService;

import static com.chenyee.stephenlau.floatingball.util.RootUtil.rootCommand;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.*;

/**
 * Created by stephenlau on 18-3-13.
 */

public class FunctionInterfaceUtils {

  public static final String TAG = FunctionInterfaceUtils.class.getSimpleName();

  public static FloatingBallService sFloatingBallService;

  private static FunctionListener nullFunctionListener = new FunctionListener() {
    @Override
    public void onFunction() {
    }
  };

  private static FunctionListener backFunctionListener = new FunctionListener() {
    @Override
    public void onFunction() {
      AccessibilityUtils.doBack(sFloatingBallService);
    }
  };

  private static FunctionListener killFrontProcessListener = new FunctionListener() {
    @Override
    public void onFunction() {
      if (!sFloatingBallService.getCurrentPackageName().contains("launcher")) {
        ActivityUtils.killBackgroundProcesses(sFloatingBallService, sFloatingBallService.getCurrentPackageName());
      }
    }
  };

  private static FunctionListener switchApp = new FunctionListener() {
    @Override
    public void onFunction() {
      if (!UsageStatsUtils.hasPermission(sFloatingBallService)) { //若用户未开启权限，则引导用户开启“Apps with usage access”权限
        sFloatingBallService.getApplicationContext().startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
      } else {
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
          try {
            UsageStatsUtils.sortUsageStatsByLastTimeUsed(sFloatingBallService);
            UsageStatsUtils.switchToRightApp(sFloatingBallService);
          } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
          }
        }
      }
    }
  };

  private static FunctionListener recentAppsFunctionListener = new FunctionListener() {
    @Override
    public void onFunction() {
      sFloatingBallService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
    }
  };

  private static FunctionListener lastAppFunctionListener = new FunctionListener() {
    @Override
    public void onFunction() {
      final boolean isOk = sFloatingBallService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
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
            sFloatingBallService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
          }
        }
      }.start();
    }
  };

  private static FunctionListener homeFunctionListener = new FunctionListener() {
    @Override
    public void onFunction() {
      AccessibilityUtils.doHome(sFloatingBallService);
    }
  };

  private static FunctionListener hideFunctionListener = new FunctionListener() {
    @Override
    public void onFunction() {
      Toast.makeText(sFloatingBallService, sFloatingBallService.getString(R.string.hide), Toast.LENGTH_LONG).show();
      sFloatingBallService.hideBallForLongTime();
    }
  };

  private static FunctionListener notificationFunctionListener = new FunctionListener() {
    @Override
    public void onFunction() {
      sFloatingBallService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
    }
  };

  private static FunctionListener deviceLockFunctionListener = new FunctionListener() {
    @Override
    public void onFunction() {
      LockScreenUtils.lockScreen(sFloatingBallService);
    }
  };

  private static FunctionListener rootLockFunctionListener = new FunctionListener() {
    @Override
    public void onFunction() {
      String apkRoot = "input keyevent 26";
      rootCommand(apkRoot);
//            Shell.SU.run(apkRoot);
    }
  };

  private static FunctionListener screenshotFunctionListener = new FunctionListener() {
    @Override
    public void onFunction() {
      sFloatingBallService.hideBallTemporary();

      Intent intent = new Intent(sFloatingBallService, ScreenCaptureImageActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
      sFloatingBallService.startActivity(intent);
    }
  };

  public static FunctionListener getListener(int key) {
    FunctionListener functionListener = nullFunctionListener;
    if (key == RECENT_APPS) {
      functionListener = FunctionInterfaceUtils.recentAppsFunctionListener;
    } else if (key == LAST_APPS) {
      functionListener = FunctionInterfaceUtils.lastAppFunctionListener;
    } else if (key == HIDE) {
      functionListener = FunctionInterfaceUtils.hideFunctionListener;
    } else if (key == NONE) {
      functionListener = FunctionInterfaceUtils.nullFunctionListener;
    } else if (key == HOME) {
      functionListener = FunctionInterfaceUtils.homeFunctionListener;
    } else if (key == LOCK_SCREEN) {
      functionListener = FunctionInterfaceUtils.deviceLockFunctionListener;
    } else if (key == ROOT_LOCK_SCREEN) {
      functionListener = FunctionInterfaceUtils.rootLockFunctionListener;
    } else if (key == NOTIFICATION) {
      functionListener = FunctionInterfaceUtils.notificationFunctionListener;
    } else if (key == SCREEN_SHOT) {
      functionListener = FunctionInterfaceUtils.screenshotFunctionListener;
    } else if (key == BACK) {
      functionListener = FunctionInterfaceUtils.backFunctionListener;
    } else if (key == KILL_PROCESS) {
      functionListener = FunctionInterfaceUtils.killFrontProcessListener;

    }

    return functionListener;
  }
}
