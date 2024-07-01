package com.magma.core.service;

import com.magma.core.configuration.MQTTConfiguration;
import com.magma.core.data.entity.Device;
import com.magma.core.data.entity.Kit;
import com.magma.core.data.support.Offset;
import com.magma.core.util.MagmaException;
import com.magma.core.util.MagmaStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DataTransmittingService {


    @Autowired
    KitCoreService kitService;

    @Autowired
    DeviceService deviceService;

    @Value("${mqtt.pub.topic}")
    private String mqttPubTopic;

    @Autowired
    MQTTConfiguration.MqttGateway mqttGateway;

    private static final Logger LOGGER = LoggerFactory.getLogger(DataTransmittingService.class);

    public String doSend(String kitId, Integer actuatorNumber, String message, Boolean sendWhenLive) {
        LOGGER.debug("Kit : {}, Actuator : {}, Message : {}", kitId, actuatorNumber, message);

        Kit kit = kitService.findKitById(kitId);

        for (String deviceId : kit.getOffsetMap().keySet()) {
            Offset offset = kit.getOffsetMap().get(deviceId);

            Device device = deviceService.findDeviceById(deviceId);
            if (offset.getActuator() <= actuatorNumber
                    && offset.getActuator() + device.getNoOfActuators() > actuatorNumber) {

                if (device.getGroup() != null) {
                    LOGGER.debug("Going to Send Kit  :  {}, Group :IR: {}, Device : {}, ActuatorNumber : {}, Message : {}", kitId, device.getGroup(), deviceId, actuatorNumber, message);
                } else {
                    LOGGER.debug("Going to Send Kit  :  {}, Device : {}, ActuatorNumber : {}, Message : {}", kitId, deviceId, actuatorNumber, message);
                }

                //TODO: Have to check Protocol Before send
                if (sendWhenLive != null && sendWhenLive) {
                    mqttGateway.send(mqttPubTopic + deviceId + "/A/" + actuatorNumber, true, message);
                } else {
                    mqttGateway.send(mqttPubTopic + deviceId + "/A/" + actuatorNumber, message);
                }
                return "Successfully Send";
            }
        }
        throw new MagmaException(MagmaStatus.INVALID_INPUT);
    }
}
