package com.magma.core.data.repository;

import com.magma.core.data.entity.AlertLimit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertLimitRepository extends MongoRepository<AlertLimit, String> {

    List<AlertLimit> findByKitId(String kitId);

    AlertLimit findByKitIdAndPropertyNumberAndLevel(String kitId, Integer number, Integer level);

    List<AlertLimit> findByKitIdAndPropertyNumber(String kitId, Integer number);

    List<AlertLimit> findByKitIdAndPropertyNumberOrderByLevelAsc(String kitId, Integer number);

    void deleteByKitIdAndLevelGreaterThan(String kitId, Integer level);

}