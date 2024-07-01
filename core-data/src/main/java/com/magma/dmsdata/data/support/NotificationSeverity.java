package com.magma.dmsdata.data.support;

/**
 * Created by nirajh on 10/1/18.
 */
public enum NotificationSeverity {
    INFO(0),
    WARNING(10),
    ERROR(20);

    private int value;

    NotificationSeverity(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}
