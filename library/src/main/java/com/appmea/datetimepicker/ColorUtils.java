package com.appmea.datetimepicker;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;

public class ColorUtils {

    private final Context context;

    public ColorUtils(Context context) {
        this.context = context;
    }

    public int getColorPrimary() {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true)) {
            return typedValue.data;
        }
        return ContextCompat.getColor(context, R.color.dtp_default_color);
    }


    public int getColorOnPrimary() {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(R.attr.colorOnPrimary, typedValue, true)) {
            return typedValue.data;
        }
        return ContextCompat.getColor(context, R.color.dtp_default_text_color);
    }

    public int getColorSurface() {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(R.attr.colorSurface, typedValue, true)) {
            return typedValue.data;
        }
        return ContextCompat.getColor(context, R.color.dtp_default_ripple_color);
    }

    public int getColorRippleSurface() {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(R.attr.colorRippleSurface, typedValue, true)) {
            return typedValue.data;
        }
        return ContextCompat.getColor(context, R.color.dtp_default_ripple_color);
    }

    public StateListDrawable createRipple() {
        StateListDrawable selector = new StateListDrawable();
        selector.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(getColorRippleSurface()));
        selector.addState(new int[]{}, new ColorDrawable(getColorSurface()));
        return selector;
    }
}
