package com.magma.core.data.support;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Connectivity {
    GSM(0), WIFI(1), NRF(2), BLUETOOTH(3), LORA(4);

    private int value;

    Connectivity(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    @JsonCreator
    public static Connectivity fromString(String key) {
        if (key == null || key.trim().isEmpty()) {
            return null; // default value
        }
        return Connectivity.valueOf(key.toUpperCase());

    }

}
