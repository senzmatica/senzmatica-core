package com.magma.core.data.support;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.core.util.UpdateStatus;
import com.magma.util.MagmaDateTimeSerializer;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

public class DeviceParameterConfigurationHistory {

    private String actionBy;

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    private DateTime updatedDate;
    private String messageId;
    private UpdateStatus updateStatus;
    private Map< String,String> networkAndCommunication;
    private Map<String, String> topicFormat;
    private Map<String, String> messageFormat;
    private Map<String,DeviceParameterUpdateStatus> updateParamList;

    public Map<String, String> getNetworkAndCommunication() {
        return networkAndCommunication;
    }

    public void setNetworkAndCommunication(Map<String, String> networkAndCommunication) {
        this.networkAndCommunication = networkAndCommunication;
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

    public void setUpdateStatus(UpdateStatus status){
        this.updateStatus=status;
    }
    public UpdateStatus getUpdateStatus(){
        return this.updateStatus;
    }
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId(){
        return this.messageId;
    }
    public void setUpdateParamList(Map<String,DeviceParameterUpdateStatus> updateParamList){
        this.updateParamList=updateParamList;
    }
    public Map<String,DeviceParameterUpdateStatus>  getUpdateParamList(){
        return this.updateParamList;
    }




}
