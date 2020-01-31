package com.appmea.datetimepicker.views;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.appmea.datetimepicker.Constants;
import com.appmea.datetimepicker.LoopItem;
import com.appmea.datetimepicker.LoopView;
import com.appmea.datetimepicker.R;
import com.appmea.datetimepicker.R2;
import com.appmea.datetimepicker.StringLoopItem;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.appmea.datetimepicker.Constants.ARGUMENT_FIELDS;
import static com.appmea.datetimepicker.Constants.ARGUMENT_LOOPS;
import static com.appmea.datetimepicker.Constants.ARGUMENT_MAX_DATE_TIME;
import static com.appmea.datetimepicker.Constants.ARGUMENT_MIN_DATE_TIME;
import static com.appmea.datetimepicker.Constants.ARGUMENT_TEXT_SIZE;
import static com.appmea.datetimepicker.Constants.TAG_DTP_DIALOG_FRAGMENT;


public class DatePickerDialogFragment extends DialogFragment {
    // ====================================================================================================================================================================================
    // <editor-fold desc="Constants">

    private static final String TAG = TAG_DTP_DIALOG_FRAGMENT;

    public static final int NONE = 0x00000000;

    public static final int LOOP_ALL   = 0x11110000;
    public static final int LOOP_YEAR  = 0x01000000;
    public static final int LOOP_MONTH = 0x00100000;
    public static final int LOOP_DAY   = 0x00010000;

    public static final int FIELD_ALL   = 0x00001111;
    public static final int FIELD_YEAR  = 0x00000100;
    public static final int FIELD_MONTH = 0x00000010;
    public static final int FIELD_DAY   = 0x00000001;
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Properties">

    private Context context;

    private List<StringLoopItem> finalYears;
    private List<StringLoopItem> finalMonths;
    private List<StringLoopItem> finalDays;

    private List<StringLoopItem> years;
    private List<StringLoopItem> months;
    private List<StringLoopItem> days;

    private int fields;
    private int loops;
    private int textSizeDP;

    private DateTime maxDateTime;
    private DateTime minDateTime;
    private DateTime selectedDateTime;

    private View view;

    @BindView(R2.id.lv_years)  LoopView<StringLoopItem> lvYear;
    @BindView(R2.id.lv_months) LoopView<StringLoopItem> lvMonth;
    @BindView(R2.id.lv_days)   LoopView<StringLoopItem> lvDay;
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
        arguments.putInt(Constants.ARGUMENT_FIELDS, builder.fields);
        arguments.putInt(ARGUMENT_LOOPS, builder.loops);
        arguments.putInt(ARGUMENT_TEXT_SIZE, builder.textSizeDP);
        arguments.putSerializable(ARGUMENT_MIN_DATE_TIME, builder.minDateTime);
        arguments.putSerializable(ARGUMENT_MAX_DATE_TIME, builder.maxDateTime);

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

        int      fields      = FIELD_ALL;
        int      loops       = NONE;
        int      textSizeDP  = (int) (16 * Resources.getSystem().getDisplayMetrics().density);
        DateTime maxDateTime = new DateTime();
        DateTime minDateTime = new DateTime(1900, 1, 1, 0, 0);
        // </editor-fold>


        // ====================================================================================================================================================================================
        // <editor-fold desc="Constructor">

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

        public Builder withMaxDateTime(DateTime maxDateTime) {
            this.maxDateTime = maxDateTime;
            return this;
        }

        public Builder withMinDateTime(DateTime minDateTime) {
            this.minDateTime = minDateTime;
            return this;
        }

        public Builder withTextSize(int textSizeDP) {
            this.textSizeDP = textSizeDP;
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
        this.context = context;

//        // Parent is a fragment (getParentFragment() returns null, if no parent fragment exists and is directly attached to an activity
//        if (getParentFragment() instanceof ConnectDialogListener) {
//            listener = (ConnectDialogListener) getParentFragment();
//        } else if (context instanceof ConnectDialogListener) {
//            listener = (ConnectDialogListener) context;
//        } else {
//            throw new IllegalArgumentException("Parent of " + DatePickerDialogFragment.class.getName() + " must implement " + ConnectDialogListener.class.getName());
//        }
    }

