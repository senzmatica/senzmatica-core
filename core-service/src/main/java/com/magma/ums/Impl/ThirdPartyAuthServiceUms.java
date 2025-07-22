package com.magma.ums.Impl;

import com.magma.ums.util.AuthType;
import org.springframework.stereotype.Service;
import com.magma.ums.service.ThirdPartyAuthService;
import com.magma.ums.data.entity.User;

import java.util.List;

@Service("ThirdPartyAuthService")
public class ThirdPartyAuthServiceUms extends ThirdPartyAuthService {

    @Override
    public String createAccount(User arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createAccount'");
    }

    @Override
    public String createCorporate(User arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createCorporate'");
    }

    @Override
    public void deleteAccount(String arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAccount'");
    }

    @Override
    public User getUser(AuthType arg0, String arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUser'");
    }

    @Override
    public List<String> getUserRefs(AuthType arg0, String arg1, List<Integer> arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserRefs'");
    }

    @Override
    public List<String> getUserRefs(AuthType arg0, String arg1, Integer arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserRefs'");
    }

    @Override
    public boolean isValid(AuthType arg0, String arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isValid'");
    }
}
