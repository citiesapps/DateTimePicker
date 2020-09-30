package com.appmea.datetimepicker.views;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.appmea.colorutils.MaterialColorUtils;
import com.appmea.datetimepicker.CircularListView;
import com.appmea.datetimepicker.DateSelectListener;
import com.appmea.datetimepicker.LoopItem;
import com.appmea.datetimepicker.LoopListener;
import com.appmea.datetimepicker.R;
import com.appmea.datetimepicker.R2;
import com.appmea.datetimepicker.Utils;
import com.appmea.datetimepicker.items.MonthLoopItem;
import com.appmea.datetimepicker.items.StringLoopItem;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static com.appmea.datetimepicker.Constants.ARGUMENT_BUTTON_TITLE;
import static com.appmea.datetimepicker.Constants.ARGUMENT_COLOR_BUTTON;
import static com.appmea.datetimepicker.Constants.ARGUMENT_COLOR_TEXT;
import static com.appmea.datetimepicker.Constants.ARGUMENT_COLOR_TEXT_SELECTED;
import static com.appmea.datetimepicker.Constants.ARGUMENT_FIELDS;
import static com.appmea.datetimepicker.Constants.ARGUMENT_LISTENER_ID;
import static com.appmea.datetimepicker.Constants.ARGUMENT_LOOPS;
import static com.appmea.datetimepicker.Constants.ARGUMENT_MAX_DATE_TIME;
import static com.appmea.datetimepicker.Constants.ARGUMENT_MIN_DATE_TIME;
import static com.appmea.datetimepicker.Constants.ARGUMENT_SELECTED_DATE_TIME;
import static com.appmea.datetimepicker.Constants.ARGUMENT_TEXT_SIZE;
import static com.appmea.datetimepicker.Constants.ARGUMENT_TITLE;
import static com.appmea.datetimepicker.Constants.TAG_DTP_DIALOG_FRAGMENT;


public class DatePickerDialogFragment extends AppCompatDialogFragment {
    // ====================================================================================================================================================================================
    // <editor-fold desc="Constants">

    private static final String TAG = TAG_DTP_DIALOG_FRAGMENT;

    public static final int NONE = 0x00000000;

    // Loop flags can be used with binary "or" |
    public static final int LOOP_ALL   = 0x11110000;
    public static final int LOOP_YEAR  = 0x01000000;
    public static final int LOOP_MONTH = 0x00100000;
    public static final int LOOP_DAY   = 0x00010000;

    // Field flags can be used with binary "or" |
    public static final int FIELD_ALL   = 0x00001111;
    public static final int FIELD_YEAR  = 0x00000100;
    public static final int FIELD_MONTH = 0x00000010;
    public static final int FIELD_DAY   = 0x00000001;
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Properties">

    private List<StringLoopItem> finalYears;
    private List<MonthLoopItem>  finalMonths;
    private List<StringLoopItem> finalDays;

    private List<StringLoopItem> years;
    private List<MonthLoopItem>  months;
    private List<StringLoopItem> days;

    private int            listenerId;
    private int            fields;
    private int            loops;
    private String         title;
    private String         titleButton;
    private int            textSizeDP;
    private int            colorText;
    private int            colorTextSelected;
    private ColorStateList colorButton;

    private DateTime maxDateTime;
    private DateTime minDateTime;
    private DateTime selectedDateTime;

    private MaterialColorUtils colorUtils;
    private View               view;

    @Nullable private DateSelectListener listener;

    @BindView(R2.id.tv_title)  TextView                         tvTitle;
    @BindView(R2.id.tv_date)   TextView                         tvDate;
    @BindView(R2.id.lv_years)  CircularListView<StringLoopItem> lvYear;
    @BindView(R2.id.lv_months) CircularListView<MonthLoopItem>  lvMonth;
    @BindView(R2.id.lv_days)   CircularListView<StringLoopItem> lvDay;
    @BindView(R2.id.tv_cancel) TextView                         tvCancel;
    @BindView(R2.id.tv_select) TextView                         tvSelect;
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Constructor">

