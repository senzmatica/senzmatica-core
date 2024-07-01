package com.magma.core.data.repository;

import com.magma.core.data.entity.ProductCore;
import com.magma.core.util.DeviceCategory;
import com.magma.core.util.ProductType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCoreRepository extends MongoRepository<ProductCore, String> {
    ProductCore findByProductTypeAndDeviceCategory(ProductType productType, DeviceCategory deviceCategory);


}
