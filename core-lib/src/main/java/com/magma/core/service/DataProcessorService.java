package com.magma.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magma.core.configuration.MQTTConfiguration;
import com.magma.core.data.dto.PropertyDTO;
import com.magma.core.data.entity.Error;
import com.magma.core.data.entity.*;
import com.magma.core.data.repository.*;
import com.magma.core.data.support.Connectivity;
import com.magma.core.data.support.GeoType;
import com.magma.core.data.support.Operation;
import com.magma.core.data.support.Shift;
import com.magma.core.grpc.Properties;
import com.magma.core.grpc.*;
import com.magma.core.util.*;
import com.magma.util.MagmaTime;
import com.magma.util.MagmaUtil;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class DataProcessorService {

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

    @Autowired
    DeviceMaintenanceRepository deviceMaintenanceRepository;

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

    public void doHandleConfiguration(String deviceId, String txt) {
        Device device = deviceRepository.findOne(deviceId);

        if (device == null) {
            LOGGER.error("No Device Found with Device Id : {}", deviceId);
            return;
        }

        LOGGER.debug("Config Response found Device : {}, Message : {}", deviceId, txt);

        if (device.getConnectivityMatrix().containsKey(Connectivity.WIFI)) {
            Map<String, String> wifi = device.getConnectivityMatrix().get(Connectivity.WIFI);
            if (txt.contains("GET:1")) {
                wifi.put("status", "success");
                wifi.remove("cmd");
                mqttGateway.send(mqttFrontTopic + deviceId + "/C", "Successfully Configured");

            } else if (txt.contains("GET:0")) {
                if (wifi.containsKey("cmd")) {
                    String cmd = wifi.remove("cmd");
                    LOGGER.debug("Trying to Connect again Device : {}, CMD : {}", deviceId, cmd);
                    mqttGateway.send(mqttPubTopic + deviceId + "/C", true, cmd);

                } else {
                    wifi.put("status", "Error in Set Parameter Please try Again");
                    mqttGateway.send(mqttFrontTopic + deviceId + "/C", "Error in Set Parameter Please try Again");

                }
            }
            deviceRepository.save(device);
        }
    }

    public void doHandle(String deviceIdOrName, String txt, String timeHttp){
        String combinedTxt = "ZZ:" + timeHttp + "/+00|" + txt;
        doHandle(deviceIdOrName, combinedTxt);
    }

    public void  doHandle(String deviceIdOrName, String txt) {
        boolean mapProperties = true;
        Map<Integer, DeviceMaintenance> deviceMaintenanceMap = new HashMap<>();

        Device device = deviceRepository.findByIdOrName(deviceIdOrName);

        if (device == null) {
            LOGGER.error("No Device Found with Device Id : {}", deviceIdOrName);
            return;
        }
        String deviceId = device.getId();

        device.setLastRawData(txt);
        deviceRepository.save(device);  //storing raw data, can be used for debugging

        DateTime azureTime = null;
        if(txt.contains("enqueuedTime")){
            String enqueuedTime = txt.substring(txt.indexOf("enqueuedTime\":\"") + 15, txt.indexOf("\",\"CombinedData"));

            DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
            DateTime utcTime = formatter.parseDateTime(enqueuedTime);
            azureTime = utcTime.withZone(DateTimeZone.forID("Asia/Colombo"));
        }

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
                String code = tmp[0];

                String stringValue = tmp[1];

                LOGGER.debug("code: {}, sens stringValue: {}, ", code, stringValue);
                switch (code) {
                    default:
                        DateTime time1 = null;
                        LOGGER.debug("sensor to save: {}, {}, {}", deviceId, code, stringValue);
                        if(azureTime != null) {
                            time1 = azureTime;
                        } else{
                            time1 = time;
                        }
                        Sensor sensor = sensorRepository.save(new Sensor(deviceId, sensorNumber, code, time1, stringValue, device.getShiftMap().get(sensorNumber), flag));
                        LOGGER.debug("saved sensor: {}", sensor);
                        sensorMap.put(sensorNumber, sensor);
                }
            }
            dataTriggerService.triggerForDeviceMaintenanceMap(deviceMaintenanceMap);
            device.getSensorMap().putAll(sensorMap);
            device.setLastSeen(time);
            deviceRepository.save(device);

        }
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

    PredictionOutputs predict(String model, PredictionInputs predictionInputs) {

        LOGGER.debug("Prediction Started Model : {}, with Sensors : {}", model, predictionInputs);
        return stub.predictList(predictionInputs);
    }

    public DateTime pastDataTime(String txt) {
        DateTime time = MagmaTime.now();

        // ZZ:12051320/+22|0-CS:0;1-CS:0:2-CS:0:3-CT:9:4-55:31:5-8:326;6-PS:1
        if (txt.contains("|")) {
            String[] data = txt.split("\\|");
            txt = data[1];                                      // 0-CS:0;1-CS:0:2-CS:0:3-CT:9:4-55:31:5-8:326;6-PS:1
            String[] conf = data[0].split(":", 2);

            if (conf[0].trim().equals("ZZ")) {
                String[] zt = conf[1].split("/");       // 12051320/+22

                if (zt[0].length() == 7) {
                    zt[0] = "0" + zt[0];
                }
                time = MagmaTime.parsePast(zt[0]);
                int value = Integer.parseInt(zt[1].replace("+", ""));
                time = time.plusMinutes(15 * (22 - value));
                LOGGER.debug("Past Data Found : {}, Formatted Time : {}, ZZ : {}", txt, time, conf[1]);
            }
        }

        return time;
    }

    public void doRegression(List<Float> points, Integer predict, Integer deg) {
        RegressionOutput response = stub.doRegression(RegressionInput.newBuilder()
                .setDeg(deg)
                .setPredict(predict)
                .addAllPoints(points)
                .build());

        LOGGER.debug("Trained List : {}, Predict List : {}", response.getTrainedList(), response.getPredictedList());
    }

    public HashMap<String, Object> getPredictionAndTrendLine(List<Float> points, Integer predict, Integer deg) {
        RegressionOutput response = stub.doRegression(RegressionInput.newBuilder()
                .setDeg(deg)
                .setPredict(predict)
                .addAllPoints(points)
                .build());

        HashMap<String, Object> res = new HashMap<>();
        res.put("trendLine", response.getTrainedList());
        res.put("predictionLine", response.getPredictedList());
        LOGGER.debug("Trained List : {}, Predict List : {}", response.getTrainedList(), response.getPredictedList());

        return res;
    }

    @PostConstruct
    private void initiate() {
        channel = ManagedChannelBuilder.forAddress("localhost", 50055)
                .usePlaintext()
                .build();
        stub = PredictConnectorGrpc.newBlockingStub(channel);
    }

    private void destroy() {
        channel.shutdown();
    }

    public String handleCodec(Device device, String rawTxt) {
        LOGGER.debug("handleCodec input for Device : {}, Message : {}", device.getId(), rawTxt);
        String magmaCodecId = device.getMagmaCodecId();

        String outPut = handleCodec(magmaCodecId, rawTxt);
        LOGGER.debug("handleCodec output for Device : {}, Message : {}", device.getId(), outPut);
        return outPut;
    }

    public String handleCodec(String magmaCodecId, String rawTxt) {

        MagmaCodec magmaCodec = null;
        if (magmaCodecId != null) {
            magmaCodec = magmaCodecRepository.findOne(magmaCodecId);
        }

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
                        rawTxt = compileCodeFileService.runDecoderFile(fileName, fileContent, result);
                        LOGGER.debug("Decoder file output: " + rawTxt);
                    } else {
                        LOGGER.debug("Decoder file input: " + rawTxt);
                        rawTxt = compileCodeFileService.runDecoderFile(fileName, fileContent, rawTxt);
                        LOGGER.debug("Decoder file output: " + rawTxt);
                    }
                } catch (Exception e) {
                    LOGGER.error("Error compiling and running codec file: " + e.getMessage());
                }
            }

        }

        return rawTxt;
    }

    public void doHandleJsonMessages(String deviceId, String txt) {
        LOGGER.debug("doHandleJsonMessages deviceId: {}, txt: {}", deviceId, txt);

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode messageArray = objectMapper.readTree(txt);
            if (messageArray.isArray()) {
                for (JsonNode jsonMessage : messageArray) {
                    // Check for "d2" or "e2" fields directly
                    if (jsonMessage.has("d2")) {
                        int invId = jsonMessage.get("d2").asInt();
                        doHandle(deviceId + "_" + invId, jsonMessage.toString());
                    }

                    if (jsonMessage.has("e2")) {
                        int invId = jsonMessage.get("e2").asInt();
                        doHandle(deviceId + "_" + invId, jsonMessage.toString());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred in doHandleJsonMessages: {}", e.getMessage(), e);
        }
    }

}
