package com.magma.dmsdata.gateway;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

@Component
public class MQTTMessageHandler implements MessageHandler {

    @Override
    public void handleMessage(Message<?> mqttMessage) {
        // Mock implementation: Simply print the received message payload
        System.out.println("Received message: " + mqttMessage.getPayload());
    }
}
