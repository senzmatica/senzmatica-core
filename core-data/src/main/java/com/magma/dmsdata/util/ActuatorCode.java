package com.magma.dmsdata.util;

public enum ActuatorCode {

    S("Solenoid"),
    M("Motor"),
    B("Buzzer"),
    T("Trigger"),
    X("Test X"),
    Y("Test Y");

    private String value;

    ActuatorCode(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
