package com.magma.core.data.support;

import com.magma.core.util.DeviceCategory;
import com.magma.core.util.ProductType;
import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.Map;

public class DeviceParameterConfiguration {

    @Id
    private String device;
    private ProductType productType;
    private String versionNum;
    private DeviceCategory deviceCategory;
    private List<ProductParameter> remoteConfigurations;

    private Map<String, List<String>> joinParameters;

    private List<DeviceParameterConfigurationHistory> updateHistory;
    private String serverIpAddress;

    public ProductType getProductType() {
        return productType;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public String getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(String versionNum) {
        this.versionNum = versionNum;
    }

    public DeviceCategory getDeviceCategory() {
        return deviceCategory;
    }

    public void setDeviceCategory(DeviceCategory deviceCategory) {
        this.deviceCategory = deviceCategory;
    }

    public List<ProductParameter> getRemoteConfigurations() {
        return remoteConfigurations;
    }

    public void setRemoteConfigurations(List<ProductParameter> remoteConfigurations) {
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
