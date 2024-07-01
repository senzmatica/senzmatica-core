package com.magma.core.data.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.core.data.support.GeoType;
import com.magma.util.MagmaDateTimeSerializer;
import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * Created by Nirajh on 6/1/16.
 * SenzMate (Pvt) Ltd.
 **/

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document
public class Geo {

    @Id
    @JsonIgnore
    private String id;

    @JsonIgnore
    private String kitId;

    private GeoType type;

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    private DateTime time;

    private Double lat;

    private Double lng;

    private Map<String, String> relativeLocation = null;

    public Geo() {
    }

    public Geo(String location, String e) {
        String tmp[] = location.split(e);
        lng = Double.parseDouble(tmp[1]);
        lat = Double.parseDouble(tmp[0]);
    }

    public Geo(DateTime time, Double lat, Double lng) {
        this.time = time;
        this.lat = lat;
        this.lng = lng;
    }

    public Map<String, String> getRelativeLocation() {
        return relativeLocation;
    }

    public void setRelativeLocation(Map<String, String> relativeLocation) {
        this.relativeLocation = relativeLocation;
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

    public GeoType getType() {
        return type;
    }

    public void setType(GeoType type) {
        this.type = type;
    }

    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Geo geo = (Geo) o;

        return !(lat != null ? !lat.equals(geo.lat) : geo.lat != null) && !(lng != null ? !lng.equals(geo.lng) : geo.lng != null);

    }

    @Override
    public int hashCode() {
        int result = lat != null ? lat.hashCode() : 0;
        result = 31 * result + (lng != null ? lng.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Geo{" +
                "id='" + id + '\'' +
                ", kitId='" + kitId + '\'' +
                ", type=" + type +
                ", time=" + time +
                ", lat=" + lat +
                ", lng=" + lng +
                ", relativeLocation=" + relativeLocation +
                '}';
    }

    public void setLongitude(double d) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setLongitude'");
    }
}
