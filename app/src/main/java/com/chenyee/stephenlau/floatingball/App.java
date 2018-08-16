package com.chenyee.stephenlau.floatingball;

import android.app.Application;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;
import com.chenyee.stephenlau.floatingball.util.SharedPrefsUtils;
import com.squareup.leakcanary.LeakCanary;


/**
 * Created by stephenlau on 18-2-24.
 */

public class App extends Application {

  private static App mInstance;

  public static int gScreenWidth;
  public static int gScreenHeight;

  @Override
  public void onCreate() {
    super.onCreate();

    mInstance = this;

    SharedPrefsUtils.setApplicationContext(getApplicationContext());

    WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    Point size = new Point();
    if (windowManager == null) return;
    //获取size
    windowManager.getDefaultDisplay().getSize(size);
    gScreenWidth = size.x;
    gScreenHeight = size.y;

    if (LeakCanary.isInAnalyzerProcess(this)) {
      // This process is dedicated to LeakCanary for heap analysis.
      // You should not init your app in this process.
      return;
    }
    LeakCanary.install(this);
    // Normal app init code...
  }

  public static App getApplication() {
    return mInstance;
  }

}