    public static void startFragment(Fragment fragment, Builder builder) {
        Fragment prev = fragment.getChildFragmentManager().findFragmentByTag(TAG);
        if (prev == null) {
            FragmentTransaction ft = fragment.getChildFragmentManager().beginTransaction();

            ft.addToBackStack(null);

            DatePickerDialogFragment dialogFragment = DatePickerDialogFragment.newInstance(builder);
            dialogFragment.show(ft, TAG);
        }
    }

    public static void startFragment(FragmentActivity activity, Builder builder) {
        Fragment prev = activity.getSupportFragmentManager().findFragmentByTag(TAG);
        if (prev == null) {
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();

            ft.addToBackStack(null);

            DatePickerDialogFragment dialogFragment = DatePickerDialogFragment.newInstance(builder);
            dialogFragment.show(ft, TAG);
        }
    }

    public static DatePickerDialogFragment newInstance(Builder builder) {
        DatePickerDialogFragment fragment = new DatePickerDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_LISTENER_ID, builder.listenerId);

        if (builder.titleRes != 0) {
            arguments.putInt(ARGUMENT_TITLE, builder.titleRes);
        } else if (builder.titleString != null) {
            arguments.putString(ARGUMENT_TITLE, builder.titleString);
        }

        if (builder.buttonTextRes != 0) {
            arguments.putInt(ARGUMENT_BUTTON_TITLE, builder.buttonTextRes);
        } else if (builder.buttonTextString != null) {
            arguments.putString(ARGUMENT_BUTTON_TITLE, builder.buttonTextString);
        }

        arguments.putInt(ARGUMENT_FIELDS, builder.fields);
        arguments.putInt(ARGUMENT_LOOPS, builder.loops);
        arguments.putInt(ARGUMENT_TEXT_SIZE, builder.textSizeDP);
        arguments.putInt(ARGUMENT_COLOR_TEXT, builder.colorText);
        arguments.putInt(ARGUMENT_COLOR_TEXT_SELECTED, builder.colorSelectedText);
        arguments.putParcelable(ARGUMENT_COLOR_BUTTON, builder.colorButton);
        arguments.putSerializable(ARGUMENT_MIN_DATE_TIME, builder.minDateTime);
        arguments.putSerializable(ARGUMENT_MAX_DATE_TIME, builder.maxDateTime);
        arguments.putSerializable(ARGUMENT_SELECTED_DATE_TIME, builder.selectedDateTime);

