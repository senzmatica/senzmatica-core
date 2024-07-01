package com.magma.dmsdata.data.support;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.util.MagmaDateTimeSerializer;
import org.joda.time.DateTime;

import java.util.Map;

public class DeviceParameterConfigurationHistory {

    private String actionBy;

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    private DateTime updatedDate;

    private Map<String, String> networkAndCommiunication;
    private Map<String, String> topicFormat;
    private Map<String, String> messageFormat;

    public Map<String, String> getNetworkAndCommiunication() {
        return networkAndCommiunication;
    }

    public void setNetworkAndCommiunication(Map<String, String> networkAndCommiunication) {
        this.networkAndCommiunication = networkAndCommiunication;
    }

    public Map<String, String> getTopicFormat() {
        return topicFormat;
    }

    public void setTopicFormat(Map<String, String> topicFormat) {
        this.topicFormat = topicFormat;
    }

    public Map<String, String> getMessageFormat() {
        return messageFormat;
    }

    public void setMessageFormat(Map<String, String> messageFormat) {
        this.messageFormat = messageFormat;
    }

    public String getActionBy() {
        return actionBy;
    }

    public void setActionBy(String actionBy) {
        this.actionBy = actionBy;
    }

    public DateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(DateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
}
