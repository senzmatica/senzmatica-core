package com.magma.core.data.support;

import com.magma.core.data.entity.TestCase;

import java.util.List;

public class DeviceConfiguration {


    private List<String> devices;

    private List<String> testConditions;

    private List<TestCase> testConfigurations;

    private String startDate;

    private String endDate;

    public DeviceConfiguration(List<String> devices) {
        this.devices = devices;
    }

    public DeviceConfiguration() {
    }

    public List<String> getDevices() {
        return devices;
    }

    public void setDevices(List<String> devices) {
        this.devices = devices;
    }

    public List<TestCase> getTestConfigurations() {
        return testConfigurations;
    }

    public void setTestConfigurations(List<TestCase> testConfigurations) {
        this.testConfigurations = testConfigurations;
    }

    public List<String> getTestConditions() {
        return testConditions;
    }

    public void setTestConditions(List<String> testConditions) {
        this.testConditions = testConditions;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
