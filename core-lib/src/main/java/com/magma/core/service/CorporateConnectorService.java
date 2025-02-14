package com.magma.core.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public abstract class CorporateConnectorService {
    public abstract void validateCorporate(String corporateId);

    public abstract void sendSMSBulk(String userId, List<String> toList, String message);

    public abstract String referenceName(String kitId);


}
