package com.magma.core.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.magma.ums.exported_services.ReferenceEntityService;
import com.magma.data.umsdata.util.ReferenceEntity;

@Service("ReferenceEntityService")
public class ReferenceEntityServiceSenzagroImpl extends ReferenceEntityService {

    @Override
    public List<ReferenceEntity> findAllReferencesCanAssignTo(Integer arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAllReferencesCanAssignTo'");
    }

    @Override
    public ReferenceEntity findReferenceById(String arg0, String arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findReferenceById'");
    }

    @Override
    public List<ReferenceEntity> findReferencesOf(String arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findReferencesOf'");
    }

    @Override
    public boolean validateReference(Integer arg0, List<String> arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateReference'");
    }

}
