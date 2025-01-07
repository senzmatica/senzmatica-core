package com.magma.core.data.repository;

import com.magma.dmsdata.data.support.DeviceParameterConfiguration;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeviceParameterConfigurationRepository extends MongoRepository<DeviceParameterConfiguration, String> {

    DeviceParameterConfiguration findByDevice(String deviceId);
}
