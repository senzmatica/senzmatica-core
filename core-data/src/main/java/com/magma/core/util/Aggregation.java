package com.magma.core.util;

/**
 * Created by john on 11/3/15.
 */
public enum Aggregation {
    // TIME --> get data at the provided 'from' time
    SUM(0), AVG(1), MAX(2), MIN(3), ANY(4), LATEST(5), PREDICT(6), AND(7), OR(8), TIME(9);

    private int value;

    Aggregation(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}
