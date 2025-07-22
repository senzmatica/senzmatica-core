package com.magma.core.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magma.core.data.support.DataHTTP;
import com.magma.core.service.DataProcessorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HTTPMessageHandler {

    @Autowired
    DataProcessorService dataProcessorService;

    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPMessageHandler.class);


    public String handleMessage(String kit, DataHTTP data) {

        try {
            LOGGER.debug("Http Data : {}", data);
            String timeHttp = data.getTime();

            if(timeHttp != null && !timeHttp.isEmpty()) {
                dataProcessorService.doHandle(kit, data.getData(), timeHttp);
            } else {
                dataProcessorService.doHandle(kit, data.getData());
            }

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
            String jsonString = oMapper.writeValueAsString(obj);
            LOGGER.debug("Logger Test: {}", jsonString);

            dataProcessorService.doHandle(kit, jsonString);
            return "Success";

        } catch (Exception e) {
            LOGGER.error("Exception Got in HTTP : ", e);
            return "Error";
        }
    }

    public String handleMessageList(String kit, List<DataHTTP> dataHTTPList) {
        LOGGER.debug("Http DataList : {}", dataHTTPList);

        for (DataHTTP data : dataHTTPList) {
            String res=handleMessage(kit, data);

            if (res.equalsIgnoreCase("Error")) {
                return "Error";
            }
        }

        return "Success";

    }
}