        fragment.setArguments(arguments);
        return fragment;
    }


    /**
     * Builder class
     */
    public static class Builder {
        // ====================================================================================================================================================================================
        // <editor-fold desc="Constants">
        // </editor-fold>


        // ====================================================================================================================================================================================
        // <editor-fold desc="Properties">

        int            listenerId;
        int            fields            = FIELD_ALL;
        int            loops             = NONE;
        int            textSizeDP        = (int) (16 * Resources.getSystem().getDisplayMetrics().density);
        int            colorText         = 0XFFAFAFAF;
        int            colorSelectedText = 0XFF000000;
        ColorStateList colorButton       = ColorStateList.valueOf(0XFF000000);
        DateTime       minDateTime       = new DateTime(1900, 1, 1, 0, 0);
        DateTime       maxDateTime       = new DateTime().plusYears(100);
        DateTime       selectedDateTime  = new DateTime();
        @StringRes
        private int    titleRes;
        private String titleString;
        @StringRes
        private int    buttonTextRes;
        private String buttonTextString;
        // </editor-fold>


        // ====================================================================================================================================================================================
        // <editor-fold desc="Constructor">


        public Builder(int listenerId) {
            this.listenerId = listenerId;
        }

        /**
         * Set field flag. <br>
         * Can be used with BIT-wise OR
         * <br>
         * Has to be one of the following values:
         * <ul>
         * <li>{@link #FIELD_ALL   }</li>
         * <li>{@link #FIELD_YEAR  }</li>
         * <li>{@link #FIELD_MONTH }</li>
         * <li>{@link #FIELD_DAY   }</li>
         * </ul>
         *
         * @param fields Field flag
         * @return Builder
         */
        public Builder withFields(int fields) {
            this.fields = fields;
            return this;
        }

        /**
         * Set loop flag. <br>
         * Can be used with BIT-wise OR
         * <br>
         * Has to be one of the following values:
         * <ul>
         * <li>{@link #LOOP_ALL   }</li>
         * <li>{@link #LOOP_YEAR  }</li>
         * <li>{@link #LOOP_MONTH }</li>
         * <li>{@link #LOOP_DAY   }</li>
         * </ul>
         *
         * @param loops Loop flag
         * @return Builder
         */
        public Builder withLoops(int loops) {
            this.loops = loops;
            return this;
        }

        public Builder withTitle(@StringRes int titleRes) {
            this.titleRes = titleRes;
            return this;
        }

        public Builder withTitle(String titleString) {
            this.titleString = titleString;
            return this;
        }

        public Builder withButtonText(@StringRes int buttonTextRes) {
            this.buttonTextRes = buttonTextRes;
            return this;
        }

        public Builder withButtonText(String buttonTextString) {
            this.buttonTextString = buttonTextString;
            return this;
        }

        public Builder withMaxDateTime(DateTime maxDateTime) {
            this.maxDateTime = maxDateTime;
            return this;
        }

        public Builder withMinDateTime(DateTime minDateTime) {
            this.minDateTime = minDateTime;
            return this;
        }

        public Builder withSelectedDateTime(DateTime selectedDateTime) {
            this.selectedDateTime = selectedDateTime;
            return this;
        }

        public Builder withTextSize(int textSizeDP) {
            this.textSizeDP = textSizeDP;
            return this;
        }

        public Builder withTextColor(int colorText) {
            this.colorText = colorText;
            return this;
        }

        public Builder withSelectedTextColor(int colorSelectedText) {
            this.colorSelectedText = colorSelectedText;
            return this;
        }

        public Builder withButtonColor(int colorButton) {
            this.colorButton = ColorStateList.valueOf(colorButton);
            return this;
        }

        public Builder withButtonColor(ColorStateList colorButton) {
            this.colorButton = colorButton;
            return this;
        }
        // </editor-fold>
    }
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Android Lifecycle">

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        colorUtils = new MaterialColorUtils(context);

        // Parent is a fragment (getParentFragment() returns null, if no parent fragment exists and is directly attached to an activity
        if (getParentFragment() instanceof DateSelectListener) {
            listener = (DateSelectListener) getParentFragment();
        } else if (context instanceof DateSelectListener) {
            listener = (DateSelectListener) context;
        } else {
            Timber.e("Parent of " + DatePickerDialogFragment.class.getName() + " must implement " + DateSelectListener.class.getName());
        }
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            listenerId = getArguments().getInt(ARGUMENT_LISTENER_ID);

            Object o = getArguments().get(ARGUMENT_TITLE);
            if (o instanceof String) {
                title = (String) o;
            } else if (o instanceof Integer) {
                title = getString((Integer) o);
            }

            o = getArguments().get(ARGUMENT_BUTTON_TITLE);
            if (o instanceof String) {
                titleButton = (String) o;
            } else if (o instanceof Integer) {
                titleButton = getString((Integer) o);
            }

            fields = getArguments().getInt(ARGUMENT_FIELDS);
            loops = getArguments().getInt(ARGUMENT_LOOPS);
            textSizeDP = getArguments().getInt(ARGUMENT_TEXT_SIZE);
            colorText = getArguments().getInt(ARGUMENT_COLOR_TEXT);
            colorTextSelected = getArguments().getInt(ARGUMENT_COLOR_TEXT_SELECTED);
            colorButton = getArguments().getParcelable(ARGUMENT_COLOR_BUTTON);

            minDateTime = (DateTime) getArguments().getSerializable(ARGUMENT_MIN_DATE_TIME);
            maxDateTime = (DateTime) getArguments().getSerializable(ARGUMENT_MAX_DATE_TIME);
            selectedDateTime = (DateTime) getArguments().getSerializable(ARGUMENT_SELECTED_DATE_TIME);

            if (minDateTime.isAfter(maxDateTime)) {
                throw new IllegalArgumentException("Minimum DateTime has to be before Maximum DateTime");
            }

            if (selectedDateTime.isBefore(minDateTime)) {
                selectedDateTime = minDateTime;
            } else if (selectedDateTime.isAfter(maxDateTime)) {
                selectedDateTime = maxDateTime;
            }

            finalYears = createDateItemList(minDateTime.getYear(), maxDateTime.getYear());
            finalMonths = createMonthList(1, 12);
            finalDays = createDateItemList(1, 31);
        } else {
            throw new IllegalArgumentException("%s: use the static starter/new instance method with appropriate parameters to create an instance of " + this.getClass().getName());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        boolean isGerman = DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMM dd yyyy").startsWith("dd");

        view = inflater.inflate(isGerman ? R.layout.dialog_dp_dialog_fragment_de : R.layout.dialog_dp_dialog_fragment_en, container);

        ButterKnife.bind(this, view);

        initViews();
        initLoopViews();

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().getDecorView().getBackground().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.TRANSPARENT, BlendModeCompat.SRC_IN));
        }
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Initialisation">

    private void initViews() {
        if (title != null) {
            tvTitle.setText(title);
            tvTitle.setVisibility(View.VISIBLE);
        }

        if (titleButton != null) {
            tvSelect.setText(titleButton);
        }


        tvDate.setTextColor(colorUtils.getColorOnPrimary());
        tvDate.setBackgroundColor(colorUtils.getColorPrimary());

        tvSelect.setTextColor(colorButton);
        tvCancel.setTextColor(colorButton);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            tvCancel.setBackground(colorUtils.createRippleSurface());
            tvSelect.setBackground(colorUtils.createRippleSurface());
        }
    }

    private void initLoopViews() {
        if (yearsEnabled()) {
            lvYear.setVisibility(View.VISIBLE);
            years = createDateItemList(minDateTime.getYear(), maxDateTime.getYear());
            lvYear.initialize(lvYear.new Initializer()
                                      .items(years)
                                      .loopEnabled((loops & LOOP_YEAR) != 0)
                                      .listener(new LoopListener() {
                                          @Override
                                          public void onItemSettled(LoopItem item) {
                                              handleYearSelected(item);
                                          }

                                          @Override
                                          public void onItemScrolled(LoopItem item) {
                                              if (item == null) {
                                                  return;
                                              }
                                              updateSelectedItemText(selectedDateTime.year().setCopy(item.getText()));
                                          }
                                      })
                                      .initPosition(calcInitYear())
                                      .textSize(textSizeDP)
                                      .textColor(colorText)
                                      .selectedTextColor(colorTextSelected)
            );
        }

        if (monthsEnabled()) {
            lvMonth.setVisibility(View.VISIBLE);
            months = createMonthList(calcInitMinMonth(), calcInitMaxMonth());
            lvMonth.initialize(lvMonth.new Initializer()
                                       .items(months)
                                       .loopEnabled((loops & LOOP_MONTH) != 0)
                                       .listener(new LoopListener() {
                                           @Override
                                           public void onItemSettled(LoopItem item) {
                                               handleMonthSelected(item);
                                           }

                                           @Override
                                           public void onItemScrolled(LoopItem item) {
                                               if (item == null) {
                                                   return;
                                               }
                                               updateSelectedItemText(selectedDateTime.monthOfYear().setCopy(item.getText()));
                                           }
                                       })
                                       .initPosition(calcInitMonth())
                                       .textSize(textSizeDP)
                                       .textColor(colorText)
                                       .selectedTextColor(colorTextSelected)
            );
        }

        if (daysEnabled()) {
            lvDay.setVisibility(View.VISIBLE);
            days = createDateItemList(calcInitMinDay(), calcInitMaxDay());
            lvDay.initialize(lvDay.new Initializer()
                                     .listener(new LoopListener() {
                                         @Override
                                         public void onItemSettled(LoopItem item) {
                                             handleDaySelected(item);
                                         }

                                         @Override
                                         public void onItemScrolled(LoopItem item) {
                                             if (item == null) {
                                                 return;
                                             }
                                             updateSelectedItemText(selectedDateTime.dayOfMonth().setCopy(item.getText()));
                                         }
                                     })
                                     .items(days)
                                     .initPosition(calcInitDay())
                                     .textSize(textSizeDP)
                                     .textColor(colorText)
                                     .selectedTextColor(colorTextSelected)
            );
        }
    }

    private int calcInitMinMonth() {
        return Utils.isSameDay(selectedDateTime, minDateTime) ? minDateTime.getMonthOfYear() : 1;
    }

    private int calcInitMaxMonth() {
        if (selectedDateTime.getYear() != maxDateTime.getYear()) {
            return 12;
        }
        return maxDateTime.getMonthOfYear();
    }

    private int calcInitMinDay() {
        return Utils.isSameDay(selectedDateTime, minDateTime) ? minDateTime.getDayOfMonth() : 1;
    }

    private int calcInitMaxDay() {
        if (selectedDateTime.getYear() == maxDateTime.getYear() && selectedDateTime.getMonthOfYear() == maxDateTime.getMonthOfYear()) {
            return maxDateTime.getDayOfMonth();
        }
        return selectedDateTime.dayOfMonth().getMaximumValue();
    }

    private int calcInitYear() {
        if (minDateTime.isEqual(selectedDateTime)) {
            return 0;
        } else if (maxDateTime.isEqual(selectedDateTime)) {
            return years.size() - 1;
        } else {
            return selectedDateTime.getYear() - minDateTime.getYear();
        }
    }

    private int calcInitMonth() {
        if (minDateTime.isEqual(selectedDateTime)) {
            return 0;
        } else if (maxDateTime.isEqual(selectedDateTime)) {
            return months.size() - 1;
        } else {
            return selectedDateTime.getMonthOfYear() - 1;
        }
    }

    private int calcInitDay() {
        if (minDateTime.isEqual(selectedDateTime)) {
            return 0;
        } else if (maxDateTime.isEqual(selectedDateTime)) {
            return days.size() - 1;
        } else {
            return selectedDateTime.getDayOfMonth() - 1;
        }
    }

    private List<StringLoopItem> createDateItemList(int min, int max) {
        List<StringLoopItem> years = new ArrayList<>();
        for (int i = min; i <= max; i++) {
            years.add(new StringLoopItem(String.valueOf(i)));
        }

        return years;
    }

    private List<MonthLoopItem> createMonthList(int min, int max) {
        List<MonthLoopItem> years = new ArrayList<>();
        for (int i = min; i <= max; i++) {
            years.add(new MonthLoopItem(i));
        }

        return years;
    }
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Interfaces">
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Methods">

    @OnClick(R2.id.tv_select)
    void onSelectClicked() {
        if (listener != null) {
            listener.onDateSelected(listenerId, selectedDateTime);
            listener.onDateSelected(listenerId, selectedDateTime.getYear(), selectedDateTime.getMonthOfYear(), selectedDateTime.getDayOfMonth());
        }
        dismiss();
    }

    @OnClick(R2.id.tv_cancel)
    void onCancelClicked() {
        dismiss();
    }

    private void updateSelectedItemText(DateTime dateTime) {
        tvDate.setText(Utils.getDayMonthAbbrYearFormatter().print(dateTime));
    }

    private void handleYearSelected(LoopItem item) {
        int currentYear = selectedDateTime.getYear();
        int newYear = Integer.parseInt(item.getText());

        if (currentYear != newYear) {
            DateTime newDateTime = selectedDateTime.year().setCopy(newYear);
            newDateTime = updateMonths(newDateTime);
            newDateTime = updateDays(newDateTime);

            selectedDateTime = newDateTime;
        }
        tvDate.setText(Utils.getDayMonthAbbrYearFormatter().print(selectedDateTime));
    }

    private void handleMonthSelected(LoopItem item) {
        int currentMonth = selectedDateTime.getMonthOfYear();

        int newMonth = 1;
        if (item instanceof MonthLoopItem) {
            newMonth = ((MonthLoopItem) item).getItem();
        } else {
            DateTime dateTime = Utils.getMonthAbbrFormatter().parseDateTime(item.getText());
            dateTime.getMonthOfYear();
        }

        if (currentMonth != newMonth) {
            DateTime newDateTime = selectedDateTime.monthOfYear().setCopy(newMonth);
            newDateTime = updateDays(newDateTime);

            selectedDateTime = newDateTime;
        }
        tvDate.setText(Utils.getDayMonthAbbrYearFormatter().print(selectedDateTime));
    }

    private void handleDaySelected(LoopItem item) {
        selectedDateTime = selectedDateTime.dayOfMonth().setCopy(item.getText());
        tvDate.setText(Utils.getDayMonthAbbrYearFormatter().print(selectedDateTime));
    }


    private DateTime updateMonths(DateTime newDateTime) {
        if (!monthsEnabled()) {
            return newDateTime;
        }

        int currentLowerLimit = months.get(0).getItem();
        int currentUpperLimit = months.get(months.size() - 1).getItem();

        int newLowerLimit = calcLowerBoundMonths(currentLowerLimit, newDateTime);
        int newUpperLimit = calcUpperBoundMonths(currentUpperLimit, newDateTime);

        if (currentLowerLimit == newLowerLimit && currentUpperLimit == newUpperLimit) {
            return newDateTime;
        }

        // .subList 1st argument is inclusive, 2nd argument is exclusive
        // As calc...() return the bounds on base 1 basis we have to subtract 1
        months = finalMonths.subList(newLowerLimit - 1, newUpperLimit);

        int currentMonthOfYear = newDateTime.getMonthOfYear();
        if (newLowerLimit > currentMonthOfYear) {
            lvMonth.updateItemsAndScrollTop(months);
            newDateTime = newDateTime.monthOfYear().setCopy(months.get(0).getItem());

        } else if (newUpperLimit < currentMonthOfYear) {
            lvMonth.updateItemsAndScrollBottom(months);
            newDateTime = newDateTime.monthOfYear().setCopy(months.get(months.size() - 1).getItem());

        } else {
            lvMonth.updateItems(months);
        }

        return newDateTime;
    }

    private DateTime updateDays(DateTime newDateTime) {
        if (!daysEnabled()) {
            return newDateTime;
        }

        int currentLowerLimit = Integer.parseInt(days.get(0).getText());
        int currentUpperLimit = Integer.parseInt(days.get(days.size() - 1).getText());

        int newLowerLimit = calcLowerBoundDays(currentLowerLimit, newDateTime);
        int newUpperLimit = calcUpperBoundDays(currentUpperLimit, newDateTime);

        if (currentLowerLimit == newLowerLimit && currentUpperLimit == newUpperLimit) {
            return newDateTime;
        }

        // .subList 1st argument is inclusive, 2nd argument is exclusive
        // As calc...() return the bounds on base 1 basis we have to subtract 1
        days = finalDays.subList(newLowerLimit - 1, newUpperLimit);

        int currentDayOfMonth = newDateTime.getDayOfMonth();
        if (newLowerLimit > currentDayOfMonth) {
            lvDay.updateItemsAndScrollTop(days);
            newDateTime = newDateTime.dayOfMonth().setCopy(Integer.parseInt(days.get(0).getText()));

        } else if (newUpperLimit < currentDayOfMonth) {
            lvDay.updateItemsAndScrollBottom(days);
            newDateTime = newDateTime.dayOfMonth().setCopy(Integer.parseInt(days.get(days.size() - 1).getText()));

        } else {
            lvDay.updateItems(days);
        }

        return newDateTime;
    }

    /**
     * @return The new upper bound <b>BASED 1</b> as JodaTime's DateTime is based 1
     */
    private int calcUpperBoundMonths(int currentUpperLimit, DateTime newDateTime) {
        // Calculate limit based on #maxDateTime
        if (newDateTime.getYear() == maxDateTime.getYear()) {
            // Subtract 1 as JodaTimes months are 1 based
            int months = maxDateTime.getMonthOfYear();
            if (currentUpperLimit > months) {
                return months;
            } else {
                return currentUpperLimit;
            }
        }

        // Calculate limit based on month
        int currentMonth = selectedDateTime.monthOfYear().getMaximumValue();
        int newMonth = newDateTime.monthOfYear().getMaximumValue();

        if (currentMonth != newMonth) {
            return newMonth;
        }

        return currentMonth;
    }

    /**
     * @return The new lower bound <b>BASED 1</b> as JodaTime's DateTime is based 1
     */
    private int calcLowerBoundMonths(int currentLowerLimit, DateTime newDateTime) {
        if (newDateTime.getYear() == minDateTime.getYear()) {
            // Subtract 1 as JodaTimes months are 1 based
            int months = minDateTime.getMonthOfYear();
            if (currentLowerLimit < months) {
                return months;
            } else {
                return currentLowerLimit;
            }
        }
        return 1;
    }

    /**
     * @return The new upper bound <b>BASED 1</b> as JodaTime's DateTime is based 1
     */
    private int calcUpperBoundDays(int currentUpperLimit, DateTime newDateTime) {
        // Calculate limit based on #maxDateTime
        if (newDateTime.getYear() == maxDateTime.getYear() && newDateTime.getMonthOfYear() == maxDateTime.getMonthOfYear()) {
            int day = maxDateTime.getDayOfMonth();
            if (currentUpperLimit > day) {
                return day;
            } else {
                return currentUpperLimit;
            }
        }

        // Calculate limit based on month
        return newDateTime.dayOfMonth().getMaximumValue();
    }

    /**
     * @return The new lower bound <b>BASED 1</b> as JodaTime's DateTime is based 1
     */
    private int calcLowerBoundDays(int currentLowerLimit, DateTime newDateTime) {
        if (newDateTime.getYear() == minDateTime.getYear() && newDateTime.getMonthOfYear() <= minDateTime.getMonthOfYear()) {
            int day = minDateTime.getDayOfMonth();
            if (currentLowerLimit < day) {
                return day;
            } else {
                return currentLowerLimit;
            }
        }
        return 1;
    }

    public static int roundCurrentYearMinus(int subtract) {
        DateTime now = new DateTime();
        int modulo = (now.getYear() - subtract) % 10;
        return now.getYear() - subtract - modulo;
    }

    private int indexOfItem(List<? extends LoopItem> list, String text) {
        for (int i = 0; i < list.size(); i++) {
            LoopItem item = list.get(i);
            if (item.getText().equals(text)) {
                return i;
            }
        }

        return -1;
    }
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Getter & Setter">

    private boolean yearsEnabled() {
        return ((fields & FIELD_YEAR) != 0);
    }

    private boolean monthsEnabled() {
        return ((fields & FIELD_MONTH) != 0);
    }

    private boolean daysEnabled() {
        return ((fields & FIELD_DAY) != 0);
    }
    // </editor-fold>
}
