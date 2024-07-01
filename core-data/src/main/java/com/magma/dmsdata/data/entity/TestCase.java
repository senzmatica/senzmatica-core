package com.magma.dmsdata.data.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.dmsdata.data.support.TestResult;
import com.magma.util.MagmaDateTimeSerializer;
import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
public class TestCase {
    @Id
    private String id;

    private Integer batchNumber;

    private TestResult status;

    private String mainTestTitle;

    private List<SubTestCase> subTestCases = new ArrayList<>();

    private Boolean isDefault;

    private List<String> devices;

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    private DateTime startDate;

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    private DateTime endDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TestCase() {
    }

    public TestCase(Integer batchNumber, TestResult status, String mainTestTitle, Boolean isDefault) {
        this.batchNumber = batchNumber;
        this.status = status;
        this.mainTestTitle = mainTestTitle;
        this.isDefault = isDefault;
    }

    public TestResult getStatus() {
        return status;
    }

    public void setStatus(TestResult status) {
        this.status = status;
    }

    public String getMainTestTitle() {
        return mainTestTitle;
    }

    public void setMainTestTitle(String mainTestTitle) {
        this.mainTestTitle = mainTestTitle;
    }

    public List<SubTestCase> getSubTestCases() {
        return subTestCases;
    }

    public void setSubTestCases(List<SubTestCase> subTestCases) {
        this.subTestCases = subTestCases;
    }

    public Boolean getDefault() {
        return isDefault;
    }

    public void setDefault(Boolean aDefault) {
        this.isDefault = aDefault;
    }

    public void addSubTestCases(SubTestCase subTestCase) {
        this.subTestCases.add(subTestCase);
    }

    public Integer getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(Integer batchNumber) {
        this.batchNumber = batchNumber;
    }

    public List<String> getDevices() {
        return devices;
    }

    public void setDevices(List<String> devices) {
        this.devices = devices;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }
}
