package com.magma.core.service;

import com.magma.dmsdata.data.entity.Kit;
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
    public List<Kit> findKitsInCorporate(String corporateId) {
        return null;
    }

    @Override
    public String referenceName(String kitId) {
        return null;
    }
}