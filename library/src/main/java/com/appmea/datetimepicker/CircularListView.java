package com.appmea.datetimepicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.appmea.datetimepicker.items.StringLoopItem;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class CircularListView<T extends LoopItem> extends View {
    // ====================================================================================================================================================================================
    // <editor-fold desc="Constants">

    private static final float PI_HALF   = (float) (Math.PI / 2F);
    private static final float PI        = (float) Math.PI;
    private static final float PI_DOUBLE = (float) (Math.PI * 2F);

    private static final float DEFAULT_TEXT_SIZE = 16;

    private static final int DEFAULT_COLOR_TEXT          = 0XFFAFAFAF;
    private static final int DEFAULT_COLOR_TEXT_SELECTED = 0XFF000000;
    private static final int DEFAULT_COLOR_DIVIDER       = 0XFFC5C5C5;

    private static final int TOUCH_SLOP       = 8;
    private static final int MINIMUM_VELOCITY = 50;
    private static final int MAXIMUM_VELOCITY = 8000;
    private static final int VELOCITY_REDUCER = 100;

    public static final int NO_POSITION = -1;
    public static final int ON_SCROLL   = 0;
    public static final int ON_SETTLE   = 1;

    @IntDef({ON_SCROLL, ON_SETTLE})
    public @interface Mode {
    }
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Properties">

    /**
     * Ranges from Min: itemHeight/2 TO Max: itemHeight * (displayableItemCount + itemCount -1)
     */
    int          currentScrollY;
    int          minScrollY;
    int          maxScrollY;
    LoopListener loopListener;

    List<T> items = new ArrayList<>();
    private T selectedItem;


    final Paint    paintText            = new Paint();
    final Paint    paintSelected        = new Paint();
    final Paint    paintDivider         = new Paint();
    final Paint    paintTest            = new Paint();
    /**
     * Need to be an odd number TODO: add check on init when making this variable
     */
    final int      displayableItemCount = 7;
    final String[] as                   = new String[displayableItemCount];
    final float[]  ratios               = new float[displayableItemCount];

    final float lineSpacingMultiplier = 1.5f;

    int     initPosition = -1;
    boolean loopEnabled;

    @Mode
    int mode;
    int   textSize;
    int   maxTextWidth;
    int   maxTextHeight;
    int   colorText;
    int   colorTextSelected;
    int   colorDivider;
    float firstLineY;
    float secondLineY;
    /**
     * Index of the top most item.
     * <b>Note:</b> this is not the selected one, as top/bottom gets filled with empty items if loop is disabled
     */
    int   preCurrentIndex;
    float measuredHeight;
    float halfCircumference;
    float radius;
    float itemHeight;
    float measuredWidth;
    private float           radiantSingleItem;
    private int             lastMotionY;
    private boolean         isBeingDragged;
    private VelocityTracker velocityTracker;

    /**
     * Index of the last item that was selected during scroll
     */
    private int lastScrolledIndex = -1;

    /**
     * Step size used for settling animation
     */
    private int           fraction;
    private FlingRunnable flingRunnable;
    private boolean       animationFinished;

    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Constructor">

    public CircularListView(Context context) {
        this(context, null);
    }

    public CircularListView(Context context, AttributeSet attributeset) {
        this(context, attributeset, 0);
    }

    public CircularListView(Context context, AttributeSet attributeset, int defStyleAttr) {
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
        final TypedArray array = context.obtainStyledAttributes(attributeset, R.styleable.CircularListView);
        try {
            textSize = (int) array.getDimension(R.styleable.CircularListView_textSize, array.getResources().getDisplayMetrics().density * DEFAULT_TEXT_SIZE);
            colorText = array.getColor(R.styleable.CircularListView_textColor, DEFAULT_COLOR_TEXT);
            colorTextSelected = array.getColor(R.styleable.CircularListView_selectedTextColor, DEFAULT_COLOR_TEXT_SELECTED);
            colorDivider = array.getColor(R.styleable.CircularListView_dividerColor, DEFAULT_COLOR_DIVIDER);
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

        mode = ON_SETTLE;

        initPaint(paintText, colorText);
        initPaint(paintSelected, colorTextSelected);
        initPaint(paintDivider, colorDivider);
        initPaint(paintTest, Color.GREEN);

        initCircularSizes();
        initScrollAndIndex();
        updateScrollRange();
    }

    private void initPaint(Paint paint, int color) {
        paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.SANS_SERIF);
        paint.setTextSize(textSize);
    }

    private void initCircularSizes() {
        measureMaxTextHeight();

        itemHeight = maxTextHeight * lineSpacingMultiplier;
        fraction = (int) (itemHeight * 0.05);

        float angleDegree = 180f / (displayableItemCount - 1);
        // As each item has the same height
        radiantSingleItem = (float) Math.toRadians(angleDegree);

//        Can't use acr, as it would falsify the itemHeight
//        acr for centered item needs to be itemHeight
//        arc = 2*sin(alpha/2)*r
//        radius = (float) (itemHeight / (2 * Math.sin(Math.toRadians(angleDegree / 2))));

        halfCircumference = itemHeight * (displayableItemCount - 1);
        radius = (float) (halfCircumference / Math.PI);
        measuredHeight = radius * 2;

        firstLineY = ((measuredHeight - itemHeight) / 2.0F);
        secondLineY = ((measuredHeight + itemHeight) / 2.0F);
    }

    private void initScrollAndIndex() {
        if (initPosition == -1) {
            if (loopEnabled) {
                initPosition = (items.size() + 1) / 2;
            } else {
                initPosition = 0;
            }
        }

        currentScrollY = (int) ((itemHeight * initPosition + itemHeight / 2f));
        preCurrentIndex = initPosition;
    }

    private void measureMaxTextHeight() {
        Paint.FontMetrics fm = paintText.getFontMetrics();
        maxTextHeight = (int) (fm.descent - fm.ascent);
    }

    private void measureMaxTextWidth() {
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


    private void updateScrollRange() {
        minScrollY = (int) (itemHeight / 2);
        maxScrollY = Math.max(minScrollY, (int) (itemHeight * (-0.5 + items.size())));

        // Sanity check
        if (currentScrollY < minScrollY) {
            currentScrollY = minScrollY;
        }

        if (currentScrollY > maxScrollY) {
            currentScrollY = maxScrollY;
        }
        Timber.e("updateScrollRange: MinScroll: %d, MaxScroll: %d", minScrollY, maxScrollY);
    }
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Android Lifecycle">

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureMaxTextWidth();

        int desiredWidth = getSuggestedMinimumWidth() + getPaddingLeft() + getPaddingRight();
        int desiredHeight = getSuggestedMinimumHeight() + getPaddingTop() + getPaddingBottom();

        int maxWidth = Math.max(desiredWidth, maxTextWidth);
        int maxHeight = (int) Math.max(desiredHeight, measuredHeight);

        setMeasuredDimension(measureDimension(maxWidth, widthMeasureSpec), measureDimension(maxHeight, heightMeasureSpec));
        measuredWidth = getMeasuredWidth();
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

    Paint fill = new Paint();

    private void updateCurrentIndex() {
        preCurrentIndex = (int) (currentScrollY / itemHeight);
        sanityCheckIndex();
        getCurrentCanvasItems();
    }

    private void sanityCheckIndex() {
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
    }

    private void getCurrentCanvasItems() {
        int k1 = 0;
        while (k1 < displayableItemCount) {
            int l1 = preCurrentIndex - (displayableItemCount / 2 - k1);
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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (items == null) {
            super.onDraw(canvas);
            return;
        }

        int scrollOffset = (int) (currentScrollY % itemHeight);
        updateCurrentIndex();

        int left = (int) ((measuredWidth - maxTextWidth) / 2);
        int j1 = 0;


        for (int i = 0; i < displayableItemCount; i++) {
            float radiantItemHeight = (itemHeight * (i + 1) - scrollOffset) / radius;
            float radiantBottom = radiantItemHeight - radiantSingleItem;

            if (radiantBottom > PI && radiantItemHeight < PI_DOUBLE) {
                // Item outside
            } else if (radiantItemHeight < PI_HALF) {
                if (radiantBottom >= 0) {
                    // Item completely on right side
                    ratios[i] = (float) (1f - Math.cos(radiantItemHeight) - (1F - Math.cos(radiantBottom)));
                } else {
                    // Item partially on right side
                    ratios[i] = (float) (1f - Math.cos(radiantItemHeight));
                }

            } else {
                if (radiantBottom < PI_HALF) {
                    // Item on both sides
                    ratios[i] = (float) (Math.abs(Math.cos(radiantItemHeight)) + Math.cos(radiantBottom));

                } else if (radiantItemHeight <= PI) {
                    // Item completely on left side
                    ratios[i] = (float) (-1f - Math.cos(radiantItemHeight) - (-1F - Math.cos(radiantBottom)));

                } else {
                    // Item partially on left side
                    ratios[i] = (float) (1f + Math.cos(radiantBottom));
                }
            }

            ratios[i] = ratios[i] * radius / itemHeight;
//            Timber.e("String: %s, RadItem: %f, RadBottom: %f, Ratio: %f", as[i], radiantItemHeight, radiantBottom, ratios[i]);
        }


        Pair<Integer, Integer> lines = drawLines(canvas);

        int translation = 0;
        while (j1 < displayableItemCount) {
            canvas.save();
            canvas.translate(0.0F, translation);

            int lowerY = translation;
            translation += itemHeight * ratios[j1];
            int upperY = translation;


            canvas.scale(1.0F, ratios[j1]);

//            fill.setStyle(Paint.Style.FILL_AND_STROKE);
//            Beautiful
//            if (as[j1].equals("")) {
//                if (j1 % 2 == 0) {
//                    fill.setARGB(255, 255, 0, 255);
//                } else {
//                    fill.setARGB(255, 255, 255, 0);
//                }
//            } else {
//                if (Integer.valueOf(as[j1]) % 2 == 0) {
//                    fill.setARGB(255, 255, 255, 255);
//                } else {
//                    fill.setARGB(255, 0, 0, 0);
//                }
//            }
//
//          Functional
            if (j1 % 2 == 0) {
                fill.setARGB(255, 255, 255, 255);
            } else {
                fill.setARGB(255, 0, 0, 0);
            }


            drawText(canvas, as[j1], lines, lowerY, upperY, left);

//            canvas.clipRect(0, 0, measuredWidth, itemHeight);
//            canvas.drawPaint(fill);
//            int yPos = (int) ((itemHeight / 2) - ((paintText.descent() + paintText.ascent()) / 2));
//            canvas.drawText(as[j1], left, yPos, paintText);

            canvas.restore();
            j1++;
        }
        // </editor-fold>


        // Call scroll listener if item index changed
        if (loopListener != null) {
            int index = getIndexOfItem(currentScrollY);
            if (index != NO_POSITION && index != lastScrolledIndex) {
                lastScrolledIndex = index;
                loopListener.onItemScrolled(getItem(index));
            }
        }
        super.onDraw(canvas);
    }

    private Pair<Integer, Integer> drawLines(Canvas canvas) {
        int middle = getHeight() / 2;
        int lowerY = middle - minScrollY;
        int upperY = middle + minScrollY;

        canvas.drawLine(0, lowerY, getWidth(), lowerY, paintDivider);
        canvas.drawLine(0, upperY, getWidth(), upperY, paintDivider);

        return new Pair<>(lowerY, upperY);
    }

    private void drawText(Canvas canvas, String text, Pair<Integer, Integer> lines, int lowerY, int upperY, int left) {
        int yPos = (int) ((itemHeight / 2) - ((paintText.descent() + paintText.ascent()) / 2));

        if ((lowerY < lines.first && upperY <= lines.first) || (lowerY >= lines.second && upperY > lines.second)) {
            // Whole item is outside of lines -> draw completely unselected
            canvas.clipRect(0, 0, measuredWidth, itemHeight);
            canvas.drawText(text, left, yPos, paintText);

        } else if (lowerY < lines.first && upperY > lines.first) {
            // Lower half of the item is selected
            int lowerDeltaY = lines.first - lowerY;
            canvas.save();
            canvas.clipRect(0, 0, measuredWidth, lowerDeltaY);
//            canvas.drawPaint(paintTest);
            canvas.drawText(text, left, yPos, paintText);
            canvas.restore();

            canvas.save();
            canvas.clipRect(0, lowerDeltaY, measuredWidth, itemHeight);
            canvas.drawText(text, left, yPos, paintSelected);
            canvas.restore();
//
        } else if (lowerY > lines.first && upperY > lines.second) {
            // Upper half of the item is selected
            int lowerDeltaY = lines.second - lowerY;
            canvas.save();
            canvas.clipRect(0, 0, measuredWidth, lowerDeltaY);
//            canvas.drawPaint(paintTest);
            canvas.drawText(text, left, yPos, paintSelected);
            canvas.restore();

            canvas.save();
            canvas.clipRect(0, lowerDeltaY, measuredWidth, itemHeight);
            canvas.drawText(text, left, yPos, paintText);
            canvas.restore();

        } else if (lowerY < lines.first && upperY > lines.second) {
            // Item is greater than lines boundaries
            canvas.clipRect(0, 0, measuredWidth, itemHeight);
            canvas.drawText(text, left, yPos, paintSelected);

        } else {
            // Item is inside the lines -> draw completely selected
            canvas.clipRect(0, 0, measuredWidth, itemHeight);
            canvas.drawText(text, left, yPos, paintSelected);
        }
    }
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="Interfaces">

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent motionevent) {
        initVelocityTrackerIfNotExists();

        switch (motionevent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (getChildCount() == 0) {
                    return false;
                }

                initOrResetVelocityTracker();
                velocityTracker.addMovement(motionevent);

                removeCallbacks(flingRunnable);

                // Remember where the motion event started
                lastMotionY = (int) motionevent.getY();
                break;


            case MotionEvent.ACTION_MOVE:
                velocityTracker.addMovement(motionevent);

                final int y = (int) motionevent.getY();
                int deltaY = lastMotionY - y;


                if (!isBeingDragged && Math.abs(deltaY) > TOUCH_SLOP) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    isBeingDragged = true;
                    if (deltaY > 0) {
                        deltaY -= TOUCH_SLOP;
                    } else {
                        deltaY += TOUCH_SLOP;
                    }
                }

                if (isBeingDragged) {
                    // Scroll to follow the motion event
                    lastMotionY = y;
                    scrollOrSpringBack(deltaY);
                    postInvalidateOnAnimation();
                }
                break;

            case MotionEvent.ACTION_UP:
                velocityTracker.addMovement(motionevent);
                final VelocityTracker finalVelocityTracker = velocityTracker;
                finalVelocityTracker.computeCurrentVelocity(1000, MAXIMUM_VELOCITY);
                final int initialVelocity = (int) finalVelocityTracker.getYVelocity();


                if (isBeingDragged || !animationFinished) {
                    animationFinished = false;

                    if ((Math.abs(initialVelocity) > MINIMUM_VELOCITY)) {
                        fling(initialVelocity);
                    } else {
                        post(settle);
                    }

                    endDrag();
                }
                break;
        }
        return true;
    }
    // </editor-fold>


    private Runnable settle = new Runnable() {
        @Override
        public void run() {
            int deltaY = (int) ((currentScrollY - minScrollY) % itemHeight);
            if (deltaY > itemHeight / 2) {
                deltaY = (int) (itemHeight - deltaY);
            } else {
                deltaY = -deltaY;
            }

            boolean finished = true;
            int realDelta;

            if (Math.abs(deltaY) > fraction) {
                if (deltaY < 0) {
                    realDelta = -fraction;
                } else {
                    realDelta = fraction;
                }
                finished = false;
            } else {
                realDelta = deltaY;
            }

            scrollOrSpringBack(realDelta);
            postInvalidateOnAnimation();

            if (finished) {
                selectItemBasedOnScroll(currentScrollY);
                animationFinished = true;
                removeCallbacks(this);
                return;
            }
            postDelayed(this, 10);
        }
    };

    private class FlingRunnable implements Runnable {
        private int limitedVelocity;
        private int velocity;
        private int reducer;
        private int absVelocity;

        public FlingRunnable(int velocity) {
            this.velocity = velocity;
            absVelocity = Math.abs(velocity);
            if (velocity < 0) {
                reducer = -VELOCITY_REDUCER;
            } else {
                reducer = VELOCITY_REDUCER;
            }

            this.limitedVelocity = Integer.MAX_VALUE;
        }

        @Override
        public void run() {
            if (limitedVelocity == Integer.MAX_VALUE) {
                if (Math.abs(velocity) > MAXIMUM_VELOCITY) {
                    if (velocity > 0.0F) {
                        limitedVelocity = MAXIMUM_VELOCITY;
                    } else {
                        limitedVelocity = -MAXIMUM_VELOCITY;
                    }
                } else {
                    limitedVelocity = velocity;
                }
            }


            if (Math.abs(limitedVelocity) >= 0.0F && Math.abs(limitedVelocity) <= MINIMUM_VELOCITY) {
                removeCallbacks(this);
                post(settle);
                return;
            }

            int i = (int) ((limitedVelocity * 10F) / 1000F);
            if (limitedVelocity < 0.0F) {
                limitedVelocity = limitedVelocity + VELOCITY_REDUCER;
            } else {
                limitedVelocity = limitedVelocity - VELOCITY_REDUCER;
            }

            postInvalidateOnAnimation();

            if (scrollOrSpringBack(-i)) {
                postDelayed(this, 10);
            } else {
                selectItemBasedOnScroll(currentScrollY);
                animationFinished = true;
                removeCallbacks(this);
            }
        }
    }


    // ====================================================================================================================================================================================
    // <editor-fold desc="Methods">

    /**
     * Returns the index of the item, which middle point on the y-axis is closest to the given scroll
     * <br>
     * <b>Note:</b> if the passed scroll does not fit within itemHeight returns {@link #NO_POSITION}
     *
     * @param scrollY The index of the closest item, or {@link #NO_POSITION}
     */
    private int getIndexOfItem(int scrollY) {
        if (getChildCount() == 0) {
            return NO_POSITION;
        }

        int topPlaceholderHeight = (int) ((getPlaceHolderCount() / 2) * itemHeight);
        int offsettedScroll = scrollY + topPlaceholderHeight;

        for (int i = 0; i < getChildCount(); i++) {
            int itemTop = (int) (topPlaceholderHeight + i * itemHeight);
            int itemBottom = (int) (itemTop + itemHeight);

            if (offsettedScroll >= itemTop && offsettedScroll <= itemBottom) {
                return i;
            }
        }

        return NO_POSITION;
    }

    private void selectItemBasedOnScroll(int scrollY) {
        if (loopListener != null) {
            int index = getIndexOfItem(scrollY);
            if (index != NO_POSITION) {
                selectedItem = items.get(index);
                loopListener.onItemSettled(selectedItem);
            }
        }
    }

    private void fling(int velocity) {
        int absVelocity = Math.abs(velocity);

        if (absVelocity > MAXIMUM_VELOCITY) {
            if (velocity < 0) {
                velocity = -MAXIMUM_VELOCITY;
            } else {
                velocity = MAXIMUM_VELOCITY;
            }
        }

        if (absVelocity < MINIMUM_VELOCITY) {
            post(settle);
            return;
        }

        flingRunnable = new FlingRunnable(velocity);
        post(flingRunnable);
    }

    /**
     * Scrolls the given deltaY of sets {@link #currentScrollY} to the bounds {@link #minScrollY}, {@link #maxScrollY} if it would exceed the boundaries
     *
     * @param deltaY The value to scroll
     * @return True if scrolling was possible; False if new scroll would be outside range and was trimmed to the boundaries
     */
    private boolean scrollOrSpringBack(int deltaY) {
        int newY = currentScrollY + deltaY;
        if (isWithinScrollRange(newY)) {
            currentScrollY += deltaY;
            return true;
        } else {
            springBack(newY);
            return false;
        }
    }

    private void springBack(int newY) {
        if (newY <= minScrollY) {
            currentScrollY = minScrollY;
        } else if (newY >= maxScrollY) {
            currentScrollY = maxScrollY;
        }
    }

    private void endDrag() {
        isBeingDragged = false;
        recycleVelocityTracker();
    }

    private void initOrResetVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        } else {
            velocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private void onItemSelected(LoopItem item) {
        if (loopListener != null) {
            loopListener.onItemSettled(item);
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

    public boolean isWithinScrollRange(int newY) {
        return newY > minScrollY && newY < maxScrollY;
    }

    /**
     * Returns the maximum scroll range
     * <br>
     * First and last item will always be ONLY half visible.
     *
     * @return Maximum scroll range
     */
    public int getScrollRange() {
        return (int) (itemHeight * (getChildCountWithPlaceholder() - 1) - getHeight());
    }

    /**
     * Returns the child count without the placeholder count
     *
     * @return Number of children excl. placeholder
     */
    public int getChildCount() {
        return items != null ? items.size() : 0;
    }

    /**
     * Returns the child count including the placeholder count for non-looped views
     *
     * @return Number of children incl. placeholder
     */
    public int getChildCountWithPlaceholder() {
        return getChildCount() + getPlaceHolderCount();
    }

    public int getPlaceHolderCount() {
        return loopEnabled ? 0 : displayableItemCount / 2;
    }


    @Nullable
    public T getItem(int position) {
        if (items == null || position < 0 || position > items.size() - 1) {
            return null;
        }

        return items.get(position);
    }


    /**
     * Initializes the LoopView with multiple values at once<br>
     * Use this method instead of calls to update...(), as those will call invalidate()/requestLayout() for every operation
     *
     * @param initializer The inner class {@link Initializer} containing the initial values
     */
    public void initialize(Initializer initializer) {
        if (initializer.listener != null) {
            this.loopListener = initializer.listener;
        }

        if (initializer.items != null) {
            this.items = initializer.items;
        }

        if (initializer.loopEnabled != null) {
            this.loopEnabled = initializer.loopEnabled;
        }

        if (initializer.initPosition != null) {
            this.initPosition = initializer.initPosition;
        }

        if (initializer.textSize != null) {
            this.textSize = initializer.textSize;
        }

        if (initializer.textColor != null) {
            this.colorText = initializer.textColor;
        }

        if (initializer.selectedTextColor != null) {
            this.colorTextSelected = initializer.selectedTextColor;
        }

        if (initializer.textSize != null || initializer.textColor != null || initializer.selectedTextColor != null) {
            initPaint(paintText, colorText);
            initPaint(paintSelected, colorTextSelected);
            initPaint(paintDivider, colorDivider);
        }

        if (initializer.mode != null) {
            this.mode = initializer.mode;
        }

        initCircularSizes();
        initScrollAndIndex();
        updateScrollRange();

        if (initializer.textSize != null) {
            requestLayout();
        } else {
            invalidate();
        }
    }
    // </editor-fold>


    // ====================================================================================================================================================================================
    // <editor-fold desc="TODO CHECK">

    public void setLoopEnabled(boolean enabled) {
        loopEnabled = enabled;
    }
    // </editor-fold>


    /**
     * Initializer class to initialize the {@link CircularListView} with multiple values at once, to prevent multiple calls to invalidate()/requestLayout()
     */
    public class Initializer {
        @Nullable private LoopListener listener;
        @Nullable private List<T>      items;
        @Nullable private Integer      textSize;
        @Nullable private Integer      textColor;
        @Nullable private Integer      selectedTextColor;
        @Nullable private Integer      initPosition;
        @Nullable private Boolean      loopEnabled;
        @Nullable private Integer      mode;

        public Initializer items(List<T> items) {
            this.items = items;
            return this;
        }

        public Initializer loopEnabled(boolean loopEnabled) {
            this.loopEnabled = loopEnabled;
            return this;
        }

        public Initializer textSize(int textSize) {
            this.textSize = textSize;
            return this;
        }

        public Initializer textColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public Initializer selectedTextColor(int selectedTextColor) {
            this.selectedTextColor = selectedTextColor;
            return this;
        }

        public Initializer initPosition(int initPosition) {
            this.initPosition = initPosition;
            return this;
        }

        public Initializer listener(LoopListener listener) {
            this.listener = listener;
            return this;
        }

        public Initializer mode(@Mode int mode) {
            this.mode = mode;
            return this;
        }
    }


    // ====================================================================================================================================================================================
    // <editor-fold desc="Updates">

    /**
     * Updates the current items.
     * <ol>
     *     <li>If the new list contains the current selected item it will set its new selected position, to the same item </li>
     *     <li>It the previous/current selected item position == 0, it will set the new position to 0 </li>
     *     <li>It the previous/current selected item position == last position, it will set the new position to the last position</li>
     * </ol>
     *
     * @param items New items
     */
    public void updateItems(List<T> items) {
        int countBeforeUpdate = getChildCount();
        int indexBeforeUpdate = getIndexOfItem(currentScrollY);

        int itemPosition = -1;
        T currentItem = getItem(indexBeforeUpdate);
        if (currentItem != null) {
            for (int i = 0; i < items.size(); ++i) {
                T item = items.get(i);
                if (currentItem.equals(item)) {
                    itemPosition = i;
                    break;
                }
            }
        }

        updateItemsInternal(items);
        updateCurrentScrollY(indexBeforeUpdate, countBeforeUpdate, itemPosition);

        invalidate();
    }

    /**
     * Updates the current items and sets the selected item to the first one
     *
     * @param items New items
     */
    public void updateItemsAndScrollTop(List<T> items) {
        updateItemsInternal(items);
        initPosition = 0;
        initScrollAndIndex();
        invalidate();
    }

    /**
     * Updates the current items and sets the selected item to the last one
     *
     * @param items New items
     */
    public void updateItemsAndScrollBottom(List<T> items) {
        updateItemsInternal(items);
        initPosition = items.size() - 1;
        initScrollAndIndex();
        invalidate();
    }

    private void updateItemsInternal(List<T> items) {
        this.items = items;
        updateScrollRange();
    }

    private void updateCurrentScrollY(int indexBeforeUpdate, int countBeforeUpdate, int itemPosition) {
        if (itemPosition != -1) {
            initPosition = itemPosition;
            initScrollAndIndex();
        } else if (indexBeforeUpdate == 0) {
            currentScrollY = minScrollY;
        } else if (indexBeforeUpdate == countBeforeUpdate - 1) {
            currentScrollY = maxScrollY;
        }
    }

    public void updateTextSize(int textSize) {
        if (this.textSize != textSize) {
            this.textSize = textSize;
            initPaint(paintText, colorText);
            initPaint(paintSelected, colorTextSelected);
            initPaint(paintDivider, colorDivider);

            initCircularSizes();
            initScrollAndIndex();
            updateScrollRange();
            requestLayout();
        }
    }

    public final void updateInitPosition(int initPosition) {
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

    public final void setListener(LoopListener LoopListener) {
        loopListener = LoopListener;
    }

    public void setMode(@Mode int mode) {
        this.mode = mode;
    }
    // </editor-fold>

}

