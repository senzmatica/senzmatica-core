package com.magma.dmsdata.data.support;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.dmsdata.data.entity.Device;
import com.magma.util.MagmaDateTimeSerializer;
import org.joda.time.DateTime;

import java.util.List;

public class CorporateDeviceSummary {
    private String summaryType;

    private String corporateId;

    private int allDevicesCount;

    private int activeDeviceCount;

    private int newDeviceCount;

    private int newActiveDeviceCount;

    private int newNonActiveDeviceCount;

    private int oldDeviceCount;

    private int oldActiveDeviceCount;

    private int oldNonActiveDeviceCount;

    private int offlineDeviceCount;

    private int onlineDeviceCount;

    private int batteryDeadDeviceCount;

    private int directPowerDeviceCount = 10;

    private int onBatteryDeviceCount = 10;

    private int onSolarPowerDeviceCount = 10;

    private List<Device> allDevices;

    private List<Device> activeDevices;

    private List<Device> newDevices;

    private List<Device> newActiveDevices;

    private List<Device> oldDevices;

    private List<Device> oldActiveDevices;

    private List<Device> offlineDevices;

    private List<Device> onlineDevices;

    private List<Device> batteryDeadDevices;

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    private DateTime summaryDate;

    public CorporateDeviceSummary() {
        this.summaryDate = new DateTime();
    }

    public String getSummaryType() {
        return summaryType;
    }

    public void setSummaryType(String summaryType) {
        this.summaryType = summaryType;
    }

    public List<Device> getActiveDevices() {
        return activeDevices;
    }

    public void setActiveDevices(List<Device> activeDevices) {
        this.activeDevices = activeDevices;
        setActiveDeviceCount(activeDevices.size());
    }

    public List<Device> getNewDevices() {
        return newDevices;
    }

    public void setNewDevices(List<Device> newDevices) {
        this.newDevices = newDevices;
        setNewDeviceCount(newDevices.size());
    }

    public List<Device> getOldDevices() {
        return oldDevices;
    }

    public void setOldDevices(List<Device> oldDevices) {
        this.oldDevices = oldDevices;
        setOldDeviceCount(oldDevices.size());
    }

    public int getActiveDeviceCount() {
        return activeDeviceCount;
    }

    public void setActiveDeviceCount(int activeDeviceCount) {
        this.activeDeviceCount = activeDeviceCount;
    }

    public int getNewDeviceCount() {
        return newDeviceCount;
    }

    public void setNewDeviceCount(int newDeviceCount) {
        this.newDeviceCount = newDeviceCount;
    }

    public int getOldDeviceCount() {
        return oldDeviceCount;
    }

    public void setOldDeviceCount(int oldDeviceCount) {
        this.oldDeviceCount = oldDeviceCount;
    }

    public DateTime getSummaryDate() {
        return summaryDate;
    }

    public int getAllDevicesCount() {
        return allDevicesCount;
    }

    public void setAllDevicesCount(int allDevicesCount) {
        this.allDevicesCount = allDevicesCount;
    }

    public int getNewActiveDeviceCount() {
        return newActiveDeviceCount;
    }

    public void setNewActiveDeviceCount(int newActiveDeviceCount) {
        this.newActiveDeviceCount = newActiveDeviceCount;
    }

    public int getOldActiveDeviceCount() {
        return oldActiveDeviceCount;
    }

    public void setOldActiveDeviceCount(int oldActiveDeviceCount) {
        this.oldActiveDeviceCount = oldActiveDeviceCount;
    }

    public List<Device> getAllDevices() {
        return allDevices;
    }

    public void setAllDevices(List<Device> allDevices) {
        this.allDevices = allDevices;
        setAllDevicesCount(allDevices.size());
    }

    public List<Device> getNewActiveDevices() {
        return newActiveDevices;
    }

    public void setNewActiveDevices(List<Device> newActiveDevices) {
        setNewActiveDeviceCount(newActiveDevices.size());
        setNewNonActiveDeviceCount(this.newDeviceCount - newActiveDevices.size());
        this.newActiveDevices = newActiveDevices;
    }

    public List<Device> getOldActiveDevices() {
        return oldActiveDevices;
    }

    public void setOldActiveDevices(List<Device> oldActiveDevices) {
        setOldActiveDeviceCount(oldActiveDevices.size());
        setOldNonActiveDeviceCount(this.oldDeviceCount - oldActiveDevices.size());
        this.oldActiveDevices = oldActiveDevices;
    }

    public int getNewNonActiveDeviceCount() {
        return newNonActiveDeviceCount;
    }

    public void setNewNonActiveDeviceCount(int newNonActiveDeviceCount) {
        this.newNonActiveDeviceCount = newNonActiveDeviceCount;
    }

    public int getOldNonActiveDeviceCount() {
        return oldNonActiveDeviceCount;
    }

    public void setOldNonActiveDeviceCount(int oldNonActiveDeviceCount) {
        this.oldNonActiveDeviceCount = oldNonActiveDeviceCount;
    }

    public String getCorporateId() {
        return corporateId;
    }

    public void setCorporateId(String corporateId) {
        this.corporateId = corporateId;
    }

    public int getOfflineDeviceCount() {
        return offlineDeviceCount;
    }

    public void setOfflineDeviceCount(int offlineDeviceCount) {
        this.offlineDeviceCount = offlineDeviceCount;
    }

    public int getOnlineDeviceCount() {
        return onlineDeviceCount;
    }

    public void setOnlineDeviceCount(int onlineDeviceCount) {
        this.onlineDeviceCount = onlineDeviceCount;
    }

    public List<Device> getOfflineDevices() {
        return offlineDevices;
    }

    public void setOfflineDevices(List<Device> offlineDevices) {
        setOfflineDeviceCount(offlineDevices.size());
        this.offlineDevices = offlineDevices;
    }

    public List<Device> getOnlineDevices() {
        return onlineDevices;
    }

    public void setOnlineDevices(List<Device> onlineDevices) {
        setOnlineDeviceCount(onlineDevices.size());
        this.onlineDevices = onlineDevices;
    }

    public int getBatteryDeadDeviceCount() {
        return batteryDeadDeviceCount;
    }

    public void setBatteryDeadDeviceCount(int batteryDeadDeviceCount) {
        this.batteryDeadDeviceCount = batteryDeadDeviceCount;
    }

    public List<Device> getBatteryDeadDevices() {
        return batteryDeadDevices;
    }

    public void setBatteryDeadDevices(List<Device> batteryDeadDevices) {
        setBatteryDeadDeviceCount(batteryDeadDevices.size());
        this.batteryDeadDevices = batteryDeadDevices;
    }

    public int getDirectPowerDeviceCount() {
        return directPowerDeviceCount;
    }

    public void setDirectPowerDeviceCount(int directPowerDeviceCount) {
        this.directPowerDeviceCount = directPowerDeviceCount;
    }

    public int getOnBatteryDeviceCount() {
        return onBatteryDeviceCount;
    }

    public void setOnBatteryDeviceCount(int onBatteryDeviceCount) {
        this.onBatteryDeviceCount = onBatteryDeviceCount;
    }

    public int getOnSolarPowerDeviceCount() {
        return onSolarPowerDeviceCount;
    }

    public void setOnSolarPowerDeviceCount(int onSolarPowerDeviceCount) {
        this.onSolarPowerDeviceCount = onSolarPowerDeviceCount;
    }

    public void setSummaryDate(DateTime summaryDate) {
        this.summaryDate = summaryDate;
    }
}
