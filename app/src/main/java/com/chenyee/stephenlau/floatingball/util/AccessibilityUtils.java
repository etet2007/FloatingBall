package com.chenyee.stephenlau.floatingball.util;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.hardware.input.InputManager;
import android.os.Build;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.widget.Toast;

import com.chenyee.stephenlau.floatingball.App;
import com.chenyee.stephenlau.floatingball.R;

import java.lang.reflect.Method;

public class AccessibilityUtils {

  private static final String TAG = "AccessibilityUtils";

  /**
   * 单击返回功能
   */
  public static boolean doBack(AccessibilityService service) {
    return service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
  }

  /**
   * 上拉返回桌面
   */
  public static void doHome(AccessibilityService service) {
    // TODO: 2018/3/9 把Rom的信息存于SharedPreference中，不用每次都判断
    //OnePlus
    if (RomUtil.isRom(RomUtil.ROM_ONEPLUS)) {
      Intent intent = new Intent();
      intent.setAction(Intent.ACTION_MAIN);// "android.intent.action.MAIN"
      intent.addCategory(Intent.CATEGORY_HOME); //"android.intent.category.HOME"
      service.startActivity(intent);
      return;
    }
    //其他手机
    boolean success = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
    if (!success) {
      Intent intent = new Intent();
      intent.setAction(Intent.ACTION_MAIN);// "android.intent.action.MAIN"
      intent.addCategory(Intent.CATEGORY_HOME); //"android.intent.category.HOME"
      service.startActivity(intent);
    }
  }

  public static void simulateKey(int keyCode) {
    //使用KeyEvent模拟按键按下与弹起
    long l = SystemClock.uptimeMillis();
    KeyEvent localKeyEvent = new KeyEvent(l, l, KeyEvent.ACTION_DOWN, keyCode, 0);
    KeyEvent localKeyEvent1 = new KeyEvent(l, l, KeyEvent.ACTION_UP, keyCode, 0);

    //新版本使用InputManager注入按键事件
    //*******IWindowManager和InputManager都是隐藏类，必须在重新生成sdk中的android.jar，并包含两个类及其依赖*****
    Class cl = InputManager.class;
    try {
      Method method = cl.getMethod("getInstance");
      method.setAccessible(true);
      Object result = method.invoke(cl);
      InputManager im = (InputManager) result;

      method = cl.getMethod("injectInputEvent", InputEvent.class, int.class);
      method.setAccessible(true);
      method.invoke(im, localKeyEvent, 0);
      method.invoke(im, localKeyEvent1, 0);

    } catch (Exception e) {
      e.printStackTrace();
    }
//        InputManager.getInstance().injectInputEvent(localKeyEvent, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
//        InputManager.getInstance().injectInputEvent(localKeyEvent1, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
  }

  /**
   * 判断辅助功能是否开启
   */
  public static boolean isAccessibilitySettingsOn() {
    Context context = App.getApplication();
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
   */
  public static void checkAccessibilitySetting() {
    Context context = App.getApplication();
    if (!isAccessibilitySettingsOn()) {
      // 引导至辅助功能设置页面
      App.getApplication().startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
          );
      Toast.makeText(App.getApplication(), context.getString(R.string.openAccessibility), Toast.LENGTH_SHORT)
          .show();
    }
  }


}