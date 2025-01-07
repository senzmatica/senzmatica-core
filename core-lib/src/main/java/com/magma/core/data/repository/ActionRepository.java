package com.magma.core.data.repository;

import com.magma.dmsdata.data.entity.Action;
import org.joda.time.DateTime;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionRepository extends MongoRepository<Action, String> {

    List<Action> findByKitId(String kitId);

    Action findByKitIdAndNumber(String kitId, Integer number);

    Action findByKitIdAndTimeGreaterThanOrderByTimeAsc(String kitId, DateTime time);

    List<Action> findByKitIdAndNumberAndTimeBetween(String kitId, Integer number, DateTime start, DateTime end);

}