package com.magma.ums.Impl;

import com.magma.ums.data.entity.SecretKey;
import com.magma.ums.data.entity.User;
import com.magma.ums.service.KeySenderService;
import org.springframework.stereotype.Service;

@Service("keySenderService")
public class KeySenderServiceUms extends KeySenderService {

    @Override
    public String resendKeyViaEmail(User arg0, SecretKey arg1, String arg2) {
        throw new UnsupportedOperationException("Unimplemented method 'resendKeyViaEmail'");
    }

    @Override
    public String resendKeyViaSMS(User arg0, SecretKey arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'resendKeyViaSMS'");
    }

    @Override
    public String sendKeyViaEmail(User arg0, SecretKey arg1,
                                  String arg2) {
        throw new UnsupportedOperationException("Unimplemented method 'sendKeyViaEmail'");
    }

    @Override
    public String sendKeyViaSMS(User arg0, SecretKey arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'sendKeyViaSMS'");
    }

    @Override
    public String sendNotification(User user, String s, String s1) {
        return null;
    }

    @Override
    public String sendKey(User user, SecretKey secretKey) {
        return null;
    }
}
