package com.chenyee.stephenlau.floatingball.quick_setting;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.chenyee.stephenlau.floatingball.floatingBall.service.FloatingBallService;
import com.chenyee.stephenlau.floatingball.util.SharedPrefsUtils;

import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.EXTRAS_COMMAND;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_IS_ADDED_BALL_IN_SETTING;

/**
 * Description: <p> Created by Liu Qitian on 18-5-3.
 */
@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingService extends TileService {
    public static final String QUICK_SETTING_REFRESH_MAIN_ACTIVITY_ACTION = "quickSettingRefreshMainActivity";
    private static final String TAG = QuickSettingService.class.getSimpleName();

    private boolean isAddedBall = false;
    private Tile tile;

    //当用户从Edit栏添加到快速设定中调用
    @Override
    public void onTileAdded() {
        Log.d(TAG, "onTileAdded");
    }

    //当用户从快速设定栏中移除的时候调用
    @Override
    public void onTileRemoved() {
        Log.d(TAG, "onTileRemoved");
    }

    /**
     * 打开下拉菜单的时候调用，当快速设置按钮并没有在编辑栏拖到设置栏中不会调用
     * 在TileAdded之后会调用一次
     */
    @Override
    public void onStartListening() {
        Log.d(TAG, "onStartListening");

        tile = getQsTile();

        isAddedBall = SharedPrefsUtils.getBooleanPreference(PREF_IS_ADDED_BALL_IN_SETTING, false);
        Log.d(TAG, "onStartListening isAddedBall:" + isAddedBall);

        refreshTile();
    }

    @Override
    public void onClick() {
        if (isAddedBall) {
            removeFloatBall();
        } else {
            addFloatBall();
        }
        isAddedBall = !isAddedBall;
        SharedPrefsUtils.setBooleanPreference(PREF_IS_ADDED_BALL_IN_SETTING, isAddedBall);

        LocalBroadcastManager.getInstance(QuickSettingService.this).sendBroadcast(new Intent(QUICK_SETTING_REFRESH_MAIN_ACTIVITY_ACTION));

        refreshTile();
    }

    // 关闭下拉菜单的时候调用,当快速设置按钮并没有在编辑栏拖到设置栏中不会调用
    // 在onTileRemoved移除之前也会调用移除
    @Override
    public void onStopListening() {
        Log.d(TAG, "onStopListening");
    }

    private void addFloatBall() {
        Intent intent = new Intent(QuickSettingService.this, FloatingBallService.class);
        Bundle data = new Bundle();
        data.putInt(EXTRAS_COMMAND, FloatingBallService.TYPE_SWITCH_ON);
        intent.putExtras(data);
        startService(intent);
    }

    private void removeFloatBall() {
        Intent intent = new Intent(QuickSettingService.this, FloatingBallService.class);
        Bundle data = new Bundle();
        data.putInt(EXTRAS_COMMAND, FloatingBallService.TYPE_REMOVE_ALL);
        intent.putExtras(data);
        startService(intent);
    }

    private void refreshTile() {
        if (isAddedBall) {
            tile.setState(Tile.STATE_ACTIVE);
        } else {
            tile.setState(Tile.STATE_INACTIVE);
        }
        tile.updateTile();
    }
}
