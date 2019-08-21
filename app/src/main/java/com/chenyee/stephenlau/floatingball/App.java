package com.chenyee.stephenlau.floatingball;

import android.app.Application;
import com.chenyee.stephenlau.floatingball.util.DimensionUtils;
import com.chenyee.stephenlau.floatingball.util.SharedPrefsUtils;

/**
 * Created by stephenlau on 18-2-24.
 */

public class App extends Application {

    private static App mInstance;

    public static App getApplication() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;

        DimensionUtils.setupScreenSize();
    }
}
