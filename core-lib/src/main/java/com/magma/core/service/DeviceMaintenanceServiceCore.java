package com.magma.core.service;


import com.magma.core.data.entity.Device;
import com.magma.core.data.entity.Offline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DeviceMaintenanceServiceCore extends DeviceMaintenanceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceMaintenanceServiceCore.class);

    @Override
    public void sendOfflineAlerts(List<Device> deviceList) {
        LOGGER.debug("Offline Devices : {}", deviceList);
    }

    @Override
    public void sendHighPriorityOfflineAlerts(List<Device> longOfflineDeviceList) {
        LOGGER.debug("Emergency Alert For Long Offline(MoreThan3-Days) {} Devices sending...", longOfflineDeviceList.size());
    }

    @Override
    public void sendPeriodicOfflineAlerts(List<Offline> deviceList) {
        LOGGER.debug("Periodic Offline Alerts : {}", deviceList);
    }

    @Override
    public void sendMqttFailureAlerts() {
        LOGGER.debug("Mqtt connection failed");
    }

    @Override
    public void sendOfflineDevicesWeekWiseAlert(Map<String, List<Device>> weekWiseOfflineMap) {
        LOGGER.debug("Offline Devices List week wise sending...week-1:{} ,week-2:{} , week-3:{} ,week-4:{}", weekWiseOfflineMap.get("week1").size(),
                weekWiseOfflineMap.get("week2").size(), weekWiseOfflineMap.get("week3").size(), weekWiseOfflineMap.get("week4").size());
    }
}
