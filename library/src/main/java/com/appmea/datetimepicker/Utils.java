package com.appmea.datetimepicker;

import android.text.format.DateFormat;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;

public class Utils {
    /**
     * Check if 2 dates are the same day, ignoring their time
     *
     * @param date1 First date
     * @param date2 Second date
     * @return True if both dates are the same day; False otherwise
     */
    public static boolean isSameDay(DateTime date1, DateTime date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        return date1.getDayOfYear() == date2.getDayOfYear() && date1.getYear() == date2.getYear();
    }


    private static final String dayOfWeekDate4 = "E MMM dd yyyy HH:mm";

    public static DateTimeFormatter getDayOfWeekMonthAbbrYearTime() {
        return DateTimeFormat.forPattern(DateFormat.getBestDateTimePattern(Locale.getDefault(), dayOfWeekDate4));
    }
}
