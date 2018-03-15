package com.chenyee.stephenlau.floatingball.util;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.chenyee.stephenlau.floatingball.R;
import com.chenyee.stephenlau.floatingball.services.FloatingBallService;

import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.HIDE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_RIGHT_SLIDE_EVENT;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.RECENT_APPS;

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
     * 上拉返回桌面
     * @param service
     */
    public static void doHome(AccessibilityService service) {
        // TODO: 2018/3/9 换为设置接口的方式 
        //OnePlus
        if(RomUtil.isRom(RomUtil.ROM_ONEPLUS)){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);// "android.intent.action.MAIN"
            intent.addCategory(Intent.CATEGORY_HOME); //"android.intent.category.HOME"
            service.startActivity(intent);

            return;
        }
        //其他手机
        boolean success=service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
        if(!success){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);// "android.intent.action.MAIN"
            intent.addCategory(Intent.CATEGORY_HOME); //"android.intent.category.HOME"
            service.startActivity(intent);
        }
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

    /**
     * 判断辅助功能是否开启
     * @param context
     * @return
     */
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
    /**
     * 判断辅助功能是否开启，并开启设置
     * @param context
     * @return
     */
    public static void checkAccessibilitySetting(Context context) {
        if(!isAccessibilitySettingsOn(context)){
            // 引导至辅助功能设置页面
            context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK ));
            Toast.makeText(context,context.getString(R.string.openAccessibility) , Toast.LENGTH_SHORT).show();
        }
    }


}