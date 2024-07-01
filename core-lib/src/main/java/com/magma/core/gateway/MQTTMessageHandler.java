package com.magma.core.gateway;

import com.magma.core.service.CoreService;
import com.magma.core.service.DataProcessorService;
import com.magma.core.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;

public class MQTTMessageHandler implements MessageHandler {

    @Autowired
    DataProcessorService dataProcessorService;

    @Autowired
    ProductService productService;

    @Autowired
    CoreService coreService;

    private static final Logger LOGGER = LoggerFactory.getLogger(MQTTMessageHandler.class);


    @Override
    public void handleMessage(Message<?> mqttMessage) {

        try {
            String txt = mqttMessage.getPayload().toString(); //as updated with codec dynamic it should be Object type
            String topic = mqttMessage.getHeaders().get("mqtt_topic").toString();

            //POLAR OLD TOPICS
            //devices/{Device_Id}/messages/events
            //SenzMate/D2S/{Device_Id}
            //SenzMate/D2S/{Device_Id}/A
            //SenzMate/S2M/{Device_Id}

            //New Topics V1
            //Topic : D2S/SA/V1/{IMEI}/S/0  Data : 30 or DT:202012|30
            //Topic : D2S/SA/V1/{IMEI}/S    Data : 0-T:30;1-H:40 or DT:202012|0-T:30;1-H:40
            //Topic : D2S/SA/V1/{IMEI}/A    Data : 0-V:0;1-S:1 or DT:202012|0-V:0;1-S:1
            //Topic : D2S/SA/V1/{IMEI}      Data : 0-T:30;1-H:40*0-V:0;1-S:1 or DT:202012|0-T:30;1-H:40*0-V:0;1-S:1

            LOGGER.debug("Topic : {}, Message : {}", topic, txt);


            String[] elements = topic.split("/");

            String deviceId;
            switch (elements[elements.length - 1]) {
                case "A":
                    //D2S/SA/V1/{Device_Id}/A
                    deviceId = elements[elements.length - 2];

                    //TODO:remove after keerthi fix topic
                    if (txt.contains("*")) {
                        String[] els = txt.split("\\*");
                        txt = els[1];
                        dataProcessorService.doHandle(deviceId, els[0]);
                    }
                    ////

                    dataProcessorService.doHandleActuators(deviceId, txt);
                    break;

                case "S":
                    //D2S/SA/V1/{Device_Id}/S
                    deviceId = elements[elements.length - 2];
                    dataProcessorService.doHandle(deviceId, txt);
                    break;

                case "C":
                    //D2S/SA/V1/{Device_Id}/S
                    deviceId = elements[elements.length - 2];
                    dataProcessorService.doHandle(deviceId, txt);
                    break;

                case "events":
                    //devices/{Device_Id}/messages/events
                    deviceId = elements[1];
                    dataProcessorService.doHandle(deviceId, txt);
                    break;

                default:
                    if (elements[elements.length - 2].equals("C") && txt.contains("rm-conf-successful")) {
                        deviceId = elements[elements.length - 3];
                        String topicNumber = elements[elements.length - 1];
                        coreService.doHandleDeviceConfigurationUpdates(deviceId, topicNumber);
                    } else if (txt.contains("Bootloader operation successful")) {
                        deviceId = elements[elements.length - 1];
                        productService.doHandleProductVersionUpdates(deviceId);
                    } else {
                        deviceId = elements[elements.length - 1];
                        if (txt.contains("*")) {
                            String[] els = txt.split("\\*");
                            txt = els[0];
                            dataProcessorService.doHandleActuators(deviceId, els[1]);
                        }
                        dataProcessorService.doHandle(deviceId, txt);
                    }
            }

        } catch (Exception e) {
            LOGGER.error("Exception Got in MQTT :", e);

        }
    }


}
