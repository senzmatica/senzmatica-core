package com.magma.core.util;

/**
 * Created by nirajh on 10/1/18.
 */
public enum AlertStatus {
    ACTIVE(0),
    EXPIRED(1);

    private int value;

    AlertStatus(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}
