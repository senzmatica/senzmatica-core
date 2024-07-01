package com.magma.core.data.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.core.data.support.TestResult;
import com.magma.util.MagmaDateTimeSerializer;
import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Document
public class SubTestCase {

    @Id
    private String id;

    private String minVal;

    private String maxVal;

    private String valueType;

    private String subTestTitle;

    private Boolean isDefault;

    private Integer batchNumber;

    private TestResult status;

    private List<String> devices = new ArrayList<>();
    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    private DateTime startTime = new DateTime();
    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    private DateTime endTime = new DateTime();

    private Map<String, Map<String, DateTime>> subTestCaseTimeBetween = new HashMap<>();


    public SubTestCase() {
    }


    public SubTestCase(Integer batchNumber, TestResult status, String subTestTitle, Boolean isDefault, String minVal, String maxVal, String valueType, List<String> devices, DateTime startTime, DateTime endTime, Map<String, Map<String, DateTime>> subTestCaseTimeBetween) {
        this.minVal = minVal;
        this.maxVal = maxVal;
        this.valueType = valueType;
        this.subTestTitle = subTestTitle;
        this.isDefault = isDefault;
        this.batchNumber = batchNumber;
        this.status = status;
        this.devices = devices;
        this.startTime = startTime;
        this.endTime = endTime;
        this.subTestCaseTimeBetween = subTestCaseTimeBetween;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(Integer batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getMinVal() {
        return minVal;
    }

    public void setMinVal(String minVal) {
        this.minVal = minVal;
    }

    public String getMaxVal() {
        return maxVal;
    }

    public void setMaxVal(String maxVal) {
        this.maxVal = maxVal;
    }

    public String getSubTestTitle() {
        return subTestTitle;
    }

    public void setSubTestTitle(String subTestTitle) {
        this.subTestTitle = subTestTitle;
    }

    public Boolean getDefault() {
        return isDefault;
    }

    public void setDefault(Boolean aDefault) {
        this.isDefault = aDefault;
    }

    public TestResult getStatus() {
        return status;
    }

    public void setStatus(TestResult status) {
        this.status = status;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public List<String> getDevices() {
        return devices;
    }

    public void setDevices(List<String> devices) {
        this.devices = devices;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }


    public Map<String, Map<String, DateTime>> getSubTestCaseTimeBetween() {
        return subTestCaseTimeBetween;
    }

    public void setSubTestCaseTimeBetween(Map<String, Map<String, DateTime>> subTestCaseTimeBetween) {
        this.subTestCaseTimeBetween = subTestCaseTimeBetween;
    }
}
