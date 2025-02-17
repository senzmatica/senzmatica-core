package com.magma.dmsdata.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.dmsdata.data.support.Shift;
import com.magma.util.MagmaDateTimeSerializer;
import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;
import java.util.Map;


public class SensorDTO {

    @NotNull(message = "Please provide device id")
    private String id;

    private String deviceId;

    private Integer number;

    private String code;

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    private DateTime time;

    private String value;

    private String label;

    @JsonIgnore
    private Map<Integer, Shift> shiftMap;

    public String flag;

    public Boolean isEditedFromUser;

    public SensorDTO() {
    }

    public SensorDTO(String id, String deviceId, Integer number, String code, DateTime time, String value, String label, Map<Integer, Shift> shiftMap, DateTime creationDate, DateTime modifiedDate, String flag, Boolean isEditedFromUser) {
        this.id = id;
        this.deviceId = deviceId;
        this.number = number;
        this.code = code;
        this.time = time;
        this.value = value;
        this.label = label;
        this.shiftMap = shiftMap;
        this.flag = flag;
        this.isEditedFromUser = isEditedFromUser;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<Integer, Shift> getShiftMap() {
        return shiftMap;
    }

    public void setShiftMap(Map<Integer, Shift> shiftMap) {
        this.shiftMap = shiftMap;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public Boolean getEditedFromUser() {
        return isEditedFromUser;
    }

    public void setEditedFromUser(Boolean editedFromUser) {
        isEditedFromUser = editedFromUser;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "SensorDTO{" +
                "id='" + id + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", number=" + number +
                ", code=" + code +
                ", time=" + time +
                ", value='" + value + '\'' +
                ", label='" + label + '\'' +
                ", shiftMap=" + shiftMap +
                ", flag='" + flag + '\'' +
                ", isEditedFromUser=" + isEditedFromUser +
                '}';
    }
}
