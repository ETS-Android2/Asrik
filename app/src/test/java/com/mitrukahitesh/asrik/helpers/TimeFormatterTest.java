/*
    Tests whether TimeFormatter handles all cases or not
 */

package com.mitrukahitesh.asrik.helpers;

import static org.junit.Assert.*;

import org.junit.Test;

public class TimeFormatterTest {
    /**
     * Tests for single digit hour and double digit minute
     */
    @Test
    public void singleDigitHour() {
        assertEquals("04:31", TimeFormatter.formatTime(4, 31));
    }

    /**
     * Tests for double digit hour and single digit minute
     */
    @Test
    public void singleDigitMinute() {
        assertEquals("11:03", TimeFormatter.formatTime(11, 3));
    }

    /**
     * Tests for single digit hour and single digit minute
     */
    @Test
    public void singleDigitHourMinute() {
        assertEquals("04:03", TimeFormatter.formatTime(4, 3));
    }

    /**
     * Tests for double digit hour and double digit minute
     */
    @Test
    public void noSingleDigit() {
        assertEquals("11:31", TimeFormatter.formatTime(11, 31));
    }
}