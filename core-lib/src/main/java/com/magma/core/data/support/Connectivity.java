package com.magma.core.data.support;

public enum Connectivity {
    GSM(0), WIFI(1), NRF(2), BLUETOOTH(3), LORA(4);

    private int value;

    Connectivity(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}
