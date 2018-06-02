package com.chenyee.stephenlau.floatingball.util;
/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.chenyee.stephenlau.floatingball.R;
import com.chenyee.stephenlau.floatingball.ScreenshotCallback;
import com.chenyee.stephenlau.floatingball.Screenshotter;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ScreenCaptureImageActivity extends Activity {
    public static final String TAG = ScreenCaptureImageActivity.class.getSimpleName();

    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private static final int REQUEST_EXTERNAL_STORAGE = 2;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private boolean mIsExist =false;
    private Handler mHandler = new Handler();

    private Messenger mMessenger = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        verifyStoragePermissions(ScreenCaptureImageActivity.this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            takeScreenshot();
        } else {
            Toast.makeText(this, "当前系统不支持快捷截屏!", Toast.LENGTH_SHORT).show();

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 500);
        }


    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void takeScreenshot() {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        //隐藏悬浮球
//        sendMsg(Config.HIDE_BALL, "hide", true);

        if (mediaProjectionManager != null) {
            startActivityForResult(
                    mediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK) {
                Screenshotter.getInstance()
                        .takeScreenshot(this, resultCode, data, new ScreenshotCallback() {
                            @Override
                            public void onScreenshot(final Bitmap bitmap) {
                                Log.d(TAG, "onScreenshot: bitmap "+bitmap);

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hhmmss");
                                        Date date = new Date();
                                        String strDate = dateFormat.format(date);
                                        String dir = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Pictures/Screenshots/";

                                        BitmapUtil.copyImageToExterl(bitmap,dir,strDate+".png");
                                        bitmap.recycle();
                                    }
                                }).start();
                                Toast.makeText(ScreenCaptureImageActivity.this, "截图成功！", Toast.LENGTH_SHORT).show();

//                                if(!mIsExist){
//                                    sendMsg(Config.HIDE_BALL, "hide", false);
//                                        Toast.makeText(ScreenCaptureImageActivity.this, "截图失败！", Toast.LENGTH_SHORT).show();
//                                    mIsExist = true;
//                                }

//                                unbindFloatService();

                                finish();
                            }
                        });
            }else {
                Toast.makeText(ScreenCaptureImageActivity.this, "截图失败！", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(this, "You denied the permission.", Toast.LENGTH_SHORT).show();
        }
    }


    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
    }

    public  void sendMsg(int what,String name,boolean action) {
        Intent intent = new Intent();
        intent.putExtra("what",what);
        intent.putExtra(name, action);
//        intent.setClass(this, FloatService.class);
        startService(intent);
    }
}