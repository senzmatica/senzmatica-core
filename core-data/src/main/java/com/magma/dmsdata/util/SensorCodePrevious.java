package com.magma.dmsdata.util;

public enum SensorCodePrevious {
    S("Status"),
    T("Temperature"),
    H("Humidity"),
    M("Moisture"),
    MA("Moisture Analog"),
    MEA("Moisture Conductivity Analog"),
    MEA0("Moisture Conductivity Analog V0"),
    MEA1("Moisture Conductivity Analog V1"),
    MEA2("Moisture Conductivity Analog V2"),
    MEA3("Moisture Conductivity Analog V3"),
    MEA4("Moisture Conductivity Analog V4"),
    IRO("Deep Root"),
    N("Noise"),
    K("Smoke"),
    V("Vibration"),
    W("Water"),
    G("GPS"),
    B("Battery"),
    BC("Battery Charging"),
    RA("Relative Age"), // Age in seconds its based on a specific timestamp
    BL("Battery Level"),
    RSSI("Received Signal Strength Indicator"),
    NSR("Noise Ratio"),
    LPG("LPG Gas"),
    D("Dust"),
    R("Rain Drop"),
    O3("O3"),
    CO("CO"),
    DS("Door Status"),
    LI("Light Intensity"),
    LIA("Light Intensity Analog"),
    LIA1("Light Intensity Analog V1"),
    CN("Soil conductivity"),
    CNA("Soil conductivity Analog"),
    PHA("Ph Analog"),
    PH("Ph"),
    CS("Current Sensor"),
    E("Energy"),
    L("Location"),
    RL("Relative Location"), // Location based on other objects such as beacons
    HB("Heart Beat"),
    X("Test X"),
    Y("Test Y"),
    RF("Rainfall"),
    CRF("Cumulative Rainfall"),
    EV("Evaporation"),
    WD("Wind Direction"),
    WS("Wind Speed"),
    SI("Sunshine Intensity"),
    ST("Soil Temperature"),
    P("Pressure"),
    WI("Wifi Signal Strength Indication"),
    CT("Count"),
    CTD("Count Discrete"), // used when count is not continues (when count is discrete)
    YC("Yield Output Count"),
    PS("Power Status"),
    IT("Internal Temperature"),
    SS("Signal Strength"),
    CF("Counter Overflow"),
    RT("Real Time"),
    LS("Log Synchronised"),
    CE_N("Nitrogen"),
    CE_P("Phosphorus"),
    CE_K("Potassium"),

    // Actuate Status
    C("Operated Status"),
    A("Alarm Status");

    private String value;

    SensorCodePrevious(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public String getValue() {
        return value;
    }
}
