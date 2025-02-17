package com.magma.dmsdata.data.support;

import com.magma.dmsdata.util.ProductStatus;

import java.util.List;
import java.util.Map;


public class ProductVersion {

    private String versionNum;

    private ProductStatus status;

    private String statusChangedBy;

    private String binURL;

    private List<RemoteConfigField> remoteConfigurations;

    private String flowChartURL;

    private List<String> devices;

    private Map<String, TestResult> deviceTestResults;

    private Map<String, List<String>> joinParameters;

    private String fileName;

    private Boolean majorVersionUpgrade;

    private  String flowChartFileName;

    public ProductVersion() {
    }

    public ProductVersion(String versionNum) {
        this.versionNum = versionNum;
    }

    public String getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(String versionNum) {
        this.versionNum = versionNum;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public String getBinURL() {
        return binURL;
    }

    public void setBinURL(String binURL) {
        this.binURL = binURL;
    }

    public List<RemoteConfigField> getRemoteConfigurations() {
        return remoteConfigurations;
    }

    public void setRemoteConfigurations(List<RemoteConfigField> remoteConfigurations) {
        this.remoteConfigurations = remoteConfigurations;
    }

    public String getFlowChartURL() {
        return flowChartURL;
    }

    public void setFlowChartURL(String flowChartURL) {
        this.flowChartURL = flowChartURL;
    }

    public List<String> getDevices() {
        return devices;
    }

    public void setDevices(List<String> devices) {
        this.devices = devices;
    }

    public Map<String, TestResult> getDeviceTestResults() {
        return deviceTestResults;
    }

    public void setDeviceTestResults(Map<String, TestResult> deviceTestResults) {
        this.deviceTestResults = deviceTestResults;
    }

    public String getStatusChangedBy() {
        return statusChangedBy;
    }

    public void setStatusChangedBy(String statusChangedBy) {
        this.statusChangedBy = statusChangedBy;
    }

    public Map<String, List<String>> getJoinParameters() {
        return joinParameters;
    }

    public void setJoinParameters(Map<String, List<String>> joinParameters) {
        this.joinParameters = joinParameters;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Boolean getMajorVersionUpgrade() {
        return majorVersionUpgrade;
    }

    public void setMajorVersionUpgrade(Boolean majorVersionUpgrade) {
        this.majorVersionUpgrade = majorVersionUpgrade;
    }

    public String getFlowChartFileName() {
        return flowChartFileName;
    }

    public void setFlowChartFileName(String flowChartFileName) {
        this.flowChartFileName = flowChartFileName;
    }
}
