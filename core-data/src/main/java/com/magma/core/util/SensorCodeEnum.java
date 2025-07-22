package com.magma.core.util;

public enum SensorCodeEnum {
    S("Status"),
    T("Temperature", "\u2103"),
    H("Humidity", "%"),
    M("Moisture", "%"),
    MA("Moisture Analog"),
    MEA("Moisture Conductivity Analog"),
    MEA0("Moisture Conductivity Analog V0"),
    MEA1("Moisture Conductivity Analog V1"),
    MEA2("Moisture Conductivity Analog V2"),
    MEA3("Moisture Conductivity Analog V3"),
    MEA4("Moisture Conductivity Analog V4"),
    IRO("Deep Root", "%"),
    N("Noise", "Noisy(1)/Normal(0)"),
    K("Smoke"),
    V("Vibration"),
    W("Water"),
    G("GPS"),
    B("Battery"),
    BC("Battery Charging"),
    RA("Relative Age"),   // Age in seconds its based on a specific timestamp
    BL("Battery Level"),
    RSSI("Received Signal Strength Indicator"),
    NSR("Noise Ratio"),
    LPG("LPG Gas"),
    D("Dust"),
    R("Rain Drop"),
    O3("O3"),
    CO("CO"),
    DS("Door Status", "OPEN(1)/CLOSED(0)"),
    LI("Light Intensity"),
    LIA("Light Intensity Analog"),
    LIA1("Light Intensity Analog V1"),
    CN("Soil conductivity", "μS/cm"),
    CNA("Soil conductivity Analog"),
    PHA("Ph Analog"),
    PH("Ph"),
    CS("Current Sensor", "Presence/Power Cut"),
    E("Energy"),
    L("Location"),
    RL("Relative Location"), // Location based on other objects such as beacons
    HB("Heart Beat", "Live(1)/Offline(0)"),
    X("Test X"),
    Y("Test Y"),
    RF("Rainfall", "mm"),
    CRF("Cumulative Rainfall", "mm"),
    EV("Evaporation"),
    WD("Wind Direction", "\u2134"),
    WS("Wind Speed", "km/h"),
    SI("Sunshine Intensity"),
    ST("Soil Temperature", "\u2103"),
    P("Pressure", "hPa"),
    WI("Wifi Signal Strength Indication"),
    CT("Count"),
    CTD("Count Discrete"),  // used when count is not continues (when count is discrete)
    YC("Yield Output Count"),
    PS("Power Status"),
    IT("Internal Temperature"),
    SS("Signal Strength"),
    SNR("Signal to Noise Ratio"),
    CF("Counter Overflow"),
    RT("Real Time"),
    LS("Log Synchronised"),
    CE_N("Nitrogen"),
    CE_P("Phosphorus"),
    CE_K("Potassium"),

    GN("Green"),
    RD("Red"),
    YW("Yellow"),
    //Actuate Status
    C("Operated Status"),
    A("Alarm Status"),

    //scada changes
    ID_MSG("Message ID"),
    ID_IV("Inv ID"),
    AP("Sum Of Active Phases"),
    CP_A("Phase A Current"),
    CP_B("Phase B Current"),
    CP_C("Phase C Current"),
    VP_AN("V Phase A to N"),
    VP_BN("V Phase B to N"),
    VP_CN("V Phase C to N"),
    AC("Total AC Power"),
    LF("Line Frequency"),
    AC_AP("AC Apparent Power"),
    AC_RP("AC Reactive Power"),
    AC_PF("AC Power Factor"),
    AC_E("AC Energy"),
    DC_C("DC Current"),
    DC_V("DC Voltage"),
    DC_P("DC Power"),
    OS("Operating State"),
    V_PCC("V to PCC"),
    S_PV("PV Status"),
    S_ECP("ECP Conn Status"),
    CC("Conn Control"),
    T_RPI("RPi Temperature"),
    T_C("Cabinet Temperature"),
    S_D("Status Description"),
    S_M("Modbus Status"),
    S_U("Update Status"),
    EF("Event Fields"),

    N_ID("Network ID"),
    LES("Lora Error State"),
    S_ID("Slave ID");


    private String value;
    private String unit;

    SensorCodeEnum(String value) {
        this(value, null);
    }

    SensorCodeEnum(String value, String unit) {
        this.value = value;
        this.unit = unit;
    }

    public String value() {
        return value;
    }

    public String getValue() {
        return value;
    }

    public String unit() {
        return unit;
    }

    public String getUnit() {
        return unit;
    }

    public static String getUnitByCode(String code) {
        for (SensorCodeEnum sensorCode : values()) {
            if (sensorCode.name().equals(code)) {
                return sensorCode.unit();
            }
        }
        return null;
    }
}
