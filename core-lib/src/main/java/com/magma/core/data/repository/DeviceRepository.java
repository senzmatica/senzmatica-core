package com.magma.core.data.repository;

import com.magma.core.data.entity.Device;
import com.magma.core.util.ActuatorCode;
import org.joda.time.DateTime;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DeviceRepository extends MongoRepository<Device, String> {

    List<Device> findByIdIn(List<String> ids);

    List<Device> findByIdInAndOfflineIsTrue(List<String> ids);

    List<Device> findByPersistenceIsTrue();

    List<Device> findByIdNotIn(List<String> ids);

    List<Device> findByBatchNumber(String batchNumber);

    List<Device> findByBatchNumberAndReferenceId(String batchNumber, String referenceId);

    Device findById(String deviceId);

    Device findByIdAndReferenceId(String deviceId, String referenceId);

    Device findByName(String name);

    @Query("{$or: [{'id': ?0}, {'name': ?0}]}")
    Device findByIdOrName(String deviceIdOrName);

    List<Device> findByMagmaCodecId(String codecId);

    List<Device> findByProtocol(String protocol);

    List<Device> findByProtocolIn(List<String> protocols);

    @Query("{ 'kitId': { $in: ?0 } }")
    List<Device> findByKitIdIn(List<String> ids);

    List<Device> findAll();

    List<Device> findByProductProductId(String productId);

    List<Device> findByProductType(String productType);

    List<Device> findByReferenceId(String referenceId);

    List<Device> findByIdInAndReferenceId(List<String> deviceIds, String referenceId);

    List<Device> findByBatchNumberInAndReferenceId(List<String> batchNumber, String referenceId);

    @Query("{'product': null, 'sensorCodes': { '$all': ?0 }, 'actuatorCodes': { '$all': ?1 }}")
    List<Device> findByProductIsNullAndSensorCodesInAndActuatorCodesIn(List<String> sensorCodes,List<String> actuatorCodes);

    @Query("{'product': null, 'sensorCodes': { '$all': ?0 }, 'actuatorCodes': { '$size': 0 }}")
    List<Device> findByProductIsNullAndSensorCodesInAndActuatorCodeEmpty(List<String> sensorCodes);

    @Query("{'product': null, 'sensorCodes': { '$size': 0 }, 'actuatorCodes': { '$all': ?0 }}")
    List<Device> findByProductIsNullAndSensorCodesEmptyAndActuatorCodeIn(List<String> actuatorCodes);

    List<Device> findByReferenceIdAndCreationDateBetween(String referenceId, DateTime fromDate, DateTime toDate);

    List<Device> findByBatchNumberInAndReferenceIdAndCreationDateBetween(List<String> batchNumber, String referenceId, DateTime fromDate, DateTime toDate);
}
