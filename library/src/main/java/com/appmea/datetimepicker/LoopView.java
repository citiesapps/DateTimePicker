package com.appmea.datetimepicker;

import android.annotation.SuppressLint;
import android.content.Context;
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

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class LoopView<T extends LoopItem> extends View {
    // ====================================================================================================================================================================================
    // <editor-fold desc="Constants">
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Properties">

    ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mFuture;
    int totalScrollY;
    Handler handler;
    LoopListener loopListener;
    private GestureDetector gestureDetector;
    private int selectedItem;
    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener;
    Context context;
    Paint paintText;  //paint that draw top and bottom text
    Paint paintSelected;  // paint that draw center text
    Paint paintDivider;  // paint that draw line besides center text
    int textSize;
    int maxTextWidth;
    int maxTextHeight;
    int colorGray;
    int colorBlack;
    int colorGrayLight;
    float lineSpacingMultiplier;
    boolean isLoop;
    int firstLineY;
    int secondLineY;
    int preCurrentIndex;
    int initPosition;
    int itemCount;
    int measuredHeight;
    int halfCircumference;
    int radius;
    int measuredWidth;
    int change;
    float y1;
    float y2;
    float dy;
    List<T> items;
    String[] as;
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Constructor">

    public LoopView(Context context) {
        super(context);
        initLoopView(context);
    }

    public LoopView(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        initLoopView(context);
    }

    public LoopView(Context context, AttributeSet attributeset, int defStyleAttr) {
        super(context, attributeset, defStyleAttr);
        initLoopView(context);
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
        int maxHeight = Math.max(desiredHeight, measuredHeight);

        setMeasuredDimension(measureDimension(maxWidth, widthMeasureSpec), measureDimension(maxHeight, heightMeasureSpec));
        measuredWidth = getMeasuredWidth();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (items == null) {
            super.onDraw(canvas);
            return;
        }
        change = (int) (totalScrollY / (lineSpacingMultiplier * maxTextHeight));
        preCurrentIndex = initPosition + change % items.size();
        if (!isLoop) {
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

        int j2 = (int) (totalScrollY % (lineSpacingMultiplier * maxTextHeight));
        int k1 = 0;
        while (k1 < itemCount) {
            int l1 = preCurrentIndex - (itemCount / 2 - k1);
            if (isLoop) {
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
        int left = (measuredWidth - maxTextWidth) / 2;

        canvas.drawLine(0.0F, firstLineY, measuredWidth, firstLineY, paintDivider);
        canvas.drawLine(0.0F, secondLineY, measuredWidth, secondLineY, paintDivider);
        int j1 = 0;
        while (j1 < itemCount) {
            canvas.save();
            // L=α* r
            // (L * π ) / (π * r)
            float itemHeight = maxTextHeight * lineSpacingMultiplier;
            double radian = ((itemHeight * j1 - j2) * Math.PI) / halfCircumference;
            float angle = (float) (90D - (radian / Math.PI) * 180D);
            if (angle >= 90F || angle <= -90F) {
                canvas.restore();
            } else {
                int translateY = (int) (radius - Math.cos(radian) * radius - (Math.sin(radian) * maxTextHeight) / 2D);
                canvas.translate(0.0F, translateY);
                canvas.scale(1.0F, (float) Math.sin(radian));
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
                    // TODO
//                    selectedItem = items.indexOf(as[j1]);
                } else {
                    canvas.clipRect(0, 0, measuredWidth, (int) (itemHeight));
                    canvas.drawText(as[j1], left, maxTextHeight, paintText);
                }
                canvas.restore();
            }
            j1++;
        }
        super.onDraw(canvas);
    }
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Initialisation">

    private void initLoopView(Context context) {
        textSize = 0;
        colorGray = 0xffafafaf;
        colorBlack = 0xff313131;
        colorGrayLight = 0xffc5c5c5;
        lineSpacingMultiplier = 2.0F;
        isLoop = true;
        initPosition = -1;
        itemCount = 7;
        as = new String[itemCount];
        y1 = 0.0F;
        y2 = 0.0F;
        dy = 0.0F;
        totalScrollY = 0;
        simpleOnGestureListener = new LoopViewGestureListener(this);
        handler = new MessageHandler(this);
        this.context = context;
        setTextSize(16F);

        paintText = new Paint();
        paintSelected = new Paint();
        paintDivider = new Paint();
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
        gestureDetector = new GestureDetector(context, simpleOnGestureListener);
        gestureDetector.setIsLongpressEnabled(false);
    }

    private void initData() {
        if (items == null) {
            return;
        }

        paintText.setColor(colorGray);
        paintText.setAntiAlias(true);
        paintText.setTypeface(Typeface.SANS_SERIF);
        paintText.setTextSize(textSize);

        paintSelected.setColor(colorBlack);
        paintSelected.setAntiAlias(true);
        paintSelected.setTypeface(Typeface.SANS_SERIF);
        paintSelected.setTextSize(textSize);

        paintDivider.setColor(colorGrayLight);
        paintDivider.setAntiAlias(true);
        paintDivider.setTextSize(textSize);

        measureTextWidthHeight();

        halfCircumference = (int) (maxTextHeight * lineSpacingMultiplier * (itemCount - 1));
        measuredHeight = (int) ((halfCircumference * 2) / Math.PI);
        radius = (int) (halfCircumference / Math.PI);
        firstLineY = (int) ((measuredHeight - lineSpacingMultiplier * maxTextHeight) / 2.0F);
        secondLineY = (int) ((measuredHeight + lineSpacingMultiplier * maxTextHeight) / 2.0F);
        if (initPosition == -1) {
            if (isLoop) {
                initPosition = (items.size() + 1) / 2;
            } else {
                initPosition = 0;
            }
        }
        preCurrentIndex = initPosition;
    }

    private void measureTextWidthHeight() {
        Rect rect = new Rect();
        for (int i = 0; i < items.size(); i++) {
            String s1 = items.get(i).getText();
            paintSelected.getTextBounds(s1, 0, s1.length(), rect);
            int textWidth = rect.width();
            if (textWidth > maxTextWidth) {
                maxTextWidth = textWidth;
            }
            paintSelected.getTextBounds("\u661F\u671F", 0, 2, rect); // 星期
            int textHeight = rect.height();
            if (textHeight > maxTextHeight) {
                maxTextHeight = textHeight;
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
                if (!isLoop) {
                    int initPositionCircleLength = (int) (initPosition * (lineSpacingMultiplier * maxTextHeight));
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

        if (!isLoop) {
            int circleLength = (int) ((float) (items.size() - 1 - initPosition) * (lineSpacingMultiplier * maxTextHeight));
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
        int offset = (int) (totalScrollY % (lineSpacingMultiplier * maxTextHeight));
        cancelFuture();
        mFuture = mExecutor.scheduleWithFixedDelay(new MTimer(this, offset), 0, 10, TimeUnit.MILLISECONDS);
    }

    protected final void smoothScroll(float velocityY) {
        cancelFuture();
        int velocityFling = 20;
        mFuture = mExecutor.scheduleWithFixedDelay(new LoopTimerTask(this, velocityY), 0, velocityFling, TimeUnit.MILLISECONDS);
    }

    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Getter & Setter">

    public void setItems(List<T> items) {
        this.items = items;
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
    // </editor-fold>

    static int getSelectedItem(LoopView loopview) {
        return loopview.selectedItem;
    }

    static void smoothScroll(LoopView loopview) {
        loopview.smoothScroll();
    }

    public void cancelFuture() {
        if (mFuture != null && !mFuture.isCancelled()) {
            mFuture.cancel(true);
            mFuture = null;
        }
    }

    public final void setNotLoop() {
        isLoop = false;
    }

    public final void setTextSize(float size) {
        if (size > 0.0F) {
            textSize = (int) (context.getResources().getDisplayMetrics().density * size);
        }
    }

    public final void setInitPosition(int initPosition) {
        this.initPosition = initPosition;
    }

    public final void setListener(LoopListener LoopListener) {
        loopListener = LoopListener;
    }

    protected final void itemSelected() {
        if (loopListener != null) {
            postDelayed(new LoopRunnable(this), 200L);
        }
    }
}
