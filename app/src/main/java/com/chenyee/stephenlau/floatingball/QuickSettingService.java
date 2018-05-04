package com.chenyee.stephenlau.floatingball;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.chenyee.stephenlau.floatingball.activity.MainActivity;
import com.chenyee.stephenlau.floatingball.floatBall.FloatingBallService;
import com.chenyee.stephenlau.floatingball.util.SharedPrefsUtils;

import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.EXTRA_TYPE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_HAS_ADDED_BALL;

/**
 * Description:
 * <p>
 * Created by Liu Qitian on 18-5-3.
 */
@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingService extends TileService {
    private static final String TAG = QuickSettingService.class.getSimpleName();
    private boolean mHasAddedBall = false;
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


    // 点击的时候
    @Override
    public void onClick() {
        Log.d(TAG, "onClick "+ mHasAddedBall);

        if (mHasAddedBall) {
            addFloatBall();
        } else {
            removeFloatBall();
        }
        LocalBroadcastManager.getInstance(QuickSettingService.this).sendBroadcast(new Intent("refreshActivity"));

        mHasAddedBall = !mHasAddedBall;
        refreshTile();
    }


    // 打开下拉菜单的时候调用,当快速设置按钮并没有在编辑栏拖到设置栏中不会调用
    // 在TileAdded之后会调用一次
    @Override
    public void onStartListening () {
        Log.d(TAG, "onStartListening");
        tile = getQsTile();
        mHasAddedBall = SharedPrefsUtils.getBooleanPreference(PREF_HAS_ADDED_BALL, false);
        refreshTile();
    }
    // 关闭下拉菜单的时候调用,当快速设置按钮并没有在编辑栏拖到设置栏中不会调用
    // 在onTileRemoved移除之前也会调用移除
    @Override
    public void onStopListening () {
        Log.d(TAG, "onStopListening");
    }

    private void addFloatBall() {
        Intent intent = new Intent(QuickSettingService.this, FloatingBallService.class);
        Bundle data = new Bundle();
        data.putInt(EXTRA_TYPE, FloatingBallService.TYPE_ADD);
        intent.putExtras(data);
        startService(intent);
    }

    private void removeFloatBall() {
        Intent intent = new Intent(QuickSettingService.this, FloatingBallService.class);
        Bundle data = new Bundle();
        data.putInt(EXTRA_TYPE, FloatingBallService.TYPE_REMOVE);
        intent.putExtras(data);
        startService(intent);
    }
    private void refreshTile() {
        if (mHasAddedBall) {
            tile.setState(Tile.STATE_INACTIVE);
        } else {
            tile.setState(Tile.STATE_ACTIVE);
        }
        tile.updateTile();
    }



}
