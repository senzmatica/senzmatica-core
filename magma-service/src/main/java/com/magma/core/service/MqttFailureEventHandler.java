package com.magma.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.integration.mqtt.event.MqttConnectionFailedEvent;
import org.springframework.stereotype.Component;

@Component
public class MqttFailureEventHandler implements ApplicationListener<MqttConnectionFailedEvent> {

    @Autowired
    DeviceMaintenanceService deviceMaintenanceService;

    @Override
    public void onApplicationEvent(MqttConnectionFailedEvent mqttConnectionFailedEvent) {
        deviceMaintenanceService.sendMqttFailureAlerts();
    }
}
