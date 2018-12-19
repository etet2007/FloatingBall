package com.chenyee.stephenlau.floatingball.util;

import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageEvents.Event;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.support.annotation.RequiresApi;
import android.util.Log;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeMap;


/**
 * if (!UsageStatsUtils.hasPermission(sFloatingBallService)) { //若用户未开启权限，则引导用户开启“Apps with usage access”权限
 * sFloatingBallService.getApplicationContext().startActivity( new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)); }
 * else { if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) { UsageStatsUtils.sortUsageStatsByLastTimeUsed(sFloatingBallService); } }
 */
public class UsageStatsUtils {

  private static final String TAG = "UsageStatsUtils";

  private static final long TWENTYSECOND = 20000;
  private static final long THIRTYSECOND = 30000;

  private static Field mLastEventField;
  private static String topPackageName;
  private static List<UsageStats> usageStatsList;

  /**
   * 检测用户是否对本app开启了“Apps with usage access”权限。
   */
  public static boolean hasPermission(Context context) {
    AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
    int mode = 0;
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
      mode = appOps
          .checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
    }
    return mode == AppOpsManager.MODE_ALLOWED;
  }

  /**
   * 更新最近应用流量列表。
   */
  public static void getUsageStatsList(Context context) throws NoSuchFieldException {
    if (Build.VERSION.SDK_INT > VERSION_CODES.LOLLIPOP_MR1) {

      UsageStatsManager usageStatsManager = (UsageStatsManager) context.getApplicationContext()
          .getSystemService(Context.USAGE_STATS_SERVICE);

      long currentTime = System.currentTimeMillis();

      usageStatsList = usageStatsManager
          .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - THIRTYSECOND, currentTime);

      //init mLastEventField
      if (mLastEventField == null) {
        mLastEventField = UsageStats.class.getField("mLastEvent");
      }

      index = 1;
    }

  }

  /**
   * 通过使用量统计功能获取前台应用。
   */
  @RequiresApi(api = VERSION_CODES.LOLLIPOP)
  public static void sortUsageStatsByLastTimeUsed(Context context) throws NoSuchFieldException, IllegalAccessException {

    getUsageStatsList(context);

    if (usageStatsList == null) {
      throw new NullPointerException();
    }

    ArrayList<UsageStats> toRemoved = new ArrayList<>();
    //remove Event.NONE
    for (UsageStats usageStats : usageStatsList) {
      int lastEvent = mLastEventField.getInt(usageStats);

      if (lastEvent == Event.NONE) {
        toRemoved.add(usageStats);
      }
    }
    usageStatsList.removeAll(toRemoved);

    Collections.sort(usageStatsList, new Comparator<UsageStats>() {
      @Override
      public int compare(UsageStats o1, UsageStats o2) {
        return Long.compare(o2.getLastTimeUsed(), o1.getLastTimeUsed());
      }
    });

  }

  private static int index;
  @RequiresApi(api = VERSION_CODES.LOLLIPOP)
  public static void switchToRightApp(Context context) {

    UsageStats usageStats = usageStatsList.get(index++);

    String packageName = usageStats.getPackageName();
    Log.d(TAG, "packageName: " + packageName);

    Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
    if (intent != null) {
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(intent);
    }

    }

  @RequiresApi(api = VERSION_CODES.LOLLIPOP)
  public static String getTopPackageName() throws IllegalAccessException {
    String topPackageName = "";

    for (UsageStats usageStats : usageStatsList) {
      int lastEvent = mLastEventField.getInt(usageStats);

      Log.d(TAG, "packageName: " + usageStats.getPackageName() + " lastEvent: " + lastEvent);

      if (lastEvent == Event.MOVE_TO_FOREGROUND) {
        topPackageName = usageStats.getPackageName();
//        break;
      }
    }
    Log.d(TAG,  "topPackageName" + topPackageName);
    return topPackageName;
  }


  @RequiresApi(api = VERSION_CODES.LOLLIPOP)
  private static void queryEvents(Context context) {
    UsageStatsManager usageStatsManager = (UsageStatsManager) context.getApplicationContext()
        .getSystemService(Context.USAGE_STATS_SERVICE);

    long currentTime = System.currentTimeMillis();

    UsageEvents usageEvents = usageStatsManager.queryEvents(currentTime - THIRTYSECOND, currentTime);
    UsageEvents.Event event = new UsageEvents.Event();
    //遍历这个事件集合，如果还有下一个事件
    while (usageEvents.hasNextEvent()) {
      //得到下一个事件放入event中,先得得到下个一事件，如果这个时候直接调用，则event的package是null，type是0。
      usageEvents.getNextEvent(event);
      Log.d(TAG, "package == " + event.getPackageName() + ",  type == " + event.getEventType());
      //如果这是个将应用置于前台的事件
      if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
        //获取这个前台事件的packageName.
        topPackageName = event.getPackageName();
//        Log.d(TAG,"topPackageName == " + topPackageName);
      }
    }
/**
 package == com.google.android.dialer,  type == 2
 package == com.google.android.apps.nexuslauncher,  type == 1
 topPackageName == com.google.android.apps.nexuslauncher

 package == com.google.android.apps.nexuslauncher,  type == 2
 package == com.chenyee.stephenlau.floatingball,  type == 1
 topPackageName == com.chenyee.stephenlau.floatingball
 */}
}
