package com.magma.core.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.magma.core.util.MagmaErrorResponse;
import com.magma.core.util.MagmaException;
import com.magma.data.umsdata.util.UmsException;
import com.magma.util.BadRequestException;

@ControllerAdvice
public class ExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandler.class);

    @org.springframework.web.bind.annotation.ExceptionHandler(BadRequestException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public MagmaErrorResponse badRequestHandler(HttpServletRequest req, BadRequestException ex) {
        LOGGER.debug("Magma Exception Bad Request : ", ex);
        return new MagmaErrorResponse(errorToMap(ex.getErrors()));
    }

    private Map<String, String> errorToMap(List<ObjectError> errors) {
        Map<String, String> results = new HashMap<>();
        for (ObjectError error : errors) {
            FieldError fieldError = (FieldError) error;
            results.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return results;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(UmsException.class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    public MagmaErrorResponse umsHandler(HttpServletRequest req, UmsException ex) {
        LOGGER.debug("Magma Exception for Logic : {}", ex);
        return new MagmaErrorResponse(ex.getStatus());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MagmaException.class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    public MagmaErrorResponse magmaHandler(HttpServletRequest req, MagmaException ex) {
        LOGGER.debug("Magma Exception for Logic : {}", ex);
        return new MagmaErrorResponse(ex.getStatus());
    }

}
