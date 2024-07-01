package com.magma.dmsdata.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.dmsdata.data.support.NotificationSeverity;
import com.magma.dmsdata.data.support.NotificationType;
import com.magma.util.MagmaDateTimeSerializer;
import com.magma.util.MagmaDurationSerializer;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document
public class Notification {

    @Id
    @JsonIgnore
    private String id;

    private String plotId;

    private String plotName;

    private String farmId;

    private String farmName;

    private NotificationType type;

    private NotificationSeverity severity;

    private String message;

    private DateTime lastInform;

    private Integer noOfInform;

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    private DateTime startTime;

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    private DateTime endTime;

    private String reference;

    @JsonIgnore
    @CreatedDate
    private DateTime creationDate;

    @JsonIgnore
    @LastModifiedDate
    private DateTime modifiedDate;

    public Notification() {
    }

    public Notification(String plotId, String plotName, String farmId, String farmName, NotificationType type,
                        String message, DateTime startTime, DateTime endTime, String reference) {
        this.plotId = plotId;
        this.plotName = plotName;
        this.farmId = farmId;
        this.farmName = farmName;
        this.type = type;
        this.message = message;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reference = reference;
        this.noOfInform = 0;
    }

    public Notification(String plotId, String plotName, String farmId, String farmName, NotificationType type,
                        NotificationSeverity severity, String message, DateTime startTime, DateTime endTime, String reference) {
        this.plotId = plotId;
        this.plotName = plotName;
        this.farmId = farmId;
        this.farmName = farmName;
        this.type = type;
        this.severity = severity;
        this.message = message;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reference = reference;
        this.noOfInform = 0;
    }

    @JsonSerialize(using = MagmaDurationSerializer.class)
    public Duration getTime() {
        return new Duration(startTime, endTime);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlotId() {
        return plotId;
    }

    public void setPlotId(String plotId) {
        this.plotId = plotId;
    }

    public String getPlotName() {
        return plotName;
    }

    public void setPlotName(String plotName) {
        this.plotName = plotName;
    }

    public String getFarmId() {
        return farmId;
    }

    public void setFarmId(String farmId) {
        this.farmId = farmId;
    }

    public String getFarmName() {
        return farmName;
    }

    public void setFarmName(String farmName) {
        this.farmName = farmName;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public NotificationSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(NotificationSeverity severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DateTime getLastInform() {
        return lastInform;
    }

    public void setLastInform(DateTime lastInform) {
        this.lastInform = lastInform;
    }

    public Integer getNoOfInform() {
        return noOfInform == null ? 0 : noOfInform;
    }

    public void setNoOfInform(Integer noOfInform) {
        this.noOfInform = noOfInform;
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

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
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
        return "Notification{" +
                "id='" + id + '\'' +
                ", plotId='" + plotId + '\'' +
                ", plotName='" + plotName + '\'' +
                ", farmId='" + farmId + '\'' +
                ", farmName='" + farmName + '\'' +
                ", type=" + type +
                ", message='" + message + '\'' +
                ", lastInform=" + lastInform +
                ", noOfInform=" + noOfInform +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", reference='" + reference + '\'' +
                ", creationDate=" + creationDate +
                ", modifiedDate=" + modifiedDate +
                '}';
    }
}