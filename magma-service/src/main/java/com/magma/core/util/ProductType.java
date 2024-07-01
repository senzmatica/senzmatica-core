package com.magma.core.util;

public enum ProductType {
    DIGI_Lora("2"),
    DIGI_Controller("1"),
    DIGI_Plant("0");

    private String value;

    ProductType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
