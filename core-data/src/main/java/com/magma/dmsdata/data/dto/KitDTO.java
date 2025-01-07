package com.magma.dmsdata.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.dmsdata.data.entity.Battery;
import com.magma.dmsdata.data.entity.Geo;
import com.magma.dmsdata.data.entity.KitModel;
import com.magma.dmsdata.data.entity.Property;
import com.magma.dmsdata.data.support.Offset;
import com.magma.dmsdata.data.support.Shift;
import com.magma.dmsdata.data.support.UserInfo;
import com.magma.dmsdata.util.DataInputMethod;
import com.magma.dmsdata.util.MagmaModelSerializer;
import com.magma.dmsdata.util.SensorCode;
import com.magma.util.MagmaUtil;
import com.magma.util.Status;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KitDTO {
    @NotNull(message = "Please provide kit id")
    @NotEmpty(message = "Kit id can't be empty")
    private String id;

    @NotNull(message = "Please provide kit name")
    @NotEmpty(message = "Kit name can't be empty")
    private String name;

    private String description;

    @Transient
    private String kitModelId;

    @JsonSerialize(using = MagmaModelSerializer.class)
    private KitModel model;

    private List<String> devices;

    private List<SensorCode> sensorSort;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Map<String, Integer> deviceMap = new HashMap<>();

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Map<String, Offset> offsetMap = new HashMap<>();

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Map<Integer, Property> propertyMap = new HashMap<>();

    private Map<Integer, Shift> shiftMap = new HashMap<>();

    private Geo geo;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Battery battery;

    private Boolean persistence;

    private Boolean maintain;

    private Integer alertLevel;

    private Integer interval;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean offline;

    private Status status;

    private Map<String, String> metaData = null;

    private DataInputMethod inputMethod;

    @JsonSerialize(using = MagmaModelSerializer.class)
    private DateTime lastSeen;

    @JsonIgnore
    @CreatedDate
    private DateTime createdDate;

    @JsonSerialize(using = MagmaModelSerializer.class)
    @LastModifiedDate
    private DateTime modifiedDate;

    private UserInfo createdBy;

    private UserInfo modifiedBy;

    public KitDTO() {
    }

    public boolean validate() {
        return MagmaUtil.validate(id) && model != null &&
                devices != null && !devices.isEmpty();
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

    public List<SensorCode> getSensorSort() {
        return sensorSort;
    }

    public void setSensorSort(List<SensorCode> sensorSort) {
        this.sensorSort = sensorSort;
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

    public Boolean getPersistence() {
        return persistence;
    }

    public void setPersistence(Boolean persistence) {
        this.persistence = persistence;
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

    public DateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(DateTime lastSeen) {
        this.lastSeen = lastSeen;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }

    public DateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(DateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
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

    public String getKitModelId() {
        return kitModelId;
    }

    public void setKitModelId(String kitModelId) {
        this.kitModelId = kitModelId;
    }
}
