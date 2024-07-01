package com.magma.core.grpc_service.helper;

import com.google.protobuf.Timestamp;
import com.magma.core.data.entity.*;
import com.magma.core.data.entity.Error;
import com.magma.core.data.repository.SensorCodeRepository;

import com.magma.core.data.support.Arithmetic;
import com.magma.core.data.support.Connectivity;
import com.magma.core.data.support.DeviceParameterConfiguration;
import com.magma.core.data.support.GeoType;
import com.magma.core.data.support.Offset;
import com.magma.core.data.support.Shift;
import com.magma.core.util.ActuatorCode;
import com.magma.core.util.DataInputMethod;

import com.magma.util.Status;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

@Component
public class ProtoEntityPopulatingHelper {

    @Autowired
    SensorCodeRepository sensorCodeRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtoEntityPopulatingHelper.class);
    DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

    private static DateTime convertTimestampToDateTime(com.google.protobuf.Timestamp timestamp) {
        return new DateTime(timestamp.getSeconds() * 1000L + timestamp.getNanos() / 1000000L, DateTimeZone.UTC);
    }

    private static String convertDateTimeToString(DateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        return formatter.print(dateTime);
    }

    public Kit populateKitFromProto(com.magma.core.grpc_service.Kit protoKit) {

        try {
            if (protoKit != null) {
                Kit kit = new Kit();
                kit.setId(protoKit.getId());
                kit.setName(protoKit.getName());
                kit.setDescription(protoKit.getDescription());
                kit.setKitModelId(protoKit.getKitModelId());

                // kit.setModel(protoKit.getModel().); // need to generate KitModel object from
                // the json string
                kit.setDevices(protoKit.getDevices().getStrList());
                kit.setDeviceMap(protoKit.getDeviceMapMap());

                Map<String, Offset> offsetMap = new HashMap();

                for (Map.Entry<String, com.magma.core.grpc_service.Offset> entry : protoKit.getOffsetMapMap()
                        .entrySet()) {
                    String key = entry.getKey();
                    Offset offset = new Offset();
                    offset.setActuator(entry.getValue().getActuator());
                    offset.setSensor(entry.getValue().getSensor());
                    offsetMap.put(key, offset);
                }
                kit.setOffsetMap(offsetMap);

                Map<Integer, Property> propertyMap = new HashMap();

                for (Map.Entry<Integer, com.magma.core.grpc_service.Property> entry : protoKit.getPropertyMapMap()
                        .entrySet()) {
                    Integer key = entry.getKey();

                    Property property = null;
                    property = populatePropertyFromProto(entry.getValue());
                    propertyMap.put(key, property);

                }
                kit.setPropertyMap(propertyMap);

                Map<Integer, Shift> shiftMap = new HashMap();

                for (Map.Entry<Integer, com.magma.core.grpc_service.Shift> entry : protoKit.getShiftMapMap()
                        .entrySet()) {
                    Integer key = entry.getKey();
                    Shift shift = new Shift();

                    com.magma.core.grpc_service.Shift protoShift = entry.getValue();
                    shift.setOperation(Arithmetic.valueOf(String.valueOf(protoShift.getOperation())));
                    shift.setMeta(protoShift.getMetaMap());

                    shiftMap.put(key, shift);
                }
                kit.setShiftMap(shiftMap);

                Map<Integer, Action> actionMap = new HashMap();

                for (Map.Entry<Integer, com.magma.core.grpc_service.Action> entry : protoKit.getActionMapMap()
                        .entrySet()) {
                    Integer key = entry.getKey();
                    Action action = new Action();

                    com.magma.core.grpc_service.Action protoAction = entry.getValue();
                    action.setId(protoAction.getId());
                    action.setKitId(protoAction.getKitId());
                    action.setNumber(protoAction.getNumber());
                    action.setCode(ActuatorCode.valueOf(protoAction.getCode()));

                    if (protoAction.getTime() != null && !"".equals(protoAction.getTime())) {
                        action.setTime(fmt.parseDateTime(protoAction.getTime()));
                    }
                    action.setValue(protoAction.getValue());
                    if (protoAction.getCreationDate() != null && !"".equals(protoAction.getCreationDate())) {
                        action.setCreationDate(fmt.parseDateTime(protoAction.getCreationDate()));
                    }
                    if (protoAction.getModifiedDate() != null && !"".equals(protoAction.getModifiedDate())) {
                        action.setModifiedDate(fmt.parseDateTime(protoAction.getModifiedDate()));
                    }
                    actionMap.put(key, action);
                }
                kit.setActionMap(actionMap);

                Geo geo = new Geo();
                com.magma.core.grpc_service.Geo protoGeo = protoKit.getGeo();
                geo.setId(protoGeo.getId());
                geo.setKitId(protoGeo.getKitId());
                geo.setType(GeoType.valueOf(String.valueOf(protoGeo.getType())));
                if (protoGeo.getTime() != null && !"".equals(protoGeo.getTime())) {
                    geo.setTime(fmt.parseDateTime(protoGeo.getTime()));
                }

                geo.setLat(protoGeo.getLat());
                geo.setLng(protoGeo.getLng());
                geo.setRelativeLocation(protoGeo.getRelativeLocationMap());

                kit.setGeo(geo);

                Battery battery = new Battery();
                com.magma.core.grpc_service.Battery protoBattery = protoKit.getBattery();

                battery.setLow(protoBattery.getLow());
                battery.setHigh(protoBattery.getHigh());

                battery.setReading(populatePropertyFromProto(protoBattery.getReading()));

                if (protoBattery.getCreationDate() != null && !"".equals(protoBattery.getCreationDate())) {
                    battery.setCreationDate(fmt.parseDateTime(protoBattery.getCreationDate()));
                }
                if (protoBattery.getModifiedDate() != null && !"".equals(protoBattery.getModifiedDate())) {
                    battery.setModifiedDate(fmt.parseDateTime(protoBattery.getModifiedDate()));
                }

                kit.setBattery(battery);
                kit.setMaintain(protoKit.getMaintain());
                kit.setAlertLevel(protoKit.getAlertLevel());
                kit.setInterval(protoKit.getInterval());
                kit.setPersistence(protoKit.getPersistence());
                kit.setOffline(protoKit.getOffline());
                kit.setStatus(Status.valueOf(String.valueOf(protoKit.getStatus())));
                kit.setAlerts(protoKit.getAlertsMap());
                kit.setMetaData(protoKit.getMetaDataMap());

                kit.setInputMethod(DataInputMethod.valueOf(String.valueOf(protoKit.getInputMethod())));

                if (protoKit.getLastSeen() != null && !"".equals(protoKit.getLastSeen())) {
                    kit.setLastSeen(fmt.parseDateTime(protoKit.getLastSeen()));
                }
                if (protoKit.getCreationDate() != null && !"".equals(protoKit.getCreationDate())) {
                    kit.setCreationDate(fmt.parseDateTime(protoKit.getCreationDate()));
                }

                return kit;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Error while populateKitFromProto ", e);
        }
        return null;
    }

    public Property populatePropertyFromProto(com.magma.core.grpc_service.Property protoProperty) {
        Property property = new Property();

        property.setId(protoProperty.getId());
        property.setKitId(protoProperty.getKitId());
        property.setNumber(protoProperty.getNumber());
        property.setCode(protoProperty.getCode());

        if (protoProperty.getTime() != null && !"".equals(protoProperty.getTime())) {
            property.setTime(fmt.parseDateTime(protoProperty.getTime()));
        }

        property.setValue(protoProperty.getValue());
        property.setPivot(protoProperty.getPivot());
        property.setInterval(protoProperty.getInterval());
        property.setAlert(protoProperty.getAlert());
        property.setError(protoProperty.getError());
        if (protoProperty.getCreationDate() != null && !"".equals(protoProperty.getCreationDate())) {
            property.setCreationDate(fmt.parseDateTime(protoProperty.getCreationDate()));
        }
        if (protoProperty.getModifiedDate() != null && !"".equals(protoProperty.getModifiedDate())) {
            property.setModifiedDate(fmt.parseDateTime(protoProperty.getModifiedDate()));
        }

        property.setStoredDataFlag(protoProperty.getStoredDataFlag());
        property.setManualDataFlag(protoProperty.getManualDataFlag());
        property.setLabel(protoProperty.getLabel());

        return property;
    }

    public Device populateDeviceFromProto(com.magma.core.grpc_service.Device protoDevice) {
        try {
            if (protoDevice != null) {
                Device device = new Device();
                device.setId(protoDevice.getId());
                device.setName(protoDevice.getName());
                device.setGroup(protoDevice.getGroup());
                device.setKitId(protoDevice.getKitId());
                device.setDescription(protoDevice.getDescription());
                device.setNoOfSensors(protoDevice.getNoOfSensors());
                String[] stringArray = {};
                SensorCode[] sensorCodes = Arrays.stream(stringArray)
                        .map(str -> new SensorCode())
                        .toArray(SensorCode[]::new);
                device.setNoOfActuators(protoDevice.getNoOfActuators());
                device.setActuatorCodes(protoDevice.getActuatorCodesList().toArray(new ActuatorCode[0]));
                device.setInterval(protoDevice.getInterval());
                device.setIntervalMin(protoDevice.getIntervalMin());
                device.setMaintain(protoDevice.getMaintain());
                device.setPersistence(protoDevice.getPersistence());
                device.setOffline(protoDevice.getOffline());

                Map<Integer, Sensor> sensorMap = new HashMap<>();
                for (Map.Entry<Integer, com.magma.core.grpc_service.Sensor> entry : protoDevice.getSensorMapMap()
                        .entrySet()) {
                    Integer key = entry.getKey();
                    com.magma.core.grpc_service.Sensor protoSensor = entry.getValue();
                    Sensor sensor = new Sensor();
                    sensor.setId(protoSensor.getId());
                    sensor.setDeviceId(protoSensor.getDeviceId());
                    sensor.setNumber(protoSensor.getNumber()); // do impl other
                    sensorMap.put(key, sensor);
                }
                device.setSensorMap(sensorMap);

                Map<Integer, Map<Integer, Shift>> shiftMap = new HashMap<>();
                for (Map.Entry<Integer, com.magma.core.grpc_service.SensorMapHelper> entry : protoDevice.getShiftMap()
                        .entrySet()) {
                    Integer key = entry.getKey();
                    com.magma.core.grpc_service.SensorMapHelper protoShift = entry.getValue();
                    Map<Integer, Shift> innerMap = new HashMap<>();
                    // Populate innerMap if needed
                    shiftMap.put(key, innerMap);
                }
                device.setShiftMap(shiftMap);

                Map<Integer, Actuator> actuatorMap = new HashMap<>();
                for (Map.Entry<Integer, com.magma.core.grpc_service.Actuator> entry : protoDevice.getActuatorMapMap()
                        .entrySet()) {
                    Integer key = entry.getKey();
                    com.magma.core.grpc_service.Actuator protoActuator = entry.getValue();
                    Actuator actuator = new Actuator();
                    actuator.setId(protoActuator.getId());
                    actuator.setDeviceId(protoActuator.getDeviceId());
                    actuator.setNumber(protoActuator.getNumber()); // do impl other
                    actuatorMap.put(key, actuator);
                }
                device.setActuatorMap(actuatorMap);

                // Populate Geo
                Geo geo = new Geo();
                com.magma.core.grpc_service.Geo protoGeo = protoDevice.getGeo();
                geo.setId(protoGeo.getId());
                // Populate other fields in Geo similarly
                device.setGeo(geo);

                // Populate Battery
                Battery battery = new Battery();
                com.magma.core.grpc_service.Battery protoBattery = protoDevice.getBattery();
                battery.setLow(protoBattery.getLow());
                battery.setHigh(protoBattery.getHigh());
                // Populate other fields in Battery similarly
                device.setBattery(battery);

                // Populate Connectivity Matrix
                // Map<Connectivity, Map<String, String>> connectivityMatrix = new HashMap<>();
                // for (Map.Entry<Integer, com.magma.core.grpc_service.ConnectivityMatrix> entry : protoDevice
                //         .getConnectivityMatrix().entrySet()) {
                //     Connectivity key = Connectivity.valueOf(entry.getKey());
                //     Map<String, String> innerMap = new HashMap<>();
                //     // Populate innerMap if needed
                //     connectivityMatrix.put(key, innerMap);
                // }
                // device.setConnectivityMatrix(connectivityMatrix);

                // // Populate Configurations
                // Map<Configuration, String> configurations = new HashMap<>();
                // for (Map.Entry<Integer, com.magma.core.grpc_service.Configuration> entry : protoDevice
                //         .getConfigurations().entrySet()) {
                //     Configuration key = Configuration.valueOf(entry.getKey());
                //     String value = entry.getValue().getValue();
                //     configurations.put(key, value);
                // }
                // device.setConfigurations(configurations);

                device.setStatus(Status.valueOf(String.valueOf(protoDevice.getStatus())));
                device.setLastSeen(convertTimestampToDateTime(protoDevice.getLastSeen()));
                device.setCreationDate(convertTimestampToDateTime(protoDevice.getCreationDate()));
                device.setModifiedDate(convertTimestampToDateTime(protoDevice.getModifiedDate()));
                device.setBatchNumber(protoDevice.getBatchNumber());

                // Populate product if needed
                ProductData productData = new ProductData();
                com.magma.core.grpc_service.ProductData protoProductData = protoDevice.getProduct();
                productData.setDeviceId(protoProductData.getDeviceId());
                productData.setProductId(protoProductData.getProductId());
                productData.setProductType(protoProductData.getProductType());
                // Populate other fields in Product similarly
                device.setProduct(productData);

                // Populate deviceParameterConfiguration if needed
                DeviceParameterConfiguration deviceParameterConfiguration = new DeviceParameterConfiguration();
                com.magma.core.grpc_service.DeviceParameterConfiguration protoDeviceParameterConfiguration = protoDevice
                        .getDeviceParameterConfiguration();
                deviceParameterConfiguration.setDevice(protoDeviceParameterConfiguration.getDevice());
                deviceParameterConfiguration.setVersionNum(protoDeviceParameterConfiguration.getVersionNum());
                deviceParameterConfiguration.setServerIpAddress(protoDeviceParameterConfiguration.getServerIpAddress());
                // Populate other fields in DeviceParameterConfiguration similarly
                device.setDeviceParameterConfiguration(deviceParameterConfiguration);

                // Populate references if needed
                device.setReferences(protoDevice.getReferencesMap());

                // Populate codec if needed
                device.setMagmaCodecId(protoDevice.getMagmaCodecId());
                device.setLastRawData(protoDevice.getLastRawData());
                device.setReferenceName(protoDevice.getReferenceName());

                // Populate metaData if needed
                device.setMetaData(protoDevice.getMetaDataMap());

                // device.setCustomPublishTopic(protoDevice.getCustomPublishTopic());
                // device.setCustomRemoteTopic(protoDevice.getCustomRemoteTopic());
                // device.setCreatedBy(protoDevice.getCreatedBy());
                // device.setModifiedBy(protoDevice.getModifiedBy());

                return device;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Error while populateDeviceFromProto ", e);
        }
        return null;
    }

    public Action populateActionFromProto(com.magma.core.grpc_service.Action protoAction) {
        try {
            if (protoAction != null) {
                Action action = new Action();
                action.setId(protoAction.getId());
                action.setKitId(protoAction.getKitId());
                action.setNumber(protoAction.getNumber());
                action.setCode(ActuatorCode.valueOf(protoAction.getCode()));
                action.setTime(fmt.parseDateTime(protoAction.getTime()));
                action.setValue(protoAction.getValue());
                action.setCreationDate(fmt.parseDateTime(protoAction.getCreationDate()));
                action.setModifiedDate(fmt.parseDateTime(protoAction.getModifiedDate()));
                return action;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Error while populateActionFromProto ", e);
        }
        return null;
    }

    public List<Device> populateDeviceListFromProto(List<com.magma.core.grpc_service.Device> protoDeviceList) {
        List<Device> deviceList = new ArrayList<>();
        try {
            for (com.magma.core.grpc_service.Device protoDevice : protoDeviceList) {
                deviceList.add(populateDeviceFromProto(protoDevice));
            }
        } catch (Exception e) {
            LOGGER.error("Error while populateDeviceListFromProto ", e);
        }
        return deviceList;
    }

    public Offline populateOfflineFromProto(com.magma.core.grpc_service.Offline protoOffline) {
        try {
            if (protoOffline != null) {
                Offline offline = new Offline();
                offline.setId(protoOffline.getId());
                offline.setKitId(protoOffline.getKitId());
                offline.setStartTime(fmt.parseDateTime(protoOffline.getStartTime()));
                offline.setEndTime(fmt.parseDateTime(protoOffline.getEndTime()));
                offline.setCreationDate(fmt.parseDateTime(protoOffline.getCreationDate()));
                offline.setModifiedDate(fmt.parseDateTime(protoOffline.getModifiedDate()));
                return offline;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Error while populateOfflineFromProto ", e);
        }
        return null;
    }

    public List<Offline> populateOfflineListFromProto(List<com.magma.core.grpc_service.Offline> protoDeviceList) {
        List<Offline> deviceList = new ArrayList<>();
        try {
            for (com.magma.core.grpc_service.Offline protoDevice : protoDeviceList) {
                deviceList.add(populateOfflineFromProto(protoDevice));
            }
        } catch (Exception e) {
            LOGGER.error("Error while populateOfflineListFromProto ", e);
        }
        return deviceList;
    }

    public Alert populateAlertFromProto(com.magma.core.grpc_service.Alert protoAlert) {
        try {
            if (protoAlert != null) {
                Alert alert = new Alert();
                alert.setId(protoAlert.getId());
                alert.setDueToPrevious(protoAlert.getDueToPrevious());
                alert.setAlertSent(protoAlert.getAlertSent());
                return alert;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Error while populateAlertFromProto ", e);
        }
        return null;
    }

    public AlertLimit populateAlertLimitFromProto(com.magma.core.grpc_service.AlertLimit protoAlertLimit) {
        try {
            if (protoAlertLimit != null) {
                AlertLimit alertLimit = new AlertLimit();
                alertLimit.setId(protoAlertLimit.getId());
                alertLimit.setKitId(protoAlertLimit.getKitId());
                alertLimit.setPropertyNumber(protoAlertLimit.getPropertyNumber());
                alertLimit.setLow(protoAlertLimit.getLow());
                alertLimit.setHigh(protoAlertLimit.getHigh());
                alertLimit.setCurrentLevelPeriod(protoAlertLimit.getCurrentLevelPeriod());
                alertLimit.setNextLevelPeriod(protoAlertLimit.getNextLevelPeriod());
                //implement more
                return alertLimit;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Error while populateAlertFromProto ", e);
        }
        return null;
    }

    public List<AlertLimit> populateAlertLimitListFromProto(List<com.magma.core.grpc_service.AlertLimit> protoAlertLimitList) {
        List<AlertLimit> alertLimitList = new ArrayList<>();
        try {
            for (com.magma.core.grpc_service.AlertLimit protoAlertLimit : protoAlertLimitList) {
                alertLimitList.add(populateAlertLimitFromProto(protoAlertLimit));
            }
        } catch (Exception e) {
            LOGGER.error("Error while populateAlertLimitListFromProto ", e);
        }
        return alertLimitList;
    }

    public GeoType populateGeoTypeFromProto(com.magma.core.grpc_service.GeoType protoGeoType) {
        try {
            if (protoGeoType != null) {
                switch (protoGeoType.getGeoType()) {
                    case NONE:
                        return null;
                    case LBS:
                        return GeoType.LBS;
                    case RLL:
                        return GeoType.RLL;
                    case GPS:
                        return GeoType.GPS;
                    default:
                        return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Error populateErrorFromProto(com.magma.core.grpc_service.Error protoError) {
        try {
            if (protoError != null) {
                Error error = new Error();
                error.setId(protoError.getId());
                error.setKitId(protoError.getKitId());
                error.setPropertyNumber(protoError.getPropertyNumber());
                error.setStartTime(fmt.parseDateTime(protoError.getStartTime()));
                error.setEndTime(fmt.parseDateTime(protoError.getEndTime()));
                error.setCreationDate(fmt.parseDateTime(protoError.getCreationDate()));
                error.setModifiedDate(fmt.parseDateTime(protoError.getModifiedDate()));
                return error;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Error while populateErrorFromProto ", e);
        }
        return null;
    }

    public List<Kit> populateKitListFromProto(List<com.magma.core.grpc_service.Kit> protoKitList) {
        List<Kit> kitList = new ArrayList<>();
        try {
            for (com.magma.core.grpc_service.Kit protoKit : protoKitList) {
                kitList.add(populateKitFromProto(protoKit));
            }
        } catch (Exception e) {
            LOGGER.error("Error while populateKitListFromProto ", e);
        }
        return kitList;
    }

    public List<Property> populatePropertyListFromProto(List<com.magma.core.grpc_service.Property> protoPropertyList) {
        List<Property> propertyList = new ArrayList<>();
        try {
            for (com.magma.core.grpc_service.Property protoProperty : protoPropertyList) {
                propertyList.add(populatePropertyFromProto(protoProperty));
            }
        } catch (Exception e) {
            LOGGER.error("Error while populatePropertyListFromProto ", e);
        }
        return propertyList;
    }

    public List<Geo> populateGeoListFromProto(List<com.magma.core.grpc_service.Geo> protoGeoList) {
        List<Geo> geoList = new ArrayList<>();
        try {
            for (com.magma.core.grpc_service.Geo protoGeo : protoGeoList) {
                geoList.add(populateGeoFromProto(protoGeo));
            }
        } catch (Exception e) {
            LOGGER.error("Error while populateGeoListFromProto ", e);
        }
        return geoList;
    }

    public Geo populateGeoFromProto(com.magma.core.grpc_service.Geo protoGeo) {
        try {
            if (protoGeo != null) {
                Geo geo = new Geo();
                geo.setId(protoGeo.getId());
                geo.setKitId(protoGeo.getKitId());
                geo.setLat(protoGeo.getLat());
                geo.setLng(protoGeo.getLng());
                return geo;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Error while populateErrorFromProto ", e);
        }
        return null;
    }

    // populate proto

    public com.magma.core.grpc_service.Kit populateProtoKitEntity(Kit kit) {
        try {
            if (kit != null) {
                return com.magma.core.grpc_service.Kit.newBuilder()
                        .setId(kit.getId())
                        .setName(kit.getName())
                        .setDescription(kit.getDescription())
                        .setKitModelId(kit.getKitModelId())
                        // .setModel(com.magma.core.grpc_service.KitModel.newBuilder().build()) // need to generate KitModel object from the json string
                        .setDevices(com.magma.core.grpc_service.StringList.newBuilder().addAllStr(kit.getDevices()).build())
                        // .setDeviceMap(kit.getDeviceMap())
                        // .setOffsetMap(kit.getOffsetMap())
                        // .setPropertyMap(kit.getPropertyMap())
                        // .setShiftMap(kit.getShiftMap())
                        // .setActionMap(kit.getActionMap())
                        .setGeo(populateProtoGeoEntity(kit.getGeo()))
                        .setBattery(convertBatteryToGrpcBattery(kit.getBattery()))
                        .setMaintain(kit.getMaintain())
                        .setAlertLevel(kit.getAlertLevel())
                        .setInterval(kit.getInterval())
                        .setPersistence(kit.getPersistence())
                        .setOffline(kit.getOffline())
                        .setStatus(convertStatusToGrpcStatus(kit.getStatus()))
                        // .setAlerts(kit.getAlerts())
                        // .setMetaData(kit.getMetaData())
                        .setInputMethod(convertDataInputMethodToGrpcDataInputMethodT(kit.getInputMethod()))
                        .setLastSeen(convertDateTimeToString(kit.getLastSeen()))
                        .setCreationDate(convertDateTimeToString(kit.getCreationDate()))
                        .build();
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Error while populate Proto Kit Entity ", e);
        }
        return null;
    }

    public List<com.magma.core.grpc_service.Kit> populateProtoKitEntityList(List<Kit> kits) {
        List<com.magma.core.grpc_service.Kit> protoKits = new ArrayList<>();
        try {
            for (Kit kit : kits) {
                protoKits.add(populateProtoKitEntity(kit));
            }
        } catch (Exception e) {
            LOGGER.error("Error while populateProtoKitList ", e);
        }
        return protoKits;
    }

    public com.magma.core.grpc_service.Geo populateProtoGeoEntity(Geo geo) {
        try {
            if (geo != null) {
                return com.magma.core.grpc_service.Geo.newBuilder()
                        .setId(geo.getId())
                        .setKitId(geo.getKitId())
                        .setType(convertGeoTypeToGrpcGeoType(geo.getType()))
                        .setTime(convertDateTimeToString(geo.getTime()))
                        .setLat(geo.getLat())
                        .setLng(geo.getLng())
                        // .setRelativeLocation(geo.getRelativeLocation())
                        .build();
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Error while populate Proto Kit Entity ", e);
        }
        return null;
    }

    public List<com.magma.core.grpc_service.Geo> populateProtoGeoEntityList(List<Geo> geos) {
        List<com.magma.core.grpc_service.Geo> protoGeos = new ArrayList<>();
        try {
            for (Geo geo : geos) {
                protoGeos.add(populateProtoGeoEntity(geo));
            }
        } catch (Exception e) {
            LOGGER.error("Error while populateProtoGeoList ", e);
        }
        return protoGeos;
    }

    public com.magma.core.grpc_service.AlertLimit populateProtoAlertLimitEntity(AlertLimit alertLimit) {
        try {
            if (alertLimit != null) {
                return com.magma.core.grpc_service.AlertLimit.newBuilder()
                        .setId(alertLimit.getId())
                        .setKitId(alertLimit.getKitId())
                        .setPropertyNumber(alertLimit.getPropertyNumber())
                        .setLow(alertLimit.getLow())
                        .setHigh(alertLimit.getHigh())
                        .setCurrentLevelPeriod(alertLimit.getCurrentLevelPeriod())
                        .setNextLevelPeriod(alertLimit.getNextLevelPeriod())
                        .build();
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Error while populate Proto Kit Entity ", e);
        }
        return null;
    }

    public List<com.magma.core.grpc_service.AlertLimit> populateProtoAlertLimitEntityList(List<AlertLimit> alertLimits) {
        List<com.magma.core.grpc_service.AlertLimit> protoAlertLimits = new ArrayList<>();
        try {
            for (AlertLimit alertLimit : alertLimits) {
                protoAlertLimits.add(populateProtoAlertLimitEntity(alertLimit));
            }
        } catch (Exception e) {
            LOGGER.error("Error while populateProtoAlertLimitList ", e);
        }
        return protoAlertLimits;
    }

    public com.magma.core.grpc_service.Property populateProtoPropertyEntity(Property property) {
        try {
            if (property != null) {
                return com.magma.core.grpc_service.Property.newBuilder()
                        .setId(property.getId())
                        .setKitId(property.getKitId())
                        .setNumber(property.getNumber())
                        .setCode(property.getCode())
                        .setTime(convertDateTimeToString(property.getTime()))
                        .setValue(property.getValue())
                        .setPivot(property.getPivot())
                        .setInterval(property.getInterval())
                        .setAlert(property.getAlert())
                        .setError(property.getError())
                        .setCreationDate(convertDateTimeToString(property.getCreationDate()))
                        .setModifiedDate(convertDateTimeToString(property.getModifiedDate()))
                        .setStoredDataFlag(property.getStoredDataFlag())
                        .setManualDataFlag(property.getManualDataFlag())
                        .setLabel(property.getLabel())
                        .build();
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Error while populate Proto Kit Entity ", e);
        }
        return null;
    }

    public List<com.magma.core.grpc_service.Property> populateProtoPropertyEntityList(List<Property> properties) {
        List<com.magma.core.grpc_service.Property> protoProperties = new ArrayList<>();
        try {
            for (Property property : properties) {
                protoProperties.add(populateProtoPropertyEntity(property));
            }
        } catch (Exception e) {
            LOGGER.error("Error while populateProtoPropertyList ", e);
        }
        return protoProperties;
    }

    public com.magma.core.grpc_service.Device populateProtoDeviceEntity(Device device) {
        try {
            if (device != null) {
                return com.magma.core.grpc_service.Device.newBuilder()
                        .setId(device.getId())
                        .setName(device.getName())
                        .setGroup(device.getGroup())
                        .setKitId(device.getKitId())
                        .setDescription(device.getDescription())
                        .setNoOfSensors(device.getNoOfSensors())
                        // .addAllActuatorCodes(Arrays.asList(device.getActuatorCodes()))
                        .setNoOfActuators(device.getNoOfActuators())
                        .setInterval(device.getInterval())
                        .setIntervalMin(device.getIntervalMin())
                        .setMaintain(device.getMaintain())
                        .setPersistence(device.getPersistence())
                        .setOffline(device.getOffline())
                        // .setStatus(convertStatusToGrpcStatus(device.getStatus()))
                        // .setLastSeen(convertDateTimeToString(device.getLastSeen()))
                        // .setCreationDate(convertDateTimeToString(device.getCreationDate()))
                        // .setModifiedDate(convertDateTimeToString(device.getModifiedDate()))
                        .setBatchNumber(device.getBatchNumber())
                        .setProduct(com.magma.core.grpc_service.ProductData.newBuilder().build()) // need to generate ProductData object from the json string
                        .setDeviceParameterConfiguration(com.magma.core.grpc_service.DeviceParameterConfiguration.newBuilder().build()) // need to generate DeviceParameterConfiguration object from the json string
                        .putAllReferences(device.getReferences())
                        .setMagmaCodecId(device.getMagmaCodecId())
                        .setLastRawData(device.getLastRawData())
                        .setReferenceName(device.getReferenceName())
                        .putAllMetaData(device.getMetaData())
                        .build();
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Error while populate Proto Kit Entity ", e);
        }
        return null;
    }

    public List<com.magma.core.grpc_service.Device> populateProtoDeviceEntityList(List<Device> devices) {
        List<com.magma.core.grpc_service.Device> protoDevices = new ArrayList<>();
        try {
            for (Device device : devices) {
                protoDevices.add(populateProtoDeviceEntity(device));
            }
        } catch (Exception e) {
            LOGGER.error("Error while populateProtoDeviceList ", e);
        }
        return protoDevices;
    }

    public com.magma.core.grpc_service.Action populateProtoActionEntity(Action action) {
        try {
            if (action != null) {
                return com.magma.core.grpc_service.Action.newBuilder()
                        .setId(action.getId())
                        .setKitId(action.getKitId())
                        .setNumber(action.getNumber())
                        // .setCode(ActuatorCode.valueOf(action.getCode().toString()))
                        .setTime(convertDateTimeToString(action.getTime()))
                        .setValue(action.getValue())
                        .setCreationDate(convertDateTimeToString(action.getCreationDate()))
                        .setModifiedDate(convertDateTimeToString(action.getModifiedDate()))
                        .build();
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Error while populate Proto Kit Entity ", e);
        }
        return null;
    }

    public com.magma.core.grpc_service.Offline populateProtoOfflineEntity(Offline offline) {
        try {
            if (offline != null) {
                return com.magma.core.grpc_service.Offline.newBuilder()
                        .setId(offline.getId())
                        .setKitId(offline.getKitId())
                        .setStartTime(convertDateTimeToString(offline.getStartTime()))
                        .setEndTime(convertDateTimeToString(offline.getEndTime()))
                        .setCreationDate(convertDateTimeToString(offline.getCreationDate()))
                        .setModifiedDate(convertDateTimeToString(offline.getModifiedDate()))
                        .build();
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Error while populate Proto Kit Entity ", e);
        }
        return null;
    }

    public List<com.magma.core.grpc_service.Offline> populateProtoOfflineEntityList(List<Offline> offlines) {
        List<com.magma.core.grpc_service.Offline> protoOfflines = new ArrayList<>();
        try {
            for (Offline offline : offlines) {
                protoOfflines.add(populateProtoOfflineEntity(offline));
            }
        } catch (Exception e) {
            LOGGER.error("Error while populateProtoOfflineList ", e);
        }
        return protoOfflines;
    }

    public com.magma.core.grpc_service.Alert populateProtoAlertEntity(Alert alert) {
        try {
            if (alert != null) {
                return com.magma.core.grpc_service.Alert.newBuilder()
                        .setId(alert.getId())
                        .setDueToPrevious(alert.getDueToPrevious())
                        .setAlertSent(alert.getAlertSent())
                        .build();
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Error while populate Proto Kit Entity ", e);
        }
        return null;
    }

    public com.magma.core.grpc_service.Error populateProtoErrorEntity(Error error) {
        try {
            if (error != null) {
                return com.magma.core.grpc_service.Error.newBuilder()
                        .setId(error.getId())
                        .setKitId(error.getKitId())
                        .setPropertyNumber(error.getPropertyNumber())
                        .setStartTime(convertDateTimeToString(error.getStartTime()))
                        .setEndTime(convertDateTimeToString(error.getEndTime()))
                        .setCreationDate(convertDateTimeToString(error.getCreationDate()))
                        .setModifiedDate(convertDateTimeToString(error.getModifiedDate()))
                        .build();
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Error while populate Proto Kit Entity ", e);
        }
        return null;
    }


    // convertors
    private com.magma.core.grpc_service.GeoType convertGeoTypeToGrpcGeoType(GeoType sourceGeoType) {
        switch (sourceGeoType) {
            case LBS:
                return com.magma.core.grpc_service.GeoType.newBuilder()
                        .setGeoType(com.magma.core.grpc_service.GeoType.GeoTypeEnum.LBS)
                        .build();
            case RLL:
                return com.magma.core.grpc_service.GeoType.newBuilder()
                        .setGeoType(com.magma.core.grpc_service.GeoType.GeoTypeEnum.RLL)
                        .build();
            case GPS:
                return com.magma.core.grpc_service.GeoType.newBuilder()
                        .setGeoType(com.magma.core.grpc_service.GeoType.GeoTypeEnum.GPS)
                        .build();
            default:
                return com.magma.core.grpc_service.GeoType.newBuilder()
                        .setGeoType(com.magma.core.grpc_service.GeoType.GeoTypeEnum.NONE)
                        .build();
        }
    }

    private com.magma.core.grpc_service.Battery convertBatteryToGrpcBattery(Battery sourcBattery) {

        try {
            if (sourcBattery != null) {
                return com.magma.core.grpc_service.Battery.newBuilder()
                        .setLow(sourcBattery.getLow())
                        .build();
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Error while populate Proto Kit Entity ", e);
        }
        return null;
    }

    private int convertStatusToGrpcStatus(Status sourceStatus) {
        switch (sourceStatus) {
            case INITIAL:
                return com.magma.core.grpc_service.Status.StatusEnum.INITIAL.getNumber();
            case ACTIVE:
                return com.magma.core.grpc_service.Status.StatusEnum.ACTIVE.getNumber();
            case SUSPENDED:
                return com.magma.core.grpc_service.Status.StatusEnum.SUSPENDED.getNumber();
            case EXPIRED:
                return com.magma.core.grpc_service.Status.StatusEnum.EXPIRED.getNumber();
            case USED:
                return com.magma.core.grpc_service.Status.StatusEnum.USED.getNumber();
            case CANCELED:
                return com.magma.core.grpc_service.Status.StatusEnum.CANCELED.getNumber();
            default:
                return com.magma.core.grpc_service.Status.StatusEnum.NONE.getNumber();
        }
    }

    private int convertDataInputMethodToGrpcDataInputMethodT(DataInputMethod inputMethod) {
        switch (inputMethod) {
            case DEVICE:
                return 1;
            case MANUAL:
                return 2;
            case IMAGE:
                return 3;
            default:
                return 0;
        }
    }

    private String[] processSensorCodes(List<com.magma.core.grpc_service.ActuatorCode> sensorCodesList) {
        SensorCode[] sensorCodesArray = sensorCodesList.stream()
                .map(sensorCode -> {
                    SensorCode code = new SensorCode();
                    code.setCodeValue(sensorCode.toString());
                    return code;
                })
                .toArray(SensorCode[]::new);

        return Arrays.stream(sensorCodesArray)
                .map(sc -> sc.getCodeValue())
                .toArray(String[]::new);
    }

}
