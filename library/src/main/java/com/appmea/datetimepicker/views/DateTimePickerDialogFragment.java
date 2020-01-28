package com.appmea.datetimepicker.views;

import android.app.Dialog;
import android.content.Context;
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
import com.appmea.datetimepicker.R;

import org.jetbrains.annotations.NotNull;

import butterknife.ButterKnife;

public class DateTimePickerDialogFragment extends DialogFragment {
    // ====================================================================================================================================================================================
    // <editor-fold desc="Constants">

    private static final String TAG = Constants.TAG_DTP_DIALOG_FRAGMENT;
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Properties">

    private Context context;

    private View view;
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Constructor">

    public static void startFragment(Fragment fragment) {
        Fragment prev = fragment.getChildFragmentManager().findFragmentByTag(TAG);
        if (prev == null) {
            FragmentTransaction ft = fragment.getChildFragmentManager().beginTransaction();

            ft.addToBackStack(null);

            DateTimePickerDialogFragment dialogFragment = DateTimePickerDialogFragment.newInstance();
            dialogFragment.show(ft, TAG);
        }
    }

    public static void startFragment(FragmentActivity activity) {
        Fragment prev = activity.getSupportFragmentManager().findFragmentByTag(TAG);
        if (prev == null) {
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();

            ft.addToBackStack(null);

            DateTimePickerDialogFragment dialogFragment = DateTimePickerDialogFragment.newInstance();
            dialogFragment.show(ft, TAG);
        }
    }

    public static DateTimePickerDialogFragment newInstance() {
        DateTimePickerDialogFragment fragment = new DateTimePickerDialogFragment();

        Bundle arguments = new Bundle();
        fragment.setArguments(arguments);

        return fragment;
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
//            throw new IllegalArgumentException("Parent of " + DateTimePickerDialogFragment.class.getName() + " must implement " + ConnectDialogListener.class.getName());
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
//        if (getArguments() != null) {
//            entityId = getArguments().getString(BUNDLE_EXTRA_STRING_ENTITY_ID);
//            if (entityId == null) {
//                Utils.writeStarterError(this);
//                dismiss();
//            }
//        } else {
//            Utils.writeStarterError(this);
//            dismiss();
//        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_dtp_dialog_fragment, container);

        ButterKnife.bind(this, view);

        view.setVisibility(View.GONE);
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
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Interfaces">
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Methods">
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Getter & Setter">
    // </editor-fold>
}
