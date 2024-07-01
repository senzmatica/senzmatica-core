package com.magma.core.util;

public enum Configuration {

    CT("Current Sensor Threshold"),
    X("Test X"),
    Y("Test Y");

    private String value;

    Configuration(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
