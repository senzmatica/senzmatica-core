package com.magma.core.service.impl;

import java.util.List;

import com.magma.core.data.entity.Kit;

public abstract class CorporateConnectorService {

    public abstract void validateCorporate(String corporateId);

    public abstract void sendSMSBulk(String userId, List<String> toList, String message);

    public abstract List<Kit> findKitsInCorporate(String corporateId);

    public abstract String referenceName(String kitId);

}
