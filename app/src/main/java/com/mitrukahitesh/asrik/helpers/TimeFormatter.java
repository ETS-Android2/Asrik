/*
    Formats time as hh:mm. Example:
    4:8 ---> 04:08
    16:9 ---> 16:09
 */

package com.mitrukahitesh.asrik.helpers;

import java.util.Locale;

public class TimeFormatter {

    /**
     * Formats hour min to hh:mm format
     */
    public static String formatTime(int hour, int min) {
        return String.format(Locale.getDefault(), "%s:%s", getString(hour), getString(min));
    }

    /**
     * Appends 0 to single digit number
     */
    private static String getString(int n) {
        if (n < 10)
            return "0" + n;
        return n + "";
    }

}
