package com.magma.dmsdata.util;

public enum AlertPersistence {
    CONTINUOUS(0),
    INTERVAL(1),
    SQUARE(2);

    private int value;

    AlertPersistence(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}
