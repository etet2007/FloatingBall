package com.chenyee.stephenlau.floatingball.ui.activity;
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
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.chenyee.stephenlau.floatingball.R;
import com.chenyee.stephenlau.floatingball.floatingBall.service.FloatingBallService;
import com.chenyee.stephenlau.floatingball.util.BitmapUtils;
import com.chenyee.stephenlau.floatingball.util.DimensionUtils;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.EXTRAS_COMMAND;

public class ScreenCaptureImageActivity extends Activity {
    public static final String TAG = ScreenCaptureImageActivity.class.getSimpleName();

    private static final int REQUEST_CODE_MEDIA_PROJECTION = 1;
    private static final int REQUEST_CODE_STORAGE = 2;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private MediaProjectionManager mediaProjectionManager;
    private VirtualDisplay virtualDisplay;
    private MediaProjection mediaProjection;

    private static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_CODE_STORAGE
            );
        }
    }

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
        mediaProjectionManager = (MediaProjectionManager) getApplicationContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mediaProjectionManager != null) {
            startActivityForResult(
                    mediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_CODE_MEDIA_PROJECTION);
        }
    }

    /**
     * 处理回调的信息。
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_MEDIA_PROJECTION && resultCode == RESULT_OK) {

            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);

            ImageReader imageReader = ImageReader.newInstance(DimensionUtils.gScreenWidth, DimensionUtils.gScreenHeight, PixelFormat.RGBA_8888, 2);
            HandlerThread processingThread = new HandlerThread("processingThread");
            processingThread.start();
            Handler processingThreadHandler = new Handler(processingThread.getLooper());

            imageReader.setOnImageAvailableListener(reader -> {
                Image image = null;
                try {
                    image = reader.acquireLatestImage();

                    if (image == null) {
                        return;
                    }
                    //图片宽高
                    int width1 = image.getWidth();
                    int height1 = image.getHeight();

                    final Image.Plane[] planes = image.getPlanes();
                    //数据缓冲区
                    final ByteBuffer buffer = planes[0].getBuffer();
                    //像素间距4行填充32
                    int pixelStride = planes[0].getPixelStride();
                    //行间距4352表示存储图片每行的数据
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * width1;

                    Bitmap bitmap = Bitmap.createBitmap(width1 + rowPadding / pixelStride, height1, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, width1, height1);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hhmmss");
                    Date date = new Date();
                    String strDate = dateFormat.format(date);
                    String screenshotFileName = strDate + ".jpg";
                    String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/Screenshots/";
                    BitmapUtils.copyImageToExternal(bitmap, dir, screenshotFileName);
                    bitmap.recycle();

                    Toast.makeText(getApplicationContext(), getString(R.string.screenshot_succeed), Toast.LENGTH_SHORT).show();

                } catch (Exception ignored) {
                    Toast.makeText(this, getString(R.string.screenshot_fail), Toast.LENGTH_SHORT).show();
                } finally {
                    if (image != null) {
                        image.close();
                    }
                    imageReader.close();

                    if (virtualDisplay != null) {
                        virtualDisplay.release();
                        virtualDisplay = null;
                    }
                    if (mediaProjection != null) {
                        mediaProjection.stop();
                        mediaProjection = null;
                    }
                    finish();
                }
            }, processingThreadHandler);

            virtualDisplay = mediaProjection.createVirtualDisplay("screenshot", DimensionUtils.gScreenWidth, DimensionUtils.gScreenHeight,
                    DimensionUtils.gScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader.getSurface(), null, null);
        } else {
            Toast.makeText(this, getString(R.string.screenshot_fail), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        setBallIsHide(false);
        super.onDestroy();
    }

    private void setBallIsHide(boolean isHide) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRAS_COMMAND, FloatingBallService.TYPE_HIDE_TEMPORARILY);
        bundle.putBoolean("isHide", isHide);

        Intent intent = new Intent(this, FloatingBallService.class)
                .putExtras(bundle);
        startService(intent);
    }
}