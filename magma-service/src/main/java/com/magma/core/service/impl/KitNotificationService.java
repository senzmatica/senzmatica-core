package com.magma.core.service.impl;

import com.magma.core.data.entity.Alert;
import com.magma.core.data.entity.Error;
import com.magma.core.data.entity.Kit;
import com.magma.core.data.entity.Offline;

public abstract class KitNotificationService {

    public abstract String sendAlert(Kit kit, Alert alert);

    public abstract String sendAlertOff(Kit kit, Alert alert);

    public abstract String sendOffline(Kit kit, Offline offline);

    public abstract String sendError(Kit kit, Error error);

}
