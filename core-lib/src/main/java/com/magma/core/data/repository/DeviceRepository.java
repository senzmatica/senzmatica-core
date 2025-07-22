package com.magma.core.data.repository;

import com.magma.core.data.entity.Device;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends MongoRepository<Device, String> {

    List<Device> findByIdIn(List<String> ids);

    List<Device> findByIdInAndOfflineIsTrue(List<String> ids);

    List<Device> findByPersistenceIsTrue();

    List<Device> findByIdNotIn(List<String> ids);

    List<Device> findByBatchNumber(String batchNumber);

    List<Device> findByBatchNumberAndReferenceId(String batchNumber, String referenceId);

    Device findByIdAndReferenceId(String deviceId, String referenceId);

    List<Device> findByName(String name);

    @Query("{$or: [{'id': ?0}, {'name': ?0}]}")
    Device findByIdOrName(String deviceIdOrName);

    List<Device> findByMagmaCodecId(String codecId);

    List<Device> findByProtocol(String protocol);

    @Query("{ 'kitId': { $in: ?0 } }")
    List<Device> findByKitIdIn(List<String> ids);

    List<Device> findAll();

    List<Device> findByProductProductId(String productId);

    List<Device> findByProductType(String productType);

    List<Device> findByReferenceId(String referenceId);

    List<Device> findByIdInAndReferenceId(List<String> deviceIds, String referenceId);

    List<Device> findByBatchNumberInAndReferenceId(List<String> batchNumber, String referenceId);
}
