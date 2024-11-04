package com.ajsoftware.khata.utils;

import android.content.Context;
import android.graphics.Color;

import com.ajsoftware.khata.R;

import io.github.muddz.styleabletoast.StyleableToast;

public class ToastUtils {

    public static void successToast(Context context, String message) {
        new StyleableToast.Builder(context).text(message).textSize(12f).textColor(Color.WHITE)
                .backgroundColor(context.getColor(R.color.blue)).solidBackground()
                .cornerRadius(8).show();
    }

    public static void errorToast(Context context, String message) {
        new StyleableToast.Builder(context).text(message).textSize(12f).textColor(Color.WHITE)
                .backgroundColor(Color.RED).solidBackground()
                .cornerRadius(8).show();
    }
}
