package com.magma.core.data.repository;

import com.magma.dmsdata.data.entity.Device;
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

    List<Device> findByBatchNumber(Integer batchNumber);

    List<Device> findByMagmaCodecId(String codecId);

    List<Device> findByProtocol(String protocol);

    @Query("{ 'kitId': { $in: ?0 } }")
    List<Device> findByKitIdIn(List<String> ids);

    List<Device> findAll();

    List<Device> findByProductProductId(String productId);

    @Query("{ $or: [ {'customPublishTopic': ?0}, {'customRemoteTopic': ?0} ] }")
    Device findByCustomPublishTopicOrCustomRemoteTopic(String topic);

    List<Device> findByProductType(String productType);

}
