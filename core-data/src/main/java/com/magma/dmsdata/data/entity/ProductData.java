package com.magma.dmsdata.data.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.magma.dmsdata.data.support.OTAUpgradeHistory;

import java.util.List;

@Document
public class ProductData {
    @Id
    private String deviceId;
    private String productId;
    private String productType;
    private String currentProductVersion;
    private String previousVersion;
    private Boolean majorVersionUpgrade;
    private String allProductVersionsOfDevice;
    private String availableProductVersions;
    private String currentVersionStatus;
    private String actionBy; // Optional field
    private String date; // Optional field
    private List<OTAUpgradeHistory> otaHistory;

    public ProductData() {
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getCurrentProductVersion() {
        return currentProductVersion;
    }

    public void setCurrentProductVersion(String currentProductVersion) {
        this.currentProductVersion = currentProductVersion;
    }

    public String getAllProductVersionsOfDevice() {
        return allProductVersionsOfDevice;
    }

    public void setAllProductVersionsOfDevice(String allProductVersionsOfDevice) {
        this.allProductVersionsOfDevice = allProductVersionsOfDevice;
    }

    public String getAvailableProductVersions() {
        return availableProductVersions;
    }

    public void setAvailableProductVersions(String availableProductVersions) {
        this.availableProductVersions = availableProductVersions;
    }

    public String getCurrentVersionStatus() {
        return currentVersionStatus;
    }

    public void setCurrentVersionStatus(String currentVersionStatus) {
        this.currentVersionStatus = currentVersionStatus;
    }

    public String getActionBy() {
        return actionBy;
    }

    public void setActionBy(String actionBy) {
        this.actionBy = actionBy;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPreviousVersion() {
        return previousVersion;
    }

    public void setPreviousVersion(String previousVersion) {
        this.previousVersion = previousVersion;
    }

    public void setMajorVersionUpgrade(Boolean majorVersionUpgrade) {
        this.majorVersionUpgrade = majorVersionUpgrade;
    }

    public Boolean getMajorVersionUpgrade() {
        return majorVersionUpgrade;
    }

    public List<OTAUpgradeHistory> getOtaHistory() {
        return otaHistory;
    }

    public void setOtaHistory(List<OTAUpgradeHistory> otaHistory) {
        this.otaHistory = otaHistory;
    }
}
