package com.magma.core.data.repository;

import com.magma.dmsdata.data.entity.Offline;
import org.joda.time.DateTime;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfflineRepository extends MongoRepository<Offline, String> {

    List<Offline> findByKitId(String kitId);

    List<Offline> findByKitIdAndStartTimeBetween(String kitId, DateTime start, DateTime end);

    List<Offline> findByStartTimeBetween(DateTime start, DateTime end);

    Offline findByKitIdAndEndTimeGreaterThanOrderByStartTimeDesc(String kitId, DateTime end);

    //get intersect ranges
    List<Offline> findByKitIdAndStartTimeLessThanAndEndTimeGreaterThan(String kitId, DateTime endTime, DateTime startTime);

}