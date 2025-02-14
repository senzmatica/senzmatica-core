package com.magma.dmsdata.data.entity;

import com.magma.dmsdata.data.support.DeviceParameterConfigurationHistory;
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
    private String payload;

    private DeviceParameterConfigurationHistory updateHistory;

    public DeviceParameterConfigurationHistory getUpdateHistory() {
        return updateHistory;
    }

    public void setUpdateHistory(DeviceParameterConfigurationHistory updateHistory) {
        this.updateHistory = updateHistory;
    }

    public Message() {
    }

    public Message(String device, DeviceParameterConfigurationHistory updateHistory, String topicNumber, String payload) {
        this.device = device;
        this.updateHistory = updateHistory;
        this.topicNumber = topicNumber;
        this.payload = payload;
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

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Message(String device, String topicNumber, String payload) {
        this.device = device;
        this.topicNumber = topicNumber;
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", device='" + device + '\'' +
                ", topicNumber='" + topicNumber + '\'' +
                ", updateHistory=" + updateHistory +
                ", message='" + payload + '\'' +
                '}';
    }


}
