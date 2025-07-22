package com.magma.core.service;

import com.magma.core.data.entity.Device;
import com.magma.core.data.entity.Offline;

import java.util.List;
import java.util.Map;

public abstract class DeviceMaintenanceService {
    public abstract void sendOfflineAlerts(List<Device> deviceList);

    public abstract void sendHighPriorityOfflineAlerts(List<Device> longOfflineDeviceList);

    public abstract void sendPeriodicOfflineAlerts(List<Offline> deviceList);

    public abstract void sendMqttFailureAlerts();

    public abstract void sendOfflineDevicesWeekWiseAlert(Map<String, List<Device>> weekWiseOfflineMap);
}
