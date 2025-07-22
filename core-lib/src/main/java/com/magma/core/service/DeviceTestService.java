package com.magma.core.service;

import com.magma.core.data.entity.*;
import com.magma.core.data.repository.*;
import com.magma.core.data.support.TestResult;
import com.magma.core.util.*;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.joda.time.DateTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
public class DeviceTestService {

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    SensorRepository sensorRepository;

    @Autowired
    PropertyRepository propertyRepository;

    @Autowired
    MongoOperations mongoOperations;

    private static final Logger logger = LoggerFactory.getLogger(DeviceTestService.class);

    //Get all existing batch numbers
    public List<String> getAllDevicesBatchNumbers() {
        logger.debug("Get All Batch Numbers available in the system");
        Query query = new Query();
        Sort sort = new Sort(Sort.Direction.DESC, "creationDate");
        query.with(sort);

        List<Device> devices = mongoTemplate.find(query, Device.class, "device");

        Set<String> batchNumbersSet = new LinkedHashSet<>();
        for (Device doc : devices) {
            if (doc.getBatchNumber() != null) {
                batchNumbersSet.add(doc.getBatchNumber());
            }
        }
        return new ArrayList<>(batchNumbersSet);
    }

    //Get Devices of specific batch
    public List<Device> getDevicesOfBatch(String batchNumber) {
        logger.debug("Get Request for devices Of batch : {} ", batchNumber);
        List<Device> devicesOfBatch = deviceRepository.findByBatchNumber(batchNumber);
        if (devicesOfBatch == null || devicesOfBatch.size() == 0) {
            throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
        }
        return devicesOfBatch;
    }

    //Assign batch number to a device
    public Device configureBatchNumber(String deviceId, String batchNumber) {
        Device deviceDB = deviceRepository.findOne(deviceId);
        String regex = "(?=.*[a-zA-Z])(?=.*[0-9])[A-Za-z0-9]+";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(batchNumber);
        if (batchNumber == null || !m.matches()) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
        if (deviceDB == null) {
            throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
        }
        deviceDB.setBatchNumber(batchNumber);
        return deviceRepository.save(deviceDB);
    }


    //Remove sensors of a batch [sensors will be removed from DB]
    public String clearTestSensorsOfBatch(String batchNumber) {
        List<Device> devicesOfBatch = deviceRepository.findByBatchNumber(batchNumber);
        List<String> deviceIdsOfBatch = new ArrayList<>();
        devicesOfBatch.forEach(device -> deviceIdsOfBatch.add(device.getId()));
        sensorRepository.deleteByDeviceIdIn(deviceIdsOfBatch);
        return "TestSensors Removed";
    }

    public List<String> getAllTestConditions() {
        return new ArrayList<String>(Arrays.asList("Battery can be any level", "The devices can be left outside for this testing", "The sensors must be connected while testing"));
    }

    public HashMap<String, Integer> getProtocolSummary() {

        List<Device> MQTTdevice = deviceRepository.findByProtocol("MQTT");
        List<Device> HTTPdevice = deviceRepository.findByProtocol("HTTP");
        List<Device> HTTPSdevice = deviceRepository.findByProtocol("HTTPS");
        List<Device> TCPdevice = deviceRepository.findByProtocol("TCP");

        HashMap<String, Integer> prtocolCount = new HashMap<>();

        prtocolCount.put("MQTT", MQTTdevice.size());
        prtocolCount.put("HTTP", HTTPdevice.size());
        prtocolCount.put("HTTPS", HTTPSdevice.size());
        prtocolCount.put("TCP", TCPdevice.size());

        return prtocolCount;

    }

    public long getDevicesCount(String referenceId) {
        return deviceRepository.findByReferenceId(referenceId).size();
    }

    public long getDevicesCountByBatchNumber(String referenceId, List<String> batchNumbers) {
        logger.debug("Get Devices Count By Batch Number : {} ", batchNumbers);
        return deviceRepository.findByBatchNumberInAndReferenceId(batchNumbers, referenceId).size();
    }

    public List<String> getDevices(String batchNumber,String referenceId) {
        List<Device> devices = deviceRepository.findByBatchNumberAndReferenceId(batchNumber,referenceId);
        List<String> deviceIds = new ArrayList<>();
        if(devices.size() > 0){
            devices.forEach( device ->{
                deviceIds.add(device.getId());
            });
        }
        return deviceIds;
    }

    public List<Device> getDevicesNotHaveBatch(String referenceId) {
        List<Device> devices = deviceRepository.findByReferenceId(referenceId);
        List<Device> devicesNotHaveBatch = new ArrayList<>();
        for (Device device : devices) {
            if(device.getBatchNumber() == null){
                devicesNotHaveBatch.add(device);
            }
        }
        return devicesNotHaveBatch;
    }

    public List<Device> updateDevice(String batchNumber, List<String> deviceIds, String referenceId) {
        List<Device> devices = new ArrayList<>();
        deviceIds.forEach(deviceId -> {
            Device device = deviceRepository.findOne(deviceId);
            if(device.getBatchNumber() == null && device.getReferenceId().equals(referenceId)){
                device.setBatchNumber(batchNumber);
                devices.add(device);
                deviceRepository.save(device);
            }
        });
        return devices;
    }

    public List<Device> getDevicesFromReferenceId(String referenceId){
        List<Device> devices = deviceRepository.findByReferenceId(referenceId);
        return devices;
    }

    public List<Device> getDevicesByIdInAndReferenceId(List<String> deviceIds, String referenceId){
        List<Device> devices = deviceRepository.findByIdInAndReferenceId(deviceIds, referenceId);
        return devices;
    }


}