    @Override
    public void onDetach() {
        context = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fields = getArguments().getInt(ARGUMENT_FIELDS);
            loops = getArguments().getInt(ARGUMENT_LOOPS);
            textSizeDP = getArguments().getInt(ARGUMENT_TEXT_SIZE);

            minDateTime = (DateTime) getArguments().getSerializable(ARGUMENT_MIN_DATE_TIME);
            maxDateTime = (DateTime) getArguments().getSerializable(ARGUMENT_MAX_DATE_TIME);

            if (minDateTime.isAfter(maxDateTime)) {
                throw new IllegalArgumentException("Minimum DateTime has to be before Maximum DateTime");
            }

            DateTime now = new DateTime();
            if (now.isBefore(minDateTime)) {
                selectedDateTime = minDateTime;
            } else if (now.isAfter(maxDateTime)) {
                selectedDateTime = maxDateTime;
            } else {
                selectedDateTime = now;
            }

            finalYears = createYears(minDateTime.getYear(), maxDateTime.getYear());
            finalMonths = createYears(1, 12);
            finalDays = createYears(1, 31);
        } else {
//            Utils.writeStarterError(this);
            dismiss();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_dp_dialog_fragment, container);

        ButterKnife.bind(this, view);
        initLoopViews();

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
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

    private void initLoopViews() {
//        if ((fields & FIELD_YEAR) != 0) {
//            years = createYears(minDateTime.getYear(), maxDateTime.getYear());
//            lvYear.setItems(years);
//            lvYear.setVisibility(View.VISIBLE);
//            lvYear.setLoopEnabled((loops & LOOP_YEAR) != 0);
////            lvYear.setListener(this::handleYearSelected);
//            lvYear.setInitPosition(0);//calcInitYear());
////            lvYear.setInitPosition(calcInitYear());
//            lvYear.setTextSize(textSizeDP);
//        }
//
//        if ((fields & FIELD_MONTH) != 0) {
//            months = createMonths(minDateTime.getMonthOfYear(), calcInitMaxMonth());
//            lvMonth.setItems(months);
//            lvMonth.setVisibility(View.VISIBLE);
//            lvMonth.setLoopEnabled((loops & LOOP_MONTH) != 0);
////            lvMonth.setListener(this::handleMonthSelected);
//            lvYear.setInitPosition(0);//calcInitYear());
////            lvMonth.setInitPosition(calcInitMonth());
//            lvMonth.setTextSize(textSizeDP);
//        }

        if ((fields & FIELD_DAY) != 0) {
            days = createDays(minDateTime.getDayOfMonth(), calcInitMaxDay());
            lvDay.setItems(days);
            lvDay.setVisibility(View.VISIBLE);
            lvDay.setLoopEnabled((loops & LOOP_DAY) != 0);
//            lvDay.setListener(this::handleDaySelected);
            lvYear.setInitPosition(4);//calcInitYear());
//            lvDay.setInitPosition(calcInitDay());
            lvDay.setTextSize(textSizeDP);
        }
    }

    private int calcInitMaxMonth() {
        if (minDateTime.getYear() != maxDateTime.getYear()) {
            return 12;
        }
        return maxDateTime.getMonthOfYear();
    }

    private int calcInitMaxDay() {
        if (minDateTime.getYear() == maxDateTime.getYear() && minDateTime.getMonthOfYear() == maxDateTime.getMonthOfYear()) {
            return maxDateTime.getDayOfMonth();
        }
        return minDateTime.dayOfMonth().getMaximumValue();
    }

    private int calcInitYear() {
        if (minDateTime.isEqual(selectedDateTime)) {
            return 0;
        } else if (maxDateTime.isEqual(selectedDateTime)) {
            return years.size()-1;
        }else{
            return selectedDateTime.getYear() - minDateTime.getYear();
        }
    }

