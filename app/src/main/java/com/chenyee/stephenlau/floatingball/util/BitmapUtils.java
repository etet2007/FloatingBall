package com.chenyee.stephenlau.floatingball.util;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.chenyee.stephenlau.floatingball.App;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtils {
    public static boolean copyImage(Bitmap bitmap,String path,String fileName){
        File file = new File(path, fileName);
        boolean isSucceed = false;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            isSucceed= bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
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

    public static boolean copyImageToExternal(Bitmap bitmap, String path, String fileName){
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        file = new File(path, fileName);
        boolean isSucceed = false;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            isSucceed= bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
//            out.flush();
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
        // 把文件插入到系统图库
//        try {
//            MediaStore.Images.Media.insertImage(App.getApplication().getContentResolver(),
//                    path, fileName, null);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        // 最后通知图库更新
        App.getApplication().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(file)));

        return isSucceed;
    }
}
