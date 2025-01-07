package com.magma.ums.Impl;

import com.magma.ums.util.ReferenceEntity;
import com.magma.ums.service.ReferenceEntityService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("ReferenceEntityService")
public class ReferenceEntityServiceUms extends ReferenceEntityService {

    @Override
    public ReferenceEntity findReferenceById(String var1, String var2) {
        return null;
    }

    @Override
    public List<ReferenceEntity> findAllReferencesCanAssignTo(Integer var1) {
        return null;
    }

    @Override
    public List<ReferenceEntity> findReferencesOf(String var1) {
        return null;
    }

    @Override
    public boolean validateReference(Integer integer, List<String> list) {
        return false;
    }
}
