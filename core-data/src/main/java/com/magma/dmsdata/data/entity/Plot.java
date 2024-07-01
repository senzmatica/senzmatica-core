package com.magma.dmsdata.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.magma.dmsdata.data.support.Coordinate;
import com.magma.dmsdata.data.support.PropertyDetail;
import com.magma.util.MagmaUtil;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document
public class Plot {

    @Id
    private String id;
    private String name;
    private List<Coordinate> coordinates;

    private String farmId;
    private String crop;
    private String area;
    private Map<Integer, PropertyDetail> propertyDetails = new HashMap<>();

    private String image;

    private String imageThumbnail;

    // Core variable
    private String kitId;

    private String contactName;

    private String contactPhoneNumber;

    private String contactEmail;

    private String contactIdNo;

    private String location;

    private Coordinate coordinate;

    private DateTime plantedDate;

    @JsonIgnore
    @CreatedDate
    private DateTime creationDate;

    public String getImageThumbnail() {
        return imageThumbnail;
    }

    public void setImageThumbnail(String imageThumbnail) {
        this.imageThumbnail = imageThumbnail;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public String getFarmId() {
        return farmId;
    }

    public void setFarmId(String farmId) {
        this.farmId = farmId;
    }

    public Map<Integer, PropertyDetail> getPropertyDetails() {
        return propertyDetails;
    }

    public void setPropertyDetails(Map<Integer, PropertyDetail> propertyDetails) {
        this.propertyDetails = propertyDetails;
    }

    public String getKitId() {
        return kitId;
    }

    public void setKitId(String kitId) {
        this.kitId = kitId;
    }

    public String getCrop() {
        return crop;
    }

    public void setCrop(String crop) {
        this.crop = crop;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhoneNumber() {
        return contactPhoneNumber;
    }

    public void setContactPhoneNumber(String contactPhoneNumber) {
        this.contactPhoneNumber = contactPhoneNumber;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactIdNo() {
        return contactIdNo;
    }

    public void setContactIdNo(String contactIdNo) {
        this.contactIdNo = contactIdNo;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public DateTime getPlantedDate() {
        return plantedDate;
    }

    public void setPlantedDate(DateTime plantedDate) {
        this.plantedDate = plantedDate;
    }

    public DateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(DateTime creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return "Plot{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", coordinates=" + coordinates +
                ", farmId='" + farmId + '\'' +
                ", crop='" + crop + '\'' +
                ", area=" + area +
                ", propertyDetails=" + propertyDetails +
                ", image='" + image + '\'' +
                ", kitId='" + kitId + '\'' +
                '}';
    }

    public Plot() {
    }

    public boolean validate() {
        return MagmaUtil.validate(farmId)
                && MagmaUtil.validate(name);
    }
}
