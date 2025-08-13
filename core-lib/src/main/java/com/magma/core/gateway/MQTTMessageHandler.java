package com.magma.core.gateway;

import com.magma.core.service.CoreService;
import com.magma.core.service.DataProcessorService;
import com.magma.core.service.ProductService;
import com.magma.util.MagmaTime;
import com.magma.util.RequestIdUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;

public class MQTTMessageHandler implements MessageHandler {

    @Autowired
    DataProcessorService dataProcessorService;

    @Autowired
    CoreService coreService;

    @Autowired
    ProductService productService;

    private static final Logger LOGGER = LoggerFactory.getLogger(MQTTMessageHandler.class);

    @Override
    public void handleMessage(Message<?> mqttMessage) {
        RequestIdUtil.generateRequestId();

        try {
            String txt = mqttMessage.getPayload().toString();
            String topic = mqttMessage.getHeaders().get("mqtt_topic").toString();

            LOGGER.debug("Received Message. Topic: {}, Message: {}", topic, txt);

            String messageType = identifyMessageType(topic, txt);

            processMessage(messageType, topic, txt);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Exception in MQTT Message Handler: ", e);
        }
    }

    private String identifyMessageType(String topic, String messageContent) {
        if (topic.contains("V1")) {
            return "V1";
        } else if (topic.contains("V2")) {
            return "V2";
        } else if (messageContent.contains("rm-conf-successful")) {
            return "CONFIGURATION";
        } else if (messageContent.contains("Bootloader operation")) {
            return "PRODUCT_UPDATE";
        } else {
            return "UNKNOWN";
        }
    }

    private void processMessage(String messageType, String topic, String messageContent) {
        switch (messageType) {
            case "V1":
                handleV1Message(topic, messageContent);
                break;
            case "V2":
                handleV2Message(topic, messageContent);
                break;
            case "CONFIGURATION":
                handleConfigurationUpdate(topic, messageContent);
                break;
            case "PRODUCT_UPDATE":
                handleProductUpdate(topic, messageContent);
                break;
            default:
                LOGGER.warn("Unknown message type for Topic: {}, Message: {}", topic, messageContent);
        }
    }

    private void handleV1Message(String topic, String messageContent) {
        String[] elements = topic.split("/");
        String deviceId = elements[elements.length - 1];

        if (messageContent.contains("*")) {
            String[] parts = messageContent.split("\\*");
            dataProcessorService.doHandle(deviceId, parts[0]);
            DateTime time = dataProcessorService.pastDataTime(parts[0]);
            dataProcessorService.doHandleActuators(deviceId, parts[1], time);
        } else {
            dataProcessorService.doHandle(deviceId, messageContent);
        }
    }

    private void handleV2Message(String topic, String messageContent) {
        String[] elements = topic.split("/");
        String deviceId = elements[elements.length - 2];
        dataProcessorService.doHandle(deviceId, messageContent);
    }

    private void handleConfigurationUpdate(String topic, String messageContent) {
        String[] elements = topic.split("/");
        String deviceId = elements[elements.length - 3];
        String topicNumber = elements[elements.length - 1];

        if (messageContent.contains("rm-conf-successful") || messageContent.contains("Remote Configuration Message Successfully received")) {
            coreService.doHandleDeviceConfigurationUpdates(deviceId, topicNumber);
        } else if (messageContent.contains("rm-conf-unsuccessful") || messageContent.contains("rm-conf-partial-successful")) {
            coreService.doHandleDeviceConfigFailure(deviceId, topicNumber, messageContent);
        }
    }

    private void handleProductUpdate(String topic, String messageContent) {
        String[] elements = topic.split("/");
        String deviceId = elements[elements.length - 1];

        if (messageContent.contains("Bootloader operation successful") || messageContent.contains("FUOTA Operation Success")) {
            productService.doHandleProductVersionUpdates(deviceId, true);
        } else if (messageContent.contains("Bootloader operation failed")) {
            productService.doHandleProductVersionUpdates(deviceId, false);
        }
    }
}