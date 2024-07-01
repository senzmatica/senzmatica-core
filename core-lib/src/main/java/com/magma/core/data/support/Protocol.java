package com.magma.core.data.support;

public enum Protocol {
    HTTP(0), HTTPS(1), MQTT(2), TCP(3);

    private int value;

    Protocol(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}
