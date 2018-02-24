package com.chenyee.stephenlau.floatingball;

import android.app.Application;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.Iconics;

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
        Iconics.registerFont(new GoogleMaterial());
        Iconics.registerFont(new CommunityMaterial());
        Iconics.registerFont(new FontAwesome());
    }
}
