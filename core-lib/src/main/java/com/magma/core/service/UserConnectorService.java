package com.magma.core.service;

import com.magma.dmsdata.data.support.UserInfo;
import org.springframework.stereotype.Service;

@Service
public abstract class UserConnectorService {
    public abstract UserInfo findUser(String userId);

}
