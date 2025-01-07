package com.magma.core.service;

import com.magma.core.service.KitNotificationService;
import com.magma.dmsdata.data.entity.Alert;
import com.magma.dmsdata.data.entity.Error;
import com.magma.dmsdata.data.entity.Kit;
import com.magma.dmsdata.data.entity.Offline;
import org.springframework.stereotype.Service;

@Service("kitNotificationService")
public class KitNotificationServiceCore extends KitNotificationService {

    @Override
    public String sendAlert(Kit kit, Alert alert) {
        return null;
    }

    @Override
    public String sendAlertOff(Kit kit, Alert alert) {
        return null;
    }

    @Override
    public String sendOffline(Kit kit, Offline offline) {
        return null;
    }

    @Override
    public String sendError(Kit kit, Error error) {
        return null;
    }
}
