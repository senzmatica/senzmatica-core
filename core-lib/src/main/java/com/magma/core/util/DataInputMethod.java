package com.magma.core.util;

public enum DataInputMethod {
    DEVICE(0),
    MANUAL(1),
    IMAGE(2);

    private int value;

    DataInputMethod(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
