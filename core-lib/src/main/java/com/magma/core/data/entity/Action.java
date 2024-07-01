package com.magma.core.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.core.util.ActuatorCode;
import com.magma.util.MagmaDateTimeSerializer;
import com.magma.util.MagmaTime;
import com.magma.util.MagmaUtil;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document
public class Action {

    @Id
    @JsonIgnore
    private String id;

    @JsonIgnore
    private String kitId;

    private Integer number;

    private ActuatorCode code;

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    private DateTime time;

    private Double value;

    @JsonIgnore
    @CreatedDate
    public DateTime creationDate;

    @JsonIgnore
    @LastModifiedDate
    public DateTime modifiedDate;

    public Action() {
    }

    public Action(String kitId, Integer number, ActuatorCode code, DateTime time, Double value) {
        this.kitId = kitId;
        this.number = number;
        this.code = code;
        this.time = time;
        this.value = value;
    }

    public boolean validate() {
        return MagmaUtil.validate(kitId) &&
                number != null &&
                code != null;
    }

    public String getDisplayName() {
        if (code.equals(ActuatorCode.B)) {
            return "Buzzer";
        } else {
            return code.value();
        }
    }

    public String getDisplayValue() {
        switch (code) {
            case S:
                return value == 1 ? "OPEN" : "CLOSED";
            case M:
                return value == 1 ? "ON" : "OFF";
            case B:
                return value == 1 ? "Triggered" : "OFF";
            default:
                return value + "";
        }
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKitId() {
        return kitId;
    }

    public void setKitId(String kitId) {
        this.kitId = kitId;
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

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
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

    public String getTimeString() {
        if (time == null) {
            return "";
        }
        return MagmaTime.formatISO8601(time);
    }

    @Override
    public String toString() {
        return "Action{" +
                "id='" + id + '\'' +
                ", kitId='" + kitId + '\'' +
                ", number=" + number +
                ", code=" + code +
                ", time=" + time +
                ", value=" + value +
                ", creationDate=" + creationDate +
                ", modifiedDate=" + modifiedDate +
                '}';
    }
}