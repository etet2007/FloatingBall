package com.chenyee.stephenlau.floatingball.activity;
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
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;

import com.chenyee.stephenlau.floatingball.R;
import com.chenyee.stephenlau.floatingball.ScreenshotCallback;
import com.chenyee.stephenlau.floatingball.Screenshot;
import com.chenyee.stephenlau.floatingball.floatBall.FloatingBallService;
import com.chenyee.stephenlau.floatingball.util.BitmapUtil;

import android.graphics.Bitmap;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.EXTRA_TYPE;

public class ScreenCaptureImageActivity extends Activity {
    public static final String TAG = ScreenCaptureImageActivity.class.getSimpleName();

    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private static final int REQUEST_EXTERNAL_STORAGE = 2;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

//    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        verifyStoragePermissions(ScreenCaptureImageActivity.this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            takeScreenshot();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.no_support_screenshot), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void takeScreenshot() {
        //隐藏悬浮球
        setBallIsHide(true);

        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mediaProjectionManager != null) {
            startActivityForResult(
                    mediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION);
        }
    }

    /**
     * 处理回调的信息。
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK) {
                //返回的数据在data中
                Screenshot.getInstance()//其实不懂为什么要使用单例子？
                        .takeScreenshot(getApplicationContext(), resultCode, data, new ScreenshotCallback() {
                            @Override
                            public void onScreenshot(final Bitmap bitmap) {

                                //文件处理 另开线程
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hhmmss");
                                        Date date = new Date();
                                        String strDate = dateFormat.format(date);
                                        String dir = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Pictures/Screenshots/";

                                        String fileName = strDate + ".jpg";

                                        BitmapUtil.copyImageToExternal(bitmap, dir, fileName);

                                        bitmap.recycle();
                                    }
                                }).start();

                                Toast.makeText(getApplicationContext(), getString(R.string.screenshot_succeed), Toast.LENGTH_SHORT).show();
                                setBallIsHide(false);
                                finish();
                            }
                        });
            }else {
                Toast.makeText(ScreenCaptureImageActivity.this, getString(R.string.screenshot_fail), Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(this, getString(R.string.screenshot_fail), Toast.LENGTH_SHORT).show();
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

    private void setBallIsHide(boolean isHide) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_TYPE, FloatingBallService.TYPE_HIDE);
        bundle.putBoolean("isHide",isHide);
        Intent intent = new Intent(this, FloatingBallService.class)
                .putExtras(bundle);
        startService(intent);
    }
}