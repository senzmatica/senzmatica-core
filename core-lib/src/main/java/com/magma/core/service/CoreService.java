package com.magma.core.service;


import com.magma.core.configuration.MQTTConfiguration;
import com.magma.core.job.CoreSchedule;
import com.magma.dmsdata.data.dto.DeviceParameterConfigurationDTO;
import com.magma.dmsdata.data.entity.*;
import com.magma.core.data.repository.*;
import com.magma.dmsdata.data.support.*;
import com.magma.dmsdata.util.*;
import com.magma.util.MagmaTime;
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
    SensorRepository sensorRepository;

    @Autowired
    SensorFailureValueRepository sensorFailureValueRepository;

    @Autowired
    DeviceParameterConfigurationRepository deviceParameterConfigurationRepository;

    @Autowired
    PropertyRepository propertyRepository;

    @Autowired
    AlertLimitRepository alertLimitRepository;

    @Autowired
    AlertRepository alertRepository;

    @Autowired
    ActuatorRepository actuatorRepository;

    @Autowired
    GeoRepository geoRepository;

    @Autowired
    DataTriggerService dataTriggerService;

    @Autowired
    ErrorRepository errorRepository;

    @Autowired
    KitNotificationService kitNotificationService;

    @Autowired
    DataProcessorService dataProcessorService;

    @Autowired
    MQTTConfiguration.MqttGateway mqttGateway;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    DeviceParameterConfigurationUtil deviceParameterConfigurationUtil;
    @Autowired
    KitTypeRepository kitTypeRepository;

    @Autowired
    DeviceService deviceService;

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

    /**
     * Update Device Parameter Configuration
     * Updates the remote configurations for a device based on the provided DeviceParameterConfiguration.
     * Sends message to the relevant device and save in message repository temporarily.
     */
    public String updateDeviceParameterConfiguration(String deviceId, DeviceParameterConfiguration deviceParameterConfiguration) {
        LOGGER.debug("Update Device's Remote Configurations for Device Id: {}", deviceId);

        // Retrieve the requested device from the repository
        Device requestedDevice = deviceRepository.findById(deviceId).orElse(null);

        // Check if the device exists
        if (requestedDevice == null) {
            // Throw an exception if the device is not found
            throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
        } else {
            // Retrieve the current device parameter configuration
            DeviceParameterConfiguration deviceParameterConfigurationDb = requestedDevice.getDeviceParameterConfiguration();

            if (deviceParameterConfiguration != null) {
                // Retrieve the old and new remote configurations
                List<ProductParameter> oldRemoteConfigurations = deviceParameterConfigurationDb.getRemoteConfigurations();
                List<ProductParameter> newRemoteConfigurations = deviceParameterConfiguration.getRemoteConfigurations();

                // Validate the parameters in the new remote configuration
                deviceParameterConfigurationUtil.validateParametersInRemoteConfiguration(newRemoteConfigurations);

                // Identify the modified parameter IDs
                List<String> modifiedIds = compareParameters(oldRemoteConfigurations, newRemoteConfigurations);
                Collections.sort(modifiedIds);

                // Generate the complete message for the modified parameters
                String completeMessage = generateMessageForModifiedIds(newRemoteConfigurations, modifiedIds);
                LOGGER.debug("Generated Complete Message: {}", completeMessage);

                // Split the complete message into chunks
                List<String> messageChunks = splitMessage(completeMessage);

                // Get the current date and time
                DateTime currentDateTime = DateTime.now();

                // Define the product parameter ID to find the remote config topic
                String productParameterIdToFind = "11"; // Remote config topic id

                // Find the default value of the remote config topic
                Optional<String> remoteConfigTopicOptional = oldRemoteConfigurations.stream()
                        .filter(param -> productParameterIdToFind.equals(param.getId()))
                        .map(ProductParameter::getDefaultValue)
                        .findFirst();

                // Check if the remote config topic is found
                if (remoteConfigTopicOptional.isPresent()) {
                    // Retrieve the remote config topic
                    String remoteConfigTopic = remoteConfigTopicOptional.get();
                    LOGGER.debug("Generated Message Length: {}", completeMessage.length());
                    LOGGER.debug("Remote Config Topic: {}", remoteConfigTopic);

                    // Check if the remote config topic contains placeholders ("#")
                    if (remoteConfigTopic.contains("#")) {
                        try {
                            // Fetch existing messages for the device
                            List<Message> messages = messageRepository.findMessagesByDevice(deviceId);

                            // Find the maximum topic number among existing messages
                            int maxTopicNumber = messages.stream()
                                    .map(Message::getTopicNumber)
                                    .mapToInt(Integer::parseInt)
                                    .max()
                                    .orElse(0);
                            LOGGER.debug("Max Topic Number: {} ", maxTopicNumber);

                            // Iterate through message chunks and send them to the MQTT gateway
                            for (int i = 0; i < messageChunks.size(); i++) {
                                // Generate a unique topic for each chunk
                                String topic = remoteConfigTopic.replace("#", String.valueOf(maxTopicNumber + i + 1));
                                String chunk = messageChunks.get(i);

                                // Send the chunk to the MQTT gateway
                                mqttGateway.send(topic, true, chunk);

                                // Extract network and communication, topic format, and message format from the chunk
                                Map<String, String> networkAndCommunication = new HashMap<>();
                                Map<String, String> topicFormat = new HashMap<>();
                                Map<String, String> messageFormat = new HashMap<>();

                                for (String part : chunk.split("\\|")[1].split(";")) {
                                    String[] keyValue = part.split("-");
                                    if (keyValue.length == 2) {
                                        String id = keyValue[0];
                                        String value = keyValue[1];

                                        // Find the corresponding parameter in the new remote configurations
                                        ProductParameter parameter = newRemoteConfigurations.stream()
                                                .filter(p -> id.equals(p.getId()))
                                                .findFirst()
                                                .orElse(null);

                                        // Populate maps based on parameter category
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

                                // Create a history entry for the device parameter configuration
                                DeviceParameterConfigurationHistory his = new DeviceParameterConfigurationHistory();
                                String actionBy = deviceParameterConfiguration.getUpdateHistory().get(0).getActionBy();
                                his.setActionBy(actionBy);
                                his.setUpdatedDate(currentDateTime);
                                his.setNetworkAndCommiunication(networkAndCommunication);
                                his.setTopicFormat(topicFormat);
                                his.setMessageFormat(messageFormat);

                                // Find existing message or create a new one
                                Message existingMessage = messageRepository.findById(deviceId + "-" + String.valueOf(maxTopicNumber + i + 1)).orElse(null);
                                if (existingMessage != null) {
                                    existingMessage.setUpdateHistory(his);
                                    existingMessage.setMessage(messageChunks.get(i));
                                    messageRepository.save(existingMessage);
                                } else {
                                    Message newMessage = new Message(deviceId, his, String.valueOf(maxTopicNumber + i + 1), messageChunks.get(i));
                                    messageRepository.save(newMessage);
                                }
                            }
                        } catch (Exception e) {
                            // Handle MQTT exception
                            LOGGER.debug("Exception", e);
                            throw new MagmaException(MagmaStatus.MQTT_EXCEPTION_IN_CONFIGURING_DEVICE);
                        }
                    } else {
                        return "Failure";
                    }
                }
            }
        }
        // Return success after completing the update
        return "Success";
    }


    //Splits a message into chunks to fit the maximum chunk size
    private List<String> splitMessage(String message) {
        int maxChunkSize = 100;
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();

        for (String part : message.split(";")) {
            if (currentChunk.length() + part.length() + 1 > maxChunkSize) {
                currentChunk.append(";");
                currentChunk.append("|END");
                chunks.add("RM|" + currentChunk);
                currentChunk = new StringBuilder();
            }

            if (currentChunk.length() > 0) {
                currentChunk.append(";");
            }
            currentChunk.append(part);
        }

        if (currentChunk.length() > 0) {
            currentChunk.append(";");
            currentChunk.append("|END");
            chunks.add("RM|" + currentChunk);
            LOGGER.debug("Chunk: {}", currentChunk);
        }
        return chunks;
    }


    // Generate the message based on the modified IDs
    private String generateMessageForModifiedIds(List<ProductParameter> parameters, List<String> modifiedIds) {
        StringBuilder messageBuilder = new StringBuilder();

        for (String id : modifiedIds) {
            for (ProductParameter parameter : parameters) {
                if (parameter.getId().equals(id)) {
                    messageBuilder.append(id).append("-").append(parameter.getDefaultValue()).append(";");
                    break;
                }
            }
        }

        // Check if 9, 10, or 11 is in modifiedIds and 12 is not in modifiedIds
        if ((modifiedIds.contains("9") || modifiedIds.contains("10") || modifiedIds.contains("11")) &&
                !modifiedIds.contains("12")) {
            messageBuilder.append("12-0;");
        }
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

        String[] fieldsToInclude = {"id", "name", "creationDate", "modifiedDate", "deviceParameterConfiguration", "product"};
        Query query = new Query(Criteria.where("deviceParameterConfiguration").ne(null));
        for (String field : fieldsToInclude) {
            query.fields().include(field);
        }
        List<Device> selectedDevices;
        selectedDevices = mongoTemplate.find(query, Device.class);
        return selectedDevices;
    }

    /**
     * Updates the device parameter configuration of the device based on the received system acknowledgement from device.
     *
     * @param tempDeviceId The identifier of the device (imei number,device name or custom identifier).
     * @param topicNumber  The topic number associated with the received message.
     */
    public void doHandleDeviceConfigurationUpdates(String tempDeviceId, String topicNumber) {
        try {
            LOGGER.debug("Device -{} To System Message to Update the device parameter configuration of device", tempDeviceId);

            // Retrieve the current device based on the temporary identifier or custom topics
            Device currentDevice = Optional.ofNullable(deviceRepository.findById(tempDeviceId).orElse(null))
                    .orElseGet(() -> deviceRepository.findByCustomPublishTopicOrCustomRemoteTopic(tempDeviceId));

            // Check if the device is found
            if (currentDevice == null) {
                // Throw an exception if the device is not found
                throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
            }

            String deviceId = currentDevice.getId();

            // Retrieve the current device parameter configuration
            DeviceParameterConfiguration current = currentDevice.getDeviceParameterConfiguration();
            LOGGER.debug("DeviceId Found:{}", deviceId);

            // Check if the current configuration is null
            if (current == null) {
                LOGGER.debug("DeviceParameterConfiguration not found for deviceId: {}", deviceId);
                throw new MagmaException(MagmaStatus.NOT_FOUND);
            }

            // Retrieve the current remote configurations
            List<ProductParameter> currentConfigurations = current.getRemoteConfigurations();

            // Retrieve the system message object based on device ID and topic number
            Message messageObject = messageRepository.findById(deviceId + "-" + topicNumber).orElse(null);
            String message = messageObject.getMessage();
            LOGGER.debug("Message: {}", message);

            // Validate the format of the received message
            String[] messageElements = message.split("\\|");

            if (messageElements.length < 3) {
                LOGGER.debug("Message sent by device is invalid");
                throw new MagmaException(MagmaStatus.INVALID_INPUT);
            }

            // Extract configuration message and split it into key-value pairs
            String configMessage = messageElements[1];
            String[] pairs = configMessage.split(";");
            Map<String, String> keyValueMap = new HashMap<>();

            for (String pair : pairs) {
                String[] parts = pair.split("-");
                String key = parts[0];
                String value = (parts.length == 2) ? parts[1] : "";
                LOGGER.debug("{}: {}", key, value);
                List<String> excludedKeys = Arrays.asList("97", "98", "99");

                // Delete the message if it contains specific excluded keys
                if (excludedKeys.contains(key)) {
                    messageRepository.delete(messageObject);
                    return;
                }
                keyValueMap.put(key, value);
            }

            // Update device parameters based on the received key-value pairs
            for (String key : keyValueMap.keySet()) {
                String value = keyValueMap.get(key);

                // Find the matching ProductParameter by key
                ProductParameter parameterToUpdate = currentConfigurations.stream()
                        .filter(parameter -> key.equals(parameter.getId()))
                        .findFirst()
                        .orElse(null);

                // Update device properties based on specific keys
                if (parameterToUpdate != null) {
                    if ("12".equals(key)) {
                        if ("1".equals(value)) {
                            // Replace specific parameters with the device ID in their default values
                            for (String idToReplace : Arrays.asList("9", "10", "11")) {
                                ProductParameter replaceParameter = currentConfigurations.stream()
                                        .filter(p -> idToReplace.equals(p.getId()))
                                        .findFirst()
                                        .orElse(null);

                                if (replaceParameter != null) {
                                    String updatedDefaultValue;
                                    String[] parts = replaceParameter.getDefaultValue().split("/");

                                    if (parts.length >= 4) {
                                        // Replacing with the device ID
                                        parts[3] = deviceId;
                                        updatedDefaultValue = String.join("/", parts);
                                        LOGGER.debug("Updated default value: {}", updatedDefaultValue);
                                        replaceParameter.setDefaultValue(updatedDefaultValue);
                                    }
                                }
                            }
                        }
                    }

                    // Update device custom topics based on specific keys
                    if ("9".equals(key)) {
                        String[] parts = value.split("/");
                        currentDevice.setCustomPublishTopic(parts[3]);
                    }
                    if ("11".equals(key)) {
                        String[] parts = value.split("/");
                        currentDevice.setCustomRemoteTopic(parts[3]);
                    }

                    // Update default values of the parameter and device configurations
                    if (parameterToUpdate.getDefaultValues() != null) {
                        parameterToUpdate.getDefaultValues().add(parameterToUpdate.getDefaultValue());
                        parameterToUpdate.getDefaultValues().remove(value);
                        parameterToUpdate.setDefaultValue(value);
                    } else {
                        parameterToUpdate.setDefaultValue(value);
                    }
                }
            }

            // Update history and save changes
            List<DeviceParameterConfigurationHistory> currentHistory = current.getUpdateHistory();
            if (currentHistory == null) {
                currentHistory = new ArrayList<>();
            }
            DeviceParameterConfigurationHistory messageHistory = messageObject.getUpdateHistory();
            currentHistory.add(messageHistory);
            current.setUpdateHistory(currentHistory);
            current.setRemoteConfigurations(currentConfigurations);
            currentDevice.setDeviceParameterConfiguration(current);
            deviceRepository.save(currentDevice);
            messageRepository.delete(messageObject);

        } catch (Exception e) {
            LOGGER.error("An error occurred during device configuration update: " + e.getMessage(), e);
        }
    }

    public Map<Connectivity, Map<String, String>> getConnectivityMatrix(String deviceId) {
        Device device = deviceService.findDeviceById(deviceId);
        return device.getConnectivityMatrix();
    }

    public Map<Connectivity, Map<String, String>> updateConnectivityMatrix(String deviceId, Map<Connectivity, Map<String, String>> matrix) {
        String topic = mqttPubTopic + deviceId + "/C";
        Device device = deviceService.findDeviceById(deviceId);
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

    public AlertLimit createOrUpdateAlertLimit(String kitId, Integer number, AlertLimit alertLimit) {
        LOGGER.debug("Create AlertLimit request found Kit : {}, Property : {}, Limit : {}", kitId, number, alertLimit);

        if (!alertLimit.validate()) {
            throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
        }

        Kit kit = kitRepository.findById(kitId).orElse(null);
        if (kit == null) {
            throw new MagmaException(MagmaStatus.KIT_NOT_FOUND);
        }
        KitModel kitModel = kit.getModel();


        if (number < 0 || kitModel.getNoOfProperties() < (number + 1)) {
            throw new MagmaException(MagmaStatus.PROPERTY_NOT_FOUND);
        }

        if (alertLimit.getLevel() < 1 || (alertLimit.getLevel() > kit.getAlertLevel())) {
            throw new MagmaException(MagmaStatus.INVALID_ALERT_LEVEL);
        }

        AlertLimit db = alertLimitRepository.findByKitIdAndPropertyNumberAndLevel(kitId, number, alertLimit.getLevel());

        if (db == null) {
            alertLimit.setCode(kitModel.getProperties()[number]);
            alertLimit.setKitId(kitId);
            alertLimit.setPropertyNumber(number);
            alertLimit.setStatus(AlertStatus.ACTIVE);

            if (alertLimit.getCurrentLevelPeriod() == null) {
                alertLimit.setCurrentLevelPeriod(3 * kit.getInterval());
            }

            if (alertLimit.getNextLevelPeriod() == null) {
                alertLimit.setNextLevelPeriod(12 * kit.getInterval());
            }

            if (alertLimit.getPersistence() == null) {
                alertLimit.setPersistence(AlertPersistence.INTERVAL);
            }

            return alertLimitRepository.save(alertLimit);
        }

        db.setHigh(alertLimit.getHigh());
        db.setLow(alertLimit.getLow());

        if (alertLimit.getCurrentLevelPeriod() != null) {
            db.setCurrentLevelPeriod(alertLimit.getCurrentLevelPeriod());
        }

        if (alertLimit.getNextLevelPeriod() != null) {
            db.setNextLevelPeriod(alertLimit.getNextLevelPeriod());
        }

        if (alertLimit.getStatus() != null) {
            db.setStatus(alertLimit.getStatus());
        }

        if (alertLimit.getPersistence() != null) {
            db.setPersistence(alertLimit.getPersistence());
        }

        return alertLimitRepository.save(db);
    }

    public String updateAlertLevel(String kitId, Integer level) {
        LOGGER.debug("Update request found Kit : {}, Level : {}", kitId, level);

        if (level == null) {
            throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
        }

        Kit db = kitRepository.findById(kitId).orElse(null);
        if (db == null) {
            throw new MagmaException(MagmaStatus.KIT_NOT_FOUND);
        }

        if (Objects.equals(db.getAlertLevel(), level)) {
            return "No Update Found";
        }

        if (db.getAlertLevel() > level) {
            alertLimitRepository.deleteByKitIdAndLevelGreaterThan(kitId, level);
        }

        db.setAlertLevel(level);
        kitRepository.save(db);

        return "Successfully Updated";
    }

    public String updateInterval(String kitId, Integer interval) {
        LOGGER.debug("Update request found Kit : {}, Interval : {}", kitId, interval);

        if (interval == null) {
            throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
        }

        Kit db = kitRepository.findById(kitId).orElse(null);
        if (db == null) {
            throw new MagmaException(MagmaStatus.KIT_NOT_FOUND);
        }
        db.setInterval(interval);
        kitRepository.save(db);

        return "Successfully Updated";
    }

    public List<Geo> findGeoHistoryByKit(String kitId, String from, String to, GeoType geoType) {

        Kit kit = kitRepository.findById(kitId).orElse(null);
        if (kit == null) {
            throw new MagmaException(MagmaStatus.KIT_NOT_FOUND);
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

        if (geoType != null) {
            return geoRepository.findByKitIdAndTypeAndTimeBetween(
                    kitId,
                    geoType,
                    MagmaTime.parse(from),
                    MagmaTime.parse(to));
        }

        return geoRepository.findByKitIdAndTimeBetween(
                kitId,
                MagmaTime.parse(from),
                MagmaTime.parse(to));

    }

    public MetaData getProperties() {
        MetaData metaData = new MetaData();
        List<EnumObj> sensorCodes = Arrays.stream(SensorCode.values()).map(s -> new EnumObj(s.name(), s.value())).collect(Collectors.toList());
        List<EnumObj> actuators = Arrays.stream(ActuatorCode.values()).map(s -> new EnumObj(s.name(), s.value())).collect(Collectors.toList());
        List<TypeOfKit> types = kitTypeRepository.findAll();
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

    public HashMap<String, DeviceSummary> getDevicesSummaryCall() {
        if (!CoreSchedule.devicesSummary.isEmpty()) {
            return CoreSchedule.devicesSummary;
        } else {
            return null;
        }
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

    private Device findDeviceById(String deviceId) {
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
}
