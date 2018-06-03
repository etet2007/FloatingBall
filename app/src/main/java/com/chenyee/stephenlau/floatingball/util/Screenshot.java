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
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.chenyee.stephenlau.floatingball.App;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by omerjerk on 17/2/16.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class Screenshot implements ImageReader.OnImageAvailableListener {
    private static final String TAG = "Screenshot";
    private static Screenshot mInstance;

    private VirtualDisplay virtualDisplay;
    private int width;
    private int height;

    private ScreenshotCallback cb;
    private ImageReader mImageReader;
    private MediaProjection mMediaProjection;

    /**
     * Get the single instance of the Screenshot class.
     * @return the instance
     */
    public static Screenshot getInstance() {
        if (mInstance == null) {
            mInstance = new Screenshot();
        }
        return mInstance;
    }

    public Screenshot() {}

    /**
     * Takes the screenshot of whatever currently is on the default display.
     * @param resultCode The result code returned by the request for accessing MediaProjection permission
     * @param data The intent returned by the same request
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Screenshot takeScreenshot(Context context, int resultCode, Intent data, ScreenshotCallback cb) {
        this.cb = cb;
        //get width and height
        WindowManager windowManager = (WindowManager) App.getApplication().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        int screenDensity = metrics.densityDpi;
        width = metrics.widthPixels;
        height = metrics.heightPixels;

        //create ImageReader
        mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888,1);
        mImageReader.setOnImageAvailableListener(Screenshot.this, null);

        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) context
                .getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mMediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
        try {
            //create virtualDisplay
            virtualDisplay = mMediaProjection.createVirtualDisplay("screenshot",
                    width, height, screenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mImageReader.getSurface(), null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Set the size of the screenshot to be taken
     * @param width width of the requested bitmap
     * @param height height of the request bitmap
     * @return the singleton instance
     */
    public Screenshot setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onImageAvailable(ImageReader reader) {
        Image image;
        try {
            image = reader.acquireLatestImage();
        }catch (UnsupportedOperationException e){
            e.printStackTrace();
            return;
        }
        if(image == null){
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
//        行间距4352表示存储图片每行的数据
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;

        Bitmap bitmap = Bitmap.createBitmap(width+rowPadding/pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0,width, height);
        image.close();

        //回调
        cb.onScreenshot(bitmap);

        if(virtualDisplay!= null){
            virtualDisplay.release();
            virtualDisplay = null;
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        mImageReader = null;

    }
}