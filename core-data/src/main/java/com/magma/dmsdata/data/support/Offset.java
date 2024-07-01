package com.magma.dmsdata.data.support;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Offset {

    private Integer sensor;

    private Integer actuator;

    public Offset() {
    }

    public Offset(Integer sensor, Integer actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    public Integer getSensor() {
        return sensor;
    }

    public void setSensor(Integer sensor) {
        this.sensor = sensor;
    }

    public Integer getActuator() {
        return actuator;
    }

    public void setActuator(Integer actuator) {
        this.actuator = actuator;
    }

    @Override
    public String toString() {
        return "Offset{" +
                "sensor=" + sensor +
                ", actuator=" + actuator +
                '}';
    }
}