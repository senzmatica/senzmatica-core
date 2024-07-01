package com.magma.core.service;

import com.magma.core.data.entity.Kit;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CorporateConnectorServiceImpl extends CorporateConnectorService {

    @Override
    public void validateCorporate(String corporateId) {
        System.out.println("Validating corporate: " + corporateId);
    }

    @Override
    public void sendSMSBulk(String userId, List<String> toList, String message) {
        System.out.println("Sending SMS bulk to: " + toList + " with message: " + message);
    }

    @Override
    public List<Kit> findKitsInCorporate(String corporateId) {
        return null;
    }

    @Override
    public String referenceName(String kitId) {
        return "ReferenceName_" + kitId;
    }
}
