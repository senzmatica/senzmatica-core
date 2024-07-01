package com.magma.dmsdata.data.support;

public enum FarmType {

    LAND("LAND"),
    HYDROPONIC("HYDROPONIC"),
    POLYTUNNEL("POLYTUNNEL"),
    SEMI_PROTECTED("SEMI_PROTECTED"),
    PROTECTED_HOUSE("PROTECTED_HOUSE"),
    SOIL_LESS_CULTIVATION("SOIL_LESS_CULTIVATION"),
    OPEN_FIELD("OPEN FIELD");

    private final String type;

    FarmType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
