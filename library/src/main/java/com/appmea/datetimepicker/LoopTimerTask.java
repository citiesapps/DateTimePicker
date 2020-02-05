package com.appmea.datetimepicker;

import java.util.TimerTask;

import timber.log.Timber;

final class LoopTimerTask extends TimerTask {

    private static final float MAX_VELOCITY     = 5000F;
    private static final float MIN_VELOCITY     = 200F;
    private static final float VELOCITY_REDUCER = 100F;

    private       float    limitedVelocity;
    private final float    originalVelocityY;
    private final LoopView loopView;

    LoopTimerTask(LoopView loopview, float originalVelocityY) {
        super();
        this.loopView = loopview;
        this.originalVelocityY = originalVelocityY;
        this.limitedVelocity = Integer.MAX_VALUE;
    }

    @Override
    public final void run() {
        if (limitedVelocity == Integer.MAX_VALUE) {
//            // Limit velocity
            if (Math.abs(originalVelocityY) > MAX_VELOCITY) {
                if (originalVelocityY > 0.0F) {
                    limitedVelocity = MAX_VELOCITY;
                } else {
                    limitedVelocity = -MAX_VELOCITY;
                }
            } else {
                limitedVelocity = originalVelocityY;
            }
        }


        if (Math.abs(limitedVelocity) >= 0.0F && Math.abs(limitedVelocity) <= MIN_VELOCITY) {
            loopView.cancelFuture();
            loopView.handler.sendEmptyMessage(2000);
            return;
        }
        int i = (int) ((limitedVelocity * 10F) / 1000F);
        LoopView loopview = loopView;
        loopview.currentScrollY = loopview.currentScrollY - i;
        if (!loopView.loopEnabled) {
            if (loopView.currentScrollY <= (int) ((float) (-loopView.initPosition) * loopView.itemHeight)) {
                // When reaching top
                limitedVelocity = 40F;
                loopView.currentScrollY = (int) ((float) (-loopView.initPosition) * loopView.itemHeight);
            } else if (loopView.currentScrollY >= (int) ((float) (loopView.items.size() - 1 - loopView.initPosition) * loopView.itemHeight)) {
                // When reaching bottom
                loopView.currentScrollY = (int) ((float) (loopView.items.size() - 1 - loopView.initPosition) * loopView.itemHeight);
                limitedVelocity = -40F;
            }
        }
        if (limitedVelocity < 0.0F) {
            // Scroll down
            limitedVelocity = limitedVelocity + VELOCITY_REDUCER;
        } else {
            // Scroll up
            limitedVelocity = limitedVelocity - VELOCITY_REDUCER;
        }
        loopView.handler.sendEmptyMessage(1000);
    }
}
