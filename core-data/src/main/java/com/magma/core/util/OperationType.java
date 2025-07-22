package com.magma.core.util;

/**
 * Created by nirajh on 10/1/18.
 */
public enum OperationType {
    REAL_TIME("r"),
    BULK("b");

    private String value;

    OperationType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
