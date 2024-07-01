package com.magma.core.data.repository;

import com.magma.core.data.entity.Kit;
import com.magma.core.data.entity.KitModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KitRepository extends MongoRepository<Kit, String> {

    List<Kit> findByIdIn(List<String> ids);

    List<Kit> findByIdInAndOfflineIsTrue(List<String> ids);

    List<Kit> findByIdInAndOfflineIsFalse(List<String> ids);

    List<Kit> findByIdInAndOfflineIsTrueAndPersistenceIsTrue(List<String> ids);

    List<Kit> findByModelIn(List<KitModel> models);

    List<Kit> findByPersistenceIsTrue();

    Kit findByDevices(String deviceId);

}
