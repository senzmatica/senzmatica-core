package com.magma.core.data.repository;

import com.magma.core.data.entity.KitModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KitModelRepository extends MongoRepository<KitModel, String> {
}