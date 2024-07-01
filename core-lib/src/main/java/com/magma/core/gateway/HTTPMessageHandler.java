package com.magma.core.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magma.core.data.support.DataHTTP;
import com.magma.core.service.DataProcessorService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HTTPMessageHandler {

    @Autowired
    DataProcessorService dataProcessorService;

    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPMessageHandler.class);


    public String handleMessage(String kit, DataHTTP data) {

        try {
            LOGGER.debug("Http Data : {}", data);
            dataProcessorService.doHandle(kit, data.getData());
            return "Success";

        } catch (Exception e) {
            LOGGER.error("Exception Got in HTTP : ", e);
            return "Error";
        }
    }

    public String handleMessage(String kit, Object obj) {
        LOGGER.debug("Http Data : {}", obj);


        ObjectMapper oMapper = new ObjectMapper();
        try {
            JSONObject jsonObject = new JSONObject(oMapper.writeValueAsString(obj));

            LOGGER.debug("Logger Test:{} ", jsonObject.toString());
            dataProcessorService.doHandle(kit, jsonObject.toString());


            return "Success";
        } catch (Exception e) {
            LOGGER.error("Exception Got in HTTP : ", e);
            return "Error";
        }
    }
}
