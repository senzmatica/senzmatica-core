package com.magma.core.data.repository;

import com.magma.dmsdata.data.entity.Alert;
import org.joda.time.DateTime;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends MongoRepository<Alert, String> {

    List<Alert> findByAlertLimitKitId(String kitId);

    Alert findByAlertLimitKitIdAndAlertLimitPropertyNumber(String kitId, Integer number);

    List<Alert> findByAlertLimitKitIdAndAlertLimitPropertyNumberAndStartTimeBetween(String kitId, Integer number, DateTime start, DateTime end);

    Alert findByAlertLimitKitIdAndAlertLimitPropertyNumberAndAlertLimitLevelAndEndTimeGreaterThanOrderByStartTimeDesc(
            String kitId, Integer number, Integer level, DateTime end);

    List<Alert> findByAlertLimitKitIdAndAlertLimitPropertyNumberAndEndTimeGreaterThanOrderByStartTimeDesc(String kitId, Integer number, DateTime end);

    List<Alert> findByAlertLimitKitIdInAndAlertLimitPropertyNumberAndEndTimeGreaterThanOrderByStartTimeDesc(List<String> kitIds, Integer number, DateTime end);

    List<Alert> findByAlertLimitKitIdInAndEndTimeGreaterThanOrderByStartTimeDesc(List<String> kitIds, DateTime end);

}
