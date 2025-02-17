package com.magma.core.service;


import com.magma.dmsdata.data.entity.*;
import com.magma.core.data.repository.AlertLimitRepository;
import com.magma.core.data.repository.UserFavouriteRepository;
import com.magma.dmsdata.data.support.CorporateDeviceSummary;
import com.magma.dmsdata.data.support.CorporateSensorSummary;
import com.magma.dmsdata.util.MagmaException;
import com.magma.dmsdata.util.MagmaStatus;
import com.magma.util.MagmaTime;
import com.magma.util.Status;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReferenceService {
    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    CorporateConnectorService corporateConnectorService;

    @Autowired
    UserFavouriteRepository userFavouriteRepository;

    @Autowired
    AlertLimitRepository alertLimitRepository;

    public CorporateDeviceSummary getCorporateWiseDeviceSummary(String userId, String corporateId, Boolean favouriteFilter) {
        DateTime dateTimeNow = MagmaTime.now();

        List<String> deviceIdsForQuery = new ArrayList<>();
        List<String> deviceIdsInCorporate = new ArrayList<>();
        String summaryType;

        if (!favouriteFilter) {
            deviceIdsForQuery = deviceIdsInCorporate;
            summaryType = "Corporate wise device summary";
        } else {
            UserFavourite userFavourite = userFavouriteRepository.findByUserId(userId);
            if (userFavourite == null) {
                return new CorporateDeviceSummary();
            } else {
                List<String> favouriteDevicesIds = userFavourite.getFavouriteDevices();
                List<String> favouriteDevicesIdsInCorporate = favouriteDevicesIds.stream().filter(deviceIdsInCorporate::contains).collect(Collectors.toList());
                deviceIdsForQuery = favouriteDevicesIdsInCorporate;
                summaryType = "Corporate wise favourite-device summary";
            }
        }
        Query allDeviceQuery = new Query(Criteria.where("_id").in(deviceIdsForQuery));
        Query allActiveDeviceQuery = new Query(Criteria.where("_id").in(deviceIdsForQuery).and("status").is(Status.ACTIVE));
        Query newDeviceQuery = new Query(Criteria.where("_id").in(deviceIdsForQuery).and("lastSeen").is(null));
        Query newActiveDeviceQuery = new Query(Criteria.where("_id").in(deviceIdsForQuery).and("status").is(Status.ACTIVE).and("lastSeen").is(null));
        Query oldDevicesQuery = new Query(Criteria.where("_id").in(deviceIdsForQuery).and("lastSeen").exists(true));
        Query oldActiveDeviceQuery = new Query(Criteria.where("_id").in(deviceIdsForQuery).and("status").is(Status.ACTIVE).and("lastSeen").exists(true));
        Query batteryDeadDeviceQuery = new Query(Criteria.where("_id").in(deviceIdsForQuery).and("noOfSensors").is(0).and("battery").is(null));

        List<Device> allDevicesInCorporate = mongoTemplate.find(allDeviceQuery, Device.class);
        List<Device> allActiveDevicesInCorporate = mongoTemplate.find(allActiveDeviceQuery, Device.class);
        List<Device> newDevicesInCorporate = mongoTemplate.find(newDeviceQuery, Device.class);
        List<Device> newActiveDevicesInCorporate = mongoTemplate.find(newActiveDeviceQuery, Device.class);
        List<Device> oldDevicesInCorporate = mongoTemplate.find(oldDevicesQuery, Device.class);
        List<Device> oldActiveDevicesInCorporate = mongoTemplate.find(oldActiveDeviceQuery, Device.class);
        List<Device> batteryDeadDevicesInCorporate = mongoTemplate.find(batteryDeadDeviceQuery, Device.class);
        List<Device> onlineDevicesInCorporate = allDevicesInCorporate.stream().filter(device -> device.getLastSeen() != null && !device.getLastSeen().isBefore(dateTimeNow.minusMinutes(device.getInterval() * 3))).collect(Collectors.toList());
        List<Device> offlineDevicesInCorporate = allDevicesInCorporate.stream().filter(device -> ((device.getLastSeen() == null) || (device.getLastSeen() != null && device.getLastSeen().isBefore(dateTimeNow.minusMinutes(device.getInterval() * 3))))).collect(Collectors.toList());

        CorporateDeviceSummary deviceSummary = new CorporateDeviceSummary();
        deviceSummary.setSummaryType(summaryType);
        deviceSummary.setCorporateId(corporateId);
        deviceSummary.setAllDevices(allDevicesInCorporate);
        deviceSummary.setActiveDevices(allActiveDevicesInCorporate);
        deviceSummary.setNewDevices(newDevicesInCorporate);
        deviceSummary.setNewActiveDevices(newActiveDevicesInCorporate);
        deviceSummary.setOldDevices(oldDevicesInCorporate);
        deviceSummary.setOldActiveDevices(oldActiveDevicesInCorporate);
        deviceSummary.setOnlineDevices(onlineDevicesInCorporate);
        deviceSummary.setOfflineDevices(offlineDevicesInCorporate);
        deviceSummary.setBatteryDeadDevices(batteryDeadDevicesInCorporate);


        //For UI
        if (Objects.equals(corporateId, "5cec12c672c17f1c94410349")) {
            deviceSummary.setBatteryDeadDeviceCount(5);
            deviceSummary.setDirectPowerDeviceCount(11);
            deviceSummary.setOnBatteryDeviceCount(2);
            deviceSummary.setOnSolarPowerDeviceCount(2);
        }
        if (Objects.equals(corporateId, "5cef79a772c17f4ef491343e")) {
            deviceSummary.setBatteryDeadDeviceCount(15);
            deviceSummary.setDirectPowerDeviceCount(15);
            deviceSummary.setOnBatteryDeviceCount(5);
            deviceSummary.setOnSolarPowerDeviceCount(12);
        }
        if (Objects.equals(corporateId, "5d087fbb72c17f428acac136")) {
            deviceSummary.setBatteryDeadDeviceCount(25);
            deviceSummary.setDirectPowerDeviceCount(16);
            deviceSummary.setOnBatteryDeviceCount(25);
            deviceSummary.setOnSolarPowerDeviceCount(16);
        }
        if (Objects.equals(corporateId, "5cde66fc72c17f1a9fc31bc9")) {
            deviceSummary.setBatteryDeadDeviceCount(7);
            deviceSummary.setDirectPowerDeviceCount(21);
            deviceSummary.setOnBatteryDeviceCount(15);
            deviceSummary.setOnSolarPowerDeviceCount(19);
        }
        if (Objects.equals(corporateId, "5d1de62372c17f18b2d4cd0c")) {
            deviceSummary.setBatteryDeadDeviceCount(4);
            deviceSummary.setDirectPowerDeviceCount(23);
            deviceSummary.setOnBatteryDeviceCount(16);
            deviceSummary.setOnSolarPowerDeviceCount(13);
        }

        return deviceSummary;
    }

    public CorporateSensorSummary getCorporateWiseSensorSummary(String corporateId) {
        DateTime dateTimeNow = MagmaTime.now();

        CorporateSensorSummary sensorSummaryOutput = new CorporateSensorSummary(corporateId);
        sensorSummaryOutput.getCorporateName();

        List<String> sensorCodesInCorporate = new ArrayList<>();
        List<String> sensorNamesInCorporate = new ArrayList<>();

        Integer totalNumberOfSensorsInCorporate = 0;
        Map<String, Map<String, Integer>> propertySensorSummaryMap = new HashMap<>();
        Map<String, Map<String, Integer>> nonPropertySensorSummaryMap = new HashMap<>();
        Map<String, Map<String, List<Device>>> propertySensorSummaryDeviceMap = new HashMap<>();

//        for (Kit kit : KitsInCorporate) {
//            List<String> deviceIdsOfKit = new ArrayList<>();
//            deviceIdsOfKit.addAll(kit.getDevices());
//            List<AlertLimit> alertLimitsForKit = alertLimitRepository.findByKitId(kit.getId());
//            totalNumberOfSensorsInCorporate += kit.getModel().getNoOfSensors();
//            Map<String, Integer> inActiveSensorsOfKit = new HashMap<>();
//            Map<String, Integer> wrongValuedSensorsOfKit = new HashMap<>();
//            Map<String, Integer> failureSensorsOfKit = new HashMap<>();
////            List<String> allSensorCodesOfKit = Arrays.asList(kit.getModel().getSensors());
////            List<String> allPropertyCodesOfKit = Arrays.asList(kit.getModel().getProperties());
//            List<String> allNonPropertyCodesOfKit = new ArrayList<>();
//
//            //Get devices Of Kit
//            Query kitDevicesQuery = new Query(Criteria.where("_id").in(deviceIdsOfKit));
//            List<Device> allDevicesInKit = mongoTemplate.find(kitDevicesQuery, Device.class);
//
//            //Divide sensor code into Property code and non-Property Code
//            for (String code : allSensorCodesOfKit) {
//                if (!allPropertyCodesOfKit.contains(code)) {
//                    allNonPropertyCodesOfKit.add(code);
//                }
//                if (code.equals("B")) {
//                    allNonPropertyCodesOfKit.add(code);
//                }
//            }
//
//            //Prepare Property sensor Map && property sensor Device Map
//            for (String psCode : allPropertyCodesOfKit) {
//                if (!sensorCodesInCorporate.contains(psCode)) {
//                    sensorCodesInCorporate.add(psCode);
//                    sensorNamesInCorporate.add(psCode);
//                }
//                propertySensorSummaryMap.putIfAbsent(psCode, new HashMap<>());
//                propertySensorSummaryDeviceMap.putIfAbsent(psCode, new HashMap<>()); //To store devices list
//                Map<String, Integer> propertySensorSpecificMap = propertySensorSummaryMap.get(psCode);
//                Map<String, List<Device>> propertySensorSpecificDeviceMap = propertySensorSummaryDeviceMap.get(psCode);
//                propertySensorSpecificMap.putIfAbsent("total sensors", 0);
//                propertySensorSpecificMap.putIfAbsent("active sensors", 0);
//                propertySensorSpecificMap.putIfAbsent("inactive sensors", 0);
//                propertySensorSpecificMap.putIfAbsent("sensor failure", 0);
//                propertySensorSpecificMap.putIfAbsent("wrong valued sensors", 0);
//                //Prepare Device Map
//                propertySensorSpecificDeviceMap.putIfAbsent("active sensors devices", new ArrayList<>());
//                propertySensorSpecificDeviceMap.putIfAbsent("sensor failure devices", new ArrayList<>());
//                propertySensorSpecificDeviceMap.putIfAbsent("wrong valued sensors devices", new ArrayList<>());
//            }
//
//            //Prepare Non-Property sensor Map
//            for (String npsCode : allNonPropertyCodesOfKit) {
//                if (!sensorCodesInCorporate.contains(npsCode)) {
//                    sensorCodesInCorporate.add(npsCode);
//                    sensorNamesInCorporate.add(npsCode);
//                }
//                nonPropertySensorSummaryMap.putIfAbsent(npsCode, new HashMap<>());
//            }
//
//            // Process Available Properties to Generate sensor Counts
//            for (Property property : kit.getProperties()) {
//                if (allPropertyCodesOfKit.contains(property.getCode())) {
//                    inActiveSensorsOfKit.putIfAbsent(property.getCode(), 0);
//                    wrongValuedSensorsOfKit.putIfAbsent(property.getCode(), 0);
//                    failureSensorsOfKit.putIfAbsent(property.getCode(), 0);
//                    Map<String, Integer> propertySensorSpecificMap = propertySensorSummaryMap.get(property.getCode());
//
//                    if (property.getTime() != null && kit.getInterval() != null && !property.getTime().isBefore(dateTimeNow.minusMinutes(kit.getInterval()))) {
//                        propertySensorSpecificMap.replace("active sensors", propertySensorSpecificMap.get("active sensors") + 1);
//                        propertySensorSpecificMap.replace("total sensors", propertySensorSpecificMap.get("total sensors") + 1);
//                    } else {
//                        propertySensorSpecificMap.replace("inactive sensors", propertySensorSpecificMap.get("inactive sensors") + 1);
//                        propertySensorSpecificMap.replace("total sensors", propertySensorSpecificMap.get("total sensors") + 1);
//                        inActiveSensorsOfKit.replace(property.getCode(), inActiveSensorsOfKit.get(property.getCode()) + 1);
//                    }
//                    if (property.getError() != null && property.getError()) {
//                        propertySensorSpecificMap.replace("sensor failure", propertySensorSpecificMap.get("sensor failure") + 1);
//                        failureSensorsOfKit.replace(property.getCode(), failureSensorsOfKit.get(property.getCode()) + 1);
//                    }
//                    List<AlertLimit> alertLimitForProperty = alertLimitsForKit.stream().filter(alertLimit -> alertLimit.getCode() == property.getCode()).collect(Collectors.toList());
//                    if (!alertLimitForProperty.isEmpty() && (property.getValue() < alertLimitForProperty.get(0).getLow() || property.getValue() > alertLimitForProperty.get(0).getHigh())) {
//                        propertySensorSpecificMap.replace("wrong valued sensors", propertySensorSpecificMap.get("wrong valued sensors") + 1);
//                        wrongValuedSensorsOfKit.replace(property.getCode(), wrongValuedSensorsOfKit.get(property.getCode()) + 1);
//                    }
//                }
//            }
//            //Generate Devices Map
//            for (String psCode : allPropertyCodesOfKit) {
//                Map<String, List<Device>> propertySensorSpecificDevicesMap = propertySensorSummaryDeviceMap.get(psCode);
//                if (inActiveSensorsOfKit.get(psCode) != null && inActiveSensorsOfKit.get(psCode) == 0) {
//                    propertySensorSpecificDevicesMap.get("active sensors devices").addAll(allDevicesInKit);
//                }
//                if (wrongValuedSensorsOfKit.get(psCode) != null && wrongValuedSensorsOfKit.get(psCode) != 0) {
//                    propertySensorSpecificDevicesMap.get("wrong valued sensors devices").addAll(allDevicesInKit);
//                }
//                if (failureSensorsOfKit.get(psCode) != null && failureSensorsOfKit.get(psCode) != 0) {
//                    propertySensorSpecificDevicesMap.get("sensor failure devices").addAll(allDevicesInKit);
//                }
//            }
//            //Process Non-Property Sensors through devices
//            for (Device device : allDevicesInKit) {
//                List<Sensor> allSensorsReadingsOfDevice = device.getSensors(); // available Sensor Readings of device
//                List<Sensor> nonPropertySensorReadingsOfDevice = allSensorsReadingsOfDevice.stream().filter(sensor -> allNonPropertyCodesOfKit.contains(sensor.getCode())).collect(Collectors.toList());
//
//                for (Sensor nonPropertySensor : nonPropertySensorReadingsOfDevice) {
//                    Map<String, Integer> nonPropertySensorSpecificMap = nonPropertySensorSummaryMap.get(nonPropertySensor.getCode());
//                    nonPropertySensorSpecificMap.putIfAbsent("total sensors", 0);
//                    nonPropertySensorSpecificMap.putIfAbsent("active sensors", 0);
//                    nonPropertySensorSpecificMap.putIfAbsent("inactive sensors", 0);
//
//                    if (nonPropertySensor.getTime() != null && device.getInterval() != null && !nonPropertySensor.getTime().isBefore(dateTimeNow.minusMinutes(device.getInterval()))) {
//                        nonPropertySensorSpecificMap.replace("active sensors", nonPropertySensorSpecificMap.get("active sensors") + 1);
//                        nonPropertySensorSpecificMap.replace("total sensors", nonPropertySensorSpecificMap.get("total sensors") + 1);
//                    } else {
//                        nonPropertySensorSpecificMap.replace("inactive sensors", nonPropertySensorSpecificMap.get("inactive sensors") + 1);
//                        nonPropertySensorSpecificMap.replace("total sensors", nonPropertySensorSpecificMap.get("total sensors") + 1);
//                    }
//                }
//            }
//        }
        //Filtering Non PropertySensors that behaved as property sensor in some Kits
        propertySensorSummaryMap.remove("Battery");

        sensorSummaryOutput.setSensorCodesInCorporate(sensorCodesInCorporate);
        sensorSummaryOutput.setSensorsInCorporate(sensorNamesInCorporate);
        sensorSummaryOutput.setTotalNumbersOfSensorsInCorporate(totalNumberOfSensorsInCorporate);
        sensorSummaryOutput.setPropertySensorSummaryMap(propertySensorSummaryMap);
        sensorSummaryOutput.setNonPropertySensorSummaryMap(nonPropertySensorSummaryMap);
        sensorSummaryOutput.setPropertySensorSummaryDeviceMap(propertySensorSummaryDeviceMap);
        return sensorSummaryOutput;
    }

    public List<Device> getCorporateWiseDevices(String userId, String corporateId, Boolean favouriteFilter, String dataFilter, String sensorName) {
        List<String> validDataFilters = Arrays.asList("Active", "NonActive", "Old", "New", "Online", "Offline", "NewActive", "NewNonActive",
                "OldActive", "OldNonActive", "OnDirectPower", "OnBattery", "BatteryDead", "OnSolarPower", "SensorFailure", "WrongValued", "ActiveSensors");

        if (dataFilter != null && !validDataFilters.contains(dataFilter)) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
        DateTime dateTimeNow = MagmaTime.now();
        corporateConnectorService.validateCorporate(corporateId);
        List<String> deviceIdsForQuery = new ArrayList<>();
        List<String> deviceIdsInCorporate = new ArrayList<>();
        if (favouriteFilter == null || !favouriteFilter) {
            deviceIdsForQuery = deviceIdsInCorporate;
        } else {
            UserFavourite userFavourite = userFavouriteRepository.findByUserId(userId);
            if (userFavourite == null) {
                throw new MagmaException(MagmaStatus.FAVOURITE_DEVICES_NOT_FOUND);
            } else {
                List<String> favouriteDevicesIds = userFavourite.getFavouriteDevices();
                List<String> favouriteDevicesIdsInCorporate = favouriteDevicesIds.stream().filter(deviceIdsInCorporate::contains).collect(Collectors.toList());
                deviceIdsForQuery = favouriteDevicesIdsInCorporate;
            }
        }
        List<Device> outputDevicesList;

        if (dataFilter == null) {
            Query allDeviceQuery = new Query(Criteria.where("_id").in(deviceIdsForQuery));
            outputDevicesList = mongoTemplate.find(allDeviceQuery, Device.class);
        } else {
            switch (dataFilter) {
                case "Active": {
                    Query allActiveDeviceQuery = new Query(Criteria.where("_id").in(deviceIdsForQuery).and("status").is(Status.ACTIVE));
                    outputDevicesList = mongoTemplate.find(allActiveDeviceQuery, Device.class);
                    break;
                }
                case "NonActive": {
                    Query allNonActiveDeviceQuery = new Query(Criteria.where("_id").in(deviceIdsForQuery).and("status").ne(Status.ACTIVE));
                    outputDevicesList = mongoTemplate.find(allNonActiveDeviceQuery, Device.class);
                    break;
                }
                case "Online": {
                    Query allDeviceQuery = new Query(Criteria.where("_id").in(deviceIdsForQuery));
                    List<Device> allDevicesInCorporate = mongoTemplate.find(allDeviceQuery, Device.class);
                    outputDevicesList = allDevicesInCorporate.stream().filter(device -> device.getLastSeen() != null && !device.getLastSeen().isBefore(dateTimeNow.minusMinutes(device.getInterval() * 3))).collect(Collectors.toList());
                    break;
                }
                case "Offline": {
                    Query allDeviceQuery = new Query(Criteria.where("_id").in(deviceIdsForQuery));
                    List<Device> allDevicesInCorporate = mongoTemplate.find(allDeviceQuery, Device.class);
                    outputDevicesList = allDevicesInCorporate.stream().filter(device -> ((device.getLastSeen() == null) || (device.getLastSeen() != null && device.getLastSeen().isBefore(dateTimeNow.minusMinutes(device.getInterval() * 3))))).collect(Collectors.toList());
                    break;
                }
                case "Old": {
                    Query oldDevicesQuery = new Query(Criteria.where("_id").in(deviceIdsForQuery).and("lastSeen").exists(true));
                    outputDevicesList = mongoTemplate.find(oldDevicesQuery, Device.class);
                    break;
                }
                case "New": {
                    Query newDeviceQuery = new Query(Criteria.where("_id").in(deviceIdsForQuery).and("lastSeen").is(null));
                    outputDevicesList = mongoTemplate.find(newDeviceQuery, Device.class);
                    break;
                }
                case "NewActive": {
                    Query newActiveDeviceQuery = new Query(Criteria.where("_id").in(deviceIdsForQuery).and("status").is(Status.ACTIVE).and("lastSeen").is(null));
                    outputDevicesList = mongoTemplate.find(newActiveDeviceQuery, Device.class);
                    break;
                }
                case "NewNonActive": {
                    Query newNonActiveDeviceQuery = new Query(Criteria.where("_id").in(deviceIdsForQuery).and("status").ne(Status.ACTIVE).and("lastSeen").is(null));
                    outputDevicesList = mongoTemplate.find(newNonActiveDeviceQuery, Device.class);
                    break;
                }
                case "OldActive": {
                    Query oldActiveDeviceQuery = new Query(Criteria.where("_id").in(deviceIdsForQuery).and("status").is(Status.ACTIVE).and("lastSeen").exists(true));
                    outputDevicesList = mongoTemplate.find(oldActiveDeviceQuery, Device.class);
                    break;
                }
                case "OldNonActive": {
                    Query oldNonActiveDeviceQuery = new Query(Criteria.where("_id").in(deviceIdsForQuery).and("status").ne(Status.ACTIVE).and("lastSeen").exists(true));
                    outputDevicesList = mongoTemplate.find(oldNonActiveDeviceQuery, Device.class);
                    break;
                }
                //SensorWise Data
                case "SensorFailure": { //One Of sensor failed devices
                    if (sensorName != null) {
                        CorporateSensorSummary sensorSummary = this.getCorporateWiseSensorSummary(corporateId);
                        try {
                            outputDevicesList = sensorSummary.getPropertySensorSummaryDeviceMap().get(sensorName).get("sensor failure devices");
                        } catch (Exception e) {
                            throw new MagmaException(MagmaStatus.INVALID_INPUT);
                        }
                    } else {
                        List<String> deviceIdsWithSensorFailure = new ArrayList<>();
//                        for (Kit kit : kitsForCorporate) {
//                            for (Property property : kit.getProperties()) {
//                                if (property.getError() != null && property.getError()) {
//                                    for (String deviceId : kit.getDevices()) {
//                                        if (!deviceIdsWithSensorFailure.contains(deviceId)) {
//                                            deviceIdsWithSensorFailure.add(deviceId);
//                                        }
//                                    }
//                                    break;
//                                }
//                            }
//                        }
                        List<String> favouriteFilteredDeviceIdsInCorporate = deviceIdsForQuery;
                        deviceIdsForQuery = deviceIdsWithSensorFailure.stream().filter(deviceId -> favouriteFilteredDeviceIdsInCorporate.contains(deviceId)).collect(Collectors.toList());
                        Query devicesWithSensorFailureQuery = new Query(Criteria.where("_id").in(deviceIdsForQuery));
                        outputDevicesList = mongoTemplate.find(devicesWithSensorFailureQuery, Device.class);
                    }
                    break;
                }
                case "WrongValued": { //One Of property sensor have alert
                    if (sensorName != null) {
                        CorporateSensorSummary sensorSummary = this.getCorporateWiseSensorSummary(corporateId);
                        try {
                            outputDevicesList = sensorSummary.getPropertySensorSummaryDeviceMap().get(sensorName).get("wrong valued sensors devices");
                        } catch (Exception e) {
                            throw new MagmaException(MagmaStatus.INVALID_INPUT);
                        }
                    } else {
                        List<String> deviceIdsWithAlerts = new ArrayList<>();
//                        for (Kit kit : kitsForCorporate) {
//                            for (Property property : kit.getProperties()) {
//                                if (property.getAlert() != null && property.getAlert()) {
//                                    for (String deviceId : kit.getDevices()) {
//                                        if (!deviceIdsWithAlerts.contains(deviceId)) {
//                                            deviceIdsWithAlerts.add(deviceId);
//                                        }
//                                    }
//                                    break;
//                                }
//                            }
//                        }
                        List<String> favouriteFilteredDeviceIdsInCorporate = deviceIdsForQuery;
                        deviceIdsForQuery = deviceIdsWithAlerts.stream().filter(deviceId -> favouriteFilteredDeviceIdsInCorporate.contains(deviceId)).collect(Collectors.toList());
                        Query devicesWithSensorFailureQuery = new Query(Criteria.where("_id").in(deviceIdsForQuery));
                        outputDevicesList = mongoTemplate.find(devicesWithSensorFailureQuery, Device.class);
                    }
                    break;
                }
                case "ActiveSensors": { //All sensor Must be Active
                    if (sensorName != null) {
                        CorporateSensorSummary sensorSummary = this.getCorporateWiseSensorSummary(corporateId);
                        try {
                            outputDevicesList = sensorSummary.getPropertySensorSummaryDeviceMap().get(sensorName).get("active sensors devices");
                        } catch (Exception e) {
                            throw new MagmaException(MagmaStatus.INVALID_INPUT);
                        }
                    } else {
                        List<String> deviceIdsWithInActiveSensors = new ArrayList<>();
//                        for (Kit kit : kitsForCorporate) {
//                            for (Property property : kit.getProperties()) {
//                                if (property.getTime().isBefore(dateTimeNow.minusMinutes(kit.getInterval()))) {
//                                    for (String deviceId : kit.getDevices()) {
//                                        if (!deviceIdsWithInActiveSensors.contains(deviceId)) {
//                                            deviceIdsWithInActiveSensors.add(deviceId);
//                                        }
//                                    }
//                                    break;
//                                }
//                            }
//                        }
                        List<String> favouriteFilteredDeviceIdsInCorporate = deviceIdsForQuery;
                        deviceIdsForQuery = favouriteFilteredDeviceIdsInCorporate.stream().filter(deviceId -> !deviceIdsWithInActiveSensors.contains(deviceId)).collect(Collectors.toList());
                        Query devicesWithSensorFailureQuery = new Query(Criteria.where("_id").in(deviceIdsForQuery));
                        outputDevicesList = mongoTemplate.find(devicesWithSensorFailureQuery, Device.class);
                    }
                    break;
                }
                case "BatteryDead":
                case "OnBattery":
                case "OnDirectPower":
                case "OnSolarPower": {
                    //Mock details Have to Change
                    Query allDeviceQuery = new Query(Criteria.where("_id").in(deviceIdsForQuery));
                    outputDevicesList = mongoTemplate.find(allDeviceQuery, Device.class);
                    break;
                }
                default: {
                    throw new MagmaException(MagmaStatus.INVALID_INPUT);
                }
            }
        }

        // TODO need to modify
//        outputDevicesList.forEach((d) -> {
//
//            Kit kit = kitRepository.findByDevices(d.getId());
//            if (kit != null) {
//                d.setReferenceName(corporateConnectorService.referenceName(kit.getId()));
//            }
//
//        });
        return outputDevicesList;
    }
}


