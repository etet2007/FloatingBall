package com.chenyee.stephenlau.floatingball;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by lqtian on 2017/9/29.
 */

//android:permission="android.permission.BIND_DEVICE_ADMIN" >
//  <meta-data
//          android:name="android.app.device_admin"
//          android:resource="@xml/lock_screen" />
//          <intent-filter>
//          <action android:name="android.app.action.DEVICE_ADMIN_ENABLED" />
//          </intent-filter>

public class LockReceiver extends DeviceAdminReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }
    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
    }
    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
    }
}
