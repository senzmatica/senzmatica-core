package com.magma.core.service;

import com.magma.core.data.support.UserInfo;
import org.springframework.stereotype.Service;

@Service("userConnectorService")
public class UserConnectorServiceCore extends UserConnectorService {

    @Override
    public UserInfo findUser(String userId) {
        return null;
    }
}
