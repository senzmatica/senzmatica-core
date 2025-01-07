package com.magma.core.service;


import com.magma.dmsdata.data.entity.Alert;
import com.magma.dmsdata.data.entity.Error;
import com.magma.dmsdata.data.entity.Kit;
import com.magma.dmsdata.data.entity.Offline;

public abstract class KitNotificationService {
    public abstract String sendAlert(Kit kit, Alert alert);

    public abstract String sendAlertOff(Kit kit, Alert alert);  //triggered when an alert goes off

    public abstract String sendOffline(Kit kit, Offline offline);

    public abstract String sendError(Kit kit, Error error);
}
