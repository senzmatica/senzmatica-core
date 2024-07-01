package com.magma.dmsdata.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.magma.dmsdata.data.entity.Battery;
import com.magma.dmsdata.data.support.Connectivity;
import com.magma.dmsdata.data.support.Protocol;
import com.magma.dmsdata.data.support.Shift;
import com.magma.dmsdata.util.ActuatorCode;
import com.magma.dmsdata.util.Configuration;
import com.magma.util.MagmaUtil;
import com.magma.util.Status;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceDTO {

    @NotNull(message = "Please provide device id")
    @NotEmpty(message = "Device id can't be empty")
    private String id;

    @NotNull(message = "Pleace provide device name")
    @NotEmpty(message = "Device name can't be empty")
    private String name;

    private String[] sensorCodes;

    private Integer noOfSensors;

    private ActuatorCode[] actuatorCodes;

    private Integer noOfActuators;

    private Boolean maintain;
    private Integer interval;

    private Integer intervalMin = 10;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String description;

    private Map<Integer, Map<Integer, Shift>> shiftMap = new HashMap<>();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Battery battery;

    private Boolean persistence;

    private Connectivity connectivity;

    private Map<Connectivity, Map<String, String>> connectivityMatrix = new HashMap<>();

    private Map<Configuration, String> configurations = new HashMap<>();

    private Protocol protocol;

    private Status status;

    private Map<String, String> metaData = null;

    private String simNumber;


    public DeviceDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean validate() {
        return MagmaUtil.validate(id)
                && sensorCodes != null && sensorCodes.length == noOfSensors
                && actuatorCodes != null && actuatorCodes.length == noOfActuators;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getSensorCodes() {
        return sensorCodes;
    }

    public void setSensorCodes(String[] sensorCodes) {
        this.sensorCodes = sensorCodes;
    }

    public ActuatorCode[] getActuatorCodes() {
        return actuatorCodes;
    }

    public void setActuatorCodes(ActuatorCode[] actuatorCodes) {
        this.actuatorCodes = actuatorCodes;
    }

    public Boolean getMaintain() {
        return maintain;
    }

    public void setMaintain(Boolean maintain) {
        this.maintain = maintain;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public Boolean getPersistence() {
        return persistence;
    }

    public void setPersistence(Boolean persistence) {
        this.persistence = persistence;
    }

    public Connectivity getConnectivity() {
        return connectivity;
    }

    public void setConnectivity(Connectivity connectivity) {
        this.connectivity = connectivity;
    }

    public Map<Connectivity, Map<String, String>> getConnectivityMatrix() {
        return connectivityMatrix;
    }

    public void setConnectivityMatrix(Map<Connectivity, Map<String, String>> connectivityMatrix) {
        this.connectivityMatrix = connectivityMatrix;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getSimNumber() {
        if (metaData == null) {
            return null;
        }
        return metaData.get("simNumber");
    }

    public void setSimNumber(String simNumber) {
        if (metaData == null) {
            metaData = new HashMap<>();
        }
        metaData.put("simNumber", simNumber);
    }

    public Integer getNoOfSensors() {
        return noOfSensors;
    }

    public void setNoOfSensors(Integer noOfSensors) {
        this.noOfSensors = noOfSensors;
    }

    public Integer getNoOfActuators() {
        return noOfActuators;
    }

    public void setNoOfActuators(Integer noOfActuators) {
        this.noOfActuators = noOfActuators;
    }

    public Integer getIntervalMin() {
        return intervalMin;
    }

    public void setIntervalMin(Integer intervalMin) {
        this.intervalMin = intervalMin;
    }

    public Map<Configuration, String> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(Map<Configuration, String> configurations) {
        this.configurations = configurations;
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<Integer, Map<Integer, Shift>> getShiftMap() {
        return shiftMap;
    }

    public void setShiftMap(Map<Integer, Map<Integer, Shift>> shiftMap) {
        this.shiftMap = shiftMap;
    }

    public Battery getBattery() {
        return battery;
    }

    public void setBattery(Battery battery) {
        this.battery = battery;
    }
}
