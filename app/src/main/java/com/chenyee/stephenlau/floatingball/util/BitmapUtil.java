package com.chenyee.stephenlau.floatingball.util;

import android.graphics.Bitmap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtil {
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

    public static boolean copyImageToExterl(Bitmap bitmap,String path,String fileName){
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        file = new File(path, fileName);
        boolean isSucceed = false;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            isSucceed= bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                return isSucceed;
            }
        }
    }
}
