package com.magma.dmsdata.data.support;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Shift {

    private Arithmetic operation;

    private Map<String, Double> meta = new HashMap<>();

    public Arithmetic getOperation() {
        return operation;
    }

    public void setOperation(Arithmetic operation) {
        this.operation = operation;
    }

    public Map<String, Double> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Double> meta) {
        this.meta = meta;
    }

    @Override
    public String toString() {
        return "Shift{" +
                "operation=" + operation +
                ", meta=" + meta +
                '}';
    }
}