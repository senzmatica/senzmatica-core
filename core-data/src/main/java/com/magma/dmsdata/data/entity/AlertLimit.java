package com.magma.dmsdata.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.magma.dmsdata.util.AlertPersistence;
import com.magma.dmsdata.util.AlertStatus;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document
public class AlertLimit {

    @Id
    @JsonIgnore
    private String id;

    private String kitId;

    private Integer propertyNumber;

    private String code;

    private Integer level;

    private Double low;

    private Double high;

    private Integer currentLevelPeriod = null;

    private Integer nextLevelPeriod;

    private AlertPersistence persistence;

    @JsonIgnore
    private AlertStatus status;

    @JsonIgnore
    @CreatedDate
    private DateTime creationDate;

    @JsonIgnore
    @LastModifiedDate
    private DateTime modifiedDate;

    public AlertLimit() {
    }

    public AlertLimit(Integer level, Double low, Double high) {
        this.level = level;
        this.low = low;
        this.high = high;
    }

    public boolean validate() {
        return level != null && low != null && high != null;
    }

    public boolean checkValidity(Double property) {
        return property >= low && property <= high;
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

    public Integer getPropertyNumber() {
        return propertyNumber;
    }

    public void setPropertyNumber(Integer propertyNumber) {
        this.propertyNumber = propertyNumber;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        this.low = low;
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        this.high = high;
    }

    public Integer getCurrentLevelPeriod() {
        return currentLevelPeriod;
    }

    public void setCurrentLevelPeriod(Integer currentLevelPeriod) {
        this.currentLevelPeriod = currentLevelPeriod;
    }

    public Integer getNextLevelPeriod() {
        return nextLevelPeriod;
    }

    public void setNextLevelPeriod(Integer nextLevelPeriod) {
        this.nextLevelPeriod = nextLevelPeriod;
    }

    public AlertPersistence getPersistence() {
        return persistence;
    }

    public void setPersistence(AlertPersistence persistence) {
        this.persistence = persistence;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
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
        return "AlertLimit{" +
                "id='" + id + '\'' +
                ", kitId='" + kitId + '\'' +
                ", propertyNumber=" + propertyNumber +
                ", code=" + code +
                ", level=" + level +
                ", low=" + low +
                ", high=" + high +
                ", currentLevelPeriod=" + currentLevelPeriod +
                ", nextLevelPeriod=" + nextLevelPeriod +
                ", status=" + status +
                ", creationDate=" + creationDate +
                ", modifiedDate=" + modifiedDate +
                '}';
    }
}
