package com.magma.dmsdata.data.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.dmsdata.data.support.TestResult;
import com.magma.util.MagmaDateTimeSerializer;
import org.joda.time.DateTime;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class TestCard {

    private TestResult testResult;
    private String deviceId;
    private Integer batchNumber;

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    private DateTime subTestCaseStartTime;
    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    private DateTime subTestCaseEndTime;

    private String mainTestTile;
    private String subTestTitle;

    public TestCard() {
    }

    public TestCard(TestResult testResult, String deviceId, Integer batchNumber, DateTime subTestCaseStartTime,
                    DateTime subTestCaseEndTime, String mainTestTile, String subTestTitle) {
        this.testResult = testResult;
        this.deviceId = deviceId;
        this.batchNumber = batchNumber;
        this.subTestCaseStartTime = subTestCaseStartTime;
        this.subTestCaseEndTime = subTestCaseEndTime;
        this.mainTestTile = mainTestTile;
        this.subTestTitle = subTestTitle;
    }

    public TestResult getTestResult() {
        return testResult;
    }

    public void setTestResult(TestResult testResult) {
        this.testResult = testResult;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(Integer batchNumber) {
        this.batchNumber = batchNumber;
    }

    public DateTime getSubTestCaseStartTime() {
        return subTestCaseStartTime;
    }

    public void setSubTestCaseStartTime(DateTime subTestCaseStartTime) {
        this.subTestCaseStartTime = subTestCaseStartTime;
    }

    public DateTime getSubTestCaseEndTime() {
        return subTestCaseEndTime;
    }

    public void setSubTestCaseEndTime(DateTime subTestCaseEndTime) {
        this.subTestCaseEndTime = subTestCaseEndTime;
    }

    public String getMainTestTile() {
        return mainTestTile;
    }

    public void setMainTestTile(String mainTestTile) {
        this.mainTestTile = mainTestTile;
    }

    public String getSubTestTitle() {
        return subTestTitle;
    }

    public void setSubTestTitle(String subTestTitle) {
        this.subTestTitle = subTestTitle;
    }
}
