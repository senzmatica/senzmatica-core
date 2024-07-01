package com.magma.core.data.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.core.data.support.TestResult;
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

    private String mainTestTitle;
    private String subTestTitle;
    private boolean check;

    public TestCard() {
    }

    public TestCard(TestResult testResult, String deviceId, Integer batchNumber, DateTime subTestCaseStartTime, DateTime subTestCaseEndTime, String mainTestTitle, String subTestTitle) {
        this.testResult = testResult;
        this.deviceId = deviceId;
        this.batchNumber = batchNumber;
        this.subTestCaseStartTime = subTestCaseStartTime;
        this.subTestCaseEndTime = subTestCaseEndTime;
        this.mainTestTitle = mainTestTitle;
        this.subTestTitle = subTestTitle;
    }

    public TestCard(TestResult testResult, String deviceId, Integer batchNumber, DateTime subTestCaseStartTime, DateTime subTestCaseEndTime, String mainTestTitle, String subTestTitle, boolean check) {
        this.testResult = testResult;
        this.deviceId = deviceId;
        this.batchNumber = batchNumber;
        this.subTestCaseStartTime = subTestCaseStartTime;
        this.subTestCaseEndTime = subTestCaseEndTime;
        this.mainTestTitle = mainTestTitle;
        this.subTestTitle = subTestTitle;
        this.check = check;
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

    public String getMainTestTitle() {
        return mainTestTitle;
    }

    public void setMainTestTitle(String mainTestTitle) {
        this.mainTestTitle = mainTestTitle;
    }

    public String getSubTestTitle() {
        return subTestTitle;
    }

    public void setSubTestTitle(String subTestTitle) {
        this.subTestTitle = subTestTitle;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }
}
