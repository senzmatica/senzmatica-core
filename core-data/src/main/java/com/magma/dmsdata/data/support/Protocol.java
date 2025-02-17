package com.magma.dmsdata.data.support;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.magma.dmsdata.util.MagmaException;
import com.magma.dmsdata.util.MagmaStatus;

public enum Protocol {
    HTTP(0), HTTPS(1), MQTT(2), TCP(3);

    private int value;

    Protocol(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    @JsonCreator
    public static Protocol fromString(String key) {
        if (key == null || key.trim().isEmpty()) {
            return null; // default value
        }
        return Protocol.valueOf(key.toUpperCase());

    }

}
