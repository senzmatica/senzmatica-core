package com.magma.dmsdata.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.util.MagmaDateTimeDeserializer;
import com.magma.util.MagmaDateTimeSerializer;
import com.magma.util.MagmaDurationSerializer;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document
public class Alert {

    @Id
    private String id;

    private AlertLimit alertLimit;

    private Boolean dueToPrevious;

    private Boolean alertSent;

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    @JsonDeserialize(using = MagmaDateTimeDeserializer.class)
    private DateTime startTime;

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    @JsonDeserialize(using = MagmaDateTimeDeserializer.class)
    private DateTime endTime;

    @JsonIgnore
    @CreatedDate
    private DateTime creationDate;

    @JsonIgnore
    @LastModifiedDate
    private DateTime modifiedDate;

    @JsonSerialize(using = MagmaDurationSerializer.class)
    public Duration getTime() {
        return new Duration(startTime, endTime);
    }

    // extra attributes to add more details
    private Map<String, Object> meta = new HashMap<>();

    public Alert() {
    }

    public Alert(AlertLimit alertLimit, DateTime startTime, DateTime endTime) {
        this.alertLimit = alertLimit;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    public void addMeta(String key, Object value) {
        this.meta.put(key, value);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AlertLimit getAlertLimit() {
        return alertLimit;
    }

    public void setAlertLimit(AlertLimit alertLimit) {
        this.alertLimit = alertLimit;
    }

    public Boolean getDueToPrevious() {
        return dueToPrevious;
    }

    public void setDueToPrevious(Boolean dueToPrevious) {
        this.dueToPrevious = dueToPrevious;
    }

    public Boolean getAlertSent() {
        return alertSent;
    }

    public void setAlertSent(Boolean alertSent) {
        this.alertSent = alertSent;
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
        return "Alert{" +
                "id='" + id + '\'' +
                ", alertLimit=" + alertLimit +
                ", dueToPrevious=" + dueToPrevious +
                ", alertSent=" + alertSent +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", creationDate=" + creationDate +
                ", modifiedDate=" + modifiedDate +
                ", meta=" + meta +
                '}';
    }
}
