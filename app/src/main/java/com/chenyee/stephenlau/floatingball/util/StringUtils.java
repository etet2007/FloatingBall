package com.chenyee.stephenlau.floatingball.util;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.webkit.MimeTypeMap;
import android.widget.Toast;


import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StringUtils {

    public static void showToast(Context x, String s) {
        Toast t = Toast.makeText(x, s, Toast.LENGTH_SHORT);
        t.show();
    }

}
