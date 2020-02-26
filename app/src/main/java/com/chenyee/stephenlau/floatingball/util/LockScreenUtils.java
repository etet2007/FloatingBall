package com.chenyee.stephenlau.floatingball.util;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.chenyee.stephenlau.floatingball.commonReceiver.LockRequestReceiver;
import com.chenyee.stephenlau.floatingball.R;
import com.chenyee.stephenlau.floatingball.setting.activity.PermissionActivity;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by lqtian on 2018/2/7.
 */

public class LockScreenUtils {

    public static boolean canLockScreen(Context context){
        DevicePolicyManager policyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        // Identifier for a specific application component (Activity, Service, BroadcastReceiver, or  ContentProvider)
        ComponentName componentName = new ComponentName(context, LockRequestReceiver.class);

        if (!policyManager.isAdminActive(componentName)) {
            return false;
        }else {
            return true;
        }
    }

    public static void openLockScreenInDevicePolicy(Context context) {
        ComponentName componentName = new ComponentName(context, LockRequestReceiver.class);
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, R.string.app_name);
        context.startActivity(intent);
    }

    public static void lockScreen(Context context){
        DevicePolicyManager policyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        // Identifier for a specific application component (Activity, Service, BroadcastReceiver, or  ContentProvider)
        ComponentName componentName = new ComponentName(context, LockRequestReceiver.class);
        if (policyManager.isAdminActive(componentName)) {//判断是否有权限
            policyManager.lockNow();
        } else {
            Toast.makeText(context, R.string.lock_screen_fail, Toast.LENGTH_SHORT).show();
        }
    }



}
