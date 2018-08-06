package com.chenyee.stephenlau.floatingball.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.chenyee.stephenlau.floatingball.App;

import java.nio.ByteBuffer;

/**
 * Created by omerjerk on 17/2/16.
 * Modify by lqt
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class ScreenshotUtil {
    private static final String TAG = "ScreenshotUtil";
    public static final String NAME_VIRTUAL_DISPLAY = "NAME_VIRTUAL_DISPLAY";
    public static final ScreenshotUtil sInstance = new ScreenshotUtil();

    private VirtualDisplay virtualDisplay;
    private MediaProjection mMediaProjection;
    private ImageReader imageReader;//如果为local变量，会被GC，程序崩溃

    private ScreenshotUtil() {
    }

    /**
     * Takes the screenshot of whatever currently is on the default display.
     *
     * @param resultCode The result code returned by the request for accessing MediaProjection permission
     * @param data       The intent returned by the same request
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void takeScreenshot(@NonNull Context context, int resultCode, Intent data, final ScreenshotCallback cb) {
        //get MediaProjection instance
        final MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) context
                .getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mMediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);

        //get width and height
        final WindowManager windowManager = (WindowManager) App.getApplication().getSystemService(Context.WINDOW_SERVICE);
        final DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        final int screenDensity = metrics.densityDpi;
        final int width = metrics.widthPixels;
        final int height = metrics.heightPixels;

        HandlerThread backgroundThread = new HandlerThread(NAME_VIRTUAL_DISPLAY);
        backgroundThread.start();
        Handler backgroundHandler = new Handler(backgroundThread.getLooper());

        //create ImageReader
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1);

        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader imageReader) {
                Log.d(TAG, "onImageAvailable: ");
                Image image;
                try {
                    image = imageReader.acquireLatestImage();
                } catch (UnsupportedOperationException e) {
                    e.printStackTrace();
                    return;
                }
                if (image == null) {
                    return;
                }
                //图片宽高
                int width = image.getWidth();
                int height = image.getHeight();

                final Image.Plane[] planes = image.getPlanes();
                //数据缓冲区
                final ByteBuffer buffer = planes[0].getBuffer();
                //像素间距4行填充32
                int pixelStride = planes[0].getPixelStride();
                //行间距4352表示存储图片每行的数据
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * width;

                Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);

                image.close();
                if (virtualDisplay != null) {
                    virtualDisplay.release();
                    virtualDisplay = null;
                }
                if (mMediaProjection != null) {
                    mMediaProjection.stop();
                    mMediaProjection = null;
                }
                //回调
                cb.onScreenshot(bitmap);
            }
        }, backgroundHandler);

        
        try {
            //create virtualDisplay
            virtualDisplay = mMediaProjection.createVirtualDisplay("screenshot",
                    width, height, screenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader.getSurface(), null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}