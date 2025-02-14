package com.magma.dmsdata.validation;

import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.stream.Collectors;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    private List<ObjectError> errors;

    public BadRequestException(List<ObjectError> errors) {
        super("Validation failed");
        this.errors = errors;
    }

    @Override
    public String getMessage() {
        return errors.stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining(", "));
    }
    public List<ObjectError> getErrors() {
        return errors;
    }
}
