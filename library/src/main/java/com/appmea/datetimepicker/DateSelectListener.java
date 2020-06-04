package com.appmea.datetimepicker;

import org.joda.time.DateTime;

public interface DateSelectListener {
    default void onDateSelected(int listenerId, DateTime dateTime) {
    }

    default void onDateSelected(int listenerId, int year, int month, int day) {
    }

    default void onTimeSelected(int listenerId, int hour, int minute) {
    }
}
