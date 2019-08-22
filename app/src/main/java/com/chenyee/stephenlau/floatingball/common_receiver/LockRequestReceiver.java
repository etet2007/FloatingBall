package com.chenyee.stephenlau.floatingball.common_receiver;

import android.app.admin.DeviceAdminReceiver;

/**
 * Created by lqtian on 2017/9/29.
 */

//在manifests中声明好就可以
//android:permission="android.permission.BIND_DEVICE_ADMIN" >
//  <meta-data
//          android:name="android.app.device_admin"
//          android:resource="@xml/lock_screen" />
//          <intent-filter>
//          <action android:name="android.app.action.DEVICE_ADMIN_ENABLED" />
//          </intent-filter>

public class LockRequestReceiver extends DeviceAdminReceiver {

}
