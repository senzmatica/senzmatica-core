package com.magma.core.grpc_service.helper;

import com.magma.dmsdata.data.entity.*;
import com.magma.dmsdata.data.entity.Error;

import com.magma.dmsdata.data.support.Arithmetic;
import com.magma.dmsdata.data.support.DeviceParameterConfiguration;
import com.magma.dmsdata.data.support.GeoType;
import com.magma.dmsdata.data.support.Offset;
import com.magma.dmsdata.data.support.Shift;
import com.magma.dmsdata.util.ActuatorCode;
import com.magma.dmsdata.util.DataInputMethod;

import com.magma.util.Status;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProtoEntityPopulatingHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtoEntityPopulatingHelper.class);
    DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

    private static DateTime convertTimestampToDateTime(com.google.protobuf.Timestamp timestamp) {
        return new DateTime(timestamp.getSeconds() * 1000L + timestamp.getNanos() / 1000000L, DateTimeZone.UTC);
    }

    private static String convertDateTimeToString(DateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        return formatter.print(dateTime);
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

    public String populateSensorCodeFromProto(com.magma.core.grpc_service.SensorCode protoSensorCode) {
        try {
            if (protoSensorCode != null) {
                switch (protoSensorCode.getSensorCode()) {
                    case S:
                        return "S";
                    case T:
                        return "T";
                    case H:
                        return "H";
                    case M:
                        return "M";
                    case MA:
                        return "MA";
                    case MEA:
                        return "MEA";
                    case MEA0:
                        return "MEA0";
                    case MEA1:
                        return "MEA1";
                    case MEA2:
                        return "MEA2";
                    case MEA3:
                        return "MEA3";
                    case MEA4:
                        return "MEA4";
                    case IRO:
                        return "IRO";
                    case N:
                        return "N";
                    case K:
                        return "K";
                    case V:
                        return "V";
                    case W:
                        return "W";
                    case G:
                        return "G";
                    case B:
                        return "B";
                    case BC:
                        return "BC";
                    case RA:
                        return "RA";
                    case BL:
                        return "BL";
                    case RSSI:
                        return "RSSI";
                    case NSR:
                        return "NSR";
                    case LPG:
                        return "LPG";
                    case D:
                        return "D";
                    case R:
                        return "R";
                    case O3:
                        return "O3";
                    case CO:
                        return "CO";
                    case DS:
                        return "DS";
                    case LI:
                        return "LI";
                    case LIA:
                        return "LIA";
                    case LIA1:
                        return "LIA1";
                    case CN:
                        return "CN";
                    case CNA:
                        return "CNA";
                    case PHA:
                        return "PHA";
                    case PH:
                        return "PH";
                    case CS:
                        return "CS";
                    case E:
                        return "E";
                    case L:
                        return "L";
                    case RL:
                        return "RL";
                    case HB:
                        return "HB";
                    case X:
                        return "X";
                    case Y:
                        return "Y";
                    case RF:
                        return "RF";
                    case CRF:
                        return "CRF";
                    case EV:
                        return "EV";
                    case WD:
                        return "WD";
                    case WS:
                        return "WS";
                    case SI:
                        return "SI";
                    case ST:
                        return "ST";
                    case P:
                        return "P";
                    case WI:
                        return "WI";
                    case CT:
                        return "CT";
                    case CTD:
                        return "CTD";
                    case YC:
                        return "YC";
                    case PS:
                        return "PS";
                    case IT:
                        return "IT";
                    case SS:
                        return "SS";
                    case CF:
                        return "CF";
                    case RT:
                        return "RT";
                    case LS:
                        return "LS";
                    case CE_N:
                        return "CE_N";
                    case CE_P:
                        return "CE_P";
                    case CE_K:
                        return "CE_K";
                    case GN:
                        return "GN";
                    case RD:
                        return "RD";
                    case YW:
                        return "YW";
                    case C:
                        return "C";
                    case A:
                        return "A";
                    case ID_MSG:
                        return "ID_MSG";
                    case ID_IV:
                        return "ID_IV";
                    case AP:
                        return "AP";
                    case CP_A:
                        return "CP_A";
                    case CP_B:
                        return "CP_B";
                    case CP_C:
                        return "CP_C";
                    case VP_AN:
                        return "VP_AN";
                    case VP_BN:
                        return "VP_BN";
                    case VP_CN:
                        return "VP_CN";
                    case AC:
                        return "AC";
                    case LF:
                        return "LF";
                    case AC_AP:
                        return "AC_AP";
                    case AC_RP:
                        return "AC_RP";
                    case AC_PF:
                        return "AC_PF";
                    case AC_E:
                        return "AC_E";
                    case DC_C:
                        return "DC_C";
                    case DC_V:
                        return "DC_V";
                    case DC_P:
                        return "DC_P";
                    case OS:
                        return "OS";
                    case V_PCC:
                        return "V_PCC";
                    case S_PV:
                        return "S_PV";
                    case S_ECP:
                        return "S_ECP";
                    case CC:
                        return "CC";
                    case T_RPI:
                        return "T_RPI";
                    case T_C:
                        return "T_C";
                    case S_D:
                        return "S_D";
                    case S_M:
                        return "S_M";
                    case S_U:
                        return "S_U";
                    case EF:
                        return "EF";
                    case N_ID:
                        return "N_ID";
                    case S_ID:
                        return "S_ID";
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

    public com.magma.core.grpc_service.SensorCode populateProtoSensorCode(String sensorCodeEnum) {
        try {
            if (sensorCodeEnum != null) {
                switch (sensorCodeEnum) {
                    case "S":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.S).build();
                    case "T":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.T).build();
                    case "H":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.H).build();
                    case "M":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.M).build();
                    case "MA":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.MA).build();
                    case "MEA":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.MEA).build();
                    case "MEA0":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.MEA0).build();
                    case "MEA1":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.MEA1).build();
                    case "MEA2":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.MEA2).build();
                    case "MEA3":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.MEA3).build();
                    case "MEA4":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.MEA4).build();
                    case "IRO":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.IRO).build();
                    case "N":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.N).build();
                    case "K":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.K).build();
                    case "V":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.V).build();
                    case "W":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.W).build();
                    case "G":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.G).build();
                    case "B":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.B).build();
                    case "BC":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.BC).build();
                    case "RA":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.RA).build();
                    case "BL":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.BL).build();
                    case "RSSI":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.RSSI).build();
                    case "NSR":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.NSR).build();
                    case "LPG":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.LPG).build();
                    case "D":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.D).build();
                    case "R":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.R).build();
                    case "O3":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.O3).build();
                    case "CO":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.CO).build();
                    case "DS":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.DS).build();
                    case "LI":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.LI).build();
                    case "LIA":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.LIA).build();
                    case "LIA1":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.LIA1).build();
                    case "CN":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.CN).build();
                    case "CNA":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.CNA).build();
                    case "PHA":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.PHA).build();
                    case "PH":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.PH).build();
                    case "CS":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.CS).build();
                    case "E":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.E).build();
                    case "L":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.L).build();
                    case "RL":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.RL).build();
                    case "HB":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.HB).build();
                    case "X":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.X).build();
                    case "Y":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.Y).build();
                    case "RF":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.RF).build();
                    case "CRF":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.CRF).build();
                    case "EV":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.EV).build();
                    case "WD":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.WD).build();
                    case "WS":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.WS).build();
                    case "SI":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.SI).build();
                    case "ST":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.ST).build();
                    case "P":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.P).build();
                    case "WI":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.WI).build();
                    case "CT":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.CT).build();
                    case "CTD":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.CTD).build();
                    case "YC":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.YC).build();
                    case "PS":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.PS).build();
                    case "IT":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.IT).build();
                    case "SS":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.SS).build();
                    case "CF":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.CF).build();
                    case "RT":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.RT).build();
                    case "LS":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.LS).build();
                    case "CE_N":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.CE_N).build();
                    case "CE_P":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.CE_P).build();
                    case "CE_K":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.CE_K).build();
                    case "GN":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.GN).build();
                    case "RD":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.RD).build();
                    case "YW":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.YW).build();
                    case "C":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.C).build();
                    case "A":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.A).build();
                    case "ID_MSG":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.ID_MSG).build();
                    case "ID_IV":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.ID_IV).build();
                    case "AP":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.AP).build();
                    case "CP_A":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.CP_A).build();
                    case "CP_B":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.CP_B).build();
                    case "CP_C":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.CP_C).build();
                    case "VP_AN":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.VP_AN).build();
                    case "VP_BN":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.VP_BN).build();
                    case "VP_CN":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.VP_CN).build();
                    case "AC":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.AC).build();
                    case "LF":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.LF).build();
                    case "AC_AP":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.AC_AP).build();
                    case "AC_RP":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.AC_RP).build();
                    case "AC_PF":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.AC_PF).build();
                    case "AC_E":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.AC_E).build();
                    case "DC_C":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.DC_C).build();
                    case "DC_V":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.DC_V).build();
                    case "DC_P":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.DC_P).build();
                    case "OS":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.OS).build();
                    case "V_PCC":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.V_PCC).build();
                    case "S_PV":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.S_PV).build();
                    case "S_ECP":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.S_ECP).build();
                    case "CC":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.CC).build();
                    case "T_RPI":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.T_RPI).build();
                    case "T_C":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.T_C).build();
                    case "S_D":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.S_D).build();
                    case "S_M":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.S_M).build();
                    case "S_U":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.S_U).build();
                    case "EF":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.EF).build();
                    case "N_ID":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.N_ID).build();
                    case "S_ID":
                        return com.magma.core.grpc_service.SensorCode.newBuilder().setSensorCode(com.magma.core.grpc_service.SensorCode.SensorCodeEnum.S_ID).build();
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
        String[] sensorCodesArray = sensorCodesList.stream()
                .map(sensorCode -> sensorCode.toString()) // Assuming ActuatorCode has a suitable toString() method
                .toArray(String[]::new);
    
        return sensorCodesArray;
    }    

}
