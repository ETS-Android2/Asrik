package com.mitrukahitesh.asrik.helpers;

import java.util.Locale;

public class TimeFormatter {

    public static String formatTime(int hour, int min) {
        return String.format(Locale.getDefault(), "%s:%s", getString(hour), getString(min));
    }

    private static String getString(int n) {
        if (n < 10)
            return "0" + n;
        return n + "";
    }

}
