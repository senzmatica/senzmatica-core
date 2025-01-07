package com.magma.core.data.repository;

import com.magma.dmsdata.data.entity.ProductTypes;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductTypesRepository extends MongoRepository<ProductTypes, String> {

    ProductTypes findByProductName(String productName);

//    ProductTypes findByProductType(String productType);
}
