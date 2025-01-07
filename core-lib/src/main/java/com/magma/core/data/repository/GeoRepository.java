package com.magma.core.data.repository;

import com.magma.dmsdata.data.entity.Geo;
import com.magma.dmsdata.data.support.GeoType;
import org.joda.time.DateTime;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeoRepository extends MongoRepository<Geo, String> {

    List<Geo> findByKitIdAndTimeBetween(String kitId, DateTime from, DateTime to);

    List<Geo> findByKitIdAndTypeAndTimeBetween(String kitId, GeoType geoType, DateTime from, DateTime to);
}