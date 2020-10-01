package com.appmea.datetimepicker.views;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.os.Bundle;
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
import com.appmea.datetimepicker.Constants;
import com.appmea.datetimepicker.DateSelectListener;
import com.appmea.datetimepicker.LoopItem;
import com.appmea.datetimepicker.LoopListener;
import com.appmea.datetimepicker.R;
import com.appmea.datetimepicker.R2;
import com.appmea.datetimepicker.Utils;
import com.appmea.datetimepicker.items.StringLoopItem;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static com.appmea.datetimepicker.Constants.ARGUMENT_BUTTON_TITLE;
import static com.appmea.datetimepicker.Constants.ARGUMENT_COLOR_BUTTON;
import static com.appmea.datetimepicker.Constants.ARGUMENT_COLOR_TEXT;
import static com.appmea.datetimepicker.Constants.ARGUMENT_COLOR_TEXT_SELECTED;
import static com.appmea.datetimepicker.Constants.ARGUMENT_LISTENER_ID;
import static com.appmea.datetimepicker.Constants.ARGUMENT_SELECTED_DATE_TIME;
import static com.appmea.datetimepicker.Constants.ARGUMENT_TEXT_SIZE;
import static com.appmea.datetimepicker.Constants.ARGUMENT_TITLE;

public class TimePickerDialogFragment extends AppCompatDialogFragment {
    // ====================================================================================================================================================================================
    // <editor-fold desc="Constants">
    //
    private static final String TAG = Constants.TAG_TIME_PICKER;
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Properties">

    @Nullable
    private DateSelectListener listener;
    private DateTime           selectedTime = new DateTime();

    private int            listenerId;
    private String         title;
    private String         titleButton;
    private int            textSizeDP;
    private int            colorText;
    private int            colorTextSelected;
    private ColorStateList colorButton;

    private MaterialColorUtils colorUtils;
    private View               view;

    @BindView(R2.id.tv_title)         TextView                         tvTitle;
    @BindView(R2.id.lv_hours)         CircularListView<StringLoopItem> lvHours;
    @BindView(R2.id.iv_double_point1) View                             vDoublePoint1;
    @BindView(R2.id.iv_double_point2) View                             vDoublePoint2;
    @BindView(R2.id.lv_minutes)       CircularListView<StringLoopItem> lvMinutes;
    @BindView(R2.id.tv_cancel)        TextView                         tvCancel;
    @BindView(R2.id.tv_select)        TextView                         tvSelect;
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Constructor">

    public static void startFragment(Fragment fragment, Builder builder) {
        Fragment prev = fragment.getChildFragmentManager().findFragmentByTag(TAG);
        if (prev == null) {
            FragmentTransaction ft = fragment.getChildFragmentManager().beginTransaction();

            ft.addToBackStack(null);

            TimePickerDialogFragment dialogFragment = TimePickerDialogFragment.newInstance(builder);
            dialogFragment.show(ft, TAG);
        }
    }

    public static void startFragment(FragmentActivity activity, Builder builder) {
        Fragment prev = activity.getSupportFragmentManager().findFragmentByTag(TAG);
        if (prev == null) {
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();

            ft.addToBackStack(null);

            TimePickerDialogFragment dialogFragment = TimePickerDialogFragment.newInstance(builder);
            dialogFragment.show(ft, TAG);
        }
    }

    public static TimePickerDialogFragment newInstance(Builder builder) {
        TimePickerDialogFragment fragment = new TimePickerDialogFragment();

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

        arguments.putInt(ARGUMENT_TEXT_SIZE, builder.textSizeDP);
        arguments.putInt(ARGUMENT_COLOR_TEXT, builder.colorText);
        arguments.putInt(ARGUMENT_COLOR_TEXT_SELECTED, builder.colorSelectedText);
        arguments.putParcelable(ARGUMENT_COLOR_BUTTON, builder.colorButton);
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
        int            textSizeDP        = (int) (16 * Resources.getSystem().getDisplayMetrics().density);
        int            colorText         = 0XFFAFAFAF;
        int            colorSelectedText = 0XFF000000;
        ColorStateList colorButton       = ColorStateList.valueOf(0XFF000000);
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
            Timber.e("Parent of " + TimePickerDialogFragment.class.getName() + " must implement " + DateSelectListener.class.getName());
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

            textSizeDP = getArguments().getInt(ARGUMENT_TEXT_SIZE);
            colorText = getArguments().getInt(ARGUMENT_COLOR_TEXT);
            colorTextSelected = getArguments().getInt(ARGUMENT_COLOR_TEXT_SELECTED);
            colorButton = getArguments().getParcelable(ARGUMENT_COLOR_BUTTON);

            selectedTime = (DateTime) getArguments().getSerializable(ARGUMENT_SELECTED_DATE_TIME);

        } else {
            Utils.writeStarterError(this);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_tp_dialog_fragment, container);
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
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
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

        tvSelect.setTextColor(colorButton);
        tvCancel.setTextColor(colorButton);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            tvCancel.setBackground(colorUtils.createRippleSurface());
            tvSelect.setBackground(colorUtils.createRippleSurface());
        }

        tvSelect.setTextColor(colorButton);
        tvCancel.setTextColor(colorButton);


        colorBackground(vDoublePoint1.getBackground(), colorTextSelected);
        colorBackground(vDoublePoint2.getBackground(), colorTextSelected);
    }

    private void initLoopViews() {
        lvHours.setVisibility(View.VISIBLE);
        lvHours.initialize(lvHours.new Initializer()
                .items(createDateItemList(23))
                .listener(new LoopListener() {
                    @Override
                    public void onItemSettled(LoopItem item) {
                        selectedTime = selectedTime.withHourOfDay(Integer.parseInt(item.getText()));
                    }
                })
                .initPosition(selectedTime.getHourOfDay())
                .textSize(textSizeDP)
                .textColor(colorText)
                .selectedTextColor(colorTextSelected)
        );

        lvMinutes.setVisibility(View.VISIBLE);
        lvMinutes.initialize(lvMinutes.new Initializer()
                .items(createDateItemList(59))
                .listener(new LoopListener() {
                    @Override
                    public void onItemSettled(LoopItem item) {
                        selectedTime = selectedTime.withMinuteOfHour(Integer.parseInt(item.getText()));
                    }
                })
                .initPosition(selectedTime.getMinuteOfHour())
                .textSize(textSizeDP)
                .textColor(colorText)
                .selectedTextColor(colorTextSelected)
        );
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
            listener.onDateSelected(listenerId, selectedTime);
            listener.onTimeSelected(listenerId, selectedTime.getHourOfDay(), selectedTime.getMinuteOfHour());
        }
        dismiss();
    }

    @OnClick(R2.id.tv_cancel)
    void onCancelClicked() {
        dismiss();
    }

    @SuppressLint("DefaultLocale")
    private List<StringLoopItem> createDateItemList(int max) {
        List<StringLoopItem> years = new ArrayList<>();
        for (int i = 0; i <= max; i++) {
            years.add(new StringLoopItem(String.format("%02d", i)));
        }

        return years;
    }

    private void colorBackground(Drawable background, int color) {
        if (background instanceof ShapeDrawable) {
            ((ShapeDrawable) background).getPaint().setColor(color);
        } else if (background instanceof GradientDrawable) {
            ((GradientDrawable) background).setColor(color);
        } else if (background instanceof ColorDrawable) {
            ((ColorDrawable) background).setColor(color);
        }
    }
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Getter & Setter">
    // </editor-fold>
}
