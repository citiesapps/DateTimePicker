package com.appmea.datetimepicker;

import android.os.Handler;
import android.os.Message;

final class MessageHandler extends Handler {

    private final LoopView loopview;

    MessageHandler(LoopView loopview) {
        super();
        this.loopview = loopview;
    }

    @Override
    public final void handleMessage(Message paramMessage) {
        if (paramMessage.what == 1000) {
            this.loopview.invalidate();
        } else if (paramMessage.what == 2000) {
            LoopView.settlePosition(loopview);
        } else if (paramMessage.what == 3000) {
            this.loopview.itemSelected();
        }
        super.handleMessage(paramMessage);
    }
}
