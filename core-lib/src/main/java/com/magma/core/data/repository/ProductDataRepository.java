package com.magma.core.data.repository;

import com.magma.core.data.entity.ProductData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDataRepository extends MongoRepository<ProductData, String> {
}
