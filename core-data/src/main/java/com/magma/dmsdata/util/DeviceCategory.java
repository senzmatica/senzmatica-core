package com.magma.dmsdata.util;

public enum DeviceCategory {
    SENSOR_DEVICE("4"),
    SLAVE_DEVICE("3"),
    MASTER_DEVICE("2"),
    CONTROL_DEVICE("1"),
    SOLENOID_DEVICE("0");

    private String value;

    DeviceCategory(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
