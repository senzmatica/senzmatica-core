package com.magma.core.data.dto;

import com.magma.core.data.support.DeviceParameterConfigurationHistory;
import com.magma.core.data.support.ProductParameter;
import com.magma.core.util.DeviceCategory;
import com.magma.core.util.ProductType;

import java.util.List;
import java.util.Map;

public class DeviceParameterConfigurationDTO {

    private List<String> deviceIds;
    private ProductType productType;
    private String versionNum;
    private DeviceCategory deviceCategory;
    private List<ProductParameter> remoteConfigurations;
    private Map<String, List<String>> joinParameters;
    private List<DeviceParameterConfigurationHistory> updateHistory;

    public List<String> getDeviceIds() {
        return deviceIds;
    }

    public void setDeviceIds(List<String> deviceIds) {
        this.deviceIds = deviceIds;
    }

    public ProductType getProductType() {
        return productType;
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

    public Map<String, List<String>> getJoinParameters() {
        return joinParameters;
    }

    public void setJoinParameters(Map<String, List<String>> joinParameters) {
        this.joinParameters = joinParameters;
    }

}
