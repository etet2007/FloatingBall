package com.chenyee.stephenlau.floatingball.util;

import static com.chenyee.stephenlau.floatingball.App.getApplication;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import com.chenyee.stephenlau.floatingball.App;
import java.lang.reflect.Method;
import java.util.List;

public class InputMethodDetector {

  public static int inputMethodWindowHeight;

  public static boolean detectIsInputing() {
    boolean isInputing = false;

    if (android.os.Build.VERSION.SDK_INT > 20) {//Work
      try {
        InputMethodManager imm = (InputMethodManager) App.getApplication().getApplicationContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE);
        Class clazz = imm.getClass();
        Method method = clazz.getMethod("getInputMethodWindowVisibleHeight", null);
        method.setAccessible(true);
        inputMethodWindowHeight = (Integer) method.invoke(imm, null);

        if (inputMethodWindowHeight > 100) {
          isInputing = true;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {//应该不work
      String defaultInputName = Settings.Secure
          .getString(getApplication().getApplicationContext().getContentResolver(),
              Settings.Secure.DEFAULT_INPUT_METHOD);
      defaultInputName = defaultInputName.substring(0, defaultInputName.indexOf("/"));

      ActivityManager activityManager = (ActivityManager) getApplication().getApplicationContext()
          .getSystemService(Context.ACTIVITY_SERVICE);
      List<RunningAppProcessInfo> appProcesses = activityManager
          .getRunningAppProcesses();

      for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
        if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
          if (appProcess.processName.equals(defaultInputName)) {
            isInputing = true;
            break;
          }
        }
      }
    }

    return isInputing;
  }
}
