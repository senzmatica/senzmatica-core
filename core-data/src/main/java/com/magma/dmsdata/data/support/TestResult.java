package com.magma.dmsdata.data.support;

public enum TestResult {
    SUCCESS(0), FAILURE(1), OFFLINE(2), PENDING(3), NOT_APPLICABLE(4), ON_GOING(5);

    private int value;

    TestResult(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
