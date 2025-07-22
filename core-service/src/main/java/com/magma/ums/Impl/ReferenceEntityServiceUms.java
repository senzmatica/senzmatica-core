package com.magma.ums.Impl;

import com.magma.ums.util.ReferenceEntity;
import com.magma.ums.data.entity.User;
import com.magma.ums.service.ReferenceEntityService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("ReferenceEntityService")
public class ReferenceEntityServiceUms extends ReferenceEntityService {

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
    public List<String> getCorporateIdsOfAccounts(List<String> arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCorporateIdsOfAccounts'");
    }

    @Override
    public List<String> getLevelFiveIdsOfLevelFour(List<String> arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLevelFiveIdsOfLevelFour'");
    }

    @Override
    public List<String> getLevelFourIdsOfCorporates(List<String> arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLevelFourIdsOfCorporates'");
    }

    @Override
    public List<User> getUsersOfReferences(List<String> arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUsersOfReferences'");
    }

    @Override
    public boolean validateReference(Integer arg0, List<String> arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateReference'");
    }
}
