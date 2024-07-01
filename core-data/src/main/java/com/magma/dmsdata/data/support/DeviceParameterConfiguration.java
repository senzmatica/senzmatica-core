package com.magma.dmsdata.data.support;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.dmsdata.util.DeviceCategory;
import com.magma.dmsdata.util.ProductType;
import com.magma.util.MagmaDateTimeSerializer;
import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;

import java.util.List;

public class DeviceParameterConfiguration {

    private List<String> deviceIds;
    @Id
    private String device;
    private ProductType productType;
    private String versionNum;
    private DeviceCategory deviceCategory;
    private List<ProductParameter> remoteConfigurations;

    private List<DeviceParameterConfigurationHistory> updateHistory;

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    private DateTime createdDate;

    public List<String> getDeviceIds() {
        return deviceIds;
    }

    public void setDeviceIds(List<String> deviceIds) {
        this.deviceIds = deviceIds;
    }

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

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getServerIpAddress() {
        return serverIpAddress;
    }

    public void setServerIpAddress(String serverIpAddress) {
        this.serverIpAddress = serverIpAddress;
    }

    private String serverIpAddress;
}
