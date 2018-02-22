package com.chenyee.stephenlau.floatingball.util;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.chenyee.stephenlau.floatingball.LockReceiver;
import com.chenyee.stephenlau.floatingball.R;

/**
 * Created by lqtian on 2018/2/7.
 */

public class LockScreenUtil {

//    private DevicePolicyManager policyManager;
//    private ComponentName componentName;

    public static boolean canLockScreen(Context context){
        DevicePolicyManager policyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        // Identifier for a specific application component (Activity, Service, BroadcastReceiver, or  ContentProvider)
        ComponentName componentName = new ComponentName(context, LockReceiver.class);

        if (!policyManager.isAdminActive(componentName)) {
            return false;
        }else {
            return true;
        }
    }

    public static void lockScreen(Context context){
        DevicePolicyManager policyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        // Identifier for a specific application component (Activity, Service, BroadcastReceiver, or  ContentProvider)
        ComponentName componentName = new ComponentName(context, LockReceiver.class);
        if (policyManager.isAdminActive(componentName)) {//判断是否有权限
            policyManager.lockNow();
//            finish();
        }else{
            //使用隐式意图调用系统方法来激活指定的设备管理器
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, R.string.app_name);
            context.startActivity(intent);
        }
    }

}
