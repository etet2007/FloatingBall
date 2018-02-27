package com.chenyee.stephenlau.floatingball;

import android.annotation.SuppressLint;
import android.app.Application;
import android.support.v4.view.LayoutInflaterCompat;



/**
 * Created by stephenlau on 18-2-24.
 */

public class App extends Application {
    private static App mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        registerFontIcons();
        initialiseStorage();
    }

    private void initialiseStorage() {
//        Prefs.init(this);
    }

    private void registerFontIcons() {
//        Iconics.registerFont(new GoogleMaterial());
//        Iconics.registerFont(new CommunityMaterial());
//        Iconics.registerFont(new FontAwesome());
    }
}
