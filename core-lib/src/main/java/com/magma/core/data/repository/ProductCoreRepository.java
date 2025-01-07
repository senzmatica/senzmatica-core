package com.magma.core.data.repository;

import com.magma.dmsdata.data.entity.ProductCore;
import com.magma.dmsdata.util.DeviceCategory;
import com.magma.dmsdata.util.ProductType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCoreRepository extends MongoRepository<ProductCore, String> {
    ProductCore findByProductTypeAndDeviceCategory(ProductType productType, DeviceCategory deviceCategory);


}
