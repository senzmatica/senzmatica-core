package com.magma.core.util;
public enum UpdateStatus {

    // ota manager and remote manager update statuses for device
    FAILED("0"),
    PENDING("1"),
    PARTIALLY_UPDATED("2"),
    UPDATED("3");



    private String value;

    UpdateStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
