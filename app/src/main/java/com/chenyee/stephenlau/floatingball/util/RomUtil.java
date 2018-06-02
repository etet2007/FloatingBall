package com.chenyee.stephenlau.floatingball.util;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by stephenlau on 18-2-22.
 */

public class RomUtil {
    private static final String TAG = "RomUtil";

    public static final String ROM_MIUI = "MIUI";
    public static final String ROM_EMUI = "EMUI";
    public static final String ROM_FLYME = "FLYME";
    public static final String ROM_OPPO = "OPPO";
    public static final String ROM_SMARTISAN = "SMARTISAN";
    public static final String ROM_VIVO = "VIVO";
    public static final String ROM_QIHU = "QIHU";
    public static final String ROM_LENOVO = "LENOVO";
    public static final String ROM_SAMSUNG = "SAMSUNG";
    public static final String ROM_ONEPLUS = "ONEPLUS";

    public static boolean isRom(String nameToCompare){
        String version = Build.DISPLAY.toUpperCase();
        String name = Build.MANUFACTURER.toUpperCase();

        Log.d(TAG, "version: "+Build.DISPLAY.toUpperCase()+" name: "+sName);
        //version: AMIGO3.6.0 name: GIONEE
        //version: ONEPLUS A5010_43_180207 name: ONEPLUS

        return name.equals(nameToCompare);
    }

    private static final String KEY_VERSION_MIUI = "ro.miui.ui.version.name";
    private static final String KEY_VERSION_EMUI = "ro.build.version.emui";
    private static final String KEY_VERSION_OPPO = "ro.build.version.opporom";
    private static final String KEY_VERSION_SMARTISAN = "ro.smartisan.version";
    private static final String KEY_VERSION_VIVO = "ro.vivo.os.version";
    private static final String KEY_VERSION_GIONEE = "ro.gn.sv.version";
    private static final String KEY_VERSION_LENOVO = "ro.lenovo.lvp.version";

    private static String sName;
    private static String sVersion;

    public static boolean isEmui() {
        return check(ROM_EMUI);
    }
    public static boolean isMiui() {
        return check(ROM_MIUI);
    }
    public static boolean isVivo() {
        return check(ROM_VIVO);
    }
    public static boolean isOppo() {
        return check(ROM_OPPO);
    }
    public static boolean isFlyme() {
        return check(ROM_FLYME);
    }
    public static boolean isQihu() {
        return check(ROM_QIHU);
    }
    public static boolean isSmartisan() {
        return check(ROM_SMARTISAN);
    }

    public static String getName() {
        if (sName == null) {
            check("");
        }
        return sName;
    }
    public static String getVersion() {
        if (sVersion == null) {
            check("");
        }
        return sVersion;
    }

    /**
     * 判断是否为该rom
     * @param rom rom的名称
     * @return
     */
    public static boolean check(String rom) {
        //已经有sName，立刻进行判断。
        if (sName != null) {
            return sName.equals(rom);
        }

        if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_MIUI))) {
            sName = ROM_MIUI;
        } else if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_EMUI))){
            sName = ROM_EMUI;
        } else if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_OPPO))){
            sName = ROM_OPPO;
        } else if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_VIVO))){
            sName = ROM_VIVO;
        } else if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_SMARTISAN))){
            sName = ROM_SMARTISAN;
        } else {
            //初始化sVersion sName
            sVersion = Build.DISPLAY;
            if (sVersion.toUpperCase().contains(ROM_FLYME)) {
                sName = ROM_FLYME;
            } else {
                sVersion = Build.UNKNOWN;
                sName = Build.MANUFACTURER.toUpperCase();
            }
            Log.d(TAG, "version: "+Build.DISPLAY.toUpperCase()+" name: "+sName);
            //version: AMIGO3.6.0 name: GIONEE
            //version: ONEPLUS A5010_43_180207 name: ONEPLUS
        }
        return sName.equals(rom);
    }

    public static String getProp(String name) {
        String line = null;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + name);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            Log.e(TAG, "Unable to read prop " + name, ex);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }
}