/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chenyee.stephenlau.floatingball.util;

import static android.content.Context.ACTIVITY_SERVICE;
import static com.chenyee.stephenlau.floatingball.util.RootUtil.rootCommand;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import java.lang.reflect.Method;
import java.util.List;

/**
 * This provides methods to help Activities load their UI.
 */
public class ActivityUtils {

  /**
   * The {@code fragment} is added to the container view with id {@code frameId}. The operation is performed by the
   * {@code fragmentManager}.
   */
  public static void addFragmentToActivity(@NonNull FragmentManager fragmentManager,
      @NonNull Fragment fragment, int frameId) {
    fragmentManager
        .beginTransaction()
        .replace(frameId, fragment)
//                .addToBackStack(null)
        .commit();
  }

  public static void killBackgroundProcesses(Context context, String packageName) {


    String apkRoot = "am force-stop " + packageName;
    rootCommand(apkRoot);


  }

  public static boolean isForeground(Context context, String myPackage) {
    ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
    List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);

    ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
    if (componentInfo.getPackageName().equals(myPackage)) {
      return true;
    }
    return false;
  }
}
