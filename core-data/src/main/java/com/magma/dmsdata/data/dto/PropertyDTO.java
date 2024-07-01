package com.magma.dmsdata.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class PropertyDTO {
    private String id;

    private Integer number;

    private String code;

    private Double value;

    @JsonIgnore
    private Double interval;

    private Boolean alert;

    private Boolean error;

    private String imageURL;

    private String label;

    public PropertyDTO() {
    }

    public PropertyDTO(String kitId, Integer number, String code, Double value, Double interval, Boolean alert, Boolean error) {
        this.number = number;
        this.code = code;
        this.value = value;
        this.interval = interval;
        this.alert = alert;
        this.error = error;
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

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
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

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
