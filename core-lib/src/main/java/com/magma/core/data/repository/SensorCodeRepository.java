package com.magma.core.data.repository;

import com.magma.core.data.entity.SensorCode;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface SensorCodeRepository extends MongoRepository<SensorCode, String> {

    SensorCode findByCode(String code);

    List<SensorCode> findByReferenceId(String referenceId);

    boolean existsByCode(String code);
}
