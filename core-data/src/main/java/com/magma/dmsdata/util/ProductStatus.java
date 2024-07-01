package com.magma.dmsdata.util;

public enum ProductStatus {
    APPROVED("1"),
    NOT_APPROVED("0");

    private String value;

    ProductStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
