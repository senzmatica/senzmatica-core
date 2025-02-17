package com.magma.core.data.repository;

import com.magma.dmsdata.data.entity.Kit;
import com.magma.dmsdata.data.entity.KitModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KitRepository extends MongoRepository<Kit, String> {
    Kit findByDevices(String deviceId);
}
