package com.magma.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magma.core.configuration.MQTTConfiguration;
import com.magma.dmsdata.data.entity.Error;
import com.magma.dmsdata.data.entity.*;
import com.magma.core.data.repository.*;
import com.magma.dmsdata.data.support.Connectivity;
import com.magma.dmsdata.data.support.GeoType;
import com.magma.dmsdata.data.support.Operation;
import com.magma.dmsdata.data.support.Shift;
import com.magma.core.grpc.Properties;
import com.magma.core.grpc.*;
import com.magma.dmsdata.util.*;
import com.magma.util.MagmaTime;
import com.magma.util.MagmaUtil;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DataProcessorService {

    @Autowired
    KitRepository kitRepository;

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    SensorRepository sensorRepository;

    @Autowired
    PropertyRepository propertyRepository;

    @Autowired
    ActionRepository actionRepository;

    @Autowired
    AlertLimitRepository alertLimitRepository;

    @Autowired
    AlertRepository alertRepository;

    @Autowired
    MongoOperations mongoOperations;

    @Autowired
    KitNotificationService kitNotificationService;

    @Autowired
    KitLbsService kitLbsService;

    @Autowired
    GeoRepository geoRepository;

    @Autowired
    ErrorRepository errorRepository;

    @Autowired
    ActuatorRepository actuatorRepository;

    @Autowired
    DataTriggerService dataTriggerService;

    @Autowired
    MagmaCodecRepository magmaCodecRepository;

    @Autowired
    CompileCodeFileService compileCodeFileService;

    @Autowired
    RunDecoderFileService runDecoderFileService;

    @Value("${device.data.interval}")
    private Integer interval;

    private PredictConnectorGrpc.PredictConnectorBlockingStub stub;

    private ManagedChannel channel;

    @Autowired
    MQTTConfiguration.MqttGateway mqttGateway;


    @Value("${mqtt.pub.topic}")
    private String mqttPubTopic;

    @Value("${magma.front.pub.topic}")
    private String mqttFrontTopic;

    @Autowired
    PropertyImageRepository propertyImageRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(DataProcessorService.class);

    public void doHandle(String deviceId, String txt) {
        String tempDeviceId = deviceId;
        Device device = Optional.ofNullable(deviceRepository.findById(tempDeviceId).orElse(null))
                .orElseGet(() -> deviceRepository.findByCustomPublishTopicOrCustomRemoteTopic(tempDeviceId));

        if (device == null) {
            LOGGER.error("No Device Found with Device Id : {}", deviceId);
            return;
        }
        deviceId = device.getId();

        device.setLastRawData(txt);
        deviceRepository.save(device);  //storing raw data, can be used for debugging

        txt = handleCodec(device, txt);

        if (device.getGroup() != null) {
            LOGGER.debug("Group :IR: {}, Device : {}, Message : {}", device.getGroup(), deviceId, txt);
        } else {
            LOGGER.debug("Device : {}, Message : {}", deviceId, txt);
        }

        //DT:1231253|0-T:25.63,DT:1231322|0-T:26.06
        //DC|0-CT:10;1-CK:20
        //0-T:30;1-H:45
        if (!txt.contains("|")) {
            txt = txt.replaceAll("\\s", "");
            int x = StringUtils.countOccurrencesOf(txt, ";") + 1;
            while (txt.contains(",")) {
                txt = txt.replaceFirst(",", ";" + x + "-");
                x++;
            }
        }

        for (String tx : txt.split(",")) {
            DateTime tem = MagmaTime.now();
            String flag;
            //TODO:Have to Change this is Topic
            if (tx.contains("|")) {
                flag = "1";
                //DT:0|0-T:30
                //DC|0-CT:10
                //DT:1231253|0-T:25.63
                String[] data = tx.split("\\|");
                tx = data[1];

                String[] conf = data[0].split(":", 2);
                switch (conf[0].trim()) {
                    case "DT":
                        if (conf[1].equals("0")) {
                            if (device.getLastSeen() == null) {
                                LOGGER.debug("Can't Store Past data First : {}, Device : {}", tx, deviceId);
                                return;
                            }

                            Integer interval = device.getInterval();
                            while (device.getLastSeen().plusMinutes(interval).isAfter(tem)) {
                                LOGGER.debug("Exceed Current Time for Interval : {}, Device : {}", interval, deviceId);
                                interval /= 2;
                            }
                            tem = device.getLastSeen().plusMinutes(interval);
                            LOGGER.debug("Past Data Found : {}, Discovered Time : {}, Interval : {}, Device : {}", tx, tem, interval, deviceId);

                        } else {
                            if (conf[1].length() == 7) {
                                conf[1] = "0" + conf[1];
                            }
                            tem = MagmaTime.parsePast(conf[1]);
                            LOGGER.debug("Past Data Found : {}, Formatted Time : {}, Device : {}", tx, tem, deviceId);
                        }
                        break;

                    //ZT:04261803/22|0-T:-46.85;1-H:-6.00;2-IRO:0062/4095/-46.85;3-B:296;4-IT:55;5-SS:00
                    case "ZT":
                    case "ZZ":
                        String[] zt = conf[1].split("/");

                        if (zt[0].length() == 7) {
                            zt[0] = "0" + zt[0];
                        }
                        tem = MagmaTime.parsePast(zt[0]);
                        tem = tem.plusMinutes(15 * (22 - Integer.parseInt(zt[1])));
                        LOGGER.debug("Past Data Found : {}, Formatted Time : {}, Device : {}, ZT : {}", tx, tem, deviceId, conf[1]);
                        break;

                    case "RT":
                        tem = MagmaTime.parsePastWithSecondIST(conf[1]);
                        LOGGER.debug("Past Data Found : {}, Formatted Time : {}, Device : {}", tx, tem, deviceId);
                        break;

                    case "UTC":
                        tem = MagmaTime.parsePastWithSecondUTC(conf[1]);
                        LOGGER.debug("Past Data Found : {}, Formatted Time : {}, Device : {}", tx, tem, deviceId);
                        break;

                    case "DD":
                        tem = MagmaTime.parseISO8601(conf[1]);
                        LOGGER.debug("Past Data Found : {}, Formatted Time : {}, Device : {}", tx, tem, deviceId);
                        break;

                    case "DC":
                        LOGGER.debug("Configurations Received : {}, Device : {}", tx, deviceId);
                        for (String confData : tx.split(";")) {
                            String[] con = tx.split("-", 2);//1-CT:30
                            String[] tmp = con[1].split(":", 2); //CT:30

                            Configuration configuration = Configuration.valueOf(tmp[0]);
                            String stringValue = tmp[1];
                            device.getConfigurations().put(configuration, stringValue);
                            //TODO: Have to Validate and Sent if not Match
                        }
                        device.setLastSeen(tem);
                        deviceRepository.save(device);
                        return;
                    default:
                        LOGGER.debug("Some other format : {} Received : {}, Device : {}", conf[0], tx, deviceId);

                }
            } else {
                flag = "0"; //Real Time
            }
            DateTime time = tem;

            if (time.isAfter(MagmaTime.now())) {
                LOGGER.error("Future Date : {}, Device : {}", time, deviceId);
                return;
            }

            Map<Integer, Sensor> sensorMap = new HashMap<>();
            for (String sensorData : tx.split(";")) { //1-T:30;2-H:30

                String[] sens = sensorData.split("-", 2);//1-T:30
                String[] tmp = sens[1].split(":"); //T:30

                Integer sensorNumber = Integer.parseInt(sens[0]);
                SensorCode code = SensorCode.valueOf(tmp[0]);

                String stringValue = tmp[1];

                Sensor sensor = sensorRepository.save(new Sensor(deviceId, sensorNumber, code, time, stringValue, device.getShiftMap().get(sensorNumber), flag));
                sensorMap.put(sensorNumber, sensor);
            }

            device.getSensorMap().putAll(sensorMap);
            device.setLastSeen(time);
            deviceRepository.save(device);

            //TODO: Have to Use a Message Queue, And Separate Processing from Here
            Kit kit = kitRepository.findByDevices(deviceId);

            if (kit == null) {
                LOGGER.info("No Kit Found with Device Id : {}", deviceId);
                return;
            }
            LOGGER.debug("Device : {}, Kit : {}, Message : {}", deviceId, kit.getId(), txt);

            if (Boolean.TRUE.equals(kit.getMaintain())) {
                LOGGER.info("Kit In Maintain Mode : {}", kit);
                return;
            }

            Integer offset = kit.getOffsetMap().get(deviceId).getSensor();
            String kitId = kit.getId();
            KitModel kitModel = kit.getModel();


            //TODO: Have to run it in background
            if (kitModel.getLbsEnabled() != null && kitModel.getLbsEnabled()) {
                Geo geo = kitLbsService.findLbs(kit);
                if (geo != null) {
                    geo.setKitId(kitId);
                    geo.setType(GeoType.LBS);
                    kit.setGeo(geoRepository.save(geo));
                }
            }

            Map<Operation, List<Sensor>> categorizedMap = new HashMap<>();
            sensorMap.forEach((sensorNumber, sensor) -> {

                Integer ksNo = sensorNumber + offset;
                sensor.setNumber(ksNo);

                if (kitModel.getRealTimeSet().containsKey(ksNo)) {

                    LOGGER.debug("Kit has RealTime Map Code : {}, Sensor : {}", sensor.getCode(), ksNo);

                    Set<Operation> operations = kitModel.getRealTimeSet().get(ksNo);
                    operations.forEach(operation -> {
                        if (!categorizedMap.containsKey(operation)) {
                            categorizedMap.put(operation, new ArrayList<>());
                        }
                        categorizedMap.get(operation).add(sensor);
                    });
                }

            });


            categorizedMap.forEach((operation, sensors) -> {

                Integer propertyNumber = operation.getPropertyNumber();

                SensorCode propertyCode;
                switch (propertyNumber) {
                    case -10:
                        propertyCode = SensorCode.B;
                        break;

                    case -11:
                        propertyCode = SensorCode.BL;
                        break;

                    case -1:
                        propertyCode = SensorCode.L;
                        break;

                    default:
                        propertyCode = kitModel.getProperties()[propertyNumber];
                }

                Property property;
                Double value = -9999.0;
                Double pivot = 0.0;


                switch (operation.getAggregation()) {
                    case ANY:
                        for (Sensor sensor : sensors) {
                            double dec = decode(propertyCode, operation, sensor, kit);
                            if (dec != -9999) {
                                value = dec;
                                break;
                            }
                        }
                        break;

                    case AND:
                        double and = 1;
                        for (Sensor sensor : sensors) {
                            double dec = decode(propertyCode, operation, sensor, kit);
                            if (dec != -9999) {
                                and *= dec;
                                value = 0.0;
                            }
                        }
                        if (value == 0.0) {
                            value = and;
                        }
                        break;

                    case OR:
                        double or = 0;
                        for (Sensor sensor : sensors) {
                            double dec = decode(propertyCode, operation, sensor, kit);
                            if (dec != -9999) {
                                or += dec;
                                value = 0.0;
                            }
                        }
                        if (value == 0.0) {
                            value = (or == 0 ? 0.0 : 1.0);
                        }

                        break;

                    case MAX:
                        value = Double.MIN_VALUE;

                        for (Sensor sensor : sensors) {
                            double dec = decode(propertyCode, operation, sensor, kit);
                            if (dec != -9999 && dec > value) {
                                value = dec;
                            }
                        }
                        break;

                    case MIN:
                        value = Double.MAX_VALUE;

                        for (Sensor sensor : sensors) {
                            double dec = decode(propertyCode, operation, sensor, kit);
                            if (dec != -9999 && dec < value) {
                                value = dec;
                            }
                        }
                        break;

                    case AVG:
                        double sum = 0;
                        int times = 0;

                        for (Sensor sensor : sensors) {
                            double dec = decode(propertyCode, operation, sensor, kit);
                            if (dec != -9999) {
                                sum += dec;
                                times++;
                            }
                        }
                        if (times > 0) {
                            value = sum / times;
                        }
                        break;

                    case SUM:
                        double total = 0;

                        for (Sensor sensor : sensors) {
                            double dec = decode(propertyCode, operation, sensor, kit);
                            if (dec != -9999) {
                                total += dec;
                                value = 0.0;
                            }
                        }
                        if (total != 0) {
                            value = total;
                        }
                        break;

                    case PREDICT:
                        value = predict(propertyNumber, propertyCode, operation, sensors, kit);

                }

                if (kit.getShiftMap().containsKey(propertyNumber)) {
                    Shift shift = kit.getShiftMap().get(propertyNumber);
                    Double piv = shift.getMeta().get("pivot");
                    switch (shift.getOperation()) {
                        case ADD:
                            value += piv;
                            break;
                        case SUB:
                            value -= piv;
                            break;
                        case MUL:
                            value *= piv;
                            break;
                        case DIV:
                            value /= piv;
                            break;
                    }
                }


                switch (propertyCode) {
                    case L:
                        break;

                    case B:
                        if (value != -9999.0 && value != Double.MIN_VALUE && value != Double.MAX_VALUE) {
                            property = new Property(kitId, propertyNumber, propertyCode, time, value, pivot, flag, "0");
                            kit.getBattery().setReading(property);
                            dataTriggerService.triggerForProperty(property);
                            propertyRepository.save(property);
                        }
                        break;

                    case BL:
                        if (value != -9999.0) {
                            property = new Property(kitId, propertyNumber, propertyCode, time, value, pivot, flag, "0");
                            kit.getBattery().setReadingPercentage(property);
                            dataTriggerService.triggerForProperty(property);
                            propertyRepository.save(property);
                        }
                        break;

                    case RF:
                        if (value != -9999.0 && value != Double.MIN_VALUE && value != Double.MAX_VALUE) {

                            Map<String, Object> params = operation.getParams();

                            if (!params.containsKey("time")) {
                                params.put("time", "00:01");
                            }

                            DateTime to = MagmaTime.getThisDayTime(time, (String) params.get("time"));
                            DateTime from = to.minusHours(6);

                            Property last = propertyRepository.findByKitIdAndNumberAndTimeBetweenOrderByTimeDesc(
                                    kit.getId(), operation.getPropertyNumber(), from, to);

                            LOGGER.debug("Last Data to Pivot : {}", last);

                            if (last == null) {
                                LOGGER.error("Didn't have prior data to calculate RF");
                                pivot = value;
                                value = 0.0;
                            } else {
                                pivot = value;
                                value = value - last.getPivot();
                            }
                        }

                    default:
                        perform(kit, new Property(kitId, propertyNumber, propertyCode, time, value, pivot, flag, "0"));
                }
            });
            kit.setLastSeen(time);
            kitRepository.save(kit);
        }
    }

    public Double decode(SensorCode propertyCode, Operation operation, Sensor sensor, Kit kit) {

        boolean isMatch = sensor.getCode() == propertyCode;

        Double value;
        String stringValue = sensor.getValue();

        switch (sensor.getCode()) {

            case L:
                //653.145600/7951.340400/20190617110114.000
                //653161800/795134240/609100446
                String[] locTemp = stringValue.split("/");
                if (operation.getPropertyNumber() == -1 &&
                        operation.getSensorNumberList().contains(sensor.getNumber()) &&
                        locTemp.length == 3) {

                    if (locTemp[2].length() == 9) {
                        locTemp[2] = "0" + locTemp[2];
                    }

                    Geo geo;
                    if (locTemp[2].length() == 10) {
                        geo = new Geo(
                                MagmaTime.parsePastWithSecondUTC(locTemp[2]),
                                MagmaUtil.parseLatPast(locTemp[0]),
                                MagmaUtil.parseLngPast(locTemp[1])
                        );
                    } else {
                        geo = new Geo(
                                MagmaTime.parseGPS(locTemp[2]),
                                MagmaUtil.parseDegree(locTemp[0]),
                                MagmaUtil.parseDegree(locTemp[1])
                        );
                    }

                    if (geo.getLat() > 0 && geo.getLng() > 0) {
                        geo.setKitId(kit.getId());
                        geo.setType(GeoType.GPS);
                        kit.setGeo(geoRepository.save(geo));
                        LOGGER.debug("Kit : {}, GPS : {}", kit.getId(), kit.getGeo());
                        //kitRepository.save(kit);
                    }
                }
                return -9999.0;

            case RL:
                String[] locTem = stringValue.split("/");

                if (locTem.length >= 2) {
                    Geo geo = new Geo(MagmaTime.now(), Double.parseDouble(locTem[0]), Double.parseDouble(locTem[1]));

                    if (locTem.length > 2) {
                        Map<String, String> relativeLocation = new HashMap<>();
                        String[] rll = Arrays.copyOfRange(locTem, 2, locTem.length);
                        geo.setRelativeLocation(getRLL(rll, relativeLocation));
                    } else {
                        Property property = kit.getProperties().stream().filter(e -> e.getCode() == SensorCode.A).findFirst().orElse(null);

                        if (property != null && property.getValue() == 1) {
                            geo.setRelativeLocation(kit.getGeo().getRelativeLocation());
                        }
                    }

                    geo.setKitId(kit.getId());
                    geo.setType(GeoType.RLL);

                    kit.setGeo(geoRepository.save(geo));
                    return 1.0;
                }
                return -9999.0;

            case B:

                if (!isMatch) {
                    LOGGER.error("Sensor Code mismatch Property : {}, Sensor : {}, Sensor number : {}, kit : {}", propertyCode, sensor.getCode(), sensor.getNumber(), kit.getId());
                    return -9999.0;
                }

                value = shift(sensor.getShiftMap(), 0, Double.parseDouble(stringValue));

                Battery battery = kit.getBattery();
                if (battery == null) {
                    if (value > 0 && value < 4.5) {
                        battery = new Battery(0.0, 4.2);
                    } else {
                        battery = new Battery(240.0, 300.0);
                    }
                    kit.setBattery(battery);
                }

                if (value > 150 || value < 75) {
                    value = (value - battery.getLow()) / (battery.getHigh() - battery.getLow());
                    value = value < 0 ? 0.0 : value > 1 ? 1.0 : value;
                } else {
                    value = -1.0;
                }
                return (double) Math.round(value * 100);

            case BL:

                if (!isMatch) {
                    LOGGER.error("Sensor Code mismatch Property : {}, Sensor : {}, kit : {}", propertyCode, sensor.getCode(), kit.getId());
                    return -9999.0;
                }

                Battery batteryL = kit.getBattery();
                if (batteryL == null) {
                    kit.setBattery(new Battery());
                }

                String reading = stringValue.replace("%", "");
                return Double.parseDouble(reading);

            case T:
            case ST:

                if (!isMatch) {
                    LOGGER.error("Sensor Code mismatch Property : {}, Sensor : {}", propertyCode, sensor.getCode());
                    return -9999.0;
                }

                value = shift(sensor.getShiftMap(), 0, Double.parseDouble(stringValue));
                // -127, 80
                if (value < -40 || value > 180 || value == 85) {
                    value = -9999.0;
                }
                return value;
            case DS:

                if (!isMatch) {
                    LOGGER.error("Sensor Code mismatch Property : {}, Sensor : {}", propertyCode, sensor.getCode());
                    return -9999.0;
                }

                value = Double.parseDouble(stringValue);

                if (value != 0 && value != 1) {
                    value = -9999.0;
                }
                return value;

            case PHA:
                value = shift(sensor.getShiftMap(), 0, Double.parseDouble(stringValue));

                if (propertyCode == SensorCode.PH) {
                    return -0.0338 * value + 24.576;
                }

                LOGGER.error("Can't Get Property : {}, From Sensor : {}", propertyCode, sensor.getCode());
                return -9999.0;

            case MEA:
            case MEA0:
            case MEA1:
            case MEA3:
            case MEA4:
                //848.45/93.8383
                String[] meaTmp = stringValue.split("/");
                Double m = shift(sensor.getShiftMap(), 0, Double.parseDouble(meaTmp[0]));
                Double c = shift(sensor.getShiftMap(), 1, Double.parseDouble(meaTmp[1]));

                if (propertyCode == SensorCode.M) {
                    double res = (0.41275 * m - 0.038989 * c - 0.000470667 * m * m - 0.000032696 * m * c + 0.000070907 * c * c) / 2;
                    if (res > 0) {
                        return res;
                    }
                    return 0.0;

                } else if (propertyCode == SensorCode.CN) {
                    if (-0.122 + 0.0013027 * c + 0.00000023334 * c * c > 0) {
                        return -0.122 + 0.0013027 * c + 0.00000023334 * c * c;
                    }
                    return 0.0;
                }

                LOGGER.error("Can't Get Property : {}, From Sensor : {}", propertyCode, sensor.getCode());
                return -9999.0;

            case MEA2:
                //848.45/93.8383
                String[] meaTmp2 = stringValue.split("/");
                Double m2 = shift(sensor.getShiftMap(), 0, Double.parseDouble(meaTmp2[0]));
                Double c2 = shift(sensor.getShiftMap(), 1, Double.parseDouble(meaTmp2[1]));

                double res1 = 305.699421 - 0.3583815 * m2 - 0.00001 * m2 * m2;

                if (propertyCode == SensorCode.M) {
                    if (res1 > 0) {
                        return res1;
                    }
                    return 0.0;
                } else if (propertyCode == SensorCode.CN) {
                    if (-0.122 + 0.0013027 * c2 + 0.00000023334 * c2 * c2 > 0) {
                        return -0.122 + 0.0013027 * c2 + 0.00000023334 * c2 * c2;
                    }
                    return 0.0;
                }

                LOGGER.error("Can't Get Property : {}, From Sensor : {}", propertyCode, sensor.getCode());
                return -9999.0;

            //case MEA4:
            //848.45/93.8383
/*                String[] meaTmp3 = stringValue.split("/");
                Double ma1 = shift(sensor.getShiftMap(), 0, Double.parseDouble(meaTmp3[0]));
                Double c3 = shift(sensor.getShiftMap(), 1, Double.parseDouble(meaTmp3[1]));

                if (propertyCode == SensorCode.M) {
                    double m3 = (ma1 - 420) / (767 - 420);
                    if (m3 < 0) {
                        m3 = 0;
                    }
                    else if (m3 > 1) {
                        m3 = 1 ;
                    }
                    double res2 = 2.781 * m3* m3* m3* m3 - 7.557 * m3* m3* m3 + 7.181  * m3* m3 - 2.864* m3+ 0.4705;
                    if (res2 > 0) {
                        return res2;
                    }
                    return 0.0;

                } else if (propertyCode == SensorCode.CN) {
                    if (0.122 + 0.0013027 * c3 + 0.00000023334 * c3 * c3 > 0) {
                        return -0.122 + 0.0013027 * c3 + 0.00000023334 * c3 * c3;
                    }
                    return 0.0;
                }

                LOGGER.error("Can't Get Property : {}, From Sensor : {}", propertyCode, sensor.getCode());
                return -9999.0;*/

//            case MEA3:
//                //848.45/93.8383
//                String[] meaTmp1 = stringValue.split("/");
//                Double m1 = Double.parseDouble(meaTmp1[0]);
//                Double c1 = Double.parseDouble(meaTmp1[1]);
//
//                if (propertyCode == SensorCode.M) {
//                    if ((193.59727872 + (-0.43709299) * m1 + (-0.0448857352) * c1 + (0.000243294997) * (m1 * m1) + (-0.0000192464167) * c1 * m1 + (0.000068699783) * (c1 * c1)) > 0) {
//                        return (193.59727872 + (-0.43709299) * m1 + (-0.0448857352) * c1 + (0.000243294997) * (m1 * m1) + (-0.0000192464167) * c1 * m1 + (0.000068699783) * (c1 * c1));
//                    }
//                    return 0.0;
//                } else if (propertyCode == SensorCode.CN) {
//                    if ((0.00382289 + (0.0000769124044) * (c1) + (0.0000000211255851) * (c1 * c1)) > 0) {
//                        return (0.00382289 + (0.0000769124044) * (c1) + (0.0000000211255851) * (c1 * c1));
//                    }
//                    return 0.0;
//                }
//
//                LOGGER.error("Can't Get Property : {}, From Sensor : {}", propertyCode, sensor.getCode());
//                return -9999.0;

            case H:
            case M:

                if (!isMatch) {
                    LOGGER.error("Sensor Code mismatch Property : {}, Sensor : {}", propertyCode, sensor.getCode());
                    return -9999.0;
                }

                value = shift(sensor.getShiftMap(), 0, Double.parseDouble(stringValue));

                //0-110 range we are consider 100-110 also a valid with reduce to 100
                if (value < 0 || value > 110) {
                    value = -9999.0;
                } else if (value > 100) {
                    value = 100.0;
                }

                return value;

            case CRF:
                if (!isMatch && propertyCode != SensorCode.RF) {
                    LOGGER.error("Sensor Code mismatch Property : {}, Sensor : {}", propertyCode, sensor.getCode());
                    return -9999.0;
                }
                return shift(sensor.getShiftMap(), 0, Double.parseDouble(stringValue));

            case IRO:
                return calculateIRO(stringValue, kit);

            default:
                if (!isMatch) {
                    LOGGER.error("Sensor Code mismatch Property : {}, Sensor : {}", propertyCode, sensor.getCode());
                    return -9999.0;
                }

                return shift(sensor.getShiftMap(), 0, Double.parseDouble(stringValue));
        }

    }

    public double calculateIRO(String stringValue) {
        return calculateIRO(stringValue, null);
    }

    private double calculateIRO(String stringValue, Kit kit) {
        String[] values = stringValue.split("/");
        Double ARead_A1 = Double.valueOf(values[0]);
        Double ARead_A2 = Double.valueOf(values[1]);
        Double tempVal = Double.valueOf(values[2]);


        int Rx = 8200;  //fixed resistor attached in series to the sensor and ground...the same value repeated for all WM and Temp Sensor.
        long open_resistance = 35000; //check the open resistance value by replacing sensor with an open and replace the value here...this value might vary slightly with circuit components
        long short_resistance = 200; // similarly check short resistance by shorting the sensor terminals and replace the value here.
        long short_CB = 240, open_CB = 255;
        double SupplyV = 3.3;
        double WM1_CB = 0;
        double SenV10K = 0;
        double SenVTempC = 0;
        double WM1_Resistance = 0;

        //Remove after devices fix
        List<String> chr = Arrays.asList("AURACGW127", "AURACGW128", "AURACGW129", "AURACGW130");

        if (kit == null || !chr.contains(kit.getId())) {
            ARead_A1 = ((ARead_A1 / 4096) * SupplyV); //get the average of the readings in the first direction and convert to volts
            ARead_A2 = ((ARead_A2 / 4096) * SupplyV); //get the average of the readings in the second direction and convert to volts
        }

        double WM1_ResistanceA = (Rx * (SupplyV - ARead_A1) / ARead_A1); //do the voltage divider math, using the Rx variable representing the known resistor
        double WM1_ResistanceB = Rx * ARead_A2 / (SupplyV - ARead_A2);  // reverse
        WM1_Resistance = ((WM1_ResistanceA + WM1_ResistanceB) / 2);  //average the two directions and apply the calibration factor

        if (WM1_Resistance > 550.00) {

            if (WM1_Resistance > 8000.00) {
                WM1_CB = -2.246 - 5.239 * (WM1_Resistance / 1000.00) * (1 + .018 * (tempVal - 24.00)) - .06756 * (WM1_Resistance / 1000.00) * (WM1_Resistance / 1000.00) * ((1.00 + 0.018 * (tempVal - 24.00)) * (1.00 + 0.018 * (tempVal - 24.00)));
            } else if (WM1_Resistance > 1000.00) {
                WM1_CB = (-3.213 * (WM1_Resistance / 1000.00) - 4.093) / (1 - 0.009733 * (WM1_Resistance / 1000.00) - 0.01205 * (tempVal));
            } else {
                WM1_CB = ((WM1_Resistance / 1000.00) * 23.156 - 12.736) * (1.00 + 0.018 * (tempVal - 24.00));
            }

        } else {
            if (WM1_Resistance > 300.00) {
                WM1_CB = 0.00;
            }

            if (WM1_Resistance < 300.00 && WM1_Resistance >= short_resistance) {
                WM1_CB = short_CB; //240 is a fault code for sensor terminal short
            }
        }

        if (WM1_Resistance >= open_resistance) {
            WM1_CB = open_CB; //255 is a fault code for open circuit or sensor not present
        }
        if (WM1_Resistance < short_resistance) {
            WM1_CB = 250; //250 is a fault code not in the code
        }

        double valueIRO = (75 - Math.abs(WM1_CB));

        if (valueIRO < 0) {
            return 0;
        }
        return valueIRO;
    }

    private Double shift(Map<Integer, Shift> shiftMap, Integer number, Double value) {

        if (shiftMap == null) {
            return value;
        }

        Shift shift = shiftMap.get(number);
        if (shift == null) {
            return value;
        }

        Double piv = shift.getMeta().get("pivot");
        switch (shift.getOperation()) {
            case ADD:
                value += piv;
                break;
            case SUB:
                value -= piv;
                break;
            case MUL:
                value *= piv;
                break;
            case DIV:
                value /= piv;
                break;
        }
        return value;
    }

    public Map<String, String> getRLL(String[] locTem, Map<String, String> relativeLocation) {

        if (locTem.length > 2) {
            String[] rll = Arrays.copyOfRange(locTem, 2, locTem.length);
            getRLL(rll, relativeLocation);

            String[] rllC = Arrays.copyOfRange(locTem, 0, 2);
            Map<String, String> rl = getRLL(rllC, relativeLocation);

            relativeLocation.putAll(rl);
            return relativeLocation;
        }

        if (locTem.length == 2) {
            relativeLocation.put(locTem[0], locTem[1]);
        }
        return relativeLocation;
    }

    private void perform(Kit kit, Property property) {
        String kitId = kit.getId();
        Integer propertyNumber = property.getNumber();
        DateTime time = property.getTime();
        Double value = property.getValue();

        property.setAlert(false);
        property.setError(false);
        kit.getPropertyMap().put(propertyNumber, property);

        if (value != -9999.0 && value != Double.MIN_VALUE && value != Double.MAX_VALUE) {

            boolean timeElapsed = false;
            AlertLimit previous = null;
            for (int level = 1; level <= kit.getAlertLevel(); level++) {

                AlertLimit alertLimit = alertLimitRepository.
                        findByKitIdAndPropertyNumberAndLevel(kitId, propertyNumber, level);

                if (kit.getAlerts().containsKey(level) && kit.getAlerts().get(level) &&
                        (timeElapsed || (alertLimit != null && alertLimit.getStatus() == AlertStatus.ACTIVE && !alertLimit.checkValidity(value)))) {

                    LOGGER.debug("Kit Alert : {} Triggered Limit : {}, Time: {}, Value : {}", kit.getAlerts(), alertLimit, timeElapsed, value);

                    property.setAlert(true);
                    Integer diff = kit.getInterval() / 2;

                    Alert alert = alertRepository
                            .findByAlertLimitKitIdAndAlertLimitPropertyNumberAndAlertLimitLevelAndEndTimeGreaterThanOrderByStartTimeDesc(
                                    kitId, propertyNumber, level, time.minusMinutes(kit.getInterval()));

                    if (alert == null) {
                        if (!timeElapsed) {
                            alert = new Alert(alertLimit, time.minusMinutes(diff), time.plusMinutes(diff));
                        } else {
                            alert = new Alert(previous, time.minusMinutes(diff), time.plusMinutes(diff));
                            alert.setDueToPrevious(true);
                        }
                    } else {
                        alert.setAlertLimit(alertLimit);
                        alert.setEndTime(time.plusMinutes(diff));
                    }


                    alert.addMeta("geo", kit.getGeo());
                    alert = alertRepository.save(alert);
                    if (timeElapsed || alertLimit.getCurrentLevelPeriod() == null || alert.getTime().getStandardMinutes() > alertLimit.getCurrentLevelPeriod()) {
                        try {
                            kitNotificationService.sendAlert(kit, alert);

                            alert.setAlertSent(true);
                            alertRepository.save(alert);

                        } catch (Exception e) {
                            LOGGER.error("Exception Occurred :{} ", e.getStackTrace());
                        }
                    }

                    //TODO: msg modifications for timeElapsed
                    if (alertLimit != null && alertLimit.getNextLevelPeriod() != null
                            && alert.getTime().getStandardMinutes() > alertLimit.getNextLevelPeriod()) {
                        timeElapsed = true;
                        alertLimit.setLevel(alertLimit.getLevel() + 1);
                        previous = alertLimit;
                    }
                } else {
                    timeElapsed = false;

                    //TODO need to check this
                    if (kit.getAlerts().containsKey(level) && kit.getAlerts().get(level) && ((alertLimit != null && alertLimit.getStatus() == AlertStatus.ACTIVE))) {
                        kitNotificationService.sendAlertOff(kit, null);
                    }
                }
            }
            dataTriggerService.triggerForProperty(property);
            propertyRepository.save(property);

        } else {

            property.setError(true);

            Error error = errorRepository.
                    findByKitIdAndPropertyNumberAndEndTimeGreaterThanOrderByStartTimeDesc(kit.getId(), propertyNumber, time.minusMillis(kit.getInterval()));

            if (error == null) {
                error = new Error(kit.getId(), propertyNumber, kit.getModifiedDate(), time.minusMillis(kit.getInterval() / 2));
            } else {
                error.setEndTime(time.plusMillis(kit.getInterval() / 2));
            }
            error = errorRepository.save(error);
            try {
                kitNotificationService.sendError(kit, error);
            } catch (Exception e) {
                LOGGER.error("Exception Occurred :{} ", e.getStackTrace());
            }
            property.setError(true);
        }

        kit.getPropertyMap().put(propertyNumber, property);
    }

    Double predict(Integer propertyNumber, SensorCode propertyCode, Operation operation, List<Sensor> sensors, Kit kit) {

        LOGGER.debug("Prediction Started For : {}, with Sensors : {}", propertyCode, sensors);
        List<SensorCode> mea = Arrays.asList(SensorCode.MEA, SensorCode.MEA0, SensorCode.MEA1, SensorCode.MEA2, SensorCode.MEA3, SensorCode.MEA4);
        List<SensorCode> temps = Arrays.asList(SensorCode.T, SensorCode.ST);

        //TODO: should Have an order in Sensors eg T then CNA
        Sensor sensorMEA = sensors.stream().filter(sensor -> mea.contains(sensor.getCode()))
                .findFirst().orElse(null);

        String[] meaTmp1 = sensorMEA.getValue().split("/");
        Float m1 = shift(sensorMEA.getShiftMap(), 0, Double.parseDouble(meaTmp1[0])).floatValue();
        Float c1 = shift(sensorMEA.getShiftMap(), 1, Double.parseDouble((meaTmp1[1]))).floatValue();
        String soilType = "normal";

        if (kit.getMetaData() != null && kit.getMetaData().containsKey("soilType")) {
            soilType = kit.getMetaData().get("soilType");
        }

        Sensor tem = sensors.stream().filter(sensor -> temps.contains(sensor.getCode()))
                .findFirst().orElse(null);
        float f;
        if (tem == null) {
            f = (float) 0.0;
        } else {
            f = shift(tem.getShiftMap(), 0, Double.parseDouble(tem.getValue())).floatValue();
        }

        try {
            switch (propertyCode) {
                case M:
                    Properties responseM = stub.predictMoisture(Sensors.newBuilder()
                            .setSoiltemperature(f)
                            .setMoisture(m1)
                            .setConductivity(c1)
                            .setSoiltype(soilType)
                            .setIdentifier(propertyNumber + kit.getId())
                            .build());
                    LOGGER.debug("Prediction Response : {}", responseM.getProperty());
                    return 100.0 * responseM.getProperty();

                case CN:
                    Properties responseCN = stub.predictConductivity(Sensors.newBuilder()
                            .setSoiltemperature(f)
                            .setMoisture(m1)
                            .setConductivity(c1)
                            .setSoiltype(soilType)
                            .build());
                    LOGGER.debug("Prediction Response : {}", responseCN.getProperty());
                    return (double) responseCN.getProperty();

                default:
                    LOGGER.debug("Some other Property Prediction Started");
            }
        } catch (StatusRuntimeException e) {
            LOGGER.error("Exception Got in predict : ", e);
        }

        return -9999.0;
    }

    public void doHandleActuators(String deviceId, String txt) {

        Kit kit = kitRepository.findByDevices(deviceId);

      /*  if (kit == null) {
            try {
                deviceId = deviceParameterConfigurationRepository.findByImeiNumber(deviceId).getDevice();
                kit = kitRepository.findByDevices(deviceId);
            } catch (Exception e) {
                LOGGER.error("No Kit Found with Device Id : {}", deviceId);
                return;
            }

        }*/
        if (kit == null) {
            //TODO: Have to Store Device Data Even Kit Not Created
            LOGGER.error("No Kit Found with Device Id : {}", deviceId);
            return;
        }

        DateTime now = MagmaTime.now();
        Integer offset = kit.getOffsetMap().get(deviceId).getActuator();
        String kitId = kit.getId();
        KitModel kitModel = kit.getModel();

        Device device = deviceRepository.findById(deviceId).orElse(null);

        if (device.getGroup() != null) {
            LOGGER.debug("Group :IR: {}, Kit : {}, Device : {}, Message : {}", device.getGroup(), kitId, deviceId, txt);

        } else {
            LOGGER.debug("Kit : {}, Device : {}, Message : {}", kitId, deviceId, txt);
        }

        //0-T:30,H:50 TODO:Have to do this in another server
        txt = txt.replaceAll("\\s", "");
        int x = StringUtils.countOccurrencesOf(txt, ";") + 1;
        while (txt.contains(",")) {
            txt = txt.replaceFirst(",", ";" + x + "-");
            x++;
        }

        for (String actuatorData : txt.split(";")) { //0-V:0;1-S:1

            String[] sens = actuatorData.split("-", 2);//0-V:0
            String[] tmp = sens[1].split(":"); //V:0

            Integer actuatorNumber = Integer.parseInt(sens[0]);
            ActuatorCode code = ActuatorCode.valueOf(tmp[0]);
            String stringValue = tmp[1];

            Actuator actuator = actuatorRepository.save(new Actuator(deviceId, actuatorNumber, code, now, stringValue));

            Integer ksNo = actuatorNumber + offset;
            //actuator.setNumber(ksNo);

            if (!kitModel.getActions()[ksNo].equals(code)) {
                if (device.getGroup() != null) {
                    LOGGER.debug("Group :IR: {}, Device : {}, Invalid data : {}", device.getGroup(), deviceId, txt);
                } else {
                    LOGGER.error("Invalid data : {}", txt);
                }
                return;
            }

            if (kit.getMaintain() == null || !kit.getMaintain()) {

                LOGGER.debug("Kit has RealTime Map Code : {}, Actuator : {}", code, ksNo);

                //TODO: Have to Find reserved Actuator of Kit
                Action action;

                switch (code) {
                    case S:
                    case M:
                    default:
                        action = new Action(kitId, ksNo, code, now, Double.parseDouble(stringValue));
                        dataTriggerService.triggerForAction(action);
                        kit.getActionMap().put(ksNo, action);
                        actionRepository.save(action);
                }

                kit.setLastSeen(now);
                kitRepository.save(kit);
            }
        }
    }

    public void doRegression(List<Float> points, Integer predict, Integer deg) {
        RegressionOutput response = stub.doRegression(RegressionInput.newBuilder()
                .setDeg(deg)
                .setPredict(predict)
                .addAllPoints(points)
                .build());

        LOGGER.debug("Trained List : {}, Predict List : {}", response.getTrainedList(), response.getPredictedList());
    }

    @PostConstruct
    private void initiate() {
        channel = ManagedChannelBuilder.forAddress("localhost", 50055)
                .usePlaintext()
                .build();
        stub = PredictConnectorGrpc.newBlockingStub(channel);
    }

    public String handleCodec(Device device, String rawTxt) {
        LOGGER.debug("handleCodec for Device : {}, Message : {}", device.getId(), rawTxt);

        MagmaCodec magmaCodec = magmaCodecRepository.findById(device.getMagmaCodecId()).orElse(null);

        if (magmaCodec != null) {

            Map<String, Object> result = new HashMap<>();

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(rawTxt);

                result = objectMapper.convertValue(jsonNode, Map.class);

            } catch (Exception e) {
                e.printStackTrace();
            }


            String fileName = magmaCodec.getDecoderFileName();
            String fileContent = magmaCodec.getDecoderFileContent();

            Boolean isCompiledSucessfully = false;
            try {
                isCompiledSucessfully = compileCodeFileService.compileCodeFile(fileName, fileContent);
            } catch (Exception e) {
                LOGGER.debug(e.toString());
            }
            if (isCompiledSucessfully) {
                try {
                    if (!result.isEmpty()) {
                        LOGGER.debug("Decoder file input: " + result);
                        rawTxt = runDecoderFileService.runDecoderFile(fileName, fileContent, result);
                        LOGGER.debug("Decoder file output: " + rawTxt);
                    } else {
                        LOGGER.debug("Decoder file input: " + rawTxt);
                        rawTxt = runDecoderFileService.runDecoderFile(fileName, fileContent, rawTxt);
                        LOGGER.debug("Decoder file output: " + rawTxt);
                    }
                } catch (Exception e) {
                    LOGGER.error("Error compiling and running codec file: " + e.getMessage());
                }
            }

        }

        return rawTxt;
    }
}
