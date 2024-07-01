package com.magma.core.data.support;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Message {

    @Id
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String device;
    private String topicNumber;

    private DeviceParameterConfigurationHistory updateHistory;

    public DeviceParameterConfigurationHistory getUpdateHistory() {
        return updateHistory;
    }

    public void setUpdateHistory(DeviceParameterConfigurationHistory updateHistory) {
        this.updateHistory = updateHistory;
    }

    public Message() {
    }

    public Message(String device, DeviceParameterConfigurationHistory updateHistory, String topicNumber, String message) {
        this.device = device;
        this.updateHistory = updateHistory;
        this.topicNumber = topicNumber;
        this.message = message;
        this.id = device + "-" + topicNumber;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getTopicNumber() {
        return topicNumber;
    }

    public void setTopicNumber(String topicNumber) {
        this.topicNumber = topicNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Message(String device, String topicNumber, String message) {
        this.device = device;
        this.topicNumber = topicNumber;
        this.message = message;
    }

    private String message;
}
