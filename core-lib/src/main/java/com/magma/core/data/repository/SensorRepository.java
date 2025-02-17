package com.magma.core.data.repository;

import com.magma.dmsdata.data.entity.Property;
import com.magma.dmsdata.data.entity.Sensor;
import org.joda.time.DateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensorRepository extends MongoRepository<Sensor, String> {

    List<Sensor> findByDeviceId(String deviceId);

    Sensor findByDeviceIdOrderByTimeDesc(String deviceId);

    List<Sensor> findTop12ByDeviceIdOrderByTimeAsc(String deviceId);

    List<Sensor> findByDeviceIdAndTime(String deviceId, DateTime time);

    Sensor findByDeviceIdAndNumberAndTime(String deviceId, Integer number, DateTime time);
    Sensor findByDeviceIdAndNumber(String deviceId, Integer number);

    List<Sensor> findByDeviceIdAndNumberAndTimeBetweenOrderByTimeAsc(String deviceId, Integer number, DateTime start, DateTime end);

    List<Sensor> findByDeviceIdAndNumberAndTimeBetweenOrderByTimeDesc(String deviceId, Integer number, DateTime start, DateTime end);

    @Query(value = "{ 'deviceId': ?0, 'number': ?1, 'time': { $gte: ?2, $lte: ?3 } }",   //latency test Dilax(drop - size 50%, time 5-10% in local)
            fields = "{ 'id': 1, 'time': 1, 'value': 1, 'label': 1 }")
    List<Sensor> findByDeviceIdAndNumberAndTimeBetweenOrderByTimeAscNew(String deviceId,
                                                                        Integer number,
                                                                        DateTime start,
                                                                        DateTime end);

    @Query(value = "{ 'deviceId': ?0, 'number': ?1, 'time': { $gte: ?2, $lte: ?3 } }",
            fields = "{ 'id': 1, 'time': 1, 'value': 1, 'label': 1 }")
    List<Sensor> findByDeviceIdAndNumberAndTimeBetweenOrderByTimeDescNew(String deviceId,
                                                                         Integer number,
                                                                         DateTime start,
                                                                         DateTime end);

//    List<Sensor> findByDeviceIdAndNumberOrderByTimeDesc(String deviceId, Integer number);

    List<Sensor> findByDeviceIdAndNumberOrderByTimeDesc(String deviceId, Integer number, Pageable pageable);

    void deleteByDeviceIdIn(List<String> deviceIds);

    List<Sensor> findByDeviceIdAndTimeBetween(String deviceId, DateTime startDateTime, DateTime endDateTime);

    List<Sensor> findByDeviceIdAndCodeAndTimeBetween(String deviceId, String code, DateTime startDateTime, DateTime endDateTime);

    List<Sensor> findByDeviceIdAndCodeAndTimeBetweenOrderByTimeAsc(String deviceId, String code, DateTime startDateTime, DateTime endDateTime);

    List<Sensor> findByDeviceIdAndNumberAndTimeBetween(String deviceId, Integer number, DateTime start, DateTime end);

    @Query("{'creationDate':{'$gte': ?0,'lte':?1}, 'value': {'$gt': ?2, '$lt': ?3}}")
    Boolean existsByTimeIntervalAndValue(DateTime startTime, DateTime endTime, Integer minValue, Integer maxValue);

    @Query("{'code': ?2, 'time': {'$gte': ?0, '$lte': ?1}}")
    List<Property> findByTimeIntervalAndCodeAndKitId(DateTime startTime, DateTime endTime, String code, String kidId);

    @Query("{'creationDate': {'$gte': ?0, '$lte': ?1}, 'code': ?2, 'value': {'$gt': ?3, '$lt': ?4}}")
    Boolean existsByTimeIntervalAndCodeAndValue(DateTime startTime, DateTime endTime, String code, Integer minValue, Integer maxValue);

    @Query(fields = "{'deviceId': 1, 'code': 1, 'value': 1, 'time': 1, '_id': 0}")
    List<Sensor> findByTimeBetween(DateTime startTime, DateTime endTime);

    List<Sensor> findByDeviceIdAndTimeBetweenOrderByTimeAsc(String deviceId, DateTime startDateTime, DateTime endDateTime);

    List<Sensor> findTop2ByDeviceIdAndCodeOrderByTimeAsc(String deviceId, String code);

    List<Sensor> findTop240ByDeviceIdOrderByTimeDesc(String deviceId);

}
