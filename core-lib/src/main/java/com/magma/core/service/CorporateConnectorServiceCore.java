package com.magma.core.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service("corporateConnectorService")
public class CorporateConnectorServiceCore extends CorporateConnectorService {

    @Override
    public void validateCorporate(String corporateId) {
    }

    @Override
    public void sendSMSBulk(String userId, List<String> toList, String message) {
    }

    @Override
    public String referenceName(String kitId) {
        return null;
    }
}
