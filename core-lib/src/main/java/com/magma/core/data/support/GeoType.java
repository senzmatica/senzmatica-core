package com.magma.core.data.support;

/**
 * Created by john on 11/3/15.
 */
public enum GeoType {
    LBS(0),
    RLL(2),     //relative location
    GPS(1);

    private int value;

    GeoType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}
