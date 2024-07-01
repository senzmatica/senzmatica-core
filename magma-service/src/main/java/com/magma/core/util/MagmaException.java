package com.magma.core.util;

public class MagmaException extends RuntimeException {
    private MagmaStatus status;

    public MagmaException(MagmaStatus status) {
        super(status.getStatusDescription());
        this.status = status;
    }

    public MagmaStatus getStatus() {
        return status;
    }

    public void setStatus(MagmaStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "MozException{" +
                "status=" + status +
                '}';
    }
}
