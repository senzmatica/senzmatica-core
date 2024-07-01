package com.magma.core.data.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class PropertyImage {

    @Id
    private String id;

    private String kitId;

    private String imageURL;

    public PropertyImage(String deviceId, String imageURL) {
        this.kitId = deviceId;
        this.imageURL = imageURL;
    }

    public String getDeviceId() {
        return kitId;
    }

    public void setDeviceId(String deviceId) {
        this.kitId = deviceId;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
