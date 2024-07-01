package com.magma.dmsdata.util;

/**
 * Created by nirajh on 10/1/18.
 */
public enum KitType {
    TRUCK("Truck"),
    MACHINE("Machine"),
    COLD_ROOM("Cold Room"),
    SERVER_ROOM("Server Room"),
    CONTAINER("Container"),
    FARM("Farm"),
    SOLENOID("Solenoid Valve");

    private String value;

    KitType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
