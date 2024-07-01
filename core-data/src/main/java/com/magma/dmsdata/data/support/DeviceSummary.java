package com.magma.dmsdata.data.support;

import java.util.ArrayList;
import java.util.List;

public class DeviceSummary {
    private int noOfDevice;
    private List<String> deviceList = new ArrayList<>();

    public int getNoOfDevice() {
        return noOfDevice;
    }

    public void setNoOfDevice(int noOfDevice) {
        this.noOfDevice = noOfDevice;
    }

    public List<String> getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(List<String> deviceList) {
        this.deviceList = deviceList;
    }

    public void addDevice(String deviceId) {
        this.deviceList.add(deviceId);
    }

    @Override
    public String toString() {
        return "DeviceSummary{" +
                "noOfDevice=" + noOfDevice +
                ", deviceList=" + deviceList +
                '}';
    }
}
