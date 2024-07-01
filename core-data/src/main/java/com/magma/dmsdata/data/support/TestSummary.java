package com.magma.dmsdata.data.support;

import com.magma.dmsdata.data.entity.TestCard;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestSummary {
    @Id
    private String id;

    private Integer batchNumber;

    // id -> success or failure Of Each TestCases
    private List<Map<String, Map<String, Map<String, ArrayList<TestCard>>>>> deviceTestResult;

    private Map<String, Map<String, Map<TestResult, Integer>>> numericalResult;

    private Map<String, String> testSuccessRate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getBatchNumber() {
        return batchNumber;
    }

    public TestSummary() {
    }

    public void setBatchNumber(Integer batchNumber) {
        this.batchNumber = batchNumber;
    }

    public List<Map<String, Map<String, Map<String, ArrayList<TestCard>>>>> getDeviceTestResult() {
        return deviceTestResult;
    }

    public void setDeviceTestResult(List<Map<String, Map<String, Map<String, ArrayList<TestCard>>>>> deviceTestResult) {
        this.deviceTestResult = deviceTestResult;
    }

    public Map<String, Map<String, Map<TestResult, Integer>>> getNumericalResult() {
        return numericalResult;
    }

    public void setNumericalResult(Map<String, Map<String, Map<TestResult, Integer>>> numericalResult) {
        this.numericalResult = numericalResult;
    }

    public Map<String, String> getTestSuccessRate() {
        return testSuccessRate;
    }

    public void setTestSuccessRate(Map<String, String> testSuccessRate) {
        this.testSuccessRate = testSuccessRate;
    }
}
