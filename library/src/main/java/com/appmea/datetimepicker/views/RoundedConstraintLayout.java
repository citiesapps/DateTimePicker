package com.appmea.datetimepicker.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewOutlineProvider;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.appmea.datetimepicker.ColorUtils;
import com.appmea.datetimepicker.R;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class RoundedConstraintLayout extends ConstraintLayout {
    // ====================================================================================================================================================================================
    // <editor-fold desc="Constants">
    private static final int DEFAULT_RADIUS           = 48;
    private static final int DEFAULT_STROKE_WIDTH     = 0;
    private static final int DEFAULT_STROKE_COLOR     = 0x1F000000;
    private static final int DEFAULT_BACKGROUND_COLOR = 0xFFFFFFFF;
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Properties">

    Path  pathStroke  = new Path();
    Paint paintStroke = new Paint();

    private ColorUtils colorUtils;


    private int   cornerRadius      = DEFAULT_RADIUS;
    /**
     * With of border stroke
     */
    private int   strokeWidth       = DEFAULT_STROKE_WIDTH;
    /**
     * Double the width of the border stroke, as drawing a path is using thickness/half as actual path middle
     */
    private float strokeWidthDouble = DEFAULT_STROKE_WIDTH * 2f;
    private int   strokeColor       = DEFAULT_STROKE_COLOR;
    private int   backgroundColor   = DEFAULT_BACKGROUND_COLOR;
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Constructor">

    public RoundedConstraintLayout(Context context) {
        this(context, null);
    }

    public RoundedConstraintLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundedConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        colorUtils = new ColorUtils(context);
        final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RoundedConstraintLayout);

        try {
            cornerRadius = array.getDimensionPixelSize(R.styleable.RoundedConstraintLayout_rcl_radius, DEFAULT_RADIUS);
            strokeWidth = array.getDimensionPixelSize(R.styleable.RoundedConstraintLayout_rcl_stroke_width, DEFAULT_STROKE_WIDTH);
            strokeWidthDouble = strokeWidth * 2f;
            strokeColor = array.getColor(R.styleable.RoundedConstraintLayout_rcl_stroke_color, DEFAULT_STROKE_COLOR);
            if (isInEditMode()) {
                backgroundColor = array.getColor(R.styleable.RoundedConstraintLayout_rcl_background_color, DEFAULT_BACKGROUND_COLOR);
            } else {
                backgroundColor = array.getColor(R.styleable.RoundedConstraintLayout_rcl_background_color, colorUtils.getColorSurface());
            }
        } finally {
            array.recycle();
        }

        paintStroke.setFlags(Paint.ANTI_ALIAS_FLAG);
        paintStroke.setStyle(Paint.Style.STROKE);
        paintStroke.setColor(strokeColor);
        paintStroke.setStrokeWidth(strokeWidthDouble);

        initBackground();
        setPadding(strokeWidth, strokeWidth, strokeWidth, strokeWidth);
        setClipToOutline(true);
    }

    private void initBackground() {
        setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        GradientDrawable shapeDrawable = new GradientDrawable();
        shapeDrawable.setCornerRadius(cornerRadius);
        shapeDrawable.setColor(backgroundColor);
        setBackground(shapeDrawable);
    }
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Android Lifecycle">

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        pathStroke.reset();
        pathStroke.addRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius, Path.Direction.CW);
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        super.dispatchDraw(canvas);
        canvas.restore();
        canvas.drawPath(pathStroke, paintStroke);
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
