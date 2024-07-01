package com.magma.core.data.dto;

import com.magma.core.data.support.Operation;
import com.magma.core.util.ActuatorCode;
import com.magma.core.util.SensorCode;
import com.magma.util.MagmaUtil;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

public class KitModelDTO {

    @NotNull(message = "Please provide kit model id")
    @NotEmpty(message = "KitModel id can't be empty")
    private String id;

    private String name;

    private Integer noOfSensors;

    private SensorCode[] sensors;

    private Integer noOfProperties;

    private SensorCode[] properties;

    private Integer noOfActuators;

    private ActuatorCode[] actuators;

    private Integer noOfActions;

    private ActuatorCode[] actions;

    private Boolean batteryEnabled;

    private Boolean gpsEnabled;

    private Boolean lbsEnabled;

    private String type;

    private Set<Operation> operations = new HashSet<>();

    public boolean validate() {
        return MagmaUtil.validate(name) &&
                noOfSensors != null &&
                noOfProperties != null &&
                noOfActuators != null &&
                noOfActions != null &&
                sensors != null &&
                properties != null &&
                actuators != null &&
                actions != null &&
                type != null &&
                operations != null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNoOfSensors() {
        return noOfSensors;
    }

    public void setNoOfSensors(Integer noOfSensors) {
        this.noOfSensors = noOfSensors;
    }

    public SensorCode[] getSensors() {
        return sensors;
    }

    public void setSensors(SensorCode[] sensors) {
        this.sensors = sensors;
    }

    public Integer getNoOfProperties() {
        return noOfProperties;
    }

    public void setNoOfProperties(Integer noOfProperties) {
        this.noOfProperties = noOfProperties;
    }

    public SensorCode[] getProperties() {
        return properties;
    }

    public void setProperties(SensorCode[] properties) {
        this.properties = properties;
    }

    public ActuatorCode[] getActuators() {
        return actuators;
    }

    public void setActuators(ActuatorCode[] actuators) {
        this.actuators = actuators;
    }

    public Integer getNoOfActuators() {
        return noOfActuators;
    }

    public void setNoOfActuators(Integer noOfActuators) {
        this.noOfActuators = noOfActuators;
    }

    public Integer getNoOfActions() {
        return noOfActions;
    }

    public void setNoOfActions(Integer noOfActions) {
        this.noOfActions = noOfActions;
    }

    public ActuatorCode[] getActions() {
        return actions;
    }

    public void setActions(ActuatorCode[] actions) {
        this.actions = actions;
    }

    public Boolean getBatteryEnabled() {
        return batteryEnabled;
    }

    public void setBatteryEnabled(Boolean batteryEnabled) {
        this.batteryEnabled = batteryEnabled;
    }

    public Boolean getGpsEnabled() {
        return gpsEnabled;
    }

    public void setGpsEnabled(Boolean gpsEnabled) {
        this.gpsEnabled = gpsEnabled;
    }

    public Boolean getLbsEnabled() {
        return lbsEnabled;
    }

    public void setLbsEnabled(Boolean lbsEnabled) {
        this.lbsEnabled = lbsEnabled;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<Operation> getOperations() {
        return operations;
    }

    public void setOperations(Set<Operation> operations) {
        this.operations = operations;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
