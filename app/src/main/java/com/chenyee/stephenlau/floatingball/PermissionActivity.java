package com.chenyee.stephenlau.floatingball;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PermissionActivity extends AppCompatActivity {
    private static final String TAG ="PermissionActivity";

    @BindView(R.id.drawOverlays_button) Button drawOverlaysButton;
    @BindView(R.id.accessibility_button) Button accessibilityButton;
    @BindView(R.id.lockScreen_button) Button lockScreenButton;
    @BindView(R.id.logo_image_view) AppCompatImageView logoImageView;
    private boolean hasDrawPermission=false;
    private boolean hasAccessibilityPermission=false;
    private boolean hasLockScreenPermission=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        ButterKnife.bind(this);

        logoImageView.animate().translationYBy(250).setDuration(3000).start();

        drawOverlaysButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //申请DrawOverlays权限
                requestDrawOverlaysPermission();
            }
        });

        accessibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestAccessibility();

            }
        });
        lockScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Identifier for a specific application component (Activity, Service, BroadcastReceiver, or  ContentProvider)
                ComponentName componentName = new ComponentName(PermissionActivity.this, LockReceiver.class);
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, R.string.app_name);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= 23) {
            if (Settings.canDrawOverlays(this)) {
                drawOverlaysButton.setEnabled(false);
                hasDrawPermission=true;
            }
        }else {//SDK_INT < 23 没方法
            drawOverlaysButton.setEnabled(false);
            hasDrawPermission=true;
        }

        if (AccessibilityUtil.isAccessibilitySettingsOn(this)) {
            accessibilityButton.setEnabled(false);
            hasAccessibilityPermission=true;
        }
        if(LockScreenUtil.canLockScreen(PermissionActivity.this)){
            lockScreenButton.setEnabled(false);
            hasLockScreenPermission=true;
        }

        if(hasDrawPermission&hasAccessibilityPermission&hasLockScreenPermission){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void requestDrawOverlaysPermission() {
            //Setting :The Settings provider contains global system-level device preferences.
            //Checks if the specified context can draw on top of other apps. As of API level 23,
            // an app cannot draw on top of other apps unless it declares the SYSTEM_ALERT_WINDOW permission
            // in its manifest, and the user specifically grants the app this capability.
            // To prompt the user to grant this approval, the app must send an intent with the action
            // ACTION_MANAGE_OVERLAY_PERMISSION, which causes the system to display a permission management screen.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, 1);
        }
        Toast.makeText(this, "请先允许FloatBall出现在顶部", Toast.LENGTH_SHORT).show();
    }




    private void requestAccessibility() {
            // 引导至辅助功能设置页面
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            Toast.makeText(this,getResources().getString(R.string.openAccessibility) , Toast.LENGTH_SHORT).show();
    }
}
