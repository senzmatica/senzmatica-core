package com.magma.core.service;


import com.magma.core.configuration.MQTTConfiguration;
import com.magma.core.data.dto.DeviceDTO;
import com.magma.core.data.dto.DeviceParameterConfigurationDTO;
import com.magma.core.data.entity.Actuator;
import com.magma.core.data.entity.Battery;
import com.magma.core.data.entity.Device;
import com.magma.core.data.entity.Kit;
import com.magma.core.data.entity.Property;
import com.magma.core.data.entity.Sensor;
import com.magma.core.data.entity.SensorCode;
import com.magma.core.data.entity.TypeOfKit;
import com.magma.core.data.repository.*;
import com.magma.core.data.support.*;
import com.magma.core.util.*;
import com.magma.util.MagmaTime;
import com.magma.util.MagmaUtil;
import com.magma.util.Status;
import com.mongodb.client.result.UpdateResult;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CoreService {

    @Value("${device.data.interval}")
    private Integer interval;


    @Value("${mqtt.broker.address}")
    private String mqttBrokerAddress;

    @Autowired
    VerneMqService verneMqService;

    @Value("${device.data.battery.property.number}")
    private Integer batteryPropertyNumber;

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreService.class);

    @Autowired
    KitRepository kitRepository;

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    DeviceParameterConfigurationRepository deviceParameterConfigurationRepository;

    @Autowired
    SensorRepository sensorRepository;

    @Autowired
    PropertyRepository propertyRepository;

    @Autowired
    UserFavouriteService userFavouriteService;

    @Autowired
    MQTTConfiguration.MqttGateway mqttGateway;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    CorporateConnectorService corporateConnectorService;

    @Autowired
    DeviceParameterConfigurationUtil deviceParameterConfigurationUtil;

    @Autowired
    UserConnectorService userConnectorService;

    @Autowired
    SensorCodeRepository sensorCodeRepository;

    @Autowired
    KitTypeRepository kitTypeRepository;

    @Autowired
    ActuatorRepository actuatorRepository;

    private final MongoTemplate mongoTemplate;

    @Value("${mqtt.pub.topic}")
    private String mqttPubTopic;

    public CoreService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<Device> addDeviceParameterConfiguration(DeviceParameterConfigurationDTO deviceParameterConfigurationDTO) {

        if (deviceParameterConfigurationDTO == null) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
        List<Device> allDevices = new ArrayList<>();
        String ip;
        List<ProductParameter> filteredList = deviceParameterConfigurationDTO.getRemoteConfigurations().stream()
                .filter(config -> "1".equals(config.getId()))
                .collect(Collectors.toList());

        if (!filteredList.isEmpty()) {
            ip = deviceParameterConfigurationUtil.extractIpAddress(filteredList.get(0).getDefaultValue());
        } else {
            ip = null;
        }
        deviceParameterConfigurationDTO.getDeviceIds().forEach((id) -> {
            Device device = deviceRepository.findById(id).orElse(null);
            if (device != null) {

                DeviceParameterConfiguration conf = new DeviceParameterConfiguration();
                BeanUtils.copyProperties(deviceParameterConfigurationDTO, conf);
                conf.setDevice(id);
                conf.setServerIpAddress(ip);
                device.setDeviceParameterConfiguration(conf);
                deviceRepository.save(device);
                allDevices.add(device);

            }
        });
        return allDevices;
    }

    public String updateDeviceParameterConfiguration(String deviceId, DeviceParameterConfiguration deviceParameterConfiguration) {
        LOGGER.debug("update Device's Remote Configurations for Device Id: {}", deviceId);
        Optional<Device> requestedDevice = deviceRepository.findById(deviceId);

        if (requestedDevice == null) {
            throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
        } else {
            List<ProductParameter> oldRemoteConfigurations = deviceParameterConfigurationRepository.findByDevice(deviceId).getRemoteConfigurations();
            List<ProductParameter> newRemoteConfigurations = deviceParameterConfiguration.getRemoteConfigurations();


            List<String> modifiedIds = compareParameters(oldRemoteConfigurations, newRemoteConfigurations);
            Collections.sort(modifiedIds);
            String messageToDevice = generateMessageForModifiedIds(newRemoteConfigurations, modifiedIds);

            String productParameterIdToFind = "11";
            Optional<String> remoteConfigTopicOptional = oldRemoteConfigurations.stream()
                    .filter(param -> productParameterIdToFind.equals(param.getId()))
                    .map(ProductParameter::getDefaultValue)
                    .findFirst();

            if (remoteConfigTopicOptional.isPresent()) {
                String remoteConfigTopic = remoteConfigTopicOptional.get();
                LOGGER.debug("Generated Message: {}", messageToDevice);
                LOGGER.debug("Generated Message Length: {}", messageToDevice.length());
                List<String> messageChunks = splitMessage(messageToDevice);
                DateTime currentDateTime = DateTime.now();
                if (remoteConfigTopic.contains("#")) {
                    try {
                        for (int i = 0; i < messageChunks.size(); i++) {
                            String topic = remoteConfigTopic.replace("#", String.valueOf(i + 1));
                            String chunk = messageChunks.get(i);
                            Map<String, String> networkAndCommunication = new HashMap<>();
                            Map<String, String> topicFormat = new HashMap<>();
                            Map<String, String> messageFormat = new HashMap<>();

                            for (String part : chunk.split("\\|")[1].split(";")) {
                                String[] keyValue = part.split("-");
                                if (keyValue.length == 2) {
                                    String id = keyValue[0];
                                    String value = keyValue[1];


                                    ProductParameter parameter = newRemoteConfigurations.stream()
                                            .filter(p -> id.equals(p.getId()))
                                            .findFirst()
                                            .orElse(null);

                                    if (parameter != null) {
                                        if ("Network & Communication".equals(parameter.getParameterCategory().trim())) {
                                            networkAndCommunication.put(parameter.getParameter(), value);
                                        } else if ("Topic Format & Interval".equals(parameter.getParameterCategory().trim())) {
                                            topicFormat.put(parameter.getParameter(), value);
                                        } else if ("Message Format".equals(parameter.getParameterCategory().trim())) {
                                            messageFormat.put(parameter.getParameter(), value);
                                        }
                                    }
                                }
                            }
                            mqttGateway.send(topic, true, chunk);
                            DeviceParameterConfigurationHistory his = new DeviceParameterConfigurationHistory();
                            String actionBy = deviceParameterConfiguration.getUpdateHistory().get(0).getActionBy();
                            his.setActionBy(actionBy);
                            his.setUpdatedDate(currentDateTime);
                            his.setNetworkAndCommiunication(networkAndCommunication);
                            his.setTopicFormat(topicFormat);
                            his.setMessageFormat(messageFormat);

                            Message existingMessage = messageRepository.findById(deviceId + "-" + String.valueOf(i + 1)).orElse(null);
                            if (existingMessage != null) {
                                existingMessage.setUpdateHistory(his);
                                existingMessage.setMessage(messageChunks.get(i));
                                messageRepository.save(existingMessage);
                            } else {
                                Message newMessage = new Message(deviceId, his, String.valueOf(i + 1), messageChunks.get(i));
                                messageRepository.save(newMessage);
                            }

                        }
                        return "Success";
                    } catch (Exception e) {
                        LOGGER.debug("Exception: ", e);
                        throw new MagmaException(MagmaStatus.MQTT_EXCEPTION_IN_CONFIGURING_DEVICE);
                    }
                }
            }
            LOGGER.debug("ProductParameter with id {} not found.", productParameterIdToFind);
            return "Failure";
        }
    }

    private List<String> splitMessage(String message) {
        int maxChunkSize = 100;
        int totalLength = message.length();
        int startIndex = 0;
        List<String> chunks = new ArrayList<>();

        while (startIndex < totalLength) {
            int endIndex = startIndex + maxChunkSize;
            if (endIndex < totalLength) {
                while (endIndex > startIndex && message.charAt(endIndex) != ';' && !Character.isDigit(message.charAt(endIndex))) {
                    endIndex--;
                }
            }
            if (endIndex <= startIndex) {
                endIndex = startIndex + maxChunkSize;
            }
            endIndex = Math.min(endIndex, totalLength);
            String chunk = message.substring(startIndex, endIndex);
            if (!chunk.startsWith("RM|")) {
                chunk = "RM|" + chunk;
            }
            if (!chunk.endsWith("|END")) {
                int lastSeparatorIndex = chunk.lastIndexOf(";|END");
                if (lastSeparatorIndex != -1) {
                    chunk = chunk.substring(0, lastSeparatorIndex + 1) + "|END";
                } else {
                    chunk = chunk + "|END";
                }
            }
            chunks.add(chunk);
            startIndex = endIndex;
        }
        return chunks;
    }

    // Generate the message based on the modified IDs
    private String generateMessageForModifiedIds(List<ProductParameter> parameters, List<String> modifiedIds) {
        // Generate the message format
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("RM|");

        for (String id : modifiedIds) {
            for (ProductParameter parameter : parameters) {
                if (parameter.getId().equals(id)) {
                    messageBuilder.append(id).append("-").append(parameter.getDefaultValue()).append(";");
                    break;
                }
            }
        }
        messageBuilder.append("|END");
        return messageBuilder.toString();
    }

    // Compare old and new parameters and return IDs of modified parameters
    private static List<String> compareParameters(List<ProductParameter> oldParameters, List<ProductParameter> newParameters) {
        Set<String> modifiedIds = new HashSet<>();

        // Compare old and new parameters and identify added or modified ones
        for (ProductParameter newParam : newParameters) {
            for (ProductParameter oldParam : oldParameters) {
                if (newParam.getId().equals(oldParam.getId()) && !Objects.equals(newParam.getDefaultValue(), oldParam.getDefaultValue())) {
                    modifiedIds.add(newParam.getId());
                    break;
                }
            }
        }
        return new ArrayList<>(modifiedIds);
    }

    public String bulkUpdateDeviceParameterConfiguration(List<DeviceParameterConfiguration> deviceParameterConfigurations) {
        StringBuilder successMessage = new StringBuilder("Successfully updated devices: ");

        for (DeviceParameterConfiguration deviceParameterConfiguration : deviceParameterConfigurations) {
            String deviceId = deviceParameterConfiguration.getDevice();
            String result = updateDeviceParameterConfiguration(deviceId, deviceParameterConfiguration);
            if ("Success".equals(result)) {
                // Append the deviceId to the success message
                successMessage.append(deviceId).append(", ");
            }
        }
        // Remove the trailing comma and space, if any
        if (successMessage.length() > 0) {
            successMessage.setLength(successMessage.length() - 2);
        }
        return successMessage.toString();
    }

    public List<Device> getAllDeviceParameterConfigurations() {
        List<DeviceParameterConfiguration> all = deviceParameterConfigurationRepository.findAll();
        List<Device> allDevices = new ArrayList<>();
        all.forEach((dp) -> {
            Optional<Device> deviceDetails = deviceRepository.findById(dp.getDevice());
            deviceDetails.ifPresent(device -> {
                device.setDeviceParameterConfiguration(dp);
                allDevices.add(device);
            });
        });
        return allDevices;
    }

    public Device create(DeviceDTO deviceDTO) {
        LOGGER.debug("Create Device request found : {}", deviceDTO);

        Device device = new Device();
        BeanUtils.copyProperties(deviceDTO, device);

        if (!device.validate()) {
            throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
        }


        Device existingDevice = deviceRepository.findById(device.getId()).orElse(null);

        if (existingDevice != null) {
            throw new MagmaException(MagmaStatus.DEVICE_ALREADY_EXISTS);
        }

        if (device.getInterval() == null) {
            device.setInterval(interval);
        }

        if (device.getMaintain() == null) {
            device.setMaintain(false);
        }

        if (device.getStatus() == null) {
            device.setStatus(Status.ACTIVE);
        }

        if (device.getBattery() == null) {
            device.setBattery(new Battery(240.0, 300.0));
        }

        if (!device.getShiftMap().isEmpty()) {
            device.getShiftMap().forEach((key1, value1) -> {
                value1.forEach((key, value) -> {
                    switch (value.getOperation()) {
                        case ADD:
                        case SUB:
                        case MUL:
                        case DIV:
                            if (!value.getMeta().containsKey("pivot")) {
                                throw new MagmaException(MagmaStatus.NEED_A_PIVOT_FOR_SHIFTING);
                            }
                            break;
                    }
                });
            });
        }

        device.setNoOfSensors(device.getSensorCodes().length);
        device.setNoOfActuators(device.getActuatorCodes().length);
        return deviceRepository.save(device);
    }

    public Device createDevice(DeviceDTO deviceDTO, String userId) {
        LOGGER.debug("Create Device request found : {}", deviceDTO);

        Device device = new Device();
        UserInfo createdBy = new UserInfo();
        UserInfo user = userConnectorService.findUser(userId);

        BeanUtils.copyProperties(user, createdBy);
        BeanUtils.copyProperties(deviceDTO, device);
        device.setCreatedBy(createdBy);

        return deviceCreateLogic(device);
    }

    private Device deviceCreateLogic(Device device) {

        if (!device.validate()) {
            throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
        }
        if (deviceRepository.findById(device.getId()).orElse(device) != null) {
            throw new MagmaException(MagmaStatus.DEVICE_ALREADY_EXISTS);
        }

        if (device.getInterval() == null) {
            device.setInterval(interval);
        }

        if (device.getMaintain() == null) {
            device.setMaintain(false);
        }

        if (device.getStatus() == null) {
            device.setStatus(Status.ACTIVE);
        }

        if (device.getBattery() == null) {
            device.setBattery(new Battery(240.0, 300.0));
        }

        if (!device.getShiftMap().isEmpty()) {
            device.getShiftMap().forEach((key1, value1) -> {
                value1.forEach((key, value) -> {
                    switch (value.getOperation()) {
                        case ADD:
                        case SUB:
                        case MUL:
                        case DIV:
                            if (!value.getMeta().containsKey("pivot")) {
                                throw new MagmaException(MagmaStatus.NEED_A_PIVOT_FOR_SHIFTING);
                            }
                            break;
                    }
                });
            });
        }
        
        device.setBatchNumber(device.getBatchNumber());

        return deviceRepository.save(device);
    }

    public List<Device> findDevices() {
        LOGGER.debug("Find all Devices request found");
        List<Device> allDevices = new ArrayList<>();
        List<Device> devices = deviceRepository.findAll();
        devices.forEach((d) -> {
            DeviceParameterConfiguration conf = deviceParameterConfigurationRepository.findByDevice(d.getId());
            if (conf != null) {
                d.setDeviceParameterConfiguration(conf);
            }

            Kit kit = kitRepository.findByDevices(d.getId());
            if (kit != null) {
                d.setReferenceName(corporateConnectorService.referenceName(kit.getId()));
            }

            allDevices.add(d);
        });

        return allDevices;
    }

    public List<Device> findDevicesWithFavouriteOrder(String userId) {
        LOGGER.debug("Find all Devices With Favourite Order request found For User:{}", userId);
        return userFavouriteService.getAllDevicesWithFavouriteOrder(userId);
    }


    public List<String> findFavouriteDeviceIds(String userId) {
        LOGGER.debug("Find Ids of Favourite devices - For User:{}", userId);
        return userFavouriteService.getUserFavouriteDevices(userId);
    }

    public Device findDeviceById(String deviceId) {
        LOGGER.debug("Find Device request found : {}", deviceId);
        Optional<Device> optionalDevice = deviceRepository.findById(deviceId);

        if (optionalDevice.isPresent()) {
            Device device = optionalDevice.get();
            DeviceParameterConfiguration conf = deviceParameterConfigurationRepository.findByDevice(deviceId);
            if (conf != null) {
                device.setDeviceParameterConfiguration(conf);
            }
            return device;
        } else {
            throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
        }
    }

    public Device updateDevice(String deviceId, DeviceDTO deviceDTO) {
        LOGGER.debug("Update request found DeviceId : {}, Device : {}", deviceId, deviceDTO);

        Device db = findDeviceById(deviceId);

        return deviceUpdateLogic(db, deviceDTO);
    }

    public Device editDevice(String deviceId, DeviceDTO deviceDTO, String userId) {
        LOGGER.debug("Update request found DeviceId : {}, Device : {}", deviceId, deviceDTO);

        Device db = findDeviceById(deviceId);
        UserInfo user = userConnectorService.findUser(userId);
        UserInfo modifiedBy = new UserInfo();

        BeanUtils.copyProperties(user, modifiedBy);
        db.setModifiedBy(modifiedBy);

        return deviceUpdateLogic(db, deviceDTO);
    }

    private Device deviceUpdateLogic(Device db, DeviceDTO deviceDTO) {
        if (!deviceDTO.validate()) {
            throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
        }

        if (MagmaUtil.validate(deviceDTO.getName())) {
            db.setName(deviceDTO.getName());
        } else {
            throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
        }

        if (MagmaUtil.validate(deviceDTO.getDescription())) {
            db.setDescription(deviceDTO.getDescription());
        }

        if (deviceDTO.getStatus() != null) {
            db.setStatus(deviceDTO.getStatus());
        }

        if (deviceDTO.getInterval() != null) {
            db.setInterval(deviceDTO.getInterval());
        }

        if (deviceDTO.getMaintain() != null) {
            db.setMaintain(deviceDTO.getMaintain());
        }

        if (deviceDTO.getBattery() == null) {
            db.setBattery(new Battery(240.0, 300.0));
        }

        if (deviceDTO.getMetaData() != null && !deviceDTO.getMetaData().isEmpty()) {
            updateMetaData(db, deviceDTO.getMetaData());
        }

        if (deviceDTO.getPersistence() != null) {
            db.setPersistence(deviceDTO.getPersistence());
        }

        if (deviceDTO.getSensorCodes() != null) {
            db.setSensorCodes(deviceDTO.getSensorCodes());
            db.setNoOfSensors(deviceDTO.getSensorCodes().length);
        }

        if (deviceDTO.getActuatorCodes() != null) {
            db.setActuatorCodes(deviceDTO.getActuatorCodes());
            db.setNoOfActuators(deviceDTO.getActuatorCodes().length);
        }

        if (deviceDTO.getProtocol() != null) {
            db.setProtocol(deviceDTO.getProtocol());
        }

        if (deviceDTO.getConnectivity() != null) {
            db.setConnectivity(deviceDTO.getConnectivity());
        }

        if (deviceDTO.getBatchNumber() != null) {
            db.setBatchNumber(deviceDTO.getBatchNumber());
        }

        if (deviceDTO.getTemperatureUnit() != null) {
            db.setTemperatureUnit(deviceDTO.getTemperatureUnit());
        }

        if (!deviceDTO.getShiftMap().isEmpty()) {
            deviceDTO.getShiftMap().forEach((key1, value1) -> {
                value1.forEach((key, value) -> {
                    switch (value.getOperation()) {
                        case ADD:
                        case SUB:
                        case MUL:
                        case DIV:
                            if (!value.getMeta().containsKey("pivot")) {
                                throw new MagmaException(MagmaStatus.NEED_A_PIVOT_FOR_SHIFTING);
                            }
                            break;
                    }
                });
            });
            db.setShiftMap(deviceDTO.getShiftMap());
        }

        return deviceRepository.save(db);
    }

    private void updateMetaData(Device device, Map<String, String> metaData) {
        if (device.getMetaData() == null) {
            device.setMetaData(metaData);
        } else {
            device.getMetaData().putAll(metaData);
        }
    }

    public String patchDevice(String deviceId, Device device) {
        LOGGER.debug("Update request found DeviceId : {}, Device : {}", deviceId, device);

        device.setId(deviceId);

        Device db = findDeviceById(deviceId);

        if (MagmaUtil.validate(device.getName())) {
            db.setName(device.getName());
        }

        if (MagmaUtil.validate(device.getDescription())) {
            db.setDescription(device.getDescription());
        }

        if (device.getStatus() != null) {
            db.setStatus(device.getStatus());
        }

        if (device.getInterval() != null) {
            db.setInterval(device.getInterval());
        }

        if (device.getMaintain() != null) {
            db.setMaintain(device.getMaintain());
        }

        if (device.getBattery() == null) {
            db.setBattery(new Battery(240.0, 300.0));
        }

        if (device.getPersistence() != null) {
            db.setPersistence(device.getPersistence());
        }

        if (device.getSensorCodes() != null) {
            db.setSensorCodes(device.getSensorCodes());
            db.setNoOfSensors(device.getSensorCodes().length);
        }

        if (device.getActuatorCodes() != null) {
            db.setActuatorCodes(device.getActuatorCodes());
            db.setNoOfActuators(device.getActuatorCodes().length);
        }

        if (device.getProtocol() != null) {
            db.setProtocol(device.getProtocol());
        }

        if (device.getConnectivity() != null) {
            db.setConnectivity(device.getConnectivity());
        }


        if (!device.getShiftMap().isEmpty()) {
            device.getShiftMap().forEach((key1, value1) -> {
                value1.forEach((key, value) -> {
                    switch (value.getOperation()) {
                        case ADD:
                        case SUB:
                        case MUL:
                        case DIV:
                            if (!value.getMeta().containsKey("pivot")) {
                                throw new MagmaException(MagmaStatus.NEED_A_PIVOT_FOR_SHIFTING);
                            }
                            break;
                    }
                });
            });
            db.setShiftMap(device.getShiftMap());
        }

        deviceRepository.save(db);

        return "Successfully Updated";
    }

    public Map<Connectivity, Map<String, String>> getConnectivityMatrix(String deviceId) {
        Device device = findDeviceById(deviceId);
        return device.getConnectivityMatrix();
    }

    public Map<Connectivity, Map<String, String>> updateConnectivityMatrix(String deviceId, Map<Connectivity, Map<String, String>> matrix) {
        String topic = mqttPubTopic + deviceId + "/C";
        Device device = findDeviceById(deviceId);
        matrix.forEach((con, map) -> {
            switch (con) {
                case WIFI: {
                    if (!device.getConnectivityMatrix().containsKey(Connectivity.WIFI)) {
                        device.getConnectivityMatrix().put(Connectivity.WIFI, new HashMap<>());
                        device.getConnectivityMatrix().get(Connectivity.WIFI).put("pollingInterval", "300");
                    }

                    Map<String, String> exits = device.getConnectivityMatrix().get(Connectivity.WIFI);

                    if (exits.containsKey("status") && !exits.get("status").equals("success")) {
                        throw new MagmaException(MagmaStatus.PENDING_OPERATION);
                    }

                    if (map.containsKey("routing")) {

                        String ssid = map.get("ssid");
                        String wifiPassword = map.get("wifiPassword");
                        String pollingInterval = exits.get("pollingInterval");

                        String msg;
                        if (map.get("routing").equals("static")) {
                            if (!map.containsKey("ip") ||
                                    !map.containsKey("subnet") ||
                                    !map.containsKey("gateway") ||
                                    !map.containsKey("dns")) {
                                throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
                            }

                            String ip = map.get("ip");
                            String subnet = map.get("subnet");
                            String gateway = map.get("gateway");
                            String dns = map.get("dns");


                            exits.put("ip", ip);
                            exits.put("subnet", subnet);
                            exits.put("gateway", gateway);
                            exits.put("dns", dns);
                            exits.put("status", "Pending set parameter : Static Routing");
                            msg = "0-GET:SS=" + ssid + "&Pa=" + wifiPassword + "&St=" + ip + "&Su=" + subnet + "&Ga=" + gateway + "&Dns=" + dns + "&Po=" + pollingInterval + "&END";
                        } else {
                            msg = "0-GET:SS=" + ssid + "&Pa=" + wifiPassword + "&St=0&Su=0&Ga=0&Dns=0&Po=" + pollingInterval + "&END";
                        }

                        mqttGateway.send(topic, true, msg);
                        exits.put("ssid", ssid);
                        exits.put("wifiPassword", wifiPassword);
                        exits.put("cmd", msg);
                        exits.put("status", "Pending set parameter : Device Connectivity Setting");
                        break;
                    }


                    if (map.containsKey("username")) {
                        String username = map.get("username");

                        String msg = "1-GET:Us=" + username + "&END";

                        mqttGateway.send(topic, true, msg);
                        exits.put("username", username);
                        exits.put("cmd", msg);
                        exits.put("status", "Pending set parameter : Device Username");
                        break;
                    }

                    if (map.containsKey("signature")) {
                        String signature = map.get("signature");

                        String msg = "2-GET:Si=" + signature + "&END";

                        mqttGateway.send(topic, true, msg);
                        exits.put("signature", signature);
                        exits.put("cmd", msg);
                        exits.put("status", "Pending set parameter : Device Signature");
                        break;
                    }

                    if (map.containsKey("pollingInterval")) {
                        String pollingInterval = map.get("pollingInterval");
                        String ssid = exits.get("ssid");
                        String wifiPassword = exits.get("wifiPassword");

                        String msg;
                        if (exits.get("routing").equals("static")) {

                            String ip = exits.get("ip");
                            String subnet = exits.get("subnet");
                            String gateway = exits.get("gateway");
                            String dns = exits.get("dns");

                            msg = "0-GET:SS=" + ssid + "&Pa=" + wifiPassword + "&St=" + ip + "&Su=" + subnet + "&Ga=" + gateway + "&Dns=" + dns + "&Po=" + pollingInterval + "&END";
                        } else {
                            msg = "0-GET:SS=" + ssid + "&Pa=" + wifiPassword + "&St=0&Su=0&Ga=0&Dns=0&Po=" + pollingInterval + "&END";
                        }

                        mqttGateway.send(topic, true, msg);
                        exits.put("pollingInterval", pollingInterval);
                        exits.put("cmd", msg);
                        exits.put("status", "Pending set parameter : Pooling Interval");
                        break;
                    }

                    if (map.containsKey("deviceId")) {
                        String id = map.get("deviceId");

                        String msg = "3-GET:Id=" + id + "&END";

                        mqttGateway.send(topic, true, msg);
                        device.setId(id);
                        exits.put("cmd", msg);
                        exits.put("status", "Pending set parameter : Device Id");
                        break;
                    }

                    if (map.containsKey("host") && map.containsKey("port")) {
                        String host = map.get("host");
                        String port = map.get("port");

                        String msg = "4-GET:IP=" + host + "&Pt=" + port + "&END";

                        mqttGateway.send(topic, true, msg);
                        exits.put("host", host);
                        exits.put("port", port);
                        exits.put("cmd", msg);
                        exits.put("status", "Pending set parameter : Device Username");
                        break;
                    }
                }

                case GSM: {
                    if (!device.getConnectivityMatrix().containsKey(Connectivity.GSM)) {
                        device.getConnectivityMatrix().put(Connectivity.GSM, new HashMap<>());
                        device.getConnectivityMatrix().get(Connectivity.GSM).put("pollingInterval", "300");
                    }

                    Map<String, String> exits = device.getConnectivityMatrix().get(Connectivity.GSM);

                    if (exits.containsKey("status") && !exits.get("status").equals("success")) {
                        throw new MagmaException(MagmaStatus.PENDING_OPERATION);
                    }

                    if (map.containsKey("routing")) {
                        String pollingInterval = exits.get("pollingInterval");

                        String msg;
                        if (map.get("routing").equals("static")) {
                            if (!map.containsKey("ip") ||
                                    !map.containsKey("subnet") ||
                                    !map.containsKey("gateway") ||
                                    !map.containsKey("dns")) {
                                throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
                            }
                            String ip = map.get("ip");
                            String subnet = map.get("subnet");
                            String gateway = map.get("gateway");
                            String dns = map.get("dns");

                            exits.put("ip", ip);
                            exits.put("subnet", subnet);
                            exits.put("gateway", gateway);
                            exits.put("dns", dns);
                            exits.put("status", "Pending set parameter : Static Routing");

                            msg = "0-GET:St=" + ip + "&Su=" + subnet + "&Ga=" + gateway + "&Dns=" + dns + "&Po=" + pollingInterval + "&END";
                        } else {
                            msg = "0-GET:St=0&Su=0&Ga=0&Dns=0&Po=" + pollingInterval + "&END";
                        }

                        mqttGateway.send(topic, true, msg);
                        exits.put("cmd", msg);
                        exits.put("status", "Pending set parameter : Device Connectivity Setting");
                        break;
                    }

                    if (map.containsKey("username")) {
                        String username = map.get("username");

                        String msg = "1-GET:Us=" + username + "&END";

                        mqttGateway.send(topic, true, msg);
                        exits.put("username", username);
                        exits.put("cmd", msg);
                        exits.put("status", "Pending set parameter : Device Username");
                        break;
                    }

                    if (map.containsKey("signature")) {
                        String signature = map.get("signature");

                        String msg = "2-GET:Si=" + signature + "&END";

                        mqttGateway.send(topic, true, msg);
                        exits.put("signature", signature);
                        exits.put("cmd", msg);
                        exits.put("status", "Pending set parameter : Device Signature");
                        break;
                    }

                    if (map.containsKey("pollingInterval")) {
                        String pollingInterval = map.get("pollingInterval");
                        String msg;
                        if (exits.get("routing").equals("static")) {

                            String ip = exits.get("ip");
                            String subnet = exits.get("subnet");
                            String gateway = exits.get("gateway");
                            String dns = exits.get("dns");

                            msg = "0-GET:St=" + ip + "&Su=" + subnet + "&Ga=" + gateway + "&Dns=" + dns + "&Po=" + pollingInterval + "&END";
                        } else {
                            msg = "0-GET:St=0&Su=0&Ga=0&Dns=0&Po=" + pollingInterval + "&END";
                        }

                        mqttGateway.send(topic, true, msg);
                        exits.put("pollingInterval", pollingInterval);
                        exits.put("cmd", msg);
                        exits.put("status", "Pending set parameter : Pooling Interval");
                        break;
                    }

                    if (map.containsKey("deviceId")) {
                        String id = map.get("deviceId");

                        String msg = "3-GET:Id=" + id + "&END";

                        mqttGateway.send(topic, true, msg);
                        device.setId(id);
                        exits.put("cmd", msg);
                        exits.put("status", "Pending set parameter : Device Id");
                        break;
                    }

                    if (map.containsKey("host") && map.containsKey("port")) {
                        String host = map.get("host");
                        String port = map.get("port");

                        String msg = "4-GET:IP=" + host + "&Pt=" + port + "&END";

                        mqttGateway.send(topic, true, msg);
                        exits.put("host", host);
                        exits.put("port", port);
                        exits.put("cmd", msg);
                        exits.put("status", "Pending set parameter : Device Username");
                        break;
                    }
                }
                default:
                    throw new MagmaException(MagmaStatus.NOT_IMPLEMENTED);
            }
        });

        deviceRepository.save(device);
        return device.getConnectivityMatrix();
    }

    public Kit findKitById(String kitId) {
        LOGGER.debug("Find Kit request found : {}", kitId);
        Optional<Kit> optionalKit = kitRepository.findById(kitId);
        return optionalKit.orElseThrow(() -> new MagmaException(MagmaStatus.KIT_NOT_FOUND));
    }

    public String deleteDevice(String deviceId) {
        LOGGER.debug("Delete request found DeviceId : {}", deviceId);

        Optional<Device> optionalDevice = deviceRepository.findById(deviceId);
        if (optionalDevice.isPresent()) {
            Device device = optionalDevice.get();
            deviceRepository.delete(device);
            return "Successfully Updated";
        } else {
            throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
        }
    }

    public List<Sensor> getAllSensorDetailsByDeviceId(String deviceId) {
        Device requestedDevice = deviceRepository.findById(deviceId).orElse(null);
        if (requestedDevice == null) {
            throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
        }

        return sensorRepository.findByDeviceId(deviceId);
    }

    public List<Sensor> findSensorHistoryByKitAndNumber(String deviceId, Integer number, Direction direction, String from, String to) {
        LOGGER.debug("Find Sensor request found Device : {}, Number : {}", deviceId, number);

        Device device = findDeviceById(deviceId);

        if (device.getSensorCodes().length < number) {
            throw new MagmaException(MagmaStatus.SENSOR_NOT_FOUND);
        }

        if (to == null && from == null) {
            to = MagmaTime.format(MagmaTime.now());
            from = MagmaTime.format(MagmaTime.now().minusDays(1));
        } else if (to == null) {
            to = from;
            from += " 00:00";
            to += " 23:59";
        } else if (from == null) {
            from = to;
            from += " 00:00";
            to += " 23:59";
        } else {
            from += " 00:00";
            to += " 23:59";
        }


        if (direction.isDescending()) {
            return sensorRepository.findByDeviceIdAndNumberAndTimeBetweenOrderByTimeDesc(
                    deviceId,
                    number,
                    MagmaTime.parse(from),
                    MagmaTime.parse(to));
        } else {
            return sensorRepository.findByDeviceIdAndNumberAndTimeBetweenOrderByTimeAsc(
                    deviceId,
                    number,
                    MagmaTime.parse(from),
                    MagmaTime.parse(to));
        }

    }

    public List<Actuator> findActuatorHistoryByKitAndNumber(String deviceId, Integer number, Direction direction, String from, String to) {
        LOGGER.debug("Find Actuator history request found Device : {}, Number : {}", deviceId, number);

        Device device = findDeviceById(deviceId);

        if (device.getSensorCodes().length < number) {
            throw new MagmaException(MagmaStatus.SENSOR_NOT_FOUND);
        }

        if (to == null && from == null) {
            to = MagmaTime.format(MagmaTime.now());
            from = MagmaTime.format(MagmaTime.now().minusDays(1));
        } else if (to == null) {
            to = from;
            from += " 00:00";
            to += " 23:59";
        } else if (from == null) {
            from = to;
            from += " 00:00";
            to += " 23:59";
        } else {
            from += " 00:00";
            to += " 23:59";
        }


        if (direction.isDescending()) {
            return actuatorRepository.findByDeviceIdAndNumberAndTimeBetweenOrderByTimeDesc(
                    deviceId,
                    number,
                    MagmaTime.parse(from),
                    MagmaTime.parse(to));
        } else {
            return actuatorRepository.findByDeviceIdAndNumberAndTimeBetweenOrderByTimeAsc(
                    deviceId,
                    number,
                    MagmaTime.parse(from),
                    MagmaTime.parse(to));
        }

    }

    public String updateKitOfflineStatus(String kitId, boolean offlineStatus) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(kitId));
        Update update = new Update();
        update.set("offline", offlineStatus);

        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Kit.class);
        if (updateResult.getMatchedCount() == 0) {
            throw new MagmaException(MagmaStatus.KIT_NOT_FOUND);
        }
        return "Successfully Updated";
    }

    public String updatePersistence(String kitId, Boolean persistence) {
        LOGGER.debug("Persistence request found Kit : {}, Persistence : {}", kitId, persistence);

        if (persistence == null) {
            throw new IllegalArgumentException("Persistence value cannot be null.");
        }

        Kit db = findKitById(kitId);
        db.setPersistence(persistence);

        List<Device> devices = deviceRepository.findByIdIn(db.getDevices());

        devices.forEach(device -> device.setPersistence(persistence));
        deviceRepository.saveAll(devices);

        kitRepository.save(db);

        return "Successfully Updated";
    }

    public MetaData getProperties() {
        MetaData metaData = new MetaData();
        List<SensorCode> codes = sensorCodeRepository.findAll();
        List<TypeOfKit> types = kitTypeRepository.findAll();
        List<EnumObj> sensorCodes = codes.stream().map(s -> new EnumObj(s.getCode(), s.getCodeValue())).collect(Collectors.toList());
        List<EnumObj> actuators = Arrays.stream(ActuatorCode.values()).map(s -> new EnumObj(s.name(), s.value())).collect(Collectors.toList());
        metaData.setSensors(sensorCodes);
        metaData.setActuators(actuators);
        metaData.setKitTypes(types);
        return metaData;
    }
    public Property changeLabel(String deviceId, String label, String propertyId) {
        Property property = propertyRepository.findById(propertyId).orElse(null);
        Device device = deviceRepository.findById(deviceId).orElse(null);
        if (device == null || property == null) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
        property.setLabel(label);
        propertyRepository.save(property);
        return property;
    }

    public void doHandleDeviceConfigurationUpdates(String deviceId, String topicNumber) {
        LOGGER.debug("Device -{} To System Message to Update the device parameter configuration of device", deviceId);
        Device requestedDevice = deviceRepository.findById(deviceId).orElse(null);
        if (requestedDevice == null) {
            throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
        } else {
            Message messageObject = messageRepository.findById(deviceId + "-" + topicNumber).orElse(null);

            String message = messageObject.getMessage();

            //valid message "RM|7-3;8-6;|END"
            String[] messageElements = message.split("\\|");
            String configMessage = messageElements[1];
            String[] pairs = configMessage.split(";");

            Map<String, String> keyValueMap = new HashMap<>();
            for (String pair : pairs) {
                String[] parts = pair.split("-");
                if (parts.length == 2) {
                    String key = parts[0];
                    String value = parts[1];
                    keyValueMap.put(key, value);
                } else if (parts.length == 1) {
                    String key = parts[0];
                    String value = "";
                    keyValueMap.put(key, value);
                }
            }

            if (messageElements.length < 3) {
                LOGGER.debug("Message sent by device is invalid");
                throw new MagmaException(MagmaStatus.INVALID_INPUT);
            }
            DeviceParameterConfiguration current = deviceParameterConfigurationRepository.findByDevice(deviceId);
            List<ProductParameter> currentConfigurations = current.getRemoteConfigurations();

            for (String key : keyValueMap.keySet()) {
                String value = keyValueMap.get(key);
                for (ProductParameter parameter : currentConfigurations) {
                    if (key.equals(parameter.getId())) {
                        if (parameter.getDefaultValues() != null) {
                            parameter.getDefaultValues().add(parameter.getDefaultValue());
                            if (parameter.getDefaultValues().contains(value)) {
                                parameter.getDefaultValues().remove(value);
                            }

                        }
                        parameter.setDefaultValue(value);
                        break;
                    }
                }
            }
            List<DeviceParameterConfigurationHistory> currentHistory = current.getUpdateHistory();
            DeviceParameterConfigurationHistory messageHistory = messageObject.getUpdateHistory();
            currentHistory.add(messageHistory);
            current.setUpdateHistory(currentHistory);
            current.setRemoteConfigurations(currentConfigurations);
            deviceParameterConfigurationRepository.save(current);

        }
    }
}
