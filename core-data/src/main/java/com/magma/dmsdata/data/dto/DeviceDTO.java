package com.magma.dmsdata.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.magma.dmsdata.data.entity.Battery;
import com.magma.dmsdata.data.support.Connectivity;
import com.magma.dmsdata.data.support.DeviceParameterConfiguration;
import com.magma.dmsdata.data.support.Protocol;
import com.magma.dmsdata.data.support.Shift;
import com.magma.dmsdata.util.ActuatorCode;
import com.magma.dmsdata.util.Configuration;
import com.magma.dmsdata.util.UpdateStatus;
import com.magma.util.MagmaUtil;
import com.magma.util.Status;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DeviceDTO {

    @NotNull(message = "Please provide device id")
    @NotEmpty(message = "Device id can't be empty")
    private String id;

    @NotNull(message = "Please provide device name")
    @NotEmpty(message = "Device name can't be empty")
    private String name;

    private String[] sensorCodes;

    private Integer noOfSensors;

    private ActuatorCode[] actuatorCodes;

    private Integer noOfActuators;

    private Boolean maintain;

    @NotNull(message = "Interval can't be null or empty")
    private Integer interval;

    @NotNull(message = "Batch Number can't be null or empty")
    private String batchNumber;

    private Integer intervalMin = 10;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String description;

    private Map<Integer, Map<Integer, Shift>> shiftMap = new HashMap<>();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Battery battery;

    @NotNull(message = "Persistence can not be null or empty")
    private Boolean persistence;

    @NotNull(message = "Connectivity can not be null or empty")
    private Connectivity connectivity;

    private Map<Connectivity, Map<String, String>> connectivityMatrix = new HashMap<>();

    private Map<Configuration, String> configurations = new HashMap<>();

    @NotNull(message = "Protocol can not be null or empty")
    private Protocol protocol;

    private Status status;

    private Map<String, String> metaData = null;

    private String simNumber;

    private String productType;

    private String productId;

    private String temperatureUnit;

    private String otaRequestTopic;

    private String otaAckTopic;

    private String remoteConfigTopic;

    private String remoteConfigAckTopic;

    private DeviceParameterConfiguration deviceParameterConfiguration;

    private String magmaCodecId;

    private String referenceId;

    private UpdateStatus lastUpdateStatus;

    private DateTime lastSeen;

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

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getTemperatureUnit() {
        return temperatureUnit;
    }

    public void setTemperatureUnit(String temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getOtaRequestTopic() {
        return otaRequestTopic;
    }

    public void setOtaRequestTopic(String otaRequestTopic) {
        this.otaRequestTopic = otaRequestTopic;
    }

    public String getOtaAckTopic() {
        return otaAckTopic;
    }

    public void setOtaAckTopic(String otaAckTopic) {
        this.otaAckTopic = otaAckTopic;
    }

    public String getRemoteConfigTopic() {
        return remoteConfigTopic;
    }

    public void setRemoteConfigTopic(String remoteConfigTopic) {
        this.remoteConfigTopic = remoteConfigTopic;
    }

    public String getRemoteConfigAckTopic() {
        return remoteConfigAckTopic;
    }

    public void setRemoteConfigAckTopic(String remoteConfigAckTopic) {
        this.remoteConfigAckTopic = remoteConfigAckTopic;
    }

    public DeviceParameterConfiguration getDeviceParameterConfiguration() {
        return deviceParameterConfiguration;
    }

    public void setDeviceParameterConfiguration(DeviceParameterConfiguration deviceParameterConfiguration) {
        this.deviceParameterConfiguration = deviceParameterConfiguration;
    }

    public String getMagmaCodecId() {
        return magmaCodecId;
    }

    public void setMagmaCodecId(String magmaCodecId) {
        this.magmaCodecId = magmaCodecId;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public void setLastUpdateStatus(UpdateStatus lastUpdateStatus){
        this.lastUpdateStatus=lastUpdateStatus;
    }
    public UpdateStatus getLastUpdateStatus(){
        return this.lastUpdateStatus;
    }
    @Override
    public String toString() {
        return "DeviceDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", sensorCodes=" + Arrays.toString(sensorCodes) +
                ", noOfSensors=" + noOfSensors +
                ", actuatorCodes=" + Arrays.toString(actuatorCodes) +
                ", noOfActuators=" + noOfActuators +
                ", maintain=" + maintain +
                ", interval=" + interval +
                ", batchNumber='" + batchNumber + '\'' +
                ", intervalMin=" + intervalMin +
                ", description='" + description + '\'' +
                ", shiftMap=" + shiftMap +
                ", battery=" + battery +
                ", persistence=" + persistence +
                ", connectivity=" + connectivity +
                ", connectivityMatrix=" + connectivityMatrix +
                ", configurations=" + configurations +
                ", protocol=" + protocol +
                ", status=" + status +
                ", metaData=" + metaData +
                ", simNumber='" + simNumber + '\'' +
                ", productType='" + productType + '\'' +
                ", productId='" + productId + '\'' +
                ", temperatureUnit='" + temperatureUnit + '\'' +
                ", otaRequestTopic='" + otaRequestTopic + '\'' +
                ", otaAckTopic='" + otaAckTopic + '\'' +
                ", remoteConfigTopic='" + remoteConfigTopic + '\'' +
                ", remoteConfigAckTopic='" + remoteConfigAckTopic + '\'' +
                ", deviceParameterConfiguration=" + deviceParameterConfiguration +
                ", magmaCodecId='" + magmaCodecId + '\'' +
                ", referenceId='" + referenceId + '\'' +
                '}';
    }

    public DateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(DateTime lastSeen) {
        this.lastSeen = lastSeen;
    }
}
