package com.magma.core.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magma.core.configuration.MQTTConfiguration;
import com.magma.dmsdata.data.entity.Device;
import com.magma.dmsdata.data.entity.Offline;
import com.magma.dmsdata.data.entity.Sensor;
import com.magma.core.data.repository.*;
import com.magma.dmsdata.data.support.DeviceSummary;
import com.magma.core.service.DeviceMaintenanceService;
import com.magma.core.service.KitCoreService;
import com.magma.util.MagmaTime;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CoreSchedule {

    @Autowired
    private OfflineRepository offlineRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    SensorFailureValueRepository sensorFailureValueRepository;

    @Autowired
    SensorRepository sensorRepository;

    @Autowired
    MQTTConfiguration.MqttGateway mqttGateway;

    @Autowired
    DeviceMaintenanceService deviceMaintenanceService;

    @Autowired
    KitCoreService kitService;

    @Value("${device.offline.interval}")
    private Integer offlineInterval;

    @Value("${device.data.countAnalyseDeviceSummary}")
    private Integer countAnalyseDeviceSummary;

    @Value("${device.data.battery.property.number}")
    private Integer batteryPropertyNumber;

    @Value("${magma.front.pub.topic}")
    private String mqttFrontTopic;

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreSchedule.class);
    public static HashMap<String, DeviceSummary> devicesSummary = new HashMap<>();


    @Scheduled(fixedRateString = "${device.offline.interval}") //in milliseconds

    @Scheduled(cron = "0 30 6/18 * * ?", zone = "Asia/Colombo")
    public void sendMaintenanceAlert() {
        LOGGER.debug("Scheduler Running for Every 6/18 30");
        DateTime now = MagmaTime.now();

        List<Device> offlineList = new ArrayList<>(); //OfflineDevices As SingleList
        List<Device> offlineDevicesMoreThan3days = new ArrayList<>();
        Map<String, List<Device>> offlineDevicesWeekWise = new HashMap<>();
        offlineDevicesWeekWise.put("week1", new ArrayList<>());
        offlineDevicesWeekWise.put("week2", new ArrayList<>());
        offlineDevicesWeekWise.put("week3", new ArrayList<>());
        offlineDevicesWeekWise.put("week4", new ArrayList<>());
        deviceRepository.findByPersistenceIsTrue().forEach(device -> {
            try {
                if (Boolean.TRUE.equals(device.getMaintain()) || device.getInterval() == null || device.getLastSeen() == null) {
                    LOGGER.debug("Skip Offline Check : {}", device);
                    return;
                }

                if (device.getLastSeen().isBefore(now.minusMinutes(3 * device.getInterval()))) {
                    offlineList.add(device);
                }
                if (device.getLastSeen().isBefore(now.minusDays(3).minusMinutes(3 * device.getInterval()))) {
                    offlineDevicesMoreThan3days.add(device);
                }

                if (device.getLastSeen().isBefore(now.minusMinutes(3 * device.getInterval())) && device.getLastSeen().isAfter(now.minusWeeks(1))) {
                    offlineDevicesWeekWise.get("week1").add(device);
                }
                if (device.getLastSeen().isBefore(now.minusWeeks(1)) && device.getLastSeen().isAfter(now.minusWeeks(2))) {
                    offlineDevicesWeekWise.get("week2").add(device);
                }
                if (device.getLastSeen().isBefore(now.minusWeeks(2)) && device.getLastSeen().isAfter(now.minusWeeks(3))) {
                    offlineDevicesWeekWise.get("week3").add(device);
                }
                if (device.getLastSeen().isBefore(now.minusWeeks(3))) {
                    offlineDevicesWeekWise.get("week4").add(device);
                }

            } catch (Exception e) {
                LOGGER.error("Exception In Schedule :", e);
            }
        });
        deviceMaintenanceService.sendOfflineAlerts(offlineList);  //This is Modified as weekWise
        deviceMaintenanceService.sendHighPriorityOfflineAlerts(offlineDevicesMoreThan3days);
        deviceMaintenanceService.sendOfflineDevicesWeekWiseAlert(offlineDevicesWeekWise);

        //send offline device alert for between every 12 hour period
        DateTime startDate = now.minusHours(12);
        List<Offline> offlineDeviceList = offlineRepository.findByStartTimeBetween(startDate, now);
        deviceMaintenanceService.sendPeriodicOfflineAlerts(offlineDeviceList);
    }

    //@Scheduled(fixedRateString = "${magma.deviceSummary.fixedRate}") //in milliseconds
    public void getDevicesSummary() {
        DateTime start = DateTime.now();
        LOGGER.debug("Device summary scheduler started");

        int deviationNegligible = 10;        //in minutes

        List<Device> devices = deviceRepository.findAll();

        int noDevicesAlive = 0;
        int noDevicesOnBattery = 0;
        int noDevicesOnCurrent = 0;
        int noDevicesBatteryDead = 0;
        int noDevicesSensorFailure = 0;
        int noDevicesCoverageIssue = 0;
        int newDevices = 0;
        int noDevicesNetworkServiceInterruption = 0;
        int noDevicesOffline = 0;
        int noDevicesDead = 0;

        DeviceSummary aliveDevices = new DeviceSummary();
        DeviceSummary onBatteryDevices = new DeviceSummary();
        DeviceSummary onCurrentDevices = new DeviceSummary();
        DeviceSummary batteryDeadDevices = new DeviceSummary();
        DeviceSummary sensorFailureDevices = new DeviceSummary();
        DeviceSummary coverageIssueDevices = new DeviceSummary();
        DeviceSummary newAddedDevices = new DeviceSummary();
        DeviceSummary networkServiceInterruptionDevices = new DeviceSummary();
        DeviceSummary offlineDevices = new DeviceSummary();
        DeviceSummary totalDevices = new DeviceSummary();
        DeviceSummary deadDevices = new DeviceSummary();


        HashMap<String, List<Double>> sensorFailureValues = new HashMap<>();
        sensorFailureValueRepository.findAll().forEach(obj -> {
            sensorFailureValues.put(obj.getCode(), obj.getValues());
        });

        for (Device device : devices) {
            totalDevices.addDevice(device.getId());
            if (device.getNoOfSensors() == 0 && device.getBattery() == null) {
                deadDevices.addDevice(device.getId());
                noDevicesDead++;
            }
            if (device.getNoOfSensors() == 0) {
                continue;
            }
            List<Sensor> deviceHistory = sensorRepository.findByDeviceIdAndNumberOrderByTimeDesc(
                    new String(device.getId()), 0, org.springframework.data.domain.PageRequest.of(0, countAnalyseDeviceSummary));
            List<Sensor> batteryHistory = sensorRepository.findByDeviceIdAndNumberOrderByTimeDesc(
                    new String(device.getId()), batteryPropertyNumber, org.springframework.data.domain.PageRequest.of(0, countAnalyseDeviceSummary));


            DateTime now = MagmaTime.now();
            if (device.getLastSeen() == null) {
                newDevices++;
                newAddedDevices.addDevice(device.getId());
            } else if (device.getLastSeen().isBefore(now.minusMinutes(device.getInterval()))) {
                noDevicesOffline++;
                offlineDevices.addDevice(device.getId());
            }

            if (!deviceHistory.isEmpty()) {
                int estimatorOnBatteryLast = 0;
                int estimatorOnBattery = 0;
                int minNoOfEntries = Math.min(countAnalyseDeviceSummary, batteryHistory.size());
                int intervalOnCurrent = device.getIntervalMin();        //in minutes
                int intervalOnBattery = device.getInterval() == null ? intervalOnCurrent : device.getInterval();
                int factor = Math.max(intervalOnBattery / intervalOnCurrent, 3);
                int i;
                for (i = 0; i < minNoOfEntries; i++) {
                    if (Integer.parseInt(batteryHistory.get(i).getValue()) > 0) {
                        if (i + 1 == minNoOfEntries) {
                            if (i < 3) {
                                estimatorOnBatteryLast += factor;
                            }
                            estimatorOnBattery += factor;
                        } else {
                            if (i < 3) {
                                estimatorOnBatteryLast += 1;
                            }
                            estimatorOnBattery += 1;
                        }
                    } else {
                        break;
                    }
                }
                boolean isPowerOnBatteryLast = device.getBattery() != null && estimatorOnBatteryLast > 3;
                boolean isPowerOnBattery = device.getBattery() != null && estimatorOnBattery > i;

                boolean hasGotLastReading = false;
                boolean anyReadingMissing = false;
                int noOfEntitiesToLoop = ((isPowerOnBatteryLast ? 1 : factor) * countAnalyseDeviceSummary);
                if (deviceHistory.size() > noOfEntitiesToLoop) {
                    int sum = 0;
                    //Loop from last reading and stepping by 1 if battery or by the factor if on current - totally countAnalyseDeviceSummary iterations
                    for (i = 1; i < noOfEntitiesToLoop; i += isPowerOnBatteryLast ? 1 : factor) {
                        Long periodX = MagmaTime.durationInMinutesLongPeriod(deviceHistory.get(i - 1).getTime(), deviceHistory.get(i).getTime());
                        sum += periodX;
                        // whether any last reading interval greater than expected configured time in the device on average.
                        boolean readingMissing = Math.abs(periodX - (isPowerOnBatteryLast ? intervalOnBattery : intervalOnCurrent)) > (isPowerOnBatteryLast ? intervalOnBattery : intervalOnCurrent) + deviationNegligible;
                        anyReadingMissing = anyReadingMissing || readingMissing;
                    }
                    int average = sum / (isPowerOnBatteryLast ? intervalOnBattery : intervalOnCurrent);
                    // whether sum last "countAnalyseDeviceSummary" readings interval deviates by less than the 'deviationNegligible' minutes or not from expected configured time in the device on average.
                    hasGotLastReading = Math.abs(average - (isPowerOnBatteryLast ? intervalOnBattery : intervalOnCurrent)) < deviationNegligible;
                }

                //was last reading from the device closer to expected time slot (now)
                if (MagmaTime.durationInMinutesLongPeriod(deviceHistory.get(0).getTime(), MagmaTime.now()) <= (isPowerOnBatteryLast ? intervalOnBattery : intervalOnCurrent) + deviationNegligible) {
                    noDevicesAlive += 1;
                    aliveDevices.addDevice(device.getId());
                    if (isPowerOnBatteryLast) {
                        noDevicesOnBattery += 1;
                        onBatteryDevices.addDevice(device.getId());
                    } else {
                        noDevicesOnCurrent += 1;
                        onCurrentDevices.addDevice(device.getId());
                    }
                    if (anyReadingMissing) {
                        noDevicesCoverageIssue += 1;
                        coverageIssueDevices.addDevice(device.getId());
                    }
                } else {
                    if (isPowerOnBattery) {
                        noDevicesBatteryDead += 1;
                        batteryDeadDevices.addDevice(device.getId());
                    }
                    if (hasGotLastReading && MagmaTime.durationInMinutesLongPeriod(deviceHistory.get(0).getTime(), MagmaTime.now()) > 525600) { //525600 One day in minutes
                        noDevicesNetworkServiceInterruption += 1;
                        networkServiceInterruptionDevices.addDevice(device.getId());
                    }
                }
                boolean isAnySensorOfDeviceFailure = false;
                for (i = 0; i < device.getSensorCodes().length && !isAnySensorOfDeviceFailure; i++) {
                    List<Sensor> deviceHistoryX = sensorRepository.findByDeviceIdAndNumberOrderByTimeDesc(
                            new String(device.getId()), i, org.springframework.data.domain.PageRequest.of(0, countAnalyseDeviceSummary));
                    List<Double> sensorFailureValueX = sensorFailureValues.get(device.getSensorCodes()[i]);
                    int estimatorSensorFailure = 0;

                    for (int j = 0; j < Math.min(countAnalyseDeviceSummary, deviceHistoryX.size()); j++) {
                        if (sensorFailureValueX != null && sensorFailureValueX.contains(Double.parseDouble(deviceHistoryX.get(0).getValue()))) {
                            estimatorSensorFailure += 1;
                        }
                    }
                    if (estimatorSensorFailure > countAnalyseDeviceSummary / 2) {
                        isAnySensorOfDeviceFailure = true;
                    }
                }
                if (isAnySensorOfDeviceFailure) {
                    noDevicesSensorFailure += 1;
                    sensorFailureDevices.addDevice(device.getId());
                }
            } else {
                newDevices += 1;
                newAddedDevices.addDevice(device.getId());
            }
        }
        aliveDevices.setNoOfDevice(noDevicesAlive);
        onBatteryDevices.setNoOfDevice(noDevicesOnBattery);
        onCurrentDevices.setNoOfDevice(noDevicesOnCurrent);
        batteryDeadDevices.setNoOfDevice(noDevicesBatteryDead);
        sensorFailureDevices.setNoOfDevice(noDevicesSensorFailure);
        coverageIssueDevices.setNoOfDevice(noDevicesCoverageIssue);
        newAddedDevices.setNoOfDevice(newDevices);
        networkServiceInterruptionDevices.setNoOfDevice(noDevicesNetworkServiceInterruption);
        offlineDevices.setNoOfDevice(noDevicesOffline);
        totalDevices.setNoOfDevice(devices.size());
        deadDevices.setNoOfDevice(noDevicesDead);

        devicesSummary.clear();
        devicesSummary.put("alive", aliveDevices);
        devicesSummary.put("dead", deadDevices);
        devicesSummary.put("onBattery", onBatteryDevices);
        devicesSummary.put("onCurrent", onCurrentDevices);
        devicesSummary.put("batteryDead", batteryDeadDevices);
        devicesSummary.put("sensorFailure", sensorFailureDevices);
        devicesSummary.put("coverageIssue", coverageIssueDevices);
        devicesSummary.put("networkInterruption", networkServiceInterruptionDevices);
        devicesSummary.put("newDevice", newAddedDevices);
        devicesSummary.put("total", totalDevices);
        devicesSummary.put("noDevicesOffline", offlineDevices);

        ObjectMapper mapper = new ObjectMapper();
        try {
            mqttGateway.send(mqttFrontTopic + "deviceSummary", mapper.writeValueAsString(devicesSummary));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        DateTime end = DateTime.now();
        LOGGER.debug("Device summary scheduler Finished Start : {}, End : {}", start, end);
    }
}
