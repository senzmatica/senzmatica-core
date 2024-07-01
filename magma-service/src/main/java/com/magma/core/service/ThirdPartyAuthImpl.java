package com.magma.core.service;

import java.util.List;

import com.magma.data.umsdata.util.AuthType;
import com.magma.ums.exported_services.ThirdPartyAuthService;
import com.magma.data.umsdata.entity.User;
import org.springframework.stereotype.Service;

@Service("ThirdPartyAuthService")
public class ThirdPartyAuthImpl extends ThirdPartyAuthService {

    @Override
    public User getUser(AuthType arg0, String arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUser'");
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
