package com.magma.core.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.core.data.support.Shift;
import com.magma.util.MagmaDateTimeDeserializer;
import com.magma.util.MagmaDateTimeSerializer;
import com.magma.util.MagmaTime;
import com.magma.util.MagmaUtil;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document
public class Sensor {

    @Id
    private String id;

    private String deviceId;

    private Integer number;

    private String code;

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    @JsonDeserialize(using = MagmaDateTimeDeserializer.class)
    private DateTime time;

    private String value;

    private String label;

    @JsonIgnore
    private Map<Integer, Shift> shiftMap;

    @JsonIgnore
    @CreatedDate
    public DateTime creationDate;

    @JsonIgnore
    @LastModifiedDate
    public DateTime modifiedDate;

    public String flag;         //0- real time

    public Boolean isEditedFromUser;

    public Sensor() {
    }

    public Sensor(String deviceId, Integer number, String code, DateTime time, String value) {
        this.deviceId = deviceId;
        this.number = number;
        this.code = code;
        this.time = time;
        this.value = value;
    }

    public Sensor(String deviceId, Integer number, String code, DateTime time, String value, Map<Integer, Shift> shiftMap) {
        this.deviceId = deviceId;
        this.number = number;
        this.code = code;
        this.time = time;
        this.value = value;
        this.shiftMap = shiftMap;
    }

    public Sensor(String deviceId, Integer number, String code, DateTime time, String value, String flag) {
        this.deviceId = deviceId;
        this.number = number;
        this.code = code;
        this.time = time;
        this.value = value;
        this.flag = flag;
    }

    public Sensor(String deviceId, Integer number, String code, DateTime time, String value, Map<Integer, Shift> shiftMap, String flag) {
        this.deviceId = deviceId;
        this.number = number;
        this.code = code;
        this.time = time;
        this.value = value;
        this.shiftMap = shiftMap;
        this.flag = flag;
        this.isEditedFromUser = false;
    }

    public boolean validate() {
        return MagmaUtil.validate(deviceId) &&
                number != null &&
                code != null;
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

    public Map<Integer, Shift> getShiftMap() {
        return shiftMap;
    }

    public void setShiftMap(Map<Integer, Shift> shiftMap) {
        this.shiftMap = shiftMap;
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

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getFlag() {
        return this.flag;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTimeString() {
        if (time == null) {
            return "";
        }
        return MagmaTime.formatISO8601(time);
    }

    public Boolean getEditedFromUser() {
        return isEditedFromUser;
    }

    public void setEditedFromUser(Boolean editedFromUser) {
        isEditedFromUser = editedFromUser;
    }

    @Override
    public String toString() {
        return "Sensor{" +
                "id='" + id + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", number=" + number +
                ", code=" + code +
                ", time=" + time +
                ", value=" + value +
                ", label='" + label +
                ", creationDate=" + creationDate +
                ", modifiedDate=" + modifiedDate +
                '}';
    }
}
