package com.magma.core.service;

import java.util.List;

import com.magma.dmsdata.data.entity.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class DeviceTestService {
    @Autowired
    MongoTemplate mongoTemplate;
    private static final Logger logger = LoggerFactory.getLogger(DeviceTestService.class);

    //Get all existing batch numbers
    public List getAllDevicesBatchNumbers() {
        logger.debug("Get All Batch Numbers available in the system");
        List<Integer> batchNumbers = mongoTemplate.query(Device.class)
                .distinct("batchNumber")
                .as(Integer.class)
                .all();
        return batchNumbers;
    }
}
