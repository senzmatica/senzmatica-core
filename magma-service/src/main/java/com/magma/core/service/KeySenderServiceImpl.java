package com.magma.core.service;

import com.magma.data.umsdata.entity.SecretKey;
import com.magma.data.umsdata.entity.User;
import com.magma.ums.exported_services.KeySenderService;
import org.springframework.stereotype.Service;


@Service("keySenderService")
public class KeySenderServiceImpl extends KeySenderService {

    @Override
    public String resendKeyViaEmail(User arg0, SecretKey arg1, String arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'resendKeyViaEmail'");
    }

    @Override
    public String resendKeyViaSMS(User arg0, SecretKey arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'resendKeyViaSMS'");
    }

    @Override
    public String sendKeyViaEmail(User arg0, SecretKey arg1, String arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendKeyViaEmail'");
    }

    @Override
    public String sendKeyViaSMS(User arg0, SecretKey arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendKeyViaSMS'");
    }


    @Override
    public String sendKey(User user, SecretKey secretKey) {
        return null;
    }
}
