package com.magma.core.data.repository;

import com.magma.dmsdata.data.entity.Property;
import org.joda.time.DateTime;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends MongoRepository<Property, String> {

    List<Property> findByKitId(String kitId);

    Property findByKitIdAndNumber(String kitId, Integer number);

    Property findByKitIdAndTimeGreaterThanOrderByTimeAsc(String kitId, DateTime time);

    Property findByKitIdAndNumberAndTimeGreaterThanEqual(String kitId, Integer number, DateTime time);

    Property findByKitIdAndNumberAndTimeLessThanEqual(String kitId, Integer number, DateTime time);

    Property findByKitIdAndNumberAndTime(String kitId, Integer number, DateTime time);

    Property findByKitIdAndNumberAndTimeBetweenOrderByTimeDesc(String kitId, Integer number, DateTime start, DateTime end);

    List<Property> findByKitIdAndNumberAndTimeBetween(String kitId, Integer number, DateTime start, DateTime end);

    @Query("{'creationDate':{'$gte': ?0,'lte':?1}, 'value': {'$gt': ?2, '$lt': ?3}}")
    Boolean existsByTimeIntervalAndValue(DateTime startTime, DateTime endTime, Integer minValue, Integer maxValue);

    @Query("{'code': ?2, 'time': {'$gte': ?0, '$lte': ?1}}")
    List<Property> findByTimeIntervalAndCode(DateTime startTime, DateTime endTime, String code);

    @Query("{'creationDate': {'$gte': ?0, '$lte': ?1}, 'code': ?2, 'value': {'$gt': ?3, '$lt': ?4}},'kidId': ?5")
    Boolean existsByTimeIntervalAndCodeAndValue(DateTime startTime, DateTime endTime, String code, Integer minValue, Integer maxValue, String kidId);


}