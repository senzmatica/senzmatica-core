package com.magma.core.service;

import com.magma.core.data.entity.Kit;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public abstract class CorporateConnectorService {
    public abstract void validateCorporate(String corporateId);

    public abstract void sendSMSBulk(String userId, List<String> toList, String message);

    public abstract List<Kit> findKitsInCorporate(String corporateId);

    public abstract String referenceName(String kitId);

}
