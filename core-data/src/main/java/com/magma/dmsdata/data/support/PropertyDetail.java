package com.magma.dmsdata.data.support;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PropertyDetail {

    private String type;
    private String note;
    private String cluster;
    private String clusterName;
    private Coordinate coordinate;
    private String code;

    public PropertyDetail() {
    }

    public PropertyDetail(String type, String note, Coordinate coordinate, String code) {
        this.type = type;
        this.note = note;
        this.coordinate = coordinate;
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @Override
    public String toString() {
        return "PropertyDetail{" +
                "type='" + type + '\'' +
                ", note='" + note + '\'' +
                ", coordinate=" + coordinate +
                ", code=" + code +
                '}';
    }
}
