package com.chenyee.stephenlau.floatingball.ui.activity;

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

import com.chenyee.stephenlau.floatingball.receiver.LockRequestReceiver;
import com.chenyee.stephenlau.floatingball.R;
import com.chenyee.stephenlau.floatingball.util.AccessibilityUtils;
import com.chenyee.stephenlau.floatingball.util.LockScreenUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.chenyee.stephenlau.floatingball.util.DimensionUtils.dip2px;

public class PermissionActivity extends AppCompatActivity {
    private static final String TAG =PermissionActivity.class.getSimpleName();

    @BindView(R.id.drawOverlays_button) Button drawOverlaysButton;
    @BindView(R.id.accessibility_button) Button accessibilityButton;
    @BindView(R.id.lockScreen_button) Button lockScreenButton;
    @BindView(R.id.logo_image_view) AppCompatImageView logoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        ButterKnife.bind(this);

        logoImageView.animate().translationYBy(dip2px(getApplicationContext(),100)).setDuration(3000).start();

        drawOverlaysButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestDrawOverlaysPermission();
            }
        });

        accessibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccessibilityUtils.checkAccessibilitySetting(PermissionActivity.this);
            }
        });
        lockScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Identifier for a specific application component (Activity, Service, BroadcastReceiver, or  ContentProvider)
                ComponentName componentName = new ComponentName(PermissionActivity.this, LockRequestReceiver.class);
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, R.string.app_name);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        boolean hasDrawPermission = false;
        boolean hasAccessibilityPermission = false;
        boolean hasLockScreenPermission = false;

        //canDrawOverlays
        if (Build.VERSION.SDK_INT >= 23) {
            if (Settings.canDrawOverlays(this)) {
                drawOverlaysButton.setEnabled(false);
                hasDrawPermission = true;
            }
        } else {//SDK_INT < 23 没方法
            drawOverlaysButton.setVisibility(View.GONE);
            hasDrawPermission = true;
        }
        //Accessibility
        if (AccessibilityUtils.isAccessibilitySettingsOn()) {
            accessibilityButton.setEnabled(false);
            hasAccessibilityPermission = true;
        }
        //LockScreen
        if (LockScreenUtils.canLockScreen(PermissionActivity.this)) {
            lockScreenButton.setEnabled(false);
            hasLockScreenPermission = true;
        }

        if (hasDrawPermission & hasAccessibilityPermission & hasLockScreenPermission) {
            startActivity(MainActivity.getStartIntent(getApplicationContext()));
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
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intent, 1);
        }
    }
}
