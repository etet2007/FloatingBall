package com.chenyee.stephenlau.floatingball.util;

import static com.chenyee.stephenlau.floatingball.App.getApplication;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.view.inputmethod.InputMethodManager;

import com.chenyee.stephenlau.floatingball.App;

import java.lang.reflect.Method;
import java.util.List;

public class InputMethodDetector {

    public static int inputMethodWindowHeight;

    public static boolean detectIsInputingWithHeight(int heightThreshold) {
        boolean isInputing = false;
        Context context = App.getApplication().getApplicationContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ 使用 WindowInsets
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            WindowMetrics windowMetrics = windowManager.getCurrentWindowMetrics();
            WindowInsets windowInsets = windowMetrics.getWindowInsets();
            int imeHeight = windowInsets.getInsets(WindowInsets.Type.ime()).bottom;
            isInputing = imeHeight > heightThreshold;
            inputMethodWindowHeight = imeHeight;
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            // 旧版本兼容方案
            try {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                Class<?> clazz = imm.getClass();
                Method method = clazz.getMethod("getInputMethodWindowVisibleHeight");
                inputMethodWindowHeight = (int) method.invoke(imm);
                isInputing = inputMethodWindowHeight > heightThreshold;
            } catch (Exception e) {
                Log.e("InputMethodDetector", "Failed to detect IME height", e);
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
