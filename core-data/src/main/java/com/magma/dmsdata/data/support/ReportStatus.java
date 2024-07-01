package com.magma.dmsdata.data.support;

/**
 * Created by nirajh on 10/1/18.
 */
public enum ReportStatus {
    REPORTED(0),
    ASSIGNED(1),
    IN_PROGRESS(2),
    DONE(3),
    APPROVED(5),
    FIXED(4);

    private int value;

    ReportStatus(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}
