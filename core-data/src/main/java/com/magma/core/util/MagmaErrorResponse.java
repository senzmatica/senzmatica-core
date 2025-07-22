package com.magma.core.util;

import com.magma.util.MagmaResponse;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class MagmaErrorResponse extends MagmaResponse {
    private String errorCode;

    private String message;

    private Map<String, String> errors;

    public MagmaErrorResponse(MagmaStatus status) {
        this.errorCode = status.getStatusCode();
        this.message = status.getStatusDescription();
    }

    public MagmaErrorResponse(Map<String, String> errors) {
        this.setStatusCode(MagmaStatus.BAD_REQUEST.getStatusCode());
        this.errorCode = HttpStatus.BAD_REQUEST.toString();
        this.errors = errors;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        return "MozErrorResponse{" +
                "errorCode='" + errorCode + '\'' +
                ", message='" + message + '\'' +
                ", errors='" + errors + '\'' +
                '}';
    }
}
