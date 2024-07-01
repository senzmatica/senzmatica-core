package com.magma.core.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.data.umsdata.util.UmsStatus;
import com.magma.util.MagmaResponse;
import org.springframework.http.HttpStatus;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class MagmaErrorResponse extends MagmaResponse {

    private String errorCode;

    private String message;

    private Map<String, String> errors;

    public MagmaErrorResponse(UmsStatus status) {
        this.errorCode = status.getStatusCode();
        this.message = status.getStatusDescription();
    }

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

    public String getMessage() {
        return message;
    }

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
