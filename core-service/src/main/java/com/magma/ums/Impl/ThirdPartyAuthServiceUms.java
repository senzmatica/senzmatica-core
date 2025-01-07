package com.magma.ums.Impl;

import com.magma.ums.util.AuthType;
import org.springframework.stereotype.Service;
import com.magma.ums.service.ThirdPartyAuthService;
import com.magma.ums.data.entity.User;

import java.util.List;

@Service("ThirdPartyAuthService")
public class ThirdPartyAuthServiceUms extends ThirdPartyAuthService {

    @Override
    public User getUser(AuthType arg0, String arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'getUser'");
    }

    @Override
    public List<String> getUserRefs(AuthType arg0, String arg1, Integer arg2) {
        throw new UnsupportedOperationException("Unimplemented method 'getUserRefs'");
    }

    @Override
    public boolean isValid(AuthType arg0, String arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'isValid'");
    }
}
