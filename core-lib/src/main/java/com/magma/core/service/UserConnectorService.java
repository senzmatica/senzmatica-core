package com.magma.core.service;

import com.magma.core.data.support.UserInfo;
import org.springframework.stereotype.Service;

@Service
public abstract class UserConnectorService {
    public abstract UserInfo findUser(String userId);

}
