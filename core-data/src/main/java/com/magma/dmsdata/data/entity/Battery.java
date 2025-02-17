package com.magma.dmsdata.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.dmsdata.util.MagmaPropertyReadingSerializer;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document
public class Battery {

    private Double low;

    private Double high;

    @JsonSerialize(using = MagmaPropertyReadingSerializer.class)
    private Property reading;

    @JsonSerialize(using = MagmaPropertyReadingSerializer.class)
    private Property readingPercentage;

    @JsonIgnore
    @CreatedDate
    public DateTime creationDate;

    @JsonIgnore
    @LastModifiedDate
    public DateTime modifiedDate;

    public Battery() {
    }

    public Battery(Double low, Double high) {
        this.low = low;
        this.high = high;
    }

    public Property getReadingPercentage() {
        return readingPercentage;
    }

    public void setReadingPercentage(Property readingPercentage) {
        this.readingPercentage = readingPercentage;
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

    public Property getReading() {
        return reading;
    }

    public void setReading(Property reading) {
        this.reading = reading;
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
        return "Battery{" +
                "low=" + low +
                ", high=" + high +
                ", reading=" + reading +
                ", creationDate=" + creationDate +
                ", modifiedDate=" + modifiedDate +
                '}';
    }
}
