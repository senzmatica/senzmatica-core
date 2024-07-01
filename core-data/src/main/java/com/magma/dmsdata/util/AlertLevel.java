package com.magma.dmsdata.util;

/**
 * Created by john on 11/3/15.
 */
public enum AlertLevel {
    ZERO(0), ONE(1), TWO(2), THREE(3);

    private int value;

    AlertLevel(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}
