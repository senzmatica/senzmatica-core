package com.magma.core.data.repository;

import com.magma.core.data.entity.SensorCode;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface SensorCodeRepository extends MongoRepository<SensorCode, String> {

    SensorCode findByCode(String code);

    boolean existsByCode(String code);
}
