package com.magma.core.data.repository;

import com.magma.dmsdata.data.entity.SensorFailureValue;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorFailureValueRepository extends MongoRepository<SensorFailureValue, String> {
}