package com.appmea.datetimepicker;

import android.text.format.DateFormat;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;

public class Constants {

    // Just adding a comment to check submodule working flow
    // 2nd one

    private static final String dateTimeFormat = "MMMM dd yyyy HH:mm";

    public static DateTimeFormatter getDayMonthYearTimeFormatter() {
        return DateTimeFormat.forPattern(DateFormat.getBestDateTimePattern(Locale.getDefault(), dateTimeFormat));
    }

    // ====================================================================================================================================================================================
    // <editor-fold desc="Fragment Tags">

    public static final String TAG_DTP_DIALOG_FRAGMENT = "dtp_dialog_frag";
    public static final String TAG_DTP_BOTTOM_SHEET    = "dtp_bottom_sheet";
    // </editor-fold>

    // ====================================================================================================================================================================================
    // <editor-fold desc="Fragment Arguments">
    public static final String ARGUMENT_BUTTON_TITLE        = "BUTTON_title";
    public static final String ARGUMENT_COLOR_BUTTON        = "color_button";
    public static final String ARGUMENT_COLOR_TEXT          = "color_text";
    public static final String ARGUMENT_COLOR_TEXT_SELECTED = "color_text_selected";
    public static final String ARGUMENT_FIELDS              = "fields";
    public static final String ARGUMENT_LOOPS               = "loops";
    public static final String ARGUMENT_MAX_DATE_TIME       = "max_date_time";
    public static final String ARGUMENT_MIN_DATE_TIME       = "min_date_time";
    public static final String ARGUMENT_SELECTED_DATE_TIME  = "selected_date_time";
    public static final String ARGUMENT_TEXT_SIZE           = "text_size";
    public static final String ARGUMENT_TITLE               = "title";
    // </editor-fold>
}
