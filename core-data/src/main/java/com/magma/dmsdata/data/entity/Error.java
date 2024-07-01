package com.magma.dmsdata.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.util.MagmaDateTimeSerializer;
import com.magma.util.MagmaPeriodSerializer;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document
public class Error {

    @Id
    private String id;

    private String kitId;

    private Integer propertyNumber;

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    private DateTime startTime;

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    private DateTime endTime;

    @JsonIgnore
    @CreatedDate
    private DateTime creationDate;

    @JsonIgnore
    @LastModifiedDate
    private DateTime modifiedDate;

    @JsonSerialize(using = MagmaPeriodSerializer.class)
    public Period getTime() {
        return new Period(startTime, endTime);
    }

    public Error() {
    }

    public Error(String kitId, Integer propertyNumber, DateTime startTime, DateTime endTime) {
        this.propertyNumber = propertyNumber;
        this.kitId = kitId;
        this.startTime = startTime;
        this.endTime = endTime;
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

    public DateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
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
        return "Offline{" +
                "id='" + id + '\'' +
                ", kitId='" + kitId + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", creationDate=" + creationDate +
                ", modifiedDate=" + modifiedDate +
                '}';
    }
}