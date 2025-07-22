package com.magma.core.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.magma.core.data.support.Operation;
import com.magma.core.util.ActuatorCode;
import com.magma.util.MagmaUtil;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document
public class KitModel {

    @Id
    private String id;
    private String name;

    private Integer noOfSensors;

    private String[] sensors;

    private Integer noOfProperties;

    private String[] properties;

    private Integer noOfActuators;

    private ActuatorCode[] actuators;

    private Integer noOfActions;

    private ActuatorCode[] actions;

    private Set<Operation> operations = new HashSet<>();


    @JsonIgnore
    //<Sensor Number, Operations>
    private Map<Integer, Set<Operation>> realTimeSet = new HashMap<>();

    @JsonIgnore
    private Set<Operation> bulkSet = new HashSet<>();

    private Boolean batteryEnabled;

    private Boolean gpsEnabled;

    private Boolean lbsEnabled;

    private String type;

    @JsonIgnore
    @CreatedDate
    private DateTime creationDate;

    @JsonIgnore
    @LastModifiedDate
    private DateTime modifiedDate;

    public KitModel() {
    }

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


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String[] getSensors() {
        return sensors;
    }

    public void setSensors(String[] sensors) {
        this.sensors = sensors;
    }

    public Integer getNoOfProperties() {
        return noOfProperties;
    }

    public void setNoOfProperties(Integer noOfProperties) {
        this.noOfProperties = noOfProperties;
    }

    public String[] getProperties() {
        return properties;
    }

    public void setProperties(String[] properties) {
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

    public Set<Operation> getOperations() {
        return operations;
    }

    public void setOperations(Set<Operation> operations) {
        this.operations = operations;
    }

    public Map<Integer, Set<Operation>> getRealTimeSet() {
        return realTimeSet;
    }

    public void setRealTimeSet(Map<Integer, Set<Operation>> realTimeSet) {
        this.realTimeSet = realTimeSet;
    }

    public Set<Operation> getBulkSet() {
        return bulkSet;
    }

    public void setBulkSet(Set<Operation> bulkSet) {
        this.bulkSet = bulkSet;
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

    public DateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(DateTime creationDate) {
        this.creationDate = creationDate;
    }

    public DateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(DateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }


    @Override
    public String toString() {
        return "KitModel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", noOfSensors=" + noOfSensors +
                ", sensors=" + Arrays.toString(sensors) +
                ", noOfProperties=" + noOfProperties +
                ", properties=" + Arrays.toString(properties) +
                ", noOfActuators=" + noOfActuators +
                ", actuators=" + Arrays.toString(actuators) +
                ", noOfActions=" + noOfActions +
                ", actions=" + Arrays.toString(actions) +
                ", operations=" + operations +
                ", realTimeSet=" + realTimeSet +
                ", bulkSet=" + bulkSet +
                ", batteryEnabled=" + batteryEnabled +
                ", gpsEnabled=" + gpsEnabled +
                ", lbsEnabled=" + lbsEnabled +
                ", type=" + type +
                ", creationDate=" + creationDate +
                ", modifiedDate=" + modifiedDate +
                '}';
    }
}
