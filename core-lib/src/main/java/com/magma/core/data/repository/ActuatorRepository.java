package com.magma.core.data.repository;

import com.magma.dmsdata.data.entity.Actuator;
import org.joda.time.DateTime;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActuatorRepository extends MongoRepository<Actuator, String> {

    List<Actuator> findByDeviceId(String deviceId);

    List<Actuator> findByDeviceIdAndNumberAndTimeBetween(String deviceId, Integer number, DateTime start, DateTime end);

    List<Actuator> findByDeviceIdAndNumberAndTimeBetweenOrderByTimeAsc(String deviceId, Integer number, DateTime start, DateTime end);

    List<Actuator> findByDeviceIdAndNumberAndTimeBetweenOrderByTimeDesc(String deviceId, Integer number, DateTime start, DateTime end);

}