package com.chenyee.stephenlau.floatingball;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import java.util.List;

public class AccessibilityUtil {
    public static final String TAG = "AccessibilityUtil";
    /**
     * 单击返回功能
     * @param service
     */
    public static void doBack(AccessibilityService service) {
        boolean success=service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
        Log.d(TAG, "doBack: "+success);
    }

    /**
     * 下拉打开通知栏
     * @param service
     */
    public static void doPullDown(AccessibilityService service) {
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
    }

    /**
     * 上拉返回桌面
     * @param service
     */
    public static void doPullUp(AccessibilityService service) {
//        boolean success=service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);

//        if(!success){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);// "android.intent.action.MAIN"

            intent.addCategory(Intent.CATEGORY_HOME); //"android.intent.category.HOME"
            service.startActivity(intent);
//        }
    }

    /**
     * 左右滑动打开多任务
     * @param service
     */
    public static void doLeftOrRight(AccessibilityService service) {
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);


        //test code
//        ActivityManager activityManager = (ActivityManager) service.getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
//
//        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
//            String log = appProcess.processName;
//
//            Log.d(TAG, log);
//            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
//                if(appProcess.processName.equals(defaultInputName)) {
//                    break;
//                }
//            }
        }
    }

    public static boolean isAccessibilitySettingsOn(Context context) {
        int accessibilityEnabled = 0;

        //If accessibility is enabled，使用Content Provider读取Setting中Secure的配置。
        try {
            //Settings.Secure: Secure system settings, containing system preferences that applications
            // can read but are not allowed to write.
            //getInt: Convenience function for retrieving a single secure settings value as an integer.
            accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        if (accessibilityEnabled == 1) {
            //List of the enabled accessibility providers.   accessibility里面有个Services列表。
            String services = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (services != null) {
                return services.toLowerCase().contains(context.getPackageName().toLowerCase());
            }
        }

        return false;
    }



}