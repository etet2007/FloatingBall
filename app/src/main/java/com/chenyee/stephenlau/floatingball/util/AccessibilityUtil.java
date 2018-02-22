package com.chenyee.stephenlau.floatingball.util;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AccessibilityUtil {
    private static final String TAG = "AccessibilityUtil";
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
    public static void doNotification(AccessibilityService service) {
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
    }

    /**
     * 上拉返回桌面
     * @param service
     */
    public static void doHome(AccessibilityService service) {
        //某些手机不支持 一加/魅族都不行
        boolean success=service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);

        if(!success){
            //smartisan os不行？
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);// "android.intent.action.MAIN"

            intent.addCategory(Intent.CATEGORY_HOME); //"android.intent.category.HOME"
            service.startActivity(intent);
        }
    }

    /**
     * 左右滑动打开多任务
     * @param service
     */
    public static void doRecents(AccessibilityService service) {
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);

        //getTopApp package name
//        getTopApp(service);

        //不可用
//        String packageName=getTopApp(service);
//        if(packageName!=null && !packageName.isEmpty()){
//            PackageManager packageManager = service.getPackageManager();
//            Intent intent=new Intent();
//            intent =packageManager.getLaunchIntentForPackage(packageName);
//            service.startActivity(intent);
//        }

    }

//    public static String  getTopApp(Context context) {
//        String topActivity = "";
//        String secondActivity = "";
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
//            if (usageStatsManager != null) {
//                long now = System.currentTimeMillis();
//                //获取60秒之内的应用数据
//                List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - 60 * 1000, now);
//                Log.i(TAG, "Running app number in last 60 seconds : " + stats.size());
//
//
//                Collections.sort(stats, new Comparator<UsageStats>() {
//                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//                    @Override
//                    public int compare(UsageStats o1, UsageStats o2) {
//
//                        return (int) -(o1.getLastTimeUsed()-o2.getLastTimeUsed());
//                    }
//                });
//
//                for (UsageStats usageStats:stats) {
//                    Log.d(TAG, "getTopApp: "+usageStats.getLastTimeUsed());
//                }
//                UsageStats second =stats.get(1);
//                if(second!=null)
//                    secondActivity = second.getPackageName();
//                Log.d(TAG, secondActivity);
//
//                //取得最近运行的一个app，即当前运行的app
////                if (!stats.isEmpty()) {
//////                if ((stats != null) && (!stats.isEmpty())) {
////                    int j = 0;
////                    for (int i = 0; i < stats.size(); i++) {
////                        if (stats.get(i).getLastTimeUsed() > stats.get(j).getLastTimeUsed()) {
////                            j = i;
////                        }
////
////                        topActivity = stats.get(j).getPackageName();
////                    }
////                }
//            }
//        }
//        return secondActivity;
//    }

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