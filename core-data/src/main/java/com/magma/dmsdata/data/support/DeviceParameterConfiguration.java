package com.magma.dmsdata.data.support;

import java.util.List;
import java.util.Map;

public class DeviceParameterConfiguration {

    private String device;
    private String productType;
    private String versionNum;
    private List<RemoteConfigField> remoteConfigurations;

    private Map<String, List<String>> joinParameters;

    private List<DeviceParameterConfigurationHistory> updateHistory;
    private String serverIpAddress;

    public String getProductType() {
        return productType;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(String versionNum) {
        this.versionNum = versionNum;
    }

    public List<RemoteConfigField> getRemoteConfigurations() {
        return remoteConfigurations;
    }

    public void setRemoteConfigurations(List<RemoteConfigField> remoteConfigurations) {
        this.remoteConfigurations = remoteConfigurations;
    }

    public List<DeviceParameterConfigurationHistory> getUpdateHistory() {
        return updateHistory;
    }

    public void setUpdateHistory(List<DeviceParameterConfigurationHistory> updateHistory) {
        this.updateHistory = updateHistory;
    }

    public String getServerIpAddress() {
        return serverIpAddress;
    }

    public void setServerIpAddress(String serverIpAddress) {
        this.serverIpAddress = serverIpAddress;
    }

    public Map<String, List<String>> getJoinParameters() {
        return joinParameters;
    }

    public void setJoinParameters(Map<String, List<String>> joinParameters) {
        this.joinParameters = joinParameters;
    }

}