    private int calcInitMonth() {
        if (minDateTime.isEqual(selectedDateTime)) {
            return 0;
        } else if (maxDateTime.isEqual(selectedDateTime)) {
            return months.size()-1;
        }else{
            return selectedDateTime.getMonthOfYear() - minDateTime.getMonthOfYear();
        }
    }

    private int calcInitDay() {
        if (minDateTime.isEqual(selectedDateTime)) {
            return 0;
        } else if (maxDateTime.isEqual(selectedDateTime)) {
            return days.size()-1;
        }else{
            return selectedDateTime.getDayOfMonth() - minDateTime.getDayOfMonth();
        }
    }

    private List<StringLoopItem> createYears(int min, int max) {
        List<StringLoopItem> years = new ArrayList<>();
        for (int i = min; i <= max; i++) {
            years.add(new StringLoopItem(String.valueOf(i)));
        }

        return years;
    }

    private List<StringLoopItem> createMonths(int min, int max) {
        List<StringLoopItem> months = new ArrayList<>();
        for (int i = min; i <= max; i++) {
            months.add(new StringLoopItem(String.valueOf(i)));
        }

        return months;
    }

    private List<StringLoopItem> createDays(int min, int max) {
        List<StringLoopItem> days = new ArrayList<>();
        for (int i = min; i <= max; i++) {
            days.add(new StringLoopItem(String.valueOf(i)));
        }

        return days;
    }
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Interfaces">
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Methods">

    private void handleYearSelected(LoopItem item) {
        int currentYear = selectedDateTime.getYear();
        int newYear = Integer.parseInt(item.getText());

        if (currentYear != newYear) {
            DateTime newDateTime = selectedDateTime.year().setCopy(newYear);
            updateMonths(newDateTime);
            updateDays(newDateTime);

            selectedDateTime = newDateTime;
        }
    }

    private void handleMonthSelected(LoopItem item) {
        int currentMonth = selectedDateTime.getMonthOfYear();
        int newMonth = Integer.parseInt(item.getText());

        if (currentMonth != newMonth) {
            DateTime newDateTime = selectedDateTime.monthOfYear().setCopy(newMonth);
            updateDays(newDateTime);

            selectedDateTime = newDateTime;
        }
    }

    private void handleDaySelected(LoopItem item) {
        selectedDateTime = selectedDateTime.dayOfMonth().setCopy(item.getText());
    }


    private void updateMonths(DateTime newDateTime) {
        int currentLowerLimit = Integer.parseInt(months.get(0).getText());
        int currentUpperLimit = Integer.parseInt(months.get(months.size() - 1).getText());

        int newLowerLimit = calcLowerBoundMonths(currentLowerLimit, newDateTime);
        int newUpperLimit = calcUpperBoundMonths(currentUpperLimit, newDateTime);

        if (currentLowerLimit != newLowerLimit || currentUpperLimit != newUpperLimit) {
            // .subList 1st argument is inclusive, 2nd argument is exclusive
            // As calc...() return the bounds on base 1 basis we have to subtract 1
            months = finalMonths.subList(newLowerLimit - 1, newUpperLimit);
            lvMonth.setItems(months);
        }
    }

    private void updateDays(DateTime newDateTime) {
        int currentLowerLimit = Integer.parseInt(days.get(0).getText());
        int currentUpperLimit = Integer.parseInt(days.get(days.size() - 1).getText());

        int newLowerLimit = calcLowerBoundDays(currentLowerLimit, newDateTime);
        int newUpperLimit = calcUpperBoundDays(currentUpperLimit, newDateTime);

        if (currentLowerLimit != newLowerLimit || currentUpperLimit != newUpperLimit) {
            // .subList 1st argument is inclusive, 2nd argument is exclusive
            // As calc...() return the bounds on base 1 basis we have to subtract 1
            days = finalDays.subList(newLowerLimit - 1, newUpperLimit);
            lvDay.setItems(days);
        }
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


    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Getter & Setter">
    // </editor-fold>
}
