package com.magma.dmsdata.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.util.*;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Arrays;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document
public class Property implements Comparable<Property> {

    @Id
    @JsonIgnore
    private String id;

    private String kitId;

    private Integer number;

    private String code;

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    @JsonDeserialize(using = MagmaDateTimeDeserializer.class)
    private DateTime time;

    private Double value;

    private Double pivot = 0.0;

    @JsonIgnore
    private Double interval;

    private Boolean alert;

    private Boolean error;

    @JsonIgnore
    @CreatedDate
    public DateTime creationDate;

    @JsonIgnore
    @LastModifiedDate
    public DateTime modifiedDate;

    public String storedDataFlag; //1 for stored data ,0 For Real time Data

    public String manualDataFlag; //1 for manual data ,0 for Sensor Data

    private String label;

    public Boolean isEditedFromUser = false;

    public Property() {
    }

    public Property(String kitId, Integer number, String code, DateTime time, Double value, Double pivot) {
        this.kitId = kitId;
        this.number = number;
        this.code = code;
        this.time = time;
        this.value = value;
        this.pivot = pivot;
    }

    public Property(String kitId, Integer number, String code, DateTime time, Double value, Double pivot, String storedDataFlag, String manualDataFlag) {
        this.kitId = kitId;
        this.number = number;
        this.code = code;
        this.time = time;
        this.value = value;
        this.pivot = pivot;
        this.storedDataFlag = storedDataFlag;
        this.manualDataFlag = manualDataFlag;
        this.isEditedFromUser = false;
    }

    public boolean validate() {
        return MagmaUtil.validate(kitId) &&
                number != null &&
                code != null;
    }

    public String getDisplayName() {
        switch (code) {
            case "T":
                return "Temp";
            case "H":
                return "Humidity";
            case "DS":
                return "Door Status";
            case "CS":
                return "Electric Current";
            case "M":
                return "Moisture";
            case "A":
                return "Alarm";
            case "HB":
                return "Online";
            default:
                return code;
        }
    }

    public String getDisplayValue() {
        if (value == -9999.0) {
            return "ERROR";
        }

        switch (code) {
            case "T":
            case "ST":
               /* if (value == -127 || value == 85) {
                    return "ERROR";
                }*/
                return value + " \u2103";
            case "WD":
                return value + " \u2134";
            case "P":
                return value + " hPa";
            case "RF":
                return value + " mm";
            case "WS":
                return value + " km/h";
            case "H":
            case "M":
                return RoundUtil.to2Digit(value) + " %";
            case "IRO":
                return RoundUtil.round(value) + " %";
            case "CN":
                return String.valueOf(RoundUtil.to2Digit(value * 1000)) + " μS/cm";
            case "DS":
                return value == 1 ? "OPEN" : "CLOSED";
            case "CS":
                return value == 1 ? "Presence" : "Power Cut";
            case "N":
                return value == 1 ? "Noisy" : "Normal";
            case "A":
                return value == 1 ? "Triggered" : "OFF";
            case "HB":
                return value == 1 ? "Live" : "Offline";
            default:
                return String.valueOf(value);
        }
    }

    public String getUnit() {
        return code;
    }

    public boolean getShowThreshold() {
        return !Arrays.asList(new String[]{"DS", "CS", "R"}).contains(code);
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

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Double getPivot() {
        return pivot;
    }

    public void setPivot(Double pivot) {
        this.pivot = pivot;
    }

    public Double getInterval() {
        return interval;
    }

    public void setInterval(Double interval) {
        this.interval = interval;
    }

    public Boolean getAlert() {
        return alert;
    }

    public void setAlert(Boolean alert) {
        this.alert = alert;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
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

    public String getStoredDataFlag() {
        return this.storedDataFlag;
    }

    public void setStoredDataFlag(String storedDataFlag) {
        this.storedDataFlag = storedDataFlag;
    }

    public String getManualDataFlag() {
        return manualDataFlag;
    }

    public void setManualDataFlag(String manualDataFlag) {
        this.manualDataFlag = manualDataFlag;
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
        return "Property{" +
                "id='" + id + '\'' +
                ", kitId='" + kitId + '\'' +
                ", number=" + number +
                ", code=" + code +
                ", time=" + time +
                ", value=" + value +
                ", pivot=" + pivot +
                ", interval=" + interval +
                ", alert=" + alert +
                ", error=" + error +
                ", creationDate=" + creationDate +
                ", modifiedDate=" + modifiedDate +
                '}';
    }

    @Override
    public int compareTo(Property o) {
        return this.number.compareTo(o.getNumber());
    }
}
