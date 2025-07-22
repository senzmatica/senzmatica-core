package com.magma.core.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.magma.core.configuration.MQTTConfiguration;
import com.magma.core.data.dto.*;
import com.magma.core.data.entity.Error;
import com.magma.core.data.entity.*;
import com.magma.core.data.repository.*;
import com.magma.core.data.support.*;
import com.magma.core.grpc.PredictionInput;
import com.magma.core.grpc.PredictionInputs;
import com.magma.core.grpc.PredictionOutputs;
import com.magma.core.grpc.SensorPredict;
import com.magma.core.job.CoreSchedule;
import com.magma.core.util.*;
import com.magma.util.MagmaTime;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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
    DeviceRepository deviceRepository;

    @Autowired
    SensorRepository sensorRepository;

    @Autowired
    SensorFailureValueRepository sensorFailureValueRepository;

    @Autowired
    PropertyRepository propertyRepository;

    @Autowired
    AlertLimitRepository alertLimitRepository;

    @Autowired
    AlertRepository alertRepository;

    @Autowired
    GeoRepository geoRepository;

    @Autowired
    DataTriggerService dataTriggerService;

    @Autowired
    ErrorRepository errorRepository;

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
    KitCoreService kitService;

    @Autowired
    DeviceService deviceService;

    @Autowired
    ProductTypeRepository productTypeRepository;

    @Autowired
    SensorCodeRepository sensorCodeRepository;

    @Autowired
    UserConnectorService userConnectorService;

    private final MongoTemplate mongoTemplate;

    @Value("${mqtt.pub.topic}")
    private String mqttPubTopic;

    public CoreService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Update Device Parameter Configuration
     * Updates the remote configurations for a device based on the provided DeviceParameterConfiguration.
     * Sends message to the relevant device and save in message repository temporarily.
     */
    public String updateDeviceParameterConfiguration(String deviceId, DeviceParameterConfiguration deviceParameterConfiguration,String userId) {
        LOGGER.debug("Update Device's Remote Configurations for Device Id: {}", deviceId);

        // Retrieve the requested device from the repository
        Device requestedDevice = deviceRepository.findOne(deviceId);

        // Check if the device exists
        if (requestedDevice == null) {
            // Throw an exception if the device is not found
            throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
        }
        String productType=requestedDevice.getProductType();
        ProductType productTypeRequestedDevice=productTypeRepository.findByProductName(productType);
        if (productTypeRequestedDevice==null){
            throw new MagmaException(MagmaStatus.PRODUCT_NOT_FOUND);
        }
        String dataFormat=productTypeRequestedDevice.getDataFormat();
        boolean retained = !"JSON".equalsIgnoreCase(dataFormat);


        // Retrieve the current device parameter configuration
        DeviceParameterConfiguration deviceParameterConfigurationDb = requestedDevice.getDeviceParameterConfiguration();

        if (deviceParameterConfiguration != null) {
            // Retrieve the old and new remote configurations
            List<RemoteConfigField> oldRemoteConfigurations = deviceParameterConfigurationDb.getRemoteConfigurations();
            List<RemoteConfigField> newRemoteConfigurations = deviceParameterConfiguration.getRemoteConfigurations();

            // Validate the parameters in the new remote configuration
            deviceParameterConfigurationUtil.validateParametersInRemoteConfiguration(newRemoteConfigurations);

            // Identify the ids of remote config fields that are modified
            List<String> modifiedIds = compareParameters(oldRemoteConfigurations, newRemoteConfigurations);
            if (modifiedIds.isEmpty()) {
                LOGGER.debug("No changes detected in remote configurations.So not sending MQTT Message");
                return "Success";
            }
            Collections.sort(modifiedIds);

            // Generate the complete message for the modified parameters
            String completeMessage = generateMessageForModifiedIds(newRemoteConfigurations, modifiedIds,dataFormat);
            LOGGER.debug("Generated Complete Message: {}", completeMessage);

            // Get the current date and time
            DateTime currentDateTime = DateTime.now();
            LOGGER.debug("current data {}", currentDateTime);
            String remoteConfigTopic=requestedDevice.getRemoteConfigTopic();
            LOGGER.debug("Generated Message Length: {}", completeMessage.length());
            LOGGER.debug("Remote Config Topic: {}", remoteConfigTopic);

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
                    //split into message chunks if the dataFormat is string
                    if ("String".equalsIgnoreCase(dataFormat)) {
                        // Split the complete message into chunk
                        List<String> messageChunks = splitMessage(completeMessage);

                        // Iterate through message chunks and send them to the MQTT gateway
                        for (int i = 0; i < messageChunks.size(); i++) {
                            // Generate a unique topic for each chunk
                            String topic = remoteConfigTopic.replace("#", String.valueOf(maxTopicNumber + i + 1));
                            String chunk = messageChunks.get(i);

                            // Send the chunk to the MQTT gateway
                            mqttGateway.send(topic, retained, chunk);

                            // Extract network and communication, topic format, and message format from the chunk
                            Map<String, String> networkAndCommunication = new HashMap<>();
                            Map<String, String> topicFormat = new HashMap<>();
                            Map<String, String> messageFormat = new HashMap<>();
                            Map<String,DeviceParameterUpdateStatus> paramUpdateList=new HashMap<String,DeviceParameterUpdateStatus>();

                            for (String part : chunk.split("\\|")[1].split(";")) {
                                String[] keyValue = part.split("-");
                                if (keyValue.length == 2) {
                                    String id = keyValue[0];
                                    String value = keyValue[1];

                                    // Find the corresponding parameter in the new remote configurations
                                    RemoteConfigField parameter = newRemoteConfigurations.stream()
                                            .filter(p -> id.equals(p.getParameterId()))
                                            .findFirst().orElse(null);

                                    // Populate maps based on parameter category
                                    if (parameter != null) {
                                        DeviceParameterUpdateStatus paramUpdateStatus=new DeviceParameterUpdateStatus();
                                        paramUpdateStatus.setParameterId(parameter.getParameterId());
                                        paramUpdateStatus.setParameter(parameter.getParameter());
                                        paramUpdateStatus.setDefaultValue(value);
                                        paramUpdateStatus.setParameterCategory(parameter.getParameterCategory());
                                        paramUpdateList.put(parameter.getParameterId(),paramUpdateStatus);
                                        //have to remove TODO LL
                                        if ("Network & Communication".equals(parameter.getParameterCategory().trim())) {
                                            networkAndCommunication.put(parameter.getParameterId(), value);
                                        } else if ("Topic Format & Interval".equals(parameter.getParameterCategory().trim())) {
                                            topicFormat.put(parameter.getParameterId(), value);
                                        } else if ("Message Format".equals(parameter.getParameterCategory().trim())) {
                                            messageFormat.put(parameter.getParameterId(), value);
                                        }
                                        //upto this
                                    }
                                }
                            }

                            // Create a history entry for the device parameter configuration
                            DeviceParameterConfigurationHistory his = new DeviceParameterConfigurationHistory();
                            his.setActionBy(userId);
                            his.setUpdatedDate(currentDateTime);
                            his.setNetworkAndCommunication(networkAndCommunication);
                            his.setTopicFormat(topicFormat);
                            his.setMessageFormat(messageFormat);
                            his.setUpdateParamList(paramUpdateList);
                            LOGGER.debug("history : {}",his);
                            his.setUpdateStatus(UpdateStatus.PENDING);
                            String msgId;
                            // Find existing message or create a new one
                            Message existingMessage = messageRepository.findOne(deviceId + "-" + String.valueOf(maxTopicNumber + i + 1));
                            if (existingMessage != null) {
                                existingMessage.setUpdateHistory(his);
                                existingMessage.setPayload(messageChunks.get(i));
                                messageRepository.save(existingMessage);
                                msgId=existingMessage.getId();
                            } else {
                                Message newMessage = new Message(deviceId, his, String.valueOf(maxTopicNumber + i + 1), messageChunks.get(i));
                                messageRepository.save(newMessage);
                                msgId=newMessage.getId();
                            }
                            his.setMessageId(msgId);
                            DeviceParameterConfiguration currentConfig=requestedDevice.getDeviceParameterConfiguration();
                            List<DeviceParameterConfigurationHistory> currentHistory=currentConfig.getUpdateHistory();
                            if (currentHistory == null) {
                                currentHistory = new ArrayList<>();
                            }
                            currentHistory.add(his);
                            currentConfig.setUpdateHistory(currentHistory);
                            requestedDevice.setDeviceParameterConfiguration(currentConfig);
                            deviceRepository.save(requestedDevice);
                        }
                    }
                    else if("JSON".equalsIgnoreCase(dataFormat)){
                        String topic = remoteConfigTopic.replace("#", String.valueOf(maxTopicNumber +  1));

                        // Send the message to the MQTT gateway
                        mqttGateway.send(topic, retained, completeMessage);

                        // Extract network and communication, topic format, and message format from the message
                        Map<String, String> networkAndCommunication = new HashMap<>();
                        Map<String, String> topicFormat = new HashMap<>();
                        Map<String, String> messageFormat = new HashMap<>();
                        Map<String,DeviceParameterUpdateStatus> paramUpdateList=new HashMap<String,DeviceParameterUpdateStatus>();

                        for (String part :  completeMessage.substring(7, completeMessage.length() - 2).split(",")){

                            String[] keyValue = part.substring(1,part.length()-1).split("\":\"");
                            if (keyValue.length == 2) {
                                String id = keyValue[0];
                                String value = keyValue[1];

                                // Find the corresponding parameter in the new remote configurations
                                RemoteConfigField parameter = newRemoteConfigurations.stream()
                                        .filter(p -> id.equals(p.getParameterId()))
                                        .findFirst().orElse(null);

                                // Populate maps based on parameter category
                                if (parameter != null) {
                                    DeviceParameterUpdateStatus paramUpdateStatus=new DeviceParameterUpdateStatus();
                                    paramUpdateStatus.setParameterId(parameter.getParameterId());
                                    paramUpdateStatus.setParameter(parameter.getParameter());
                                    paramUpdateStatus.setDefaultValue(value);
                                    paramUpdateStatus.setParameterCategory(parameter.getParameterCategory());
                                    paramUpdateList.put(parameter.getParameterId(),paramUpdateStatus);
                                    if ("Network & Communication".equals(parameter.getParameterCategory().trim())) {
                                        networkAndCommunication.put(parameter.getParameterId(), value);
                                    } else if ("Topic Format & Interval".equals(parameter.getParameterCategory().trim())) {
                                        topicFormat.put(parameter.getParameterId(), value);
                                    } else if ("Message Format".equals(parameter.getParameterCategory().trim())) {
                                        messageFormat.put(parameter.getParameterId(), value);
                                    }
                                }
                            }
                        }

                        // Create a history entry for the device parameter configuration
                        DeviceParameterConfigurationHistory his = new DeviceParameterConfigurationHistory();
                        his.setActionBy(userId);
                        his.setUpdatedDate(currentDateTime);
                        his.setNetworkAndCommunication(networkAndCommunication);
                        his.setTopicFormat(topicFormat);
                        his.setMessageFormat(messageFormat);
                        his.setUpdateParamList(paramUpdateList);
                        his.setUpdateStatus(UpdateStatus.PENDING);
                        String msgId;

                        // Find existing message or create a new one
                        Message existingMessage = messageRepository.findOne(deviceId + "-" + String.valueOf(maxTopicNumber  + 1));
                        if (existingMessage != null) {
                            existingMessage.setUpdateHistory(his);
                            existingMessage.setPayload(completeMessage);
                            msgId=existingMessage.getId();
                            messageRepository.save(existingMessage);
                        } else {
                            Message newMessage = new Message(deviceId, his, String.valueOf(maxTopicNumber + 1),completeMessage);
                            messageRepository.save(newMessage);
                            msgId=newMessage.getId();
                        }
                        his.setMessageId(msgId);
                        DeviceParameterConfiguration currentConfig=requestedDevice.getDeviceParameterConfiguration();
                        List<DeviceParameterConfigurationHistory> currentHistory=currentConfig.getUpdateHistory();
                        if (currentHistory == null) {
                            currentHistory = new ArrayList<>();
                        }
                        currentHistory.add(his);
                        currentConfig.setUpdateHistory(currentHistory);
                        requestedDevice.setDeviceParameterConfiguration(currentConfig);
                        deviceRepository.save(requestedDevice);
                    }
                } catch (Exception e) {
                    // Handle MQTT exception
                    LOGGER.error("Exception while Sending Remote Config Message to Device:{}",deviceId);
                    throw new MagmaException(MagmaStatus.MQTT_EXCEPTION_IN_CONFIGURING_DEVICE);
                }
            } else {
                throw new MagmaException(MagmaStatus.TOPIC_FORMAT_INVALID);
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
    private String generateMessageForModifiedIds(List<RemoteConfigField> parameters, List<String> modifiedIds,String dataFormat) {
        if ("JSON".equalsIgnoreCase(dataFormat)) {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode jsonNode = objectMapper.createObjectNode();
            ObjectNode childNode = objectMapper.createObjectNode();
            jsonNode.set("RM", childNode);

            for (String id : modifiedIds) {
                for (RemoteConfigField parameter : parameters) {
                    if (parameter.getParameterId().equals(id)) {
                        if ("INTEGER".equalsIgnoreCase(parameter.getFormat())) {
                            childNode.put(id, Integer.parseInt(parameter.getDefaultValue()));
                        } else {
                            childNode.put(id, parameter.getDefaultValue());
                        }
                        break;
                    }
                }
            }
            try {
                return objectMapper.writeValueAsString(jsonNode);
            } catch (Exception e) {
                LOGGER.error("Error while converting ObjectNode to JSON string");
                throw new MagmaException(MagmaStatus.MESSAGE_GENERATION_FAILED);
            }
        }


        StringBuilder messageBuilder = new StringBuilder();

        for (String id : modifiedIds) {
            for (RemoteConfigField parameter : parameters) {
                if (parameter.getParameterId().equals(id)) {
                    messageBuilder.append(id).append("-").append(parameter.getDefaultValue()).append(";");
                    break;
                }
            }
        }
        return messageBuilder.toString();
    }


    // Compare old and new parameters and return IDs of modified parameters
    private static List<String> compareParameters(List<RemoteConfigField> oldParameters, List<RemoteConfigField> newParameters) {
        Set<String> modifiedIds = new HashSet<>();

        // Compare old and new parameters and identify added or modified ones
        for (RemoteConfigField newParam : newParameters) {
            for (RemoteConfigField oldParam : oldParameters) {
                if (newParam.getParameterId().equals(oldParam.getParameterId()) && !Objects.equals(newParam.getDefaultValue(), oldParam.getDefaultValue())) {
                    modifiedIds.add(newParam.getParameterId());
                    break;
                }
            }
        }
        return new ArrayList<>(modifiedIds);
    }

    public List<Map<String,String>> bulkUpdateDeviceParameterConfiguration(List<DeviceParameterConfiguration> deviceParameterConfigurations,String userId) {
       /* List<Map<String,String>> devicesUpdateParamFailed= new ArrayList<>();
        List<Map<String,String>>  devicesUpdateParamSuccess=new ArrayList<>(); */
        List<Map<String,String>> resultBulk=new ArrayList<>();
        String productType=deviceParameterConfigurations.get(0).getProductType();  //devices come with request are of same product Type
        ProductType productTypeRequested=productTypeRepository.findByProductName(productType);
        if (productTypeRequested==null){
            throw new MagmaException(MagmaStatus.PRODUCT_NOT_FOUND);
        }

        for (DeviceParameterConfiguration deviceParameterConfiguration : deviceParameterConfigurations) {
            String deviceId = deviceParameterConfiguration.getDevice();
            try {
                String result = updateDeviceParameterConfiguration(deviceId, deviceParameterConfiguration, userId);
                if ("Success".equals(result)) {
                    // Append the deviceId to the success message
                    Map<String, String> successResult = new HashMap<>();
                    successResult.put("deviceId", deviceId);
                    successResult.put("result", "Success");
                    resultBulk.add(successResult);
                }
            }
            catch (MagmaException e  ){
                Map<String, String> failureResult = new HashMap<>();
                failureResult.put("deviceId", deviceId);
                failureResult.put("result", "Failure");
                failureResult.put("message",e.getMessage());
                failureResult.put("status",e.getStatus().toString());
                resultBulk.add(failureResult);
            }
        }
        return resultBulk;


    }

    public List<DeviceDTO> getAllDeviceParameterConfigurations() {


        Query productQuery = new Query(Criteria.where("remotelyConfigurable").is(true));


        List<String> rmConfigProductNames = mongoTemplate.find(productQuery, ProductType.class)
                .stream()
                .map(ProductType::getProductName)
                .collect(Collectors.toList());

        String[] fieldsToInclude = {"id", "name", "productType","creationDate", "modifiedDate","lastSeen", "deviceParameterConfiguration", "product","lastSeen","configurations","serverIpAddress"};
        //only return configurable devices' parameters only.
        Query query = new Query(Criteria.where("productType").in(rmConfigProductNames));
        for (String field : fieldsToInclude) {
            query.fields().include(field);
        }
        List<Device> selectedDevices;
        selectedDevices = mongoTemplate.find(query, Device.class);
        List<DeviceDTO> deviceDtos = selectedDevices.stream()
                .map(device -> {
                    DeviceDTO dto = new DeviceDTO();
                    BeanUtils.copyProperties(device, dto);
                    DeviceParameterConfiguration deviceParamConfig=device.getDeviceParameterConfiguration();
                    List<DeviceParameterConfigurationHistory> his=deviceParamConfig.getUpdateHistory();
                    if (his != null && !his.isEmpty()) {
                        Optional<DeviceParameterConfigurationHistory> lastUpdateOptional = his.stream()
                                .filter(history -> history!=null && history.getUpdatedDate() != null) // Filter out null updatedDate
                                .sorted(Comparator.comparing(DeviceParameterConfigurationHistory::getUpdatedDate).reversed()) // Sort by updatedDate
                                .findFirst();
                        LOGGER.debug("sorted device by date rm {}",device);
                        DeviceParameterConfigurationHistory lastUpdate;
                        if(lastUpdateOptional.isPresent()) {
                            lastUpdate = lastUpdateOptional.get();
                            LOGGER.debug("last update: {}", lastUpdate);
                            dto.setLastUpdateStatus(lastUpdate.getUpdateStatus());
                        }
                        else{
                            LOGGER.warn("RM manager: Last update history not available");
                        }
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        return deviceDtos;
    }

    public List<Device> getDevicesOtaUpgradable() {
        Query productQuery = new Query(Criteria.where("otaUpgradable").is(true));
        List<String> otaProductNames = mongoTemplate.find(productQuery, ProductType.class)
                .stream()
                .map(ProductType::getProductName)
                .collect(Collectors.toList());
        //return devices of ota upgradable productType only
        Query query = new Query(Criteria.where("productType").in(otaProductNames));
        List<Device> selectedDevices;
        selectedDevices = mongoTemplate.find(query, Device.class);
        return selectedDevices;
    }

    /**
     * Updates the device parameter configuration of the device based on the received system acknowledgement from device.
     *
     * @param deviceIdOrName The identifier of the device (imei number,device name or custom identifier).
     * @param topicNumber  The topic number associated with the received message.
     */
    public void doHandleDeviceConfigurationUpdates(String deviceIdOrName, String topicNumber) {
        try {
            LOGGER.debug("Device -{} To System Message to Update the device parameter configuration of device", deviceIdOrName);

            // Retrieve the current device
            Device currentDevice = deviceRepository.findByIdOrName(deviceIdOrName);
            String productType = currentDevice.getProductType();
            ProductType productTypeRequestedDevice = productTypeRepository.findByProductName(productType);
            String dataFormat = productTypeRequestedDevice.getDataFormat();

            // Retrieve the current device parameter configuration
            DeviceParameterConfiguration current = currentDevice.getDeviceParameterConfiguration();
            LOGGER.debug("DeviceId Found:{}", deviceIdOrName);

            if (current == null) {
                LOGGER.error("DeviceParameterConfiguration not found for deviceId: {}", deviceIdOrName);
                throw new MagmaException(MagmaStatus.NOT_FOUND);
            }

            // Retrieve the system message object based on device ID and topic number
            String deviceId=currentDevice.getId();
            String messageId = deviceId + "-" + topicNumber;//message is saved with imei id and topic
            Message messageObject = messageRepository.findOne(messageId);
            String message = messageObject.getPayload();
            LOGGER.debug("Message: {}", message);

            // Handle JSON format
            if ("JSON".equalsIgnoreCase(dataFormat)) {
                handleJsonMessage(message, current, deviceId, currentDevice, messageObject);
                return;
            }
            List<DeviceParameterConfigurationHistory> history = current.getUpdateHistory();
            Optional<DeviceParameterConfigurationHistory> matchedHistory = history.stream().filter((h) -> messageId != null && h.getMessageId() != null && messageId.equals(h.getMessageId())).findFirst();
            DeviceParameterConfigurationHistory his = (DeviceParameterConfigurationHistory)matchedHistory.get();

            String[] messageElements = message.split("\\|");
            if (messageElements.length < 3) {
                LOGGER.error("Message sent by device is invalid");
                throw new MagmaException(MagmaStatus.INVALID_INPUT);
            }

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

            updateDeviceParameters(keyValueMap, current, currentDevice);
            his.setUpdateStatus(UpdateStatus.UPDATED);
            saveDeviceConfigurationHistory(currentDevice, current, messageObject);

        } catch (Exception e) {
            LOGGER.error("An error occurred during device configuration update: " + e.getMessage(), e);
        }
    }

    public void doHandleDeviceConfigFailure(String deviceIdOrName, String topicNumber,String textFromDevice){
        try{
            LOGGER.debug("Device -{} To System Message to Update the device parameter configuration of device", deviceIdOrName);
            // Retrieve the current device
            Device currentDevice = deviceRepository.findByIdOrName(deviceIdOrName);
            ProductType productTypeRequestedDevice = productTypeRepository.findByProductName(currentDevice.getProductType());
            String dataFormat = productTypeRequestedDevice.getDataFormat();

            DeviceParameterConfiguration current = currentDevice.getDeviceParameterConfiguration();
            if (current == null) {
                LOGGER.error("DeviceParameterConfiguration not found for deviceId: {}", deviceIdOrName);
                throw new MagmaException(MagmaStatus.NOT_FOUND);
            }

            String deviceId=currentDevice.getId();   //message is saved with imei id and topic
            String messageId = deviceId + "-" + topicNumber;
            Message messageObject = messageRepository.findOne(messageId);

            List<DeviceParameterConfigurationHistory> history=current.getUpdateHistory();
            Optional<DeviceParameterConfigurationHistory> matchedHistory = history.stream()
                    .filter(h ->
                    {return messageId != null && h.getMessageId() != null && messageId.equals(h.getMessageId());})
                    .findFirst();
            // Regular Expression
            String pattern = "(rm-conf-(partial-successful|unsuccessful|successful))(?:;\\s*valid-param:\\{([^}]*)\\})?(?:;\\s*invalid-param:\\{([^}]*)\\})?";
            Map<String,Object> statusResult=getStatusfromText(pattern,textFromDevice,deviceIdOrName);
            LOGGER.debug("result {} {} {}",statusResult.get("status"),statusResult.get("validParams"),statusResult.get("invalidParams"));
            DeviceParameterConfigurationHistory his=matchedHistory.get();
            his.setUpdateStatus((UpdateStatus) statusResult.get("status"));
            List<String> paramsToUpdate=new ArrayList<>();
            List<String> invalidParams=new ArrayList<>();
            if(statusResult.get("validParams")!=null) {
                paramsToUpdate = (List<String>) statusResult.get("validParams");
            }
            LOGGER.debug("paramsToUpdate",paramsToUpdate);
            //update validity in history TODO
            Map<String,String> networkParams=his.getNetworkAndCommunication();
            Map<String,String> messageFormat=his.getMessageFormat();
            Map<String,String> topicFormat=his.getTopicFormat();
            Map<String,DeviceParameterUpdateStatus> paramUpdateList=his.getUpdateParamList();

            if(statusResult.get("invalidParams")!=null){
                invalidParams= (List<String>) statusResult.get("invalidParams");
                invalidParams.forEach(param->{
                    DeviceParameterUpdateStatus paramUpdateStatus=paramUpdateList.get(param);
                    if(paramUpdateStatus!=null){
                        paramUpdateStatus.setIsinValid(true);
                    }
                    paramUpdateList.put(param,paramUpdateStatus);
                });
            }
            LOGGER.debug("invalidParams",invalidParams);

            paramsToUpdate.forEach(param->{
                DeviceParameterUpdateStatus paramUpdateStatus=paramUpdateList.get(param);
                if(paramUpdateStatus!=null){
                    paramUpdateStatus.setIsinValid(false);
                }
                paramUpdateList.put(param,paramUpdateStatus);

            });
            his.setUpdateParamList(paramUpdateList);

            //only update valid params in the remote config
            String message = messageObject.getPayload();
            LOGGER.debug("Message: {}", message);

            // Handle JSON format  update params history and validity TODO
            if ("JSON".equalsIgnoreCase(dataFormat)) {
                //have to only update invalid params LLL TODO
                handleJsonMessage(message, current, deviceId, currentDevice, messageObject);
                return;
            }

            String[] messageElements = message.split("\\|");
            if (messageElements.length < 3) {
                LOGGER.error("Message sent by device is invalid");
                throw new MagmaException(MagmaStatus.INVALID_INPUT);
            }

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
                if (paramsToUpdate.contains(key)){
                    keyValueMap.put(key, value);}
            }
            LOGGER.error("update params: " + keyValueMap.toString());
            if(keyValueMap.isEmpty()){
                saveDeviceConfigurationHistory(currentDevice, current, messageObject);
            }
            else{
                LOGGER.debug(keyValueMap.values().toString());
                updateDeviceParameters(keyValueMap, current, currentDevice);
                saveDeviceConfigurationHistory(currentDevice, current, messageObject);
            }


        }



        catch (Exception e) {
            LOGGER.error("An error occured ");
        }

    }
    public Map<String,Object> getStatusfromText(String pattern ,String textFromDevice,String deviceIdOrName) {
        UpdateStatus status;
        Map<String,Object> resultObject=new HashMap<>() ;
        List<String> elements=Arrays.asList(textFromDevice.split(";"));
        LOGGER.debug("splitted text from device: {} ", elements.get(0).contains("rm-conf-unsuccessful"));
        if(elements.get(0).contains("rm-conf-unsuccessful")) {
            status = UpdateStatus.FAILED;
            resultObject.put("status",status);

            LOGGER.debug("rm-conf-unsuccessful: {} ", elements.get(0));
            if ((long) elements.size() >= 2) {
                String patternNew = "(\\s*invalid-param:\\{([^}]*)\\})";
                Pattern regex = Pattern.compile(patternNew);
                Matcher matcher = regex.matcher(elements.get(1));
                List<String> params;
                if (matcher.find()) {
                    params = Arrays.asList(matcher.group(2).split(","));
                    LOGGER.debug("matchers invalid: {} ", params.toString());

                    resultObject.put("invalidParams",params);
                }


            }
        }
        else if(elements.get(0).contains("rm-conf-partial-successful")) {

            LOGGER.debug("rm-conf-partial-successful : {} {} ", elements.get(1),elements.size());

            // assuming valid invalid params comes in Order TODO LL
            if ((long) elements.size() >= 3) {
                String patternInvalid = "(\\s*invalid-param:\\{([^}]*)\\})";
                Pattern regex = Pattern.compile(patternInvalid);

                Matcher matcher = regex.matcher(elements.get(2));
                boolean isMatchFound = matcher.find(); // Call find() once

                LOGGER.debug("Matchers input: {}, Match found: {}", elements.get(2), isMatchFound);

                List<String> params = new ArrayList<>();
                if (isMatchFound) {
                    String matchedGroup = matcher.group(2);
                    params = Arrays.asList(matchedGroup.split(","));
                    LOGGER.debug("matchers invalid: {} ", params);

                    resultObject.put("invalidParams",params);
                }
                String patternvalid = "(\\s*valid-param:\\{([^}]*)\\})";
                Pattern regexValid = Pattern.compile(patternvalid);

                Matcher matcherValid = regexValid.matcher(elements.get(1));
                Boolean isMatchFound_valid=matcherValid.find();
                LOGGER.debug("matchers: {} {}", isMatchFound_valid);

                List<String> paramsValid;
                if (isMatchFound_valid) {
                    paramsValid = Arrays.asList(matcherValid.group(2).split(","));
                    LOGGER.debug("matchers valid: {} ", paramsValid.toString());
                    resultObject.put("validParams",paramsValid);

                }
                if(isMatchFound_valid){
                    status = UpdateStatus.PARTIALLY_UPDATED;
                    resultObject.put("status",status);

                }
                //Case if no valid parameters found??



            }
        }



//        // Compile the regex
//        Pattern regex = Pattern.compile(pattern);
//
//        Matcher matcher = regex.matcher(textFromDevice);
//        UpdateStatus status;
//        Map<String,Object> resultObject=new HashMap<>() ;
//        LOGGER.debug("MATCHERS {}",String.valueOf(matcher.matches()));
//        if (matcher.matches()) {
//
//            // Extract status
//            if( matcher.group(1)=="rm-conf-unsuccessful"){
//                status=UpdateStatus.FAILED;
//            }
//            else if(matcher.group(1)=="rm-conf-partial-successful"){
//                status=UpdateStatus.PARTIALLY_UPDATED;
//            }
//            else{
//                status=null;
//                //have to update with proper result object for incorrect messgae or throw exception TODO LL
//                return null;
//            }
//
//            resultObject.put("status",status);

//            // Extract valid and invalid params
//            String validParams = matcher.group(3);
//            String invalidParams = matcher.group(4);

        else {
            LOGGER.debug("No match for log: {} ", deviceIdOrName);
        }
        return resultObject;

    }
    private void handleJsonMessage(String message, DeviceParameterConfiguration current, String deviceId,
                                   Device currentDevice, Message messageObject) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(message);
            JsonNode rmNode = rootNode.path("RM");

            if (rmNode.isMissingNode()) {
                LOGGER.error("Message sent by device {} is invalid",deviceId);
                throw new MagmaException(MagmaStatus.INVALID_INPUT);
            }

            Map<String, String> keyValueMap = new HashMap<>();
            rmNode.fieldNames().forEachRemaining(field -> {
                String value = rmNode.get(field).asText();
                keyValueMap.put(field, value);
            });

            updateDeviceParameters(keyValueMap, current, currentDevice);
            saveDeviceConfigurationHistory(currentDevice, currentDevice.getDeviceParameterConfiguration(), messageObject);

        } catch (Exception e) {
            LOGGER.error("Error while handling JSON message from device : {}",deviceId);
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
    }

    private void updateDeviceParameters(Map<String, String> keyValueMap, DeviceParameterConfiguration deviceParameterConfiguration
            , Device currentDevice) {
        List<RemoteConfigField> currentConfigurations = deviceParameterConfiguration.getRemoteConfigurations();
        for (String key : keyValueMap.keySet()) {
            String value = keyValueMap.get(key);

            currentConfigurations.stream()
                    .filter(parameter -> key.equals(parameter.getParameterId()))
                    .findFirst()
                    .ifPresent(parameterToUpdate -> parameterToUpdate.setDefaultValue(value));

        }
        currentDevice.setDeviceParameterConfiguration(deviceParameterConfiguration);
        deviceRepository.save(currentDevice);
    }

    private void saveDeviceConfigurationHistory(Device currentDevice, DeviceParameterConfiguration currentConfig,
                                                Message messageObject) {
        List<DeviceParameterConfigurationHistory> currentHistory = currentConfig.getUpdateHistory();
        if (currentHistory == null) {
            currentHistory = new ArrayList<>();
        }
        DeviceParameterConfigurationHistory messageHistory = messageObject.getUpdateHistory();

//        currentHistory.forEach(his->{
//            if(Objects.equals(his.getMessageId(), messageObject.getId())){
//                his.setUpdateStatus(UpdateStatus.UPDATED);
//                his.setMessageId(null);
//            }
//        });
        currentConfig.setUpdateHistory(currentHistory);
        currentDevice.setDeviceParameterConfiguration(currentConfig);
        deviceRepository.save(currentDevice);
        messageRepository.delete(messageObject);
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

    public MetaData getProperties() {
        MetaData metaData = new MetaData();
        List<SensorCode> codes = sensorCodeRepository.findAll();
        List<EnumObj> sensorCodes = codes.stream().map(s -> new EnumObj(s.getCode(), s.getCodeValue())).collect(Collectors.toList());
        List<EnumObj> actuators = Arrays.stream(ActuatorCode.values()).map(s -> new EnumObj(s.name(), s.value())).collect(Collectors.toList());
        List<TypeOfKit> types = kitTypeRepository.findAll();
        metaData.setSensors(sensorCodes);
        metaData.setActuators(actuators);
        metaData.setKitTypes(types);
        return metaData;
    }

    public TypeOfKit createKitType(TypeOfKitDTO typeOfKitDTO) {
        TypeOfKit kitType = new TypeOfKit();
        validateKitType(typeOfKitDTO.getValue());
        String name = typeOfKitDTO.getValue().replaceAll("\\s", "-").toUpperCase();
        BeanUtils.copyProperties(typeOfKitDTO, kitType);
        kitType.setName(name);
        return kitTypeRepository.save(kitType);
    }


    public List<TypeOfKit> getAllKitType() {
        return kitTypeRepository.findAll();
    }

    public void validateKitType(String value) {
        TypeOfKit typeOfKit = kitTypeRepository.findByValue(value);

        if (typeOfKit != null && typeOfKit.getValue().equals(value)) {
            throw new MagmaException(MagmaStatus.DUPLICATE_KIT_TYPE);
        }
    }

    public SensorFailureValue createSensorFailureEntry(SensorFailureValue sensorFailureValue) {
        if (!sensorFailureValue.validate()) {
            throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
        }
        return sensorFailureValueRepository.save(sensorFailureValue);
    }

    public List<SensorFailureValue> getSensorFailureValues() {
        return sensorFailureValueRepository.findAll();
    }

    public HashMap<String, DeviceSummary> getDevicesSummaryCall() {
        if (!CoreSchedule.devicesSummary.isEmpty()) {
            return CoreSchedule.devicesSummary;
        } else {
            return null;
        }
    }

    private boolean checkDuplicate(String kitId, Property property) {
        return propertyRepository.findByKitIdAndNumberAndTime(
                kitId,
                property.getNumber(),
                property.getTime()) == null;

    }

    public Property changeLabel(String deviceId, String label, String propertyId) {
        Property property = propertyRepository.findOne(propertyId);
        Device device = deviceRepository.findOne(deviceId);
        if (device == null || property == null) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
        property.setLabel(label);
        propertyRepository.save(property);
        return property;
    }

    public List<Property> changePropertyLabels(String deviceId, List<PropertyDTO> propertyDTOS) {
        Device device = deviceRepository.findOne(deviceId);
        if (device == null) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
        for (PropertyDTO propertyDTO : propertyDTOS) {
            Property propertyDB = propertyRepository.findOne(propertyDTO.getId());
            if (propertyDB == null) {
                throw new MagmaException(MagmaStatus.INVALID_INPUT);
            }
        }
        List<Property> labelChangedProperties = new ArrayList<>();
        for (PropertyDTO propertyDTO : propertyDTOS) {
            Property property = propertyRepository.findOne(propertyDTO.getId());
            property.setLabel(propertyDTO.getLabel());
            propertyRepository.save(property);
            labelChangedProperties.add(property);
        }
        return labelChangedProperties;
    }

    public List<Sensor> changeSensorLabels(String deviceId, SensorChangeRequestDTO SensorChangeRequestDTO) {
        Device device = deviceRepository.findOne(deviceId);
        if (device == null) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
        List<SensorDTO> sensorDTOs = SensorChangeRequestDTO.getSensorDTOS();
        List<SensorCodeDTO> SensorCodeDTOs = SensorChangeRequestDTO.getSensorCodeDTOS();

        for (SensorDTO sensorDTO : sensorDTOs) {
            Sensor sensorDB = sensorRepository.findOne(sensorDTO.getId());
            if (sensorDB == null) {
                throw new MagmaException(MagmaStatus.INVALID_INPUT);
            }
        }
        List<Sensor> labelChangedSensors = new ArrayList<>();
        for (SensorDTO sensorDTO : sensorDTOs) {
            Sensor sensor = sensorRepository.findOne(sensorDTO.getId());
            DateTime timeCreated = sensor.getTime();
            sensor.setLabel(sensorDTO.getLabel());
            sensorRepository.save(sensor);
            labelChangedSensors.add(sensor);
            changeRelativeSensors(deviceId, timeCreated, sensorDTO, SensorCodeDTOs);
        }
        return labelChangedSensors;
    }

    private void changeRelativeSensors(String deviceId, DateTime timeCreated, SensorDTO sensorDTO, List<SensorCodeDTO> SensorCodeDTOs) {

        List<String> sensorsNeedToUpdate = SensorCodeDTOs.stream()
                .map(SensorCodeDTO::getCode)
                .collect(Collectors.toList());

        List<Sensor> sensorList = sensorRepository.findByDeviceIdAndTime(deviceId, timeCreated);

        for (Sensor sensor : sensorList) {
            if (sensorsNeedToUpdate.contains(sensor.getCode().toString())) {
                sensor.setLabel(sensorDTO.getLabel());
                sensorRepository.save(sensor);
            }
        }
    }


    //Connectivity Manager
    public Map<String, Integer> getConnectivityProtocolDetails() {
        Map<String, Integer> protocolDetails = new HashMap<>();
        List<Device> allDevices = deviceRepository.findAll();
        for (Device device : allDevices) {
            if (device.getProtocol() == null) {
                continue;
            }
            protocolDetails.putIfAbsent(device.getProtocol().toString(), 1);
            protocolDetails.put(device.getProtocol().toString(), protocolDetails.get(device.getProtocol().toString()) + 1);
        }
        return protocolDetails;
    }

    public Map<String, Protocol> changeConnectivityProtocol(String deviceId, Protocol protocol) {
        Device deviceDB = deviceRepository.findOne(deviceId);
        if (deviceDB == null) {
            throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
        }
        //Remove connectivity Protocol
        if (protocol == null) {
            deviceDB.setProtocol(null);
        }

        //Change another protocol
        deviceDB.setProtocol(protocol);
        deviceRepository.save(deviceDB);

        Map<String, Protocol> response = new HashMap<>();
        response.put(deviceId, deviceDB.getProtocol());

        return response;
    }

    public String changeStatusOfTheProtocol(List<Map<String, String>> statusConfigs) {
        List<Map<String, String>> response = new ArrayList<>();

        //Validate request body
        for (Map<String, String> statusConfig : statusConfigs) {
            if (!statusConfig.containsKey("deviceId") || !statusConfig.containsKey("status")) {
                throw new MagmaException(MagmaStatus.INVALID_INPUT);
            }
            Device db = deviceRepository.findOne(statusConfig.get("deviceId"));
            if (db == null) {
                throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
            }
            if (!statusConfig.get("status").equals("enabled") && !statusConfig.get("status").equals("disabled")) {
                throw new MagmaException(MagmaStatus.INVALID_INPUT);
            }
        }

        //Modify Status
        for (Map<String, String> statusConfig : statusConfigs) {
            Device db = deviceRepository.findOne(statusConfig.get("deviceId"));
            if (db.getReferences() == null || db.getProtocol() == null) {
                continue;
            }
            db.getReferences().putIfAbsent("protocolStatus", statusConfig.get("status"));
            db.getReferences().replace("protocolStatus", statusConfig.get("status"));
            deviceRepository.save(db);
        }
        return "updated!";
    }

    //TODO:: Temp - have to remove
    public List<Device> configureClients(Map<String, String> configs) {
        List<Device> mod = new ArrayList<>();
        for (String device : configs.keySet()) {
            Device d = deviceRepository.findOne(device);
            if (d != null) {
                mod.add(d);
                if (d.getReferences() == null) {
                    d.setReferences(new HashMap<>());
                }
                d.getReferences().putIfAbsent("client", configs.get(device));
                deviceRepository.save(d);
            }
        }
        return mod;
    }

    //TODO::Have to add Required filters
    public List<Map<String, String>> getClientsDeviceConnectivity() {
        List<String> allClients = new ArrayList<>();
        List<Device> allDevices = deviceRepository.findAll();
        List<Map<String, String>> clientConnectivity = new ArrayList<>();
        Protocol[] availableProtocols = Protocol.values();

        //Collect client details
        for (Device device : allDevices) {
            if (device.getReferences() != null && device.getReferences().containsKey("client")) {
                if (!allClients.contains(device.getReferences().get("client"))) {
                    allClients.add(device.getReferences().get("client"));
                }
            }
        }
        //Prepare unique Map
        for (String client : allClients) {
            for (Protocol protocol : availableProtocols) {
                Map<String, String> clientProtocol = new HashMap<>();
                clientProtocol.put("client", client);
                clientProtocol.put("protocol", protocol.toString());
                clientProtocol.put("devices", "0");
                clientProtocol.put("protocolStatus", "enabled");
                clientConnectivity.add(clientProtocol);
            }
        }

        //Produce Output
        for (Device device : allDevices) {
            if (device.getProtocol() == null || device.getReferences() == null || !device.getReferences().containsKey("client")) {
                continue;
            }
            String deviceProtocol = device.getProtocol().toString();
            String client = device.getReferences().get("client");

            for (Map<String, String> clientProtocol : clientConnectivity) {
                if (clientProtocol.get("client").equals(client) && clientProtocol.get("protocol").equals(deviceProtocol)) {
                    clientProtocol.putIfAbsent("devices", "0");
                    clientProtocol.put("devices", String.valueOf(Integer.parseInt(clientProtocol.get("devices")) + 1));
                }
                //Change status of Protocol
                if (device.getReferences().containsKey("protocolStatus") && device.getReferences().get("protocolStatus").equals("disabled")) {
                    clientProtocol.replace("protocolStatus", "disabled");
                }

            }
        }

        return clientConnectivity;
    }

    public void validateClientId(String client) {
        //Validate Client Name
        List<Device> allDevices = deviceRepository.findAll();
        List<String> allClients = new ArrayList<>();

        for (Device device : allDevices) {
            if (device.getReferences() != null && device.getReferences().containsKey("client")) {
                if (!allClients.contains(device.getReferences().get("client"))) {
                    allClients.add(device.getReferences().get("client"));
                }
            }
        }
        if (!allClients.contains(client)) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
    }

    public List<Device> devicesOfSpecificClient(String clientId) {
        List<Device> allDevices = deviceRepository.findAll();
        List<Device> devicesOfClient = new ArrayList<>();
        for (Device device : allDevices) {
            if (device.getReferences() != null && device.getReferences().containsKey("client") && device.getReferences().get("client").equals(clientId)) {
                devicesOfClient.add(device);
            }
        }
        return devicesOfClient;
    }

    public List<Map<String, String>> getClientDeviceConnectivity(String client) {
        //Validate Client Name
        validateClientId(client);

        List<Device> allDevices = deviceRepository.findAll();
        List<Map<String, String>> devicesOfClient = new ArrayList<>();
        //Collect Devices details for that client
        for (Device device : allDevices) {
            if (device.getReferences() != null && device.getReferences().containsKey("client")) {
                if (device.getReferences().get("client").equals(client)) {
                    Map<String, String> deviceMap = new HashMap<>();
                    deviceMap.put("deviceId", device.getId());
                    if (device.getCreationDate() != null) {
                        deviceMap.put("created", device.getCreationDate().toString());
                    }

                    if (device.getReferences() != null && device.getReferences().containsKey("protocolStatus")) {
                        deviceMap.put("protocolStatus", device.getReferences().get("protocolStatus"));
                    } else {
                        deviceMap.put("protocolStatus", "not-configured");
                    }
                    devicesOfClient.add(deviceMap);
                }
            }
        }
        return devicesOfClient;
    }

    public Map<String, String> getConnectionDetailsOfDevice(String deviceID) {
        //Validate DeviceID
        Device deviceDB = deviceRepository.findOne(deviceID);
        if (deviceDB == null) {
            throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
        }

        Map<String, String> response = new HashMap<>();

        if (deviceDB.getProtocol() != null) {
            response.put("Connectivity Protocol", deviceDB.getProtocol().toString());
        } else {
            response.put("Connectivity Protocol", "Not Configured");
        }

        if (deviceDB.getReferences() != null && deviceDB.getReferences().containsKey("client")) {
            response.put("client", deviceDB.getReferences().get("client"));
        } else {
            response.put("client", "Not Configured");
        }

        //Protocol Wise Details
        if (deviceDB.getProtocol() != null && deviceDB.getReferences() != null) {
            String protocol = deviceDB.getProtocol().toString();
            if (protocol.equals("HTTP") || protocol.equals("HTTPS")) {
                response.put("access_key", deviceDB.getReferences().getOrDefault("access_key", "Not Configured"));
            } else if (protocol.equals("MQTT")) {
                if (deviceDB.getReferences().containsKey("MQTT_user_name") && deviceDB.getReferences().containsKey("MQTT_password")) {
                    response.put("username", deviceDB.getReferences().get("username"));
                    response.put("password", deviceDB.getReferences().get("password"));
                    response.put("broker", mqttBrokerAddress);
                } else {
                    response.put("broker", mqttBrokerAddress);
                    response.put("username", "Not Configured");
                    response.put("password", "Not Configured");
                }
            } else {
                response.put("message", "Service Not Implemented For This device");
            }
        }
        return response;
    }


    public Map<String, String> generateConnectivityCredentials(Map<String, String> connectivityDetails) {
        List<String> validProtocols = Stream.of(Protocol.values()).map(Protocol::name).collect(Collectors.toList());

        //Validate RequestBody
        if (connectivityDetails == null || !connectivityDetails.containsKey("clientId") || !connectivityDetails.containsKey("protocol") ||
                !validProtocols.contains(connectivityDetails.get("protocol"))) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
        //validate Client Id
        validateClientId(connectivityDetails.get("clientId"));

        //Return Object
        Map<String, String> response = new HashMap<>();

        //Collect Devices Of SpecificClient
        List<Device> devicesOfClient = devicesOfSpecificClient(connectivityDetails.get("clientId"));


        //Generate Credentials and store In reference protocol wise
        if (connectivityDetails.get("protocol").equals("HTTP")
                || connectivityDetails.get("protocol").equals("HTTPS")
                || connectivityDetails.get("protocol").equals("TCP")) {

            String access_key = connectivityDetails.get("clientId") + getRandomCredentials(10);

            for (Device device : devicesOfClient) {
                if (device.getProtocol() != null & device.getProtocol().toString().equals(connectivityDetails.get("protocol"))) {
                    device.getReferences().putIfAbsent("access_key", access_key);
                    device.getReferences().replace("access_key", access_key);
                }
                deviceRepository.save(device);
            }
            response.put("access_key", access_key);

            //TODO: Need to send those details to Each device

            return response;

        } else if (connectivityDetails.get("protocol").equals("MQTT")) {
            String userName = connectivityDetails.get("clientId");
            String password = getRandomCredentials(5);

            for (Device device : devicesOfClient) {
                if (device.getProtocol() != null & device.getProtocol().toString().equals(connectivityDetails.get("protocol"))) {
                    device.getReferences().putIfAbsent("MQTT_user_name", userName);
                    device.getReferences().replace("MQTT_password", password);
                }
                deviceRepository.save(device);
            }

            //Store In Vmq auth In DataBase
            Vmq_acl_auth vmq_acl_auth = new Vmq_acl_auth();
            vmq_acl_auth.setUsername(userName);
            vmq_acl_auth.setPassword(password);
            try {
                verneMqService.createVmqAclAuth(vmq_acl_auth);
                response.put("UserName", userName);
                response.put("Password", password);
            } catch (Exception e) {
                response.put("Message", "Configuration Failed-Password Hash Error");
            }

            return response;
        } else {
            response.put("message", "Invalid Protocol");
            return response;
        }
    }

    private String getRandomCredentials(int len) {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < len) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }

    public List<String> getAllSensorsOfBatch(String batch, String referenceId){
        List<Device> devicesOfBatch = deviceRepository.findByBatchNumberAndReferenceId(batch,referenceId);
        List<String> sensorCodes = new ArrayList<>();

        devicesOfBatch.forEach((d)->{
            if(d.getSensorCodes()!=null){
                Arrays.stream(d.getSensorCodes()).forEach((s)->{
                    if(!sensorCodes.contains(s)){
                        sensorCodes.add(s);
                    }
                });
            }
        });
        return sensorCodes;
    }

    public List<Sensor> getSensorReadingsByDateRange(Integer sYear, Integer sMonth, Integer sDate,Integer eYear,Integer eMonth,Integer eDate,String referenceId){
        DateTime startDate=new DateTime(sYear,sMonth,sDate,0,0);
        DateTime endDate= new DateTime(eYear,eMonth,eDate,23,59);

        List<Device> devices = deviceRepository.findByReferenceId(referenceId);
        List<String> deviceIds  = new ArrayList<>();
        devices.forEach((d)->{
            deviceIds.add(d.getId());
        });

        List<Sensor> filteredSensorReadings = new ArrayList<>();

        List<Sensor> requestedSensorReadings = sensorRepository.findByTimeBetween(startDate, endDate);

        requestedSensorReadings.forEach((s)->{
            if(deviceIds.contains(s.getDeviceId())){
                filteredSensorReadings.add(s);
            }
        });
        return filteredSensorReadings;
    }

    public List<Sensor> getAllSensorsByDeviceAndCode(String deviceId, String sensorCode, String from ,String to,String referenceId){
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(DateTimeZone.forID("Asia/Colombo"));
        DateTime fromDate = formatter.parseDateTime(from);
        DateTime toDate = formatter.parseDateTime(to);

        List<Device> devices = deviceRepository.findByReferenceId(referenceId);
        List<String> deviceIds = new ArrayList<>();

        devices.forEach((d)->{
            deviceIds.add(d.getId());
        });

        List<Sensor> finalSensors = new ArrayList<>();

        List<Sensor> sensors = sensorRepository.findByDeviceIdAndCodeAndTimeBetween(deviceId, sensorCode, fromDate, toDate);

        sensors.forEach((s)->{
            if(deviceIds.contains(s.getDeviceId())){
                finalSensors.add(s);
            }
        });
        return finalSensors;
    }

    public List<SensorCode> addSensorCodes(List<SensorCode>sensorCodes){

        List<SensorCode> addedSensorCodes = new ArrayList<>();
        sensorCodes.forEach((sc)->{
            SensorCode sensorCode = sensorCodeRepository.findByCode(sc.getCode());
            if(sensorCode==null){
                sc.setCode(sc.getCode());
                SensorCode s = sensorCodeRepository.save(sc);
                addedSensorCodes.add(s);
            }
        });

        return addedSensorCodes;
    }

    public List<SensorCode> getAllSensorCodes(){
        return sensorCodeRepository.findAll();
    }
}
