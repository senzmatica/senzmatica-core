package com.magma.core.data.repository;

import com.magma.core.data.entity.Error;
import org.joda.time.DateTime;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorRepository extends MongoRepository<Error, String> {

    List<Error> findByKitId(String kitId);

    Error findByKitIdAndPropertyNumber(String kitId, Integer number);

    List<Error> findByKitIdAndPropertyNumberAndStartTimeBetween(String kitId, Integer number, DateTime start, DateTime end);

    Error findByKitIdAndPropertyNumberAndEndTimeGreaterThanOrderByStartTimeDesc(String kitId, Integer number, DateTime end);

}