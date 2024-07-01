package com.magma.core.service;

import org.springframework.stereotype.Service;
import com.magma.core.data.entity.Alert;
import com.magma.core.data.entity.Error;
import com.magma.core.data.entity.Kit;
import com.magma.core.data.entity.Offline;

@Service
public class KitNotificationServiceImpl extends KitNotificationService {

    @Override
    public String sendAlert(Kit kit, Alert alert) {
        // Implementation
        return "Alert sent";
    }

    @Override
    public String sendAlertOff(Kit kit, Alert alert) {
        // Implementation
        return "Alert off sent";
    }

    @Override
    public String sendOffline(Kit kit, Offline offline) {
        // Implementation
        return "Offline sent";
    }

    @Override
    public String sendError(Kit kit, Error error) {
        // Implementation
        return "Error sent";
    }
}
