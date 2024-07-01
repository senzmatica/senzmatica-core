package com.magma.dmsdata.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.dmsdata.util.ActuatorCode;
import com.magma.util.MagmaDateTimeSerializer;
import com.magma.util.MagmaUtil;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document
public class Actuator {

    @Id
    private String id;

    private String deviceId;

    private Integer number;

    private ActuatorCode code;

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    private DateTime time;

    private String value;

    @JsonIgnore
    @CreatedDate
    public DateTime creationDate;

    @JsonIgnore
    @LastModifiedDate
    public DateTime modifiedDate;

    public Actuator() {
    }

    public Actuator(String deviceId, Integer number, ActuatorCode code, DateTime time, String value) {
        this.deviceId = deviceId;
        this.number = number;
        this.code = code;
        this.time = time;
        this.value = value;
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

    public ActuatorCode getCode() {
        return code;
    }

    public void setCode(ActuatorCode code) {
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
        return "Sensor{" +
                "id='" + id + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", number=" + number +
                ", code=" + code +
                ", time=" + time +
                ", value=" + value +
                ", creationDate=" + creationDate +
                ", modifiedDate=" + modifiedDate +
                '}';
    }
}