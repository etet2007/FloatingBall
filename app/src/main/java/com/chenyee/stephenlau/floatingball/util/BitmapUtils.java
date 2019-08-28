package com.chenyee.stephenlau.floatingball.util;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;

import com.chenyee.stephenlau.floatingball.App;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtils {

    public static boolean copyImage(Bitmap bitmap, String path, String fileName) {
        File file = new File(path, fileName);
        boolean isSucceed = false;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            isSucceed = bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return isSucceed;
    }

    public static boolean copyImageToExternal(Bitmap bitmap, String path, String fileName) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        file = new File(path, fileName);
        boolean isSucceed = false;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            isSucceed = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 最后通知图库更新
        App.getApplication().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(file)));

        return isSucceed;
    }

    /**
     * 复制图片置软件文件夹内。
     */
    public static void copyBackgroundImage(String imagePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        if (bitmap == null) {
            Toast.makeText(App.getApplication(), "Read image error", Toast.LENGTH_LONG).show();
            return;
        }
        //copy source image
        String path = App.getApplication().getFilesDir().toString();
        BitmapUtils.copyImage(bitmap, path, "ballBackground.png");
        bitmap.recycle();
    }
}
