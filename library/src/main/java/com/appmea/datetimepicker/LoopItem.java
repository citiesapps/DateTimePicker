package com.appmea.datetimepicker;

public interface LoopItem {
    String getText();

    default boolean equals(LoopItem o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return getText().equals(o.getText());
    }
}
