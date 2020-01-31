package com.appmea.datetimepicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class LoopView<T extends LoopItem> extends View {
    // ====================================================================================================================================================================================
    // <editor-fold desc="Constants">

    private static final float PI_HALF = (float) (Math.PI / 2F);

    private static final float DEFAULT_TEXT_SIZE = 40;

    private static final int DEFAULT_COLOR_TEXT          = 0XFFAFAFAF;
    private static final int DEFAULT_COLOR_TEXT_SELECTED = 0XFF313131;
    private static final int DEFAULT_COLOR_DIVIDER       = 0XFFC5C5C5;
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Properties">

    final   ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?>       mFuture;
    int          totalScrollY;
    Handler      handler;
    LoopListener loopListener;

    List<T> items;
    private T selectedItem;

    private GestureDetector gestureDetector;

    final Paint    paintText             = new Paint();
    final Paint    paintSelected         = new Paint();
    final Paint    paintDivider          = new Paint();
    final int      itemCount             = 7;
    final String[] as                    = new String[itemCount];
    final float    lineSpacingMultiplier = 2.0F;

    int     initPosition = -1;
    boolean loopEnabled;

    int   textSize;
    int   maxTextWidth;
    int   maxTextHeight;
    int   colorText;
    int   colorTextSelected;
    int   colorDivider;
    float firstLineY;
    float secondLineY;
    int   preCurrentIndex;
    float measuredHeight;
    float halfCircumference;
    float radius;
    float itemHeight;
    float measuredWidth;
    int   change;
    float y1;
    float y2;
    float dy;


    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Constructor">

    public LoopView(Context context) {
        this(context, null);
    }

    public LoopView(Context context, AttributeSet attributeset) {
        this(context, attributeset, 0);
    }

    public LoopView(Context context, AttributeSet attributeset, int defStyleAttr) {
        super(context, attributeset, defStyleAttr);
        if (isInEditMode()) {
            items = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                items.add((T) new StringLoopItem(String.valueOf(i)));
            }
            initPosition = 3;
        }

        initLoopView(context, attributeset);
    }
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Initialisation">

    private void initLoopView(Context context, AttributeSet attributeset) {
        final TypedArray array = context.obtainStyledAttributes(attributeset, R.styleable.LoopView);
        try {
            textSize = (int) array.getDimension(R.styleable.LoopView_textSize, array.getResources().getDisplayMetrics().density * DEFAULT_TEXT_SIZE);
            colorText = array.getColor(R.styleable.LoopView_textColor, DEFAULT_COLOR_TEXT);
            colorTextSelected = array.getColor(R.styleable.LoopView_selectedTextColor, DEFAULT_COLOR_TEXT_SELECTED);
            colorDivider = array.getColor(R.styleable.LoopView_dividerColor, DEFAULT_COLOR_DIVIDER);
        } finally {
            array.recycle();
            if (textSize == 0) {
                textSize = (int) DEFAULT_TEXT_SIZE;
            }

            if (colorText == 0) {
                colorText = DEFAULT_COLOR_TEXT;
            }

            if (colorTextSelected == 0) {
                colorTextSelected = DEFAULT_COLOR_TEXT_SELECTED;
            }

            if (colorDivider == 0) {
                colorDivider = DEFAULT_COLOR_DIVIDER;
            }
        }

        initPaint(paintText, colorText);
        initPaint(paintSelected, colorTextSelected);
        initPaint(paintDivider, colorDivider);

        handler = new MessageHandler(this);
        GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new LoopViewGestureListener(this);
        gestureDetector = new GestureDetector(context, simpleOnGestureListener);
        gestureDetector.setIsLongpressEnabled(false);
    }

    private void initPaint(Paint paint, int color) {
        paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.SANS_SERIF);
        paint.setTextSize(textSize);
    }

    private void initData() {
        if (items == null) {
            return;
        }

        measureTextWidthHeight();
        itemHeight = maxTextHeight * lineSpacingMultiplier;
        halfCircumference = itemHeight * (itemCount - 1);
        radius = (float) (halfCircumference / Math.PI);
        measuredHeight = radius * 2;

        firstLineY = ((measuredHeight - itemHeight) / 2.0F);
        secondLineY = ((measuredHeight + itemHeight) / 2.0F);
        if (initPosition == -1) {
            if (loopEnabled) {
                initPosition = (items.size() + 1) / 2;
            } else {
                initPosition = 0;
            }
        }
        preCurrentIndex = initPosition;
    }

    private void measureTextWidthHeight() {
        Paint.FontMetrics fm = paintText.getFontMetrics();
        maxTextHeight = (int) (fm.descent - fm.ascent);

        Rect rect = new Rect();
        for (int i = 0; i < items.size(); i++) {
            String string = items.get(i).getText();
            paintSelected.getTextBounds(string, 0, string.length(), rect);
            int textWidth = rect.width();
            if (textWidth > maxTextWidth) {
                maxTextWidth = textWidth;
            }
        }
    }

    private int measureDimension(int desiredSize, int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = desiredSize;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        if (result < desiredSize) {
            Timber.e("The view is too small, the content might get cut");
        }

        return result;
    }
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Android Lifecycle">

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        initData();

        int desiredWidth = getSuggestedMinimumWidth() + getPaddingLeft() + getPaddingRight();
        int desiredHeight = getSuggestedMinimumHeight() + getPaddingTop() + getPaddingBottom();

        int maxWidth = Math.max(desiredWidth, maxTextWidth);
        int maxHeight = (int) Math.max(desiredHeight, measuredHeight);

        setMeasuredDimension(measureDimension(maxWidth, widthMeasureSpec), measureDimension(maxHeight, heightMeasureSpec));
        measuredWidth = getMeasuredWidth();
    }

    Paint  fill = new Paint();
    Random rnd  = new Random();

    @Override
    protected void onDraw(Canvas canvas) {
        if (items == null) {
            super.onDraw(canvas);
            return;
        }
        change = (int) (totalScrollY / itemHeight);
        Timber.e("change: %d", change);
        preCurrentIndex = initPosition + change % items.size();
        if (!loopEnabled) {
            if (preCurrentIndex < 0) {
                preCurrentIndex = 0;
            }
            if (preCurrentIndex > items.size() - 1) {
                preCurrentIndex = items.size() - 1;
            }
        } else {
            if (preCurrentIndex < 0) {
                preCurrentIndex = items.size() + preCurrentIndex;
            }
            if (preCurrentIndex > items.size() - 1) {
                preCurrentIndex = preCurrentIndex - items.size();
            }
        }
        Timber.e("index: %d", preCurrentIndex);

        int scrollOffset = (int) (totalScrollY % itemHeight);
        int k1 = 0;
        while (k1 < itemCount) {
            int l1 = preCurrentIndex - (itemCount / 2 - k1);
            if (loopEnabled) {
                if (l1 < 0) {
                    l1 = l1 + items.size();
                }
                if (l1 > items.size() - 1) {
                    l1 = l1 - items.size();
                }
                as[k1] = items.get(l1).getText();
            } else if (l1 < 0) {
                as[k1] = "";
            } else if (l1 > items.size() - 1) {
                as[k1] = "";
            } else {
                as[k1] = items.get(l1).getText();
            }
            k1++;
        }
        //auto calculate the text's left value when draw
        int left = (int) ((measuredWidth - maxTextWidth) / 2);

//        canvas.drawLine(0.0F, firstLineY, measuredWidth, firstLineY, paintDivider);
//        canvas.drawLine(0.0F, secondLineY, measuredWidth, secondLineY, paintDivider);
        int j1 = 0;

        while (j1 < itemCount) {
            canvas.save();
            // L=α* r
            // (L * π ) / (π * r)
            float itemHeight = maxTextHeight * lineSpacingMultiplier;
            double radian = ((itemHeight * j1 - scrollOffset) * Math.PI) / halfCircumference;
            float angle = (float) (90D - (radian / Math.PI) * 180D);
            if (angle >= 90F || angle <= -90F) {
                canvas.restore();
            } else {
                int translateY = (int) (radius - Math.cos(radian) * radius - (Math.sin(radian) * maxTextHeight) / 2D);
                canvas.translate(0.0F, translateY);
                Timber.e("onDraw: %d, %f", j1, Math.sin(radian));

                canvas.scale(1.0F, (float) Math.sin(radian));
                fill.setStyle(Paint.Style.FILL_AND_STROKE);
                fill.setARGB(180, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

                canvas.save();
                canvas.clipRect(0, 0, measuredWidth, (int) (itemHeight));
                canvas.drawPaint(fill);
                canvas.restore();

                if (translateY <= firstLineY && maxTextHeight + translateY >= firstLineY) {
                    canvas.save();
                    //top = 0,left = (measuredWidth - maxTextWidth)/2
                    canvas.clipRect(0, 0, measuredWidth, firstLineY - translateY);
                    canvas.drawText(as[j1], left, maxTextHeight, paintText);
                    canvas.restore();
                    canvas.save();
                    canvas.clipRect(0, firstLineY - translateY, measuredWidth, (int) (itemHeight));
                    canvas.drawText(as[j1], left, maxTextHeight, paintSelected);
                    canvas.restore();
                } else if (translateY <= secondLineY && maxTextHeight + translateY >= secondLineY) {
                    canvas.save();
                    canvas.clipRect(0, 0, measuredWidth, secondLineY - translateY);
                    canvas.drawText(as[j1], left, maxTextHeight, paintSelected);
                    canvas.restore();
                    canvas.save();
                    canvas.clipRect(0, secondLineY - translateY, measuredWidth, (int) (itemHeight));
                    canvas.drawText(as[j1], left, maxTextHeight, paintText);
                    canvas.restore();
                } else if (translateY >= firstLineY && maxTextHeight + translateY <= secondLineY) {
                    canvas.clipRect(0, 0, measuredWidth, (int) (itemHeight));
                    canvas.drawText(as[j1], left, maxTextHeight, paintSelected);
                    selectedItem = findItem(as[j1]);

                } else {
                    canvas.clipRect(0, 0, measuredWidth, (int) (itemHeight));
                    canvas.drawText(as[j1], left, maxTextHeight, paintText);
                }
                canvas.restore();
            }
            j1++;
        }
//        while (j1 < itemCount) {
//            canvas.save();
//            // B=α* r
//            // (B * π ) / (π * r)
//            float radiantItemHeight = (float) ((itemHeight * j1 - scrollOffset)*Math.PI / (radius*2));
//            float radiantFromTop = PI_HALF - radiantItemHeight;
//            int angleItem = (int) Math.round((radiantItemHeight / Math.PI) * 180);
//            int angleTop = (int) Math.round((radiantFromTop / Math.PI) * 180);
//
//            double sin = Math.abs(Math.sin(radiantItemHeight ));
//            Timber.e("onDraw: Item: %d | radTop: %f | angleTop: %d | sin: %f | %s", j1, radiantFromTop, angleTop, sin, angleTop >= 90 || angleTop <= -90);
//            if (angleTop >= 90 || angleTop <= -90) {
//                canvas.restore();
//            } else {
//                int translateY = (int) (radius - Math.cos(radiantItemHeight) * radius - (Math.sin(radiantItemHeight) * maxTextHeight) / 2D);
////                int translateY = (int) (radius - Math.cos(radiantItemHeight) * radius - (Math.sin(radiantItemHeight) * maxTextHeight) / 2D);
//                canvas.translate(0.0F, translateY);
//                Timber.e("onDraw: scale: %f", (float) (1f - sin));
//                canvas.scale(1.0F, (float) sin);
//
//                fill.setStyle(Paint.Style.FILL_AND_STROKE);
//                fill.setARGB(180, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
//
//                canvas.clipRect(0, 0, measuredWidth, itemHeight);
////                canvas.drawPaint(fill);
//                canvas.drawText(as[j1], left, maxTextHeight, paintText);
//
//
////                if (translateY <= firstLineY && maxTextHeight + translateY >= firstLineY) {
////                    canvas.save();
////                    //top = 0,left = (measuredWidth - maxTextWidth)/2
////                    canvas.clipRect(0, 0, measuredWidth, firstLineY - translateY);
////                    canvas.drawText(as[j1], left, maxTextHeight, paintText);
////                    canvas.restore();
////                    canvas.save();
////                    canvas.clipRect(0, firstLineY - translateY, measuredWidth, (int) (itemHeight));
////                    canvas.drawText(as[j1], left, maxTextHeight, paintSelected);
////                    canvas.restore();
////                } else if (translateY <= secondLineY && maxTextHeight + translateY >= secondLineY) {
////                    canvas.save();
////                    canvas.clipRect(0, 0, measuredWidth, secondLineY - translateY);
////                    canvas.drawText(as[j1], left, maxTextHeight, paintSelected);
////                    canvas.restore();
////                    canvas.save();
////                    canvas.clipRect(0, secondLineY - translateY, measuredWidth, (int) (itemHeight));
////                    canvas.drawText(as[j1], left, maxTextHeight, paintText);
////                    canvas.restore();
////                } else if (translateY >= firstLineY && maxTextHeight + translateY <= secondLineY) {
////                    canvas.clipRect(0, 0, measuredWidth, (int) (itemHeight));
////                    canvas.drawText(as[j1], left, maxTextHeight, paintSelected);
////                    selectedItem = findItem(as[j1]);
////
////                } else {
////                    canvas.clipRect(0, 0, measuredWidth, (int) (itemHeight));
////                    canvas.drawText(as[j1], left, maxTextHeight, paintText);
////                }
//                canvas.restore();
//            }
//            j1++;
//        }
        super.onDraw(canvas);
    }
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Interfaces">

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent motionevent) {
        switch (motionevent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                y1 = motionevent.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                y2 = motionevent.getRawY();
                dy = y1 - y2;
                y1 = y2;
                totalScrollY = (int) ((float) totalScrollY + dy);
                if (!loopEnabled) {
                    int initPositionCircleLength = (int) (initPosition * itemHeight);
                    int initPositionStartY = -1 * initPositionCircleLength;
                    if (totalScrollY < initPositionStartY) {
                        totalScrollY = initPositionStartY;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            default:
                if (!gestureDetector.onTouchEvent(motionevent) && motionevent.getAction() == MotionEvent.ACTION_UP) {
                    smoothScroll();
                }
                return true;
        }

        if (!loopEnabled) {
            int circleLength = (int) ((float) (items.size() - 1 - initPosition) * itemHeight);
            if (totalScrollY >= circleLength) {
                totalScrollY = circleLength;
            }
        }
        invalidate();

        if (!gestureDetector.onTouchEvent(motionevent) && motionevent.getAction() == MotionEvent.ACTION_UP) {
            smoothScroll();
        }
        return true;
    }
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Methods">
    private void smoothScroll() {
        int offset = (int) (totalScrollY % itemHeight);
        cancelFuture();
        mFuture = mExecutor.scheduleWithFixedDelay(new MTimer(this, offset), 0, 10, TimeUnit.MILLISECONDS);
    }

    protected final void smoothScroll(float velocityY) {
        cancelFuture();
        int velocityFling = 20;
        mFuture = mExecutor.scheduleWithFixedDelay(new LoopTimerTask(this, velocityY), 0, velocityFling, TimeUnit.MILLISECONDS);
    }

    protected final void itemSelected() {
        if (loopListener != null) {
            loopListener.onItemSelect(selectedItem);
        }
    }

    @Nullable
    private T findItem(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        for (T item : items) {
            if (item.getText().equals(text)) {
                return item;
            }
        }

        return null;
    }
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Getter & Setter">

    public void setItems(List<T> items) {
        this.items = items;
        if (items != null) {
            if (!items.isEmpty()) {
                this.initPosition = Math.min(items.size() - 1, initPosition);
                Timber.e("setItems: %d", initPosition);
            }
        }
        initData();
        invalidate();
    }

    @Nullable
    public T getItem(int position) {
        if (items == null || position < 0 || position > items.size() - 1) {
            return null;
        }

        return items.get(position);
    }

    public void setLoopEnabled(boolean enabled) {
        loopEnabled = enabled;
    }

    public final void setListener(LoopListener LoopListener) {
        loopListener = LoopListener;
    }
    // </editor-fold>


    /**
     * öalskdjfö jasöldkfj ölasd
     */

    static void smoothScroll(LoopView loopview) {
        loopview.smoothScroll();
    }

    public void cancelFuture() {
        if (mFuture != null && !mFuture.isCancelled()) {
            mFuture.cancel(true);
            mFuture = null;
        }
    }

    public void setTextSize(int textSize) {
        if (this.textSize != textSize) {
            this.textSize = textSize;
            invalidate();
        }
    }

    public final void setInitPosition(int initPosition) {
        if (items != null) {
            if (items.isEmpty()) {
                return;
            }

            this.initPosition = Math.min(items.size() - 1, initPosition);
            return;
        }

        this.initPosition = initPosition;
        invalidate();
    }


}
