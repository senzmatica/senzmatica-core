package com.magma.core.data.repository;

import com.magma.core.data.entity.Property;
import com.magma.core.data.entity.Sensor;
import org.joda.time.DateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensorRepository extends MongoRepository<Sensor, String> {

    List<Sensor> findByDeviceId(String deviceId);

    @Query("{'deviceId': { $in: ?0 }}")
    List<Sensor> findAllDevicesByIdList(List<String> deviceIds);

    Sensor findByDeviceIdOrderByTimeDesc(String deviceId);

    Sensor findByDeviceIdAndNumberAndTime(String deviceId, Integer number, DateTime time);

    List<Sensor> findByDeviceIdAndNumberAndTimeBetweenOrderByTimeAsc(String deviceId, Integer number, DateTime start, DateTime end);

    List<Sensor> findByDeviceIdAndNumberAndTimeBetweenOrderByTimeDesc(String deviceId, Integer number, DateTime start, DateTime end);

//    List<Sensor> findByDeviceIdAndNumberOrderByTimeDesc(String deviceId, Integer number);

    List<Sensor> findByDeviceIdAndNumberOrderByTimeDesc(String deviceId, Integer number, Pageable pageable);

    void deleteByDeviceIdIn(List<String> deviceIds);

    List<Sensor> findByDeviceIdAndTimeBetween(String deviceId, DateTime startDateTime, DateTime endDateTime);

    List<Sensor> findByDeviceIdAndCodeAndTimeBetween(String deviceId, String code, DateTime startDateTime, DateTime endDateTime);

    List<Sensor> findByDeviceIdAndNumberAndTimeBetween(String deviceId, Integer number, DateTime start, DateTime end);

    @Query("{'creationDate':{'$gte': ?0,'lte':?1}, 'value': {'$gt': ?2, '$lt': ?3}}")
    boolean existsByTimeIntervalAndValue(DateTime startTime, DateTime endTime, Integer minValue, Integer maxValue);

    @Query("{'code': ?2, 'time': {'$gte': ?0, '$lte': ?1}}")
    List<Property> findByTimeIntervalAndCodeAndKitId(DateTime startTime, DateTime endTime, String code, String kidId);

    @Query("{'creationDate': {'$gte': ?0, '$lte': ?1}, 'code': ?2, 'value': {'$gt': ?3, '$lt': ?4}}")
    boolean existsByTimeIntervalAndCodeAndValue(DateTime startTime, DateTime endTime, String code, Integer minValue, Integer maxValue);
}