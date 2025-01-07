package com.magma.core.data.repository;

import com.magma.dmsdata.data.entity.KitModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KitModelRepository extends MongoRepository<KitModel, String> {
}