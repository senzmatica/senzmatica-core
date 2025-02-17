package com.magma.core.data.repository;

import com.magma.dmsdata.data.entity.ProductType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductTypeRepository extends MongoRepository<ProductType, String> {

    ProductType findByProductName(String productName);

//    ProductTypes findByProductType(String productType);
}
