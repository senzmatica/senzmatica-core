package com.magma.core.data.repository;

import com.magma.core.data.entity.TypeOfKit;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface KitTypeRepository extends MongoRepository<TypeOfKit, String> {
    TypeOfKit findByValue(String value);
}
