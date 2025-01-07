package com.magma.dmsdata.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.dmsdata.data.support.Offset;
import com.magma.dmsdata.data.support.Shift;
import com.magma.dmsdata.data.support.UserInfo;
import com.magma.dmsdata.util.DataInputMethod;
import com.magma.dmsdata.util.MagmaModelSerializer;
import com.magma.dmsdata.util.SensorCode;
import com.magma.util.MagmaDateTimeSerializer;
import com.magma.util.MagmaTime;
import com.magma.util.MagmaUtil;
import com.magma.util.Status;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document
public class Kit {

    @Id
    private String id;

    private String name;

    private String description;

    @Transient
    private String kitModelId;

    @JsonSerialize(using = MagmaModelSerializer.class)
    private KitModel model;

    private List<String> devices;

    private List<SensorCode> sensorSort;

    // <DeviceId, Index> TODO: Have to Remove
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Map<String, Integer> deviceMap = new HashMap<>();

    // <DeviceId, Offset>
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Map<String, Offset> offsetMap = new HashMap<>();

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Map<Integer, Property> propertyMap = new HashMap<>();

    private Map<Integer, Shift> shiftMap = new HashMap<>();

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Map<Integer, Action> actionMap = new HashMap<>();

    private Geo geo;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Battery battery;

    private Boolean maintain;

    private Integer alertLevel;

    private Integer interval;

    private Boolean persistence;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean offline;

    private Status status;

    private Map<Integer, Boolean> alerts = new HashMap<>();

    private Map<String, String> metaData = null;

    private DataInputMethod inputMethod;

    public List<Property> getProperties() {
        List<Property> properties = new ArrayList<>(propertyMap.values());
        Collections.sort(properties);
        return properties;
    }

    public List<Action> getActions() {
        return new ArrayList<>(actionMap.values());
    }

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    private DateTime lastSeen;

    @JsonIgnore
    @CreatedDate
    private DateTime creationDate;

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    @LastModifiedDate
    private DateTime modifiedDate;

    private String referenceName;

    private UserInfo createdBy;

    private UserInfo modifiedBy;

    public Kit() {
    }

    public boolean validate() {
        return MagmaUtil.validate(id) && model != null &&
                devices != null && !devices.isEmpty();
    }

    public String getReferenceName() {
        return referenceName;
    }

    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    public boolean validateAdd() {
        return MagmaUtil.validate(id) && MagmaUtil.validate(name);
    }

    public String getKitModelId() {
        return kitModelId;
    }

    public void setKitModelId(String kitModelId) {
        this.kitModelId = kitModelId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public KitModel getModel() {
        return model;
    }

    public void setModel(KitModel model) {
        this.model = model;
    }

    public List<String> getDevices() {
        return devices;
    }

    public void setDevices(List<String> devices) {
        this.devices = devices;
    }

    public Map<String, Integer> getDeviceMap() {
        return deviceMap;
    }

    public void setDeviceMap(Map<String, Integer> deviceMap) {
        this.deviceMap = deviceMap;
    }

    public Map<String, Offset> getOffsetMap() {
        return offsetMap;
    }

    public void setOffsetMap(Map<String, Offset> offsetMap) {
        this.offsetMap = offsetMap;
    }

    public Map<Integer, Property> getPropertyMap() {
        return propertyMap;
    }

    public void setPropertyMap(Map<Integer, Property> propertyMap) {
        this.propertyMap = propertyMap;
    }

    public Map<Integer, Shift> getShiftMap() {
        return shiftMap;
    }

    public void setShiftMap(Map<Integer, Shift> shiftMap) {
        this.shiftMap = shiftMap;
    }

    public Map<Integer, Action> getActionMap() {
        return actionMap;
    }

    public void setActionMap(Map<Integer, Action> actionMap) {
        this.actionMap = actionMap;
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

    public Integer getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(Integer alertLevel) {
        this.alertLevel = alertLevel;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Map<Integer, Boolean> getAlerts() {
        return alerts;
    }

    public void setAlerts(Map<Integer, Boolean> alerts) {
        this.alerts = alerts;
    }

    public DateTime getLastSeen() {
        return lastSeen;
    }

    public String getTimeString() {
        if (lastSeen == null) {
            return "";
        }
        return MagmaTime.formatISO8601(lastSeen);
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

    public Map<String, String> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

    public DataInputMethod getInputMethod() {
        return inputMethod;
    }

    public void setInputMethod(DataInputMethod inputMethod) {
        this.inputMethod = inputMethod;
    }

    public UserInfo getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserInfo createdBy) {
        this.createdBy = createdBy;
    }

    public UserInfo getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(UserInfo modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public List<SensorCode> getSensorSort() {
        return sensorSort;
    }

    public void setSensorSort(List<SensorCode> sensorSort) {
        this.sensorSort = sensorSort;
    }

    @Override
    public String toString() {
        return "Kit{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", kitModelId='" + kitModelId + '\'' +
                ", model=" + model +
                ", devices=" + devices +
                ", sensorSort=" + sensorSort +
                ", deviceMap=" + deviceMap +
                ", offsetMap=" + offsetMap +
                ", propertyMap=" + propertyMap +
                ", shiftMap=" + shiftMap +
                ", actionMap=" + actionMap +
                ", geo=" + geo +
                ", battery=" + battery +
                ", maintain=" + maintain +
                ", alertLevel=" + alertLevel +
                ", interval=" + interval +
                ", persistence=" + persistence +
                ", offline=" + offline +
                ", status=" + status +
                ", alerts=" + alerts +
                ", metaData=" + metaData +
                ", inputMethod=" + inputMethod +
                ", lastSeen=" + lastSeen +
                ", creationDate=" + creationDate +
                ", modifiedDate=" + modifiedDate +
                ", referenceName='" + referenceName + '\'' +
                ", createdBy=" + createdBy +
                ", modifiedBy=" + modifiedBy +
                '}';
    }
}
