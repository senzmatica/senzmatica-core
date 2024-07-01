package com.magma.dmsdata.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.dmsdata.data.support.*;
import com.magma.dmsdata.util.ActuatorCode;
import com.magma.dmsdata.util.Configuration;
import com.magma.util.MagmaDateTimeSerializer;
import com.magma.util.MagmaUtil;
import com.magma.util.Status;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document
public class Device {

    @Id
    private String id;

    private String name;
    private String group; // can be used to group devices

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String kitId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String description;

    private Integer noOfSensors;

    private String[] sensorCodes;

    @JsonIgnore
    private Map<Integer, Sensor> sensorMap = new HashMap<>();

    private Map<Integer, Map<Integer, Shift>> shiftMap = new HashMap<>();

    private Integer noOfActuators;

    private ActuatorCode[] actuatorCodes;

    @JsonIgnore
    private Map<Integer, Actuator> actuatorMap = new HashMap<>();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Geo geo;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Battery battery;

    private Boolean maintain;

    private Integer interval;

    private Integer intervalMin = 10;

    private Boolean persistence;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean offline;

    private Connectivity connectivity;

    private Map<Connectivity, Map<String, String>> connectivityMatrix = new HashMap<>();

    private Protocol protocol;

    private Map<Configuration, String> configurations = new HashMap<>();

    private Status status;

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    private DateTime lastSeen;

    @JsonIgnore
    @CreatedDate
    private DateTime creationDate;

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    @LastModifiedDate
    private DateTime modifiedDate;

    private Integer batchNumber;

    private ProductData product;

    private DeviceParameterConfiguration deviceParameterConfiguration;

    private Map<String, String> references;

    private MagmaCodec codec;

    private String magmaCodecId;
    private String lastRawData;
    private String referenceName;

    private Map<String, String> metaData = null;

    public Device() {
    }

    public Device(String id, Integer interval, Integer noOfSensors, String[] sensorCodes, Integer noOfActuators,
                  ActuatorCode[] actuatorCodes) {
        this.id = id;
        this.interval = interval;
        this.noOfSensors = noOfSensors;
        this.sensorCodes = sensorCodes;
        this.noOfActuators = noOfActuators;
        this.actuatorCodes = actuatorCodes;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    public boolean validate() {
        return MagmaUtil.validate(id)
                && sensorCodes != null && sensorCodes.length == noOfSensors
                && actuatorCodes != null && actuatorCodes.length == noOfActuators;
    }

    public String getLastRawData() {
        return lastRawData;
    }

    public void setLastRawData(String lastRawData) {
        this.lastRawData = lastRawData;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean validateAdd() {
        return MagmaUtil.validate(id) && MagmaUtil.validate(name);
    }

    public List<Sensor> getSensors() {
        return new ArrayList<>(sensorMap.values());
    }

    public List<Actuator> getActuators() {
        return new ArrayList<>(actuatorMap.values());
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

    public String getKitId() {
        return kitId;
    }

    public void setKitId(String kitId) {
        this.kitId = kitId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getNoOfSensors() {
        return noOfSensors;
    }

    public void setNoOfSensors(Integer noOfSensors) {
        this.noOfSensors = noOfSensors;
    }

    public String[] getSensorCodes() {
        return sensorCodes;
    }

    public void setSensorCodes(String[] sensorCodes) {
        this.sensorCodes = sensorCodes;
    }

    public Map<Integer, Sensor> getSensorMap() {
        return sensorMap;
    }

    public void setSensorMap(Map<Integer, Sensor> sensorMap) {
        this.sensorMap = sensorMap;
    }

    public Map<Integer, Map<Integer, Shift>> getShiftMap() {
        return shiftMap;
    }

    public void setShiftMap(Map<Integer, Map<Integer, Shift>> shiftMap) {
        this.shiftMap = shiftMap;
    }

    public Integer getNoOfActuators() {
        return noOfActuators;
    }

    public void setNoOfActuators(Integer noOfActuators) {
        this.noOfActuators = noOfActuators;
    }

    public ActuatorCode[] getActuatorCodes() {
        return actuatorCodes;
    }

    public void setActuatorCodes(ActuatorCode[] actuatorCodes) {
        this.actuatorCodes = actuatorCodes;
    }

    public Map<Integer, Actuator> getActuatorMap() {
        return actuatorMap;
    }

    public void setActuatorMap(Map<Integer, Actuator> actuatorMap) {
        this.actuatorMap = actuatorMap;
    }

    public Geo getGeo() {
        return geo;
    }

    public void setGeo(Geo geo) {
        this.geo = geo;
    }

    public Battery getBattery() {
        return battery;
    }

    public void setBattery(Battery battery) {
        this.battery = battery;
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

    public Boolean getOffline() {
        return offline;
    }

    public void setOffline(Boolean offline) {
        this.offline = offline;
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

    public Map<Configuration, String> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(Map<Configuration, String> configurations) {
        this.configurations = configurations;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public DateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(DateTime lastSeen) {
        this.lastSeen = lastSeen;
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

    public Integer getIntervalMin() {
        return intervalMin;
    }

    public void setIntervalMin(Integer intervalMin) {
        this.intervalMin = intervalMin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Device device = (Device) o;

        return id.equals(device.id);

    }

    public Integer getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(Integer batchNumber) {
        this.batchNumber = batchNumber;
    }

    public MagmaCodec getCodec() {
        return codec;
    }

    public void setCodec(MagmaCodec magmaCodec) {
        this.codec = magmaCodec;
    }

    public String getMagmaCodecId() {
        return magmaCodecId;
    }

    public void setMagmaCodecId(String magmaCodecId) {
        this.magmaCodecId = magmaCodecId;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public ProductData getProduct() {
        return product;
    }

    public void setProduct(ProductData product) {
        this.product = product;
    }

    public DeviceParameterConfiguration getDeviceParameterConfiguration() {
        return deviceParameterConfiguration;
    }

    public void setDeviceParameterConfiguration(DeviceParameterConfiguration deviceParameterConfiguration) {
        this.deviceParameterConfiguration = deviceParameterConfiguration;
    }

    public Map<String, String> getReferences() {
        return references;
    }

    public void setReferences(Map<String, String> references) {
        this.references = references;
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

    // public String getReferenceName(){
    // return corporateConnectorService.referenceName(this.kitId);
    // }

    @Override
    public String toString() {
        return "Device{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", kitId='" + kitId + '\'' +
                ", description='" + description + '\'' +
                ", noOfSensors=" + noOfSensors +
                ", sensorCodes=" + Arrays.toString(sensorCodes) +
                ", sensorMap=" + sensorMap +
                ", shiftMap=" + shiftMap +
                ", noOfActuators=" + noOfActuators +
                ", actuatorCodes=" + Arrays.toString(actuatorCodes) +
                ", actuatorMap=" + actuatorMap +
                ", geo=" + geo +
                ", battery=" + battery +
                ", maintain=" + maintain +
                ", interval=" + interval +
                ", intervalMin=" + intervalMin +
                ", persistence=" + persistence +
                ", offline=" + offline +
                ", connectivity=" + connectivity +
                ", connectivityMatrix=" + connectivityMatrix +
                ", protocol=" + protocol +
                ", configurations=" + configurations +
                ", status=" + status +
                ", lastSeen=" + lastSeen +
                ", creationDate=" + creationDate +
                ", modifiedDate=" + modifiedDate +
                ", magmaDecoderId=" + magmaCodecId +
                ", batchNumber=" + batchNumber +
                ", product=" + product +
                ", deviceParameterConfiguration=" + deviceParameterConfiguration +
                '}';
    }

}
