package com.magma.core.util;

public enum ActuatorCode {

    S("Solenoid"),
    M("Motor"),
    B("Buzzer"),
    T("Trigger"),
    X("Test X"),
    Y("Test Y"),
    GS("Game Status"),
    C("Color");

    private String value;

    ActuatorCode(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
