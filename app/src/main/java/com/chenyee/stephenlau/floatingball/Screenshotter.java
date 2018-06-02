package com.chenyee.stephenlau.floatingball;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import java.nio.ByteBuffer;

/**
 * Created by omerjerk on 17/2/16.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class Screenshotter implements ImageReader.OnImageAvailableListener {
    private static final String TAG = "Screenshotter";
    private static Screenshotter mInstance;

    private VirtualDisplay virtualDisplay;
    private int width;
    private int height;
    private int mScreenDensity = 0;

    private ScreenshotCallback cb;
    private ImageReader mImageReader;
    private MediaProjection mMediaProjection;

    /**
     * Get the single instance of the Screenshotter class.
     * @return the instance
     */
    public static Screenshotter getInstance() {
        if (mInstance == null) {
            mInstance = new Screenshotter();
        }

        return mInstance;
    }

    private Screenshotter() {}

    /**
     * Takes the screenshot of whatever currently is on the default display.
     * @param resultCode The result code returned by the request for accessing MediaProjection permission
     * @param data The intent returned by the same request
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Screenshotter takeScreenshot(Context context, int resultCode, Intent data, final ScreenshotCallback cb) {
//        Context context1 = context;
        this.cb = cb;

//        int resultCode1 = resultCode;
//        Intent data1 = data;

        //get width and height
        WindowManager windowManager = (WindowManager) App.getApplication().getSystemService(Context.WINDOW_SERVICE);
//        Point size = new Point();
//        windowManager.getDefaultDisplay().getSize(size);
//        width = size.x;//1080
//        height = size.y;//2034
        //get dpi
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        width = metrics.widthPixels;
        height = metrics.heightPixels;

        //ImageReader
        mImageReader = ImageReader.newInstance(width, height, 0x1,1);
        mImageReader.setOnImageAvailableListener(Screenshotter.this, null);

        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) context
                .getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mMediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
        try {
            //VirtualDisplay
            virtualDisplay = mMediaProjection.createVirtualDisplay("Screenshotter",
                    width, height, mScreenDensity,
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
    public Screenshotter setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onImageAvailable(ImageReader reader) {
        Image image = null;
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
        image.close();
        mImageReader = null;
    }
}