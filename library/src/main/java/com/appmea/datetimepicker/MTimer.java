package com.appmea.datetimepicker;

/**
 * Created by prati on 06-Jul-16 at VARAHI TECHNOLOGIES.
 * http://www.varahitechnologies.com
 */

import java.util.TimerTask;

import timber.log.Timber;

final class MTimer extends TimerTask {

    int realTotalOffset;
    int realOffset;
    int offset;
    final LoopView loopView;

    MTimer(LoopView loopview, int offset) {
        super();
        this.loopView = loopview;
        this.offset = offset;
        realTotalOffset = Integer.MAX_VALUE;
        realOffset = 0;
    }

    @Override
    public final void run() {
        Timber.e("run: ");
        if (realTotalOffset == Integer.MAX_VALUE) {
            offset = (int)((offset + loopView.itemHeight) % loopView.itemHeight);
            if ((float) offset > loopView.itemHeight / 2.0F) {
                realTotalOffset = (int) (loopView.itemHeight - (float) offset);
            } else {
                realTotalOffset = -offset;
            }
        }
        realOffset = (int) ((float) realTotalOffset * 0.1F);

        if (realOffset == 0) {
            if (realTotalOffset < 0) {
                realOffset = -1;
            } else {
                realOffset = 1;
            }
        }
        if (Math.abs(realTotalOffset) <= 0) {
            loopView.cancelFuture();
            loopView.handler.sendEmptyMessage(3000);
            return;
        } else {
            loopView.currentScrollY = loopView.currentScrollY + realOffset;
            loopView.handler.sendEmptyMessage(1000);
            realTotalOffset = realTotalOffset - realOffset;
            return;
        }
    }
}
