package com.appmea.datetimepicker;

public interface LoopListener {
    /**
     * Called when the settle animation is finished
     *
     * @param item The selected item
     */
    default void onItemSettled(LoopItem item) {
    }

    /**
     * Called on every draw cycle, to be able to update views accordingly
     *
     * @param item The selected item
     */
    default void onItemScrolled(LoopItem item) {
    }
}
