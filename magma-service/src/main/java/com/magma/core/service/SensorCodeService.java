package com.magma.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.magma.core.data.repository.SensorCodeRepository;
import com.magma.core.data.entity.SensorCode;

@Service
public class SensorCodeService {

    @Autowired
    SensorCodeRepository sensorCodeRepository;

    public SensorCode findByCode(String code) {
        return sensorCodeRepository.findByCode(code);
    }

}
