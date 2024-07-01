package com.magma.core.data.support;

/**
 * Created by nirajh on 10/1/18.
 */
public enum NotificationType {
    LIMIT_EXCEEDS(0),
    BATTERY_LOW(1),
    DEVICE_OFFLINE(2),
    SENSOR_ERROR(3),
    PENDING_PAYMENT(4),
    SCHEDULE(5);

    private int value;

    NotificationType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}
